package org.openntf.logging.scheduler;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import lotus.domino.NotesException;
import lotus.domino.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.SimpleLog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.openntf.domino.DAOFactory;
import org.openntf.domino.logger.LoggerDAO;
import org.openntf.logging.LogBuffer;
import org.openntf.logging.config.SystemConfiguration;
import org.openntf.logging.entity.LogEntry;
import org.openntf.utils.JSFUtils;

import com.google.common.collect.Multimap;
import com.ibm.domino.xsp.module.nsf.ThreadSessionExecutor;
import com.ibm.xsp.designer.context.ServletXSPContext;

/**
 * Responsible for starting a new thread.
 * 
 * @author Olle Thalén
 *
 */
public class Scheduler {

	private static Log logger = null;

	public static boolean start(final long delay) {
		synchronized (Scheduler.class) {
			if (runningJob == null) {
				if (logger == null) {
					SimpleLog log = new SimpleLog(Scheduler.class.getName());
					log.setLevel(SystemConfiguration.isDiagnostic() ? SimpleLog.LOG_LEVEL_ALL
							: SimpleLog.LOG_LEVEL_ERROR);
					logger = log;
				}
				runningJobDone = false;
				latch = new CountDownLatch(1);
				Map<?, ?> applicationScope = (Map<?, ?>) JSFUtils
						.getVariable("applicationScope");

				ServletXSPContext xspContext = (ServletXSPContext) JSFUtils
						.getVariable("context");
				String fullName = xspContext.getUser().getDistinguishedName();
				runningJob = new LogDocumentWorker("Log job", applicationScope
						.get("dbFilePath").toString(), fullName);
				synchronized (runningJob) {
					runningJob.addJobChangeListener(new JobChangeAdapter() {
						public void done(IJobChangeEvent event) {
							runningJob = null;
							logger.debug("Running job is now finished, counting down the latch..");
							latch.countDown();
						}
					});
					AccessController
							.doPrivileged(new PrivilegedAction<Object>() {
								public Object run() {
									runningJob.schedule(delay);
									return null;
								}
							});
				}
				return true;
			} else {
				logger.debug("Job is currently running, can't start it.");
				return false;
			}
		}
	}

	/**
	 * If the running job is done but the JobChangeListener hasn't been notified
	 * yet, then wait until the job is completely done then attempt to start a
	 * new job. <br/>
	 * The attached JobChangeListener will count down the latch when its done
	 * method is invoked.
	 * 
	 * @param delay
	 */
	public static void waitAndStart(long delay) {
		if (runningJobDone) {
			try {
				logger.info("Running job is done but will wait until the JobChangeListener has been notified.");
				latch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// Now the job if free for scheduling, if multiple threads have been
			// activated by the latch, only one will start the new job.
			// the others will abort.
			Scheduler.start(delay);
		}
	}

	private static LogDocumentWorker runningJob;

	private static volatile boolean runningJobDone = false;
	private static CountDownLatch latch;

	private static class LogDocumentWorker extends Job {

		private ThreadSessionExecutor<IStatus> executor;

		public LogDocumentWorker(String name, final String dbFilePath,
				final String userName) {
			super(name);

			this.executor = new ThreadSessionExecutor<IStatus>() {

				@Override
				protected IStatus run(Session session) throws NotesException {
					synchronized (Scheduler.class) {
						LogBuffer buf = (LogBuffer) LogFactory.getFactory()
								.getAttribute("buffer");
						Multimap<String, List<LogEntry>> logQueue = buf
								.getQueue();

						DAOFactory factory = new DAOFactory(session, dbFilePath);
						List<LoggerDAO> appenders = factory.getLoggerDAO();

						if (appenders == null) {
							return Status.CANCEL_STATUS;
						}
						while (true) {
							synchronized (logQueue) {
								if (logQueue.isEmpty()) {
									runningJobDone = true;
									break;
								}

								try {	
									Set<String> set = logQueue.keySet();
									Iterator<String> it = set.iterator();
									//put all keys in a temporary list,
									//so we don't cause a ConcurrentModificationException
									ArrayList<String> keys = new ArrayList<String>();
									while (it.hasNext()) {
										keys.add(it.next());
									}
									for (String keyUsername : keys) {
										Collection<List<LogEntry>> col = logQueue
												.removeAll(keyUsername);
										Scheduler.logger.info("removed collection size: "
														+ col.size());
										synchronized (col) {
											for (List<LogEntry> logEntries : col) {
												for (LoggerDAO appender : appenders) {
													appender.createLogDocument(
															logEntries,
															keyUsername);
												}
											}
										}
									}

								} catch (Throwable t) {
									t.printStackTrace();
								}
							}
						}
						factory.recycle();

						return Status.OK_STATUS;
					}
				}
			};
		}

		@Override
		protected IStatus run(IProgressMonitor arg0) {
			try {
				try {
					IStatus st = null;

					IStatus status = executor.run();
					st = status;

					return st;

				} catch (NullPointerException e) {
					e.printStackTrace();
					return Status.CANCEL_STATUS;
				}

			} catch (Throwable t) {
				t.printStackTrace();
				return Status.CANCEL_STATUS;
			}
		}

	}
}
