package org.openntf.logging.entity;

import org.openntf.logging.config.LogLevel;

/**
 * Represents a log entry
 * 
 * @author Olle Thalén
 *
 */
public class LogEntry {
	private String logEntry;
	private LogLevel level;
	private boolean error;
	//if this entry is an error, stackTrace contains the throwables stack trace as a an array of Strings
	private String[] stackTrace;
	private String user;
	
	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getLogEntry() {
		return logEntry;
	}
	
	public void setLogEntry(String logEntry) {
		this.logEntry = logEntry;
	}
	
	public boolean isError() {
		return error;
	}
	
	public void setError(boolean error) {
		this.error = error;
	}

	public LogLevel getLevel() {
		return level;
	}

	public void setLevel(LogLevel level) {
		this.level = level;
	}

	public String[] getStackTrace() {
		return stackTrace;
	}

	public void setStackTrace(String[] stackTrace) {
		this.stackTrace = stackTrace;
	}
	
	@Override
	public String toString() {
		return user + ": " + logEntry;
	}
	
}
