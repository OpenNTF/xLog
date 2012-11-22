package org.openntf.logging;

/**
 * Interface used by log implementations to handle different parts of the logging life-cycle.
 * 
 * @author olle.thalen
 *
 */
public interface ResourceHandler {

	/**
	 * When this method is invoked it tells the implementation class that the
	 * current log transaction is completed, and a new one can start after this call. 
	 */
	public void commit();
}
