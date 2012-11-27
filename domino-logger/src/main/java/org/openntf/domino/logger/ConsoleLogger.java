package org.openntf.domino.logger;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openntf.logging.entity.LogEntry;

import lotus.domino.Database;
import lotus.domino.Session;

/**
 * Log appender which logs to System.out
 * 
 * @author Olle Thalén
 *
 */
public class ConsoleLogger extends LoggerDAO {


	public ConsoleLogger(Session session, Database dbCurrent,
			Database dbLogger) {
		super(session, dbCurrent, dbLogger);
	}

	@Override
	public void createLogDocument(List<LogEntry> logEntries, String userName) {
		for (LogEntry entry : logEntries) {
			System.out.println(entry.getLogEntry());
			if (entry.isError()) {
				System.out.println(StringUtils.join(entry.getStackTrace(), "\n"));
			}
		}
	}

}
