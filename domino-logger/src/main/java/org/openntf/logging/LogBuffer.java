package org.openntf.logging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.openntf.logging.entity.LogEntry;
import org.openntf.utils.JSFUtils;
import org.openntf.utils.UniqueThreadIdGenerator;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.ibm.xsp.designer.context.ServletXSPContext;


public class LogBuffer implements Observer {

	private Map<String, List<LogEntry>> logEntries = Collections.synchronizedMap(new HashMap<String, List<LogEntry>>());
	private Multimap<String, List<LogEntry>> queue;
	private Map<Integer, String> classNames = Collections.synchronizedMap(new HashMap<Integer, String>());
	
	public LogBuffer() {
		Multimap<String, List<LogEntry>> tmp = HashMultimap.create();
		
		this.queue = Multimaps.synchronizedMultimap(tmp);
	}
	
	@Override
	public void update(Observable o, Object arg) {
		LogEntry logentry = (LogEntry) arg;
		String currentId = String.valueOf(UniqueThreadIdGenerator.getCurrentThreadId());
		List<LogEntry> list = logEntries.get(currentId);
		if (list == null) {
			list = new ArrayList<LogEntry>();
			logEntries.put(currentId, list);
		}
		classNames.remove(list.hashCode());
		list.add(logentry);
		String name = ((DominoLogger)o).getEntryPoints().get(Integer.parseInt(currentId));
		classNames.put(list.hashCode(), name);
	}
	
	/**
	 * Saves the current logentry list in the queue, using the thread-local variable to identify which list to
	 * put in the queue. <br/>
	 * The queue is a multimap so if the same user logs twice before the queue has been processed, both logentry
	 * lists will be available in the queue.
	 */
	public void saveLogDocument() {
		String currentId = String.valueOf(UniqueThreadIdGenerator.getCurrentThreadId());
		if (logEntries.containsKey(currentId)) {
			try {
				String fullName = null;
				try {
					ServletXSPContext xspContext = (ServletXSPContext) JSFUtils
							.getVariable("context");
					fullName = xspContext.getUser().getDistinguishedName();
				} catch (NullPointerException e) {
					fullName = "Test user";
				}
				
				queue.put(fullName, logEntries.remove(currentId));
			} catch (IllegalStateException e) {
				System.out.println("Failed to add log entries to queue, no log document will be created.");
			}
		}
	}

	public Map<Integer, String> getClassNames() {
		return classNames;
	}

	public void setClassNames(Map<Integer, String> classNames) {
		this.classNames = classNames;
	}

	public Multimap<String, List<LogEntry>> getQueue() {
		return queue;
	}

	public void setQueue(Multimap<String, List<LogEntry>> queue) {
		this.queue = queue;
	}
}