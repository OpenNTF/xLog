package org.openntf.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.SimpleLog;

public class DiagnosticLogging {
	private static Log logger = new SimpleLog(DiagnosticLogging.class.getName());
	
	public static void initLogger(boolean diagnostic) {
		SimpleLog log = new SimpleLog(DiagnosticLogging.class.getName());
		log.setLevel(diagnostic ? SimpleLog.LOG_LEVEL_ALL : SimpleLog.LOG_LEVEL_ERROR);
		DiagnosticLogging.logger = log;
	}
	
	public static Log getLogger() {
		return logger;
	}
}
