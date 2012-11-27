package org.openntf.utils;

import lotus.domino.Database;
import lotus.domino.Session;

/**
 * 
 * @author Olle Thalén
 *
 */
public interface DominoProvider {

	public Database getCurrentDatabase();
	public Session getCurrentSession();
}
