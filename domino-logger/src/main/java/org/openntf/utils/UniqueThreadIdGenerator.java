package org.openntf.utils;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Generator class used to get a transaction ID.
 * 
 * @author Olle Thalén
 *
 */
public class UniqueThreadIdGenerator {

	/**
	 * This method returns a hash of the users distinguished name and the sessionId (if present)
	 * 
	 * @return hashcode which can be used as identifier
	 */
    public static int getCurrentThreadId() {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(JSFUtils.getUsername());
        String sessionId = JSFUtils.getSessionID();
        if (sessionId != null) {
        	builder.append(sessionId);
        }
        return builder.hashCode();
    }
} 
