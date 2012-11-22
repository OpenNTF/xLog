package org.openntf.logging.events;

import java.util.Map;

/**
 * Listener interface for when relevant data in application scope has been modified
 * 
 * @author olle.thalen
 *
 */
public interface ApplicationScopeListener {

	public void update(Map<?, ?> applicationScope);
}
