package org.openntf.logging;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lotus.domino.Session;

import org.apache.commons.logging.LogFactory;
import org.openntf.domino.DAOFactory;
import org.openntf.domino.logger.LoggerDAO;
import org.openntf.logging.entity.LogEntry;
import org.openntf.utils.JSFUtils;

import com.google.common.collect.Multimap;

/**
 * Used when synchronous logging is set
 * 
 * @author Olle Thalén
 * 
 */
public abstract class LoggerService {

	/**
	 * Single-threaded implementation
	 * 
	 * @param session
	 */
	public static void processBuffer(Session session) {
		LogBuffer buf = (LogBuffer) LogFactory.getFactory().getAttribute(
				"buffer");

		Multimap<String, List<LogEntry>> logQueue = buf.getQueue();
		Map<?, ?> applicationScope = (Map<?, ?>) JSFUtils
				.getVariable("applicationScope");
		DAOFactory factory = new DAOFactory(session, applicationScope.get(
				"dbFilePath").toString());
		List<LoggerDAO> daos = factory.getLoggerDAO();

		while (true) {
			synchronized (logQueue) {
				if (logQueue.isEmpty()) {
					break;
				}
				Set<String> set = logQueue.keySet();
				Iterator<String> it = set.iterator();
				// put all keys in a temporary list,
				// so we don't cause a ConcurrentModificationException
				ArrayList<String> keys = new ArrayList<String>();
				while (it.hasNext()) {
					keys.add(it.next());
				}

				for (String userName : keys) {
					for (List<LogEntry> logEntries : logQueue
							.removeAll(userName)) {
						synchronized (logEntries) {
							for (LoggerDAO appender : daos) {
								appender.createLogDocument(logEntries, userName);
							}
						}
					}
				}
			}
		}
		factory.recycle();
	}
}
