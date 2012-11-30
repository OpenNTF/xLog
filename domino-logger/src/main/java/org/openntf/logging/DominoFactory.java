package org.openntf.logging;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogConfigurationException;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.SimpleLog;
import org.openntf.logging.config.SystemConfiguration;
import org.openntf.utils.DominoProvider;

/**
 * The log factory implementation. <br/>
 * This class is responsible for creating logger objects, it also contains a
 * LogBuffer instance which stores log data in thread-safe data structures.
 * 
 * @author Olle Thalén
 * 
 */
public class DominoFactory extends LogFactory implements ResourceHandler {

	private Map<Class<?>, DominoLogger> loggers = Collections
			.synchronizedMap(new HashMap<Class<?>, DominoLogger>());
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
		return null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Log getInstance(Class clazz) throws LogConfigurationException {
		List<String> ignoreList = SystemConfiguration.getIgnoreList();		
		if (clazz != null && clazz.getPackage() != null) {
			
			String packageName = clazz.getPackage().getName();

			for (String ignorePrefix : ignoreList) {
				if (packageName.startsWith(ignorePrefix)) {
					SimpleLog logger = new SimpleLog(clazz.getName());
					logger.setLevel(SimpleLog.LOG_LEVEL_OFF);
					return logger;
				}
			}
		}
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
