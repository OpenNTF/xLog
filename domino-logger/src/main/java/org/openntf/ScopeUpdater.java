package org.openntf;

import java.util.Map;

import org.openntf.logging.events.ApplicationScopeListener;
import org.openntf.logging.events.EventUtils;
import org.openntf.utils.JSFUtils;

/**
 * Utility class for sending updates to listeners
 * 
 * @author Olle Thalén
 *
 */
public class ScopeUpdater {

	public void update() {
		Map<?, ?> applicationScope = (Map<?, ?>) JSFUtils.getVariable("applicationScope");
		for (ApplicationScopeListener listener : EventUtils.getListeners()) {
			listener.update(applicationScope);
		}
	}
}
