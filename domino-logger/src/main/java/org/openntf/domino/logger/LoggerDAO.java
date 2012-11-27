package org.openntf.domino.logger;

import java.util.List;

import lotus.domino.Database;
import lotus.domino.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.SimpleLog;
import org.openntf.logging.config.SystemConfiguration;
import org.openntf.logging.entity.LogEntry;

/**
 * Log appenders should extend this class in order to do some specific logging.
 * 
 * @author Olle Thalén
 *
 */
public abstract class LoggerDAO {

	protected Session session;
	protected Database dbCurrent;
	protected Database dbLogger;
	protected Log logger;
	
	protected LoggerDAO(Session session, Database dbCurrent, Database dbLogger) {
		this.session = session;
		this.dbCurrent = dbCurrent;
		this.dbLogger = dbLogger;
		SimpleLog log = new SimpleLog(LoggerDAO.class.getName());
		log.setLevel(SystemConfiguration.isDiagnostic() ? SimpleLog.LOG_LEVEL_ALL : SimpleLog.LOG_LEVEL_ERROR);
		logger = log;
	}

	/**
	 * This method is the entry point for a log appender.
	 * 
	 * @param logEntries all log entries added during one request
	 * @param userName the user who added the log entries
	 * 
	 * @author Olle Thalén
	 */
	public abstract void createLogDocument(List<LogEntry> logEntries, String userName);
	
	public void recycle() {
		this.session = null;
		this.dbCurrent = null;
		this.dbLogger = null;
		this.logger = null;
	}
}
