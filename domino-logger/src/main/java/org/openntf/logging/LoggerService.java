package org.openntf.logging;

import java.util.List;
import java.util.Map;
import java.util.Set;

import lotus.domino.Session;

import org.apache.commons.logging.LogFactory;
import org.openntf.domino.DAOFactory;
import org.openntf.domino.logger.LoggerDAO;
import org.openntf.logging.entity.LogEntry;
import org.openntf.utils.JSFUtils;

import com.google.common.collect.ImmutableSet;
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
		LogBuffer buf = (LogBuffer) LogFactory.getFactory()
				.getAttribute("buffer");
		
		Multimap<String, List<LogEntry>> logQueue = buf.getQueue();
		Map<?, ?> applicationScope = (Map<?, ?>) JSFUtils.getVariable("applicationScope");
		DAOFactory factory = new DAOFactory(session, applicationScope.get("dbFilePath").toString());
		List<LoggerDAO> daos = factory.getLoggerDAO();
		
		while (!logQueue.isEmpty()) {
			Set<String> keys = ImmutableSet.copyOf(logQueue.keySet());
			for (String userName : keys) {
				for (List<LogEntry> logEntries : logQueue.get(userName)) {
					for (LoggerDAO appender : daos) {
						appender.createLogDocument(logEntries, userName);
					}
				}
				logQueue.removeAll(userName);
			}
		}
		factory.recycle();
	}
}
