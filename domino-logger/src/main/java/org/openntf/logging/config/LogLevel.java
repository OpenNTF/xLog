package org.openntf.logging.config;

/**
 * Represents the log levels
 * 
 * @author Olle Thalén
 *
 */
public enum LogLevel {
	ALL(1), TRACE(2), DEBUG(3), INFO(4), WARN(5), ERROR(6), FATAL(7);

	private int value;

	private LogLevel(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static LogLevel getLevel(String logLevel) {
		if (logLevel == null || logLevel.length() > 1
				|| !Character.isDigit(logLevel.charAt(0))) {
			return null;
		} else {
			int val = Integer.parseInt(logLevel);
			for (LogLevel level : LogLevel.values()) {
				if (level.getValue() == val) {
					return level;
				}
			}
		}
		return null;
	}
}
