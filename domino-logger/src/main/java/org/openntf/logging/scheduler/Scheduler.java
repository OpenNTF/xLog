package org.openntf.logging.scheduler;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import lotus.domino.NotesException;
import lotus.domino.Session;

import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.openntf.domino.DAOFactory;
import org.openntf.domino.logger.LoggerDAO;
import org.openntf.logging.LogBuffer;
import org.openntf.logging.entity.LogEntry;
import org.openntf.utils.JSFUtils;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.ibm.domino.xsp.module.nsf.ThreadSessionExecutor;
import com.ibm.xsp.designer.context.ServletXSPContext;

public class Scheduler {

	public static boolean start(final long delay) {
		synchronized (Scheduler.class) {
			if (runningJob == null) {
				runningJobDone = false;
				latch = new CountDownLatch(1);
				Map<?, ?> applicationScope = (Map<?, ?>) JSFUtils.getVariable("applicationScope");
				
				ServletXSPContext xspContext = (ServletXSPContext) JSFUtils
						.getVariable("context");
				String fullName = xspContext.getUser().getDistinguishedName();
				runningJob = new LogDocumentWorker("Log job",
						applicationScope.get("dbFilePath").toString(), fullName);
				synchronized (runningJob) {
					runningJob.addJobChangeListener(new JobChangeAdapter() {
						public void done(IJobChangeEvent event) {
							runningJob = null;
							System.out.println("Running job is now finished, counting down the latch..");
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
				System.out.println("Job is currently running, can't start it.");
				return false;
			}
		}
	}
	
	/**
	 * If the running job is done but the JobChangeListener hasn't been notified yet,
	 * then wait until the job is completely done then attempt to start a new job. <br/>
	 * The attached JobChangeListener will count down the latch when its done method is invoked.
	 *  
	 * @param delay
	 */
	public static void waitAndStart(long delay) {
		if (runningJobDone) {
			try {
				System.out.println("Running job is done but will wait until the JobChangeListener has been notified.");
				latch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//Now the job if free for scheduling, if multiple threads have been activated by the latch, only one will start the new job.
			//the others will abort. 
			Scheduler.start(delay);
		}
	}

	private static LogDocumentWorker runningJob;
	
	private static volatile boolean runningJobDone = false;
	private static CountDownLatch latch;
	
	private static class LogDocumentWorker extends Job {

		private ThreadSessionExecutor<IStatus> executor;
		
		public LogDocumentWorker(String name, final String dbFilePath, final String userName) {
			super(name);
			
			this.executor = new ThreadSessionExecutor<IStatus>() {

				@Override
				protected IStatus run(Session session) throws NotesException {
					
					LogBuffer buf = (LogBuffer) LogFactory.getFactory()
							.getAttribute("buffer");
					Multimap<String, List<LogEntry>> logQueue = buf.getQueue();
					DAOFactory factory = new DAOFactory(session, dbFilePath);
					List<LoggerDAO> appenders = factory.getLoggerDAO();
					
					if (appenders == null) {
						return Status.CANCEL_STATUS;
					}
					while (true) {
						synchronized(logQueue) {
							if (logQueue.isEmpty()) {
								runningJobDone = true;
								break;
							}
						}
						Set<String> keys = ImmutableSet.copyOf(logQueue.keySet());
						for (String userName : keys) {
							for (List<LogEntry> logEntries : logQueue.get(userName)) {
								for (LoggerDAO appender : appenders) {
									appender.createLogDocument(logEntries, userName);
								}
							}
							Collection<List<LogEntry>> col = logQueue.removeAll(userName);
							System.out.println("number of entries removed for user " + userName + ": " + col.size());
						}												
					}
					factory.recycle();
					return Status.OK_STATUS;
				}
			};
		}

		@Override
		protected IStatus run(IProgressMonitor arg0) {
			try {
				try {
					IStatus status = executor.run();
					return status;
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

	public static void setImplementations(List<String> implementations) {
		synchronized (systemInfo) {
			systemInfo.setLogImplementations(implementations);
		}
	}

	public static List<String> getImplementations() {
		return systemInfo.getLogImplementations();
	}

	private static final SystemInfo systemInfo = new SystemInfo();

	private static class SystemInfo {
		private List<String> logImplementations = new ArrayList<String>();

		/**
		 * Returns all class names for the log implementations
		 * @return a list with fully qualified class names
		 */
		public List<String> getLogImplementations() {
			return logImplementations;
		}

		public void setLogImplementations(List<String> logImplementations) {
			this.logImplementations = logImplementations;
		}	

	}
}
