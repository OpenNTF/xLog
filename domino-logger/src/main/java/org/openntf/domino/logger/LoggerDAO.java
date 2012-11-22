package org.openntf.domino.logger;

import java.util.List;

import org.openntf.logging.entity.LogEntry;

import lotus.domino.Database;
import lotus.domino.Session;


public abstract class LoggerDAO {

	protected Session session;
	protected Database dbCurrent;
	protected Database dbLogger;
	
	protected LoggerDAO(Session session, Database dbCurrent, Database dbLogger) {
		this.session = session;
		this.dbCurrent = dbCurrent;
		this.dbLogger = dbLogger;
	}
	
	public abstract void createLogDocument(List<LogEntry> logEntries, String userName);
	
	public void recycle() {
		this.session = null;
		this.dbCurrent = null;
		this.dbLogger = null;
	}
}
