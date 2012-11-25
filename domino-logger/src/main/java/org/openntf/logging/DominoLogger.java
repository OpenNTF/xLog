package org.openntf.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.View;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.SimpleLog;
import org.openntf.logging.config.LogLevel;
import org.openntf.logging.config.SystemConfiguration;
import org.openntf.logging.entity.LogEntry;
import org.openntf.logging.events.ApplicationScopeListener;
import org.openntf.utils.DominoProvider;
import org.openntf.utils.JSFUtils;
import org.openntf.utils.UniqueThreadIdGenerator;

/**
 * Log implementation which creates log entries and sends them to its Observer.
 * 
 * @author Olle Thalén
 *
 */
public class DominoLogger extends Observable implements Log, ApplicationScopeListener {
	
	private Map<Integer, String> entryPoints = Collections.synchronizedMap(new HashMap<Integer, String>());
	private Map<Integer, Boolean> flags = Collections.synchronizedMap(new HashMap<Integer, Boolean>());
	private LogLevel logLevel;
	private DominoProvider provider;
	private final static String DOT = ".";
	private final static String LEFT = "(";
	private final static String RIGHT = ")";
	private Log logger;
	
	public DominoLogger(DominoProvider provider, Observer o) {
		SimpleLog log = new SimpleLog(DominoLogger.class.getName());
		log.setLevel(SystemConfiguration.isDiagnostic() ? SimpleLog.LOG_LEVEL_ALL : SimpleLog.LOG_LEVEL_ERROR);
		this.logger = log;
		this.provider = provider;
		addObserver(o);
		this.checkLogLevel();
	}
	
	private void addLogEntry(Object message, LogLevel level, Throwable t) {
		int id = UniqueThreadIdGenerator.getCurrentThreadId();
		logger.debug("transaction id used when adding a log message: " + id);
		logger.debug("thread used when adding a log entry: " + Thread.currentThread().getId());
		
		boolean newDocument = flags.containsKey(id) ? flags.get(id) : true;
		
		StackTraceElement[] elems = Thread.currentThread().getStackTrace();
		StackTraceElement el = getElement(elems);
		
		/*
		 * Put the entry point of the log event in the entryPoints map if this is the first log entry
		 */
		if (newDocument) {
			entryPoints.put(id, el.getClassName() + "." + el.getMethodName());
			if (!flags.containsKey(id)) {
				flags.put(id, false);
			}
		}
		LogEntry entry = new LogEntry();
		entry.setError(t != null);
		
		if (message == null) {
			entry.setLogEntry(StringUtils.EMPTY);
		} else {
			StringBuilder builder = new StringBuilder(100);
			builder.append(el.getClassName());
			builder.append(DOT);
			builder.append(el.getMethodName());
			builder.append(LEFT);
			builder.append(el.getLineNumber());
			builder.append(RIGHT);
			StringBuilder builder2 = new StringBuilder(message.toString().length() + 100);
			builder2.append("[");
			builder2.append(level.name());
			builder2.append("] ");
			builder2.append(builder);
			builder2.append(": ");
			builder2.append(message.toString());
			
			entry.setLogEntry(builder2.toString());
		}
		
		entry.setLevel(level);
		if (t != null) {
			StringWriter out = new StringWriter();
			PrintWriter pw = new PrintWriter(out);
			t.printStackTrace(pw);
			pw.flush();
			
			String[] lines = out.getBuffer().toString().split("\n");
			entry.setStackTrace(lines);
		}
		setChanged();
		notifyObservers(entry);
	}
	
	private StackTraceElement getElement(StackTraceElement[] elems) {
		int offset = 0;
		for (int i = 0; i < elems.length; i++) {
			if (elems[i].getMethodName().contains("addLogEntry")) {
				offset = i;
				break;
			}
		}
		return elems[offset + 2];
	}
	
	@Override
	public boolean isDebugEnabled() {
		//checkLogLevel();
		return logLevel == null ? false : LogLevel.DEBUG.getValue() >= logLevel.getValue();
	}

	@Override
	public boolean isErrorEnabled() {
		//checkLogLevel();
		return logLevel == null ? false : LogLevel.ERROR.getValue() >= logLevel.getValue();
	}

	@Override
	public boolean isFatalEnabled() {
		//checkLogLevel();
		return logLevel == null ? false : LogLevel.FATAL.getValue() >= logLevel.getValue();
	}

	@Override
	public boolean isInfoEnabled() {
		//checkLogLevel();
		return logLevel == null ? false : LogLevel.INFO.getValue() >= logLevel.getValue();
	}

	@Override
	public boolean isTraceEnabled() {
		//checkLogLevel();
		return logLevel == null ? false : LogLevel.TRACE.getValue() >= logLevel.getValue();
	}

	@Override
	public boolean isWarnEnabled() {
		//checkLogLevel();
		return logLevel == null ? false : LogLevel.WARN.getValue() >= logLevel.getValue();
	}

	@Override
	public void trace(Object message) {
		if (isTraceEnabled()) {
			addLogEntry(message, LogLevel.TRACE, null);
		}
	}

	@Override
	public void trace(Object message, Throwable t) {
		if (isTraceEnabled()) {
			addLogEntry(message, LogLevel.TRACE, t);
		}
	}

	private void configDatabaseLogLevel() {
		Database db = null;
		try {
			db = provider == null ? JSFUtils.getCurrentDatabase() : provider.getCurrentDatabase();
		} catch (NullPointerException e) {
			return;
		}
		View view;
		try {
			view = db.getView("allByForm.LU");
			Document doc = view.getDocumentByKey("LoggerConfig");
			String level = doc.getItemValueString("LogLevel");
			this.logLevel = LogLevel.getLevel(level);
			doc.recycle();
		} catch (NotesException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void debug(Object message) {
		if (isDebugEnabled()) {
			addLogEntry(message, LogLevel.DEBUG, null);
		}
	}

	@Override
	public void debug(Object message, Throwable t) {
		if (isDebugEnabled()) {
			addLogEntry(message, LogLevel.DEBUG, t);
		}
	}

	/*
	 * Make this check and set operation synchronized, enough that one thread sets the logLevel.
	 */
	private void checkLogLevel() {
		logger.debug("calling checkLogLevel");
		synchronized(this) {
			if (this.logLevel == null) {
				configDatabaseLogLevel();
			}
		}
	}
	
	@Override
	public void info(Object message) {
		if (isInfoEnabled()) {
			addLogEntry(message, LogLevel.INFO, null);
		}
	}

	@Override
	public void info(Object message, Throwable t) {
		if (isInfoEnabled()) {
			addLogEntry(message, LogLevel.INFO, t);
		}
	}

	@Override
	public void warn(Object message) {
		if (isWarnEnabled()) {
			addLogEntry(message, LogLevel.WARN, null);
		}
		
	}

	@Override
	public void warn(Object message, Throwable t) {
		if (isWarnEnabled()) {
			addLogEntry(message, LogLevel.WARN, t);
		}
	}

	@Override
	public void error(Object message) {
		if (isErrorEnabled()) {
			addLogEntry(message, LogLevel.ERROR, null);
		}
	}

	@Override
	public void error(Object message, Throwable t) {
		if (isErrorEnabled()) {
			addLogEntry(message, LogLevel.ERROR, t);
		}		
	}

	@Override
	public void fatal(Object message) {
		if (isFatalEnabled()) {
			addLogEntry(message, LogLevel.FATAL, null);
		}
		
	}

	@Override
	public void fatal(Object message, Throwable t) {
		if (isFatalEnabled()) {
			addLogEntry(message, LogLevel.FATAL, null);
		}
	}
	
	/**
	 * Set the new document flag for the current thread.
	 * If the current thread invokes any logging methods on this instance later,
	 * the first call will be treated as a new log document.
	 */
	public void newDocument() {
		flags.put(UniqueThreadIdGenerator.getCurrentThreadId(), true);
	}

	public Map<Integer, String> getEntryPoints() {
		return entryPoints;
	}

	public void setEntryPoints(Map<Integer, String> entryPoints) {
		this.entryPoints = entryPoints;
	}

	public LogLevel getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(LogLevel logLevel) {
		this.logLevel = logLevel;
	}

	public DominoProvider getProvider() {
		return provider;
	}

	public void setProvider(DominoProvider provider) {
		this.provider = provider;
	}

	@Override
	public void update(Map<?, ?> applicationScope) {
		String logLevel = (String) applicationScope.get("logLevel");
		this.logLevel = LogLevel.getLevel(logLevel);
	}

}
