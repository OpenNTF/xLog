package org.openntf.logging;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogConfigurationException;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.SimpleLog;
import org.openntf.utils.DominoProvider;


public class DominoFactory extends LogFactory implements ResourceHandler {

	private Map<Class<?>, DominoLogger> loggers = Collections.synchronizedMap(new HashMap<Class<?>, DominoLogger>());
	private LogBuffer buffer = new LogBuffer();
	private DominoProvider provider = null;
	
	@Override
	public Object getAttribute(String name) {
		if (name.equals("buffer")) {
			return buffer;
		} else if (name.equals("scopeListeners")) {
			return loggers.values();
		} else {
			return null;
		}
	}

	@Override
	public String[] getAttributeNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Log getInstance(Class clazz) throws LogConfigurationException {
		DominoLogger log = loggers.get(clazz);
		if (log == null) {
			log = new DominoLogger(provider, buffer);
			loggers.put(clazz, log);
		}
		return log;
	}

	@Override
	public Log getInstance(String name) throws LogConfigurationException {
		try {
			Class<?> c = Class.forName(name);
			return getInstance(c);
		} catch (ClassNotFoundException e) {
			System.out.println("class " + name + " not found");
			SimpleLog logger = new SimpleLog(name);
			logger.setLevel(SimpleLog.LOG_LEVEL_OFF);
			return logger;
		}
	}

	@Override
	public void release() {
	}

	@Override
	public void removeAttribute(String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setAttribute(String name, Object value) {
		if (name.equals("provider")) {
			this.provider = (DominoProvider) value;
		}
		
	}

	@Override
	public void commit() {
		buffer.saveLogDocument();
		for (DominoLogger logger : loggers.values()) {
			logger.newDocument();
		}
	}

}
