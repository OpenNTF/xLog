package org.openntf.utils;

import java.util.Map;

import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.View;

public class ApplicationScopeUtils {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void checkAndSet() {
		try {
			Map applicationScope = (Map) JSFUtils.getVariable("applicationScope");
			Document doc = null;
			if (!applicationScope.containsKey("async") || !applicationScope.containsKey("logLevel") ||
					!applicationScope.containsKey("delay") || !applicationScope.containsKey("dbFilePath")) {
				doc = getConfigurationDocument();
				if (!applicationScope.containsKey("async")) {
					if (doc == null) {
						applicationScope.put("async", "false");
					} else {
						applicationScope.put("async", doc.getItemValueString("Async"));
					}
				}
				if (!applicationScope.containsKey("logLevel")) {
					if (doc == null) {
						applicationScope.put("logLevel", "1");
					} else {
						applicationScope.put("logLevel", doc.getItemValueString("LogLevel"));
					}
				}
				if (!applicationScope.containsKey("delay")) {
					if (doc == null) {
						applicationScope.put("delay", Double.valueOf(0));
					} else {
						applicationScope.put("delay", doc.getItemValueDouble("Delay"));
					}
				}
				if (!applicationScope.containsKey("dbFilePath")) {
					applicationScope.put("dbFilePath", JSFUtils.getCurrentDatabase().getFilePath());
				}
				if (doc != null) {
					doc.recycle();
				}
			}
		} catch (NotesException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static Document getConfigurationDocument() throws NotesException {
		View view = JSFUtils.getCurrentDatabase().getView("allByForm.LU");
		if (view == null) {
			return null;
		}
		return view.getDocumentByKey("LoggerConfig");
	}
}
