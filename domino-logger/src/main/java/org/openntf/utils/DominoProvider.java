package org.openntf.utils;

import lotus.domino.Database;
import lotus.domino.Session;

/**
 * 
 * @author Olle Thal�n
 *
 */
public interface DominoProvider {

	public Database getCurrentDatabase();
	public Session getCurrentSession();
}
