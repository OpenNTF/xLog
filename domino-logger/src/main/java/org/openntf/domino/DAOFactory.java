package org.openntf.domino;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import lotus.domino.Database;
import lotus.domino.DbDirectory;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.View;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.SimpleLog;
import org.openntf.domino.logger.ConsoleLogger;
import org.openntf.domino.logger.LoggerDAO;
import org.openntf.logging.config.SystemConfiguration;


public class DAOFactory {

	private Session session;
	private Database dbLogger;
	private String dbPath;
	private Log logger;
	
	public DAOFactory(Session session, String dbPath) {
		this.session = session;
		this.dbPath = dbPath;
		SimpleLog log = new SimpleLog(DAOFactory.class.getName());
		log.setLevel(SystemConfiguration.isDiagnostic() ? SimpleLog.LOG_LEVEL_ALL : SimpleLog.LOG_LEVEL_ERROR);
		this.logger = log;
	}

	public void recycle() {
		if (dbLogger != null) {
			try {
				dbLogger.recycle();
				dbLogger = null;
			} catch (NotesException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public List<LoggerDAO> getLoggerDAO() {
		List<String> classNames = null;
		try {
			classNames = SystemConfiguration.getImplementations();
		} catch (Throwable t) {
			t.printStackTrace();
		}

		try {
			Database db = session.getDatabase(null, dbPath);
			View view = db.getView("allByForm.LU");
			Document doc = view.getDocumentByKey("LoggerConfig");
			String logServer = doc.getItemValueString("LogServer");
			String replicaId = doc.getItemValueString("LogReplicaID");
			DbDirectory directory = null;
			directory = StringUtils.isEmpty(logServer) ? session
					.getDbDirectory(db.getServer()) : session
					.getDbDirectory(logServer);
			Database dbLog = directory.openDatabaseByReplicaID(replicaId);
			this.dbLogger = dbLog;
			
			logger.debug("db path: " + dbLog.getFilePath());
			if (!dbLog.isOpen()) {
				dbLog.open();
			}
			logger.debug("is database open: " + dbLog.isOpen());
			doc.recycle();

			ArrayList<LoggerDAO> daos = new ArrayList<LoggerDAO>();
			for (String className : classNames) {
				logger.debug("classname: " + className);
				Class<?> c = null;
				try {
					c = Class.forName(className);
				} catch (Throwable t) {
					t.printStackTrace();
					continue;
				}
				Object instance = null;
				try {
					Constructor<?> con = c.getConstructor(new Class<?>[] {
							Session.class, Database.class, Database.class });

					instance = con.newInstance(new Object[] { session, db,
							dbLog });
				} catch (NoSuchMethodException e) {
					instance = c.newInstance();
				}
				daos.add((LoggerDAO) instance);
			}
			if (daos.isEmpty()) {
				daos.add(new ConsoleLogger(session, db, dbLog));
			}
			return daos;
		} catch (Throwable t) {
			t.printStackTrace();
			return null;
		}
	}
}
