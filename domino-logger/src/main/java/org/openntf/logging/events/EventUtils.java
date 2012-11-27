package org.openntf.logging.events;

import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.LogFactory;

/**
 * Utility class
 * 
 * @author Olle Thalén
 *
 */
public class EventUtils {

	public static List<ApplicationScopeListener> getListeners() {
		LogFactory fac = LogFactory.getFactory();
		try {
			@SuppressWarnings("unchecked")
			List<ApplicationScopeListener> list = (List<ApplicationScopeListener>) fac.getAttribute("scopeListeners");
			return list;
		} catch (ClassCastException e) {
			return Collections.emptyList();
		}
	}
}
