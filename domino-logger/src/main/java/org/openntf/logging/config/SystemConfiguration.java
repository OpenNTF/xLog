package org.openntf.logging.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Keep track of the system configuration
 * 
 * @author Olle Thalén
 *
 */
public class SystemConfiguration {
	
	public static void setImplementations(List<String> implementations) {
		synchronized (systemInfo) {
			systemInfo.setLogImplementations(implementations);
		}
	}

	public static void setDiagnositc(boolean diagnostic) {
		synchronized(systemInfo) {
			systemInfo.setDiagnostic(diagnostic);
		}
	}
	
	public static boolean isDiagnostic() {
		return systemInfo.isDiagnostic();
	}
	
	public static List<String> getImplementations() {
		return systemInfo.getLogImplementations();
	}

	private static final SystemInfo systemInfo = new SystemInfo();

	private static class SystemInfo {
		private List<String> logImplementations = new ArrayList<String>();
		private boolean diagnostic = false;
		public boolean isDiagnostic() {
			return diagnostic;
		}

		public void setDiagnostic(boolean diagnostic) {
			this.diagnostic = diagnostic;
		}

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
