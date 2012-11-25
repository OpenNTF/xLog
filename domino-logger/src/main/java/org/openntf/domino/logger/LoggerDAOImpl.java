package org.openntf.domino.logger;

import java.util.List;

import lotus.domino.ACL;
import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.Item;
import lotus.domino.NotesException;
import lotus.domino.RichTextItem;
import lotus.domino.RichTextParagraphStyle;
import lotus.domino.Session;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.openntf.logging.LogBuffer;
import org.openntf.logging.entity.LogEntry;

public class LoggerDAOImpl extends LoggerDAO {

	public LoggerDAOImpl(Session session, Database dbCurrent, Database dbLogger) {
		super(session, dbCurrent, dbLogger);
	}
	
	@Override
	public void createLogDocument(List<LogEntry> logEntries, String userName) {
		logger.debug("creating a new log document (thread: " + Thread.currentThread().getId() + ")");
		 
		Document logDoc;
		try {
			logDoc = dbLogger.createDocument();
			logDoc.replaceItemValue("Form", "LogEntry");
			
			logger.debug("Username: " + userName);
			switch (dbCurrent.queryAccess(userName)) {
			case ACL.LEVEL_AUTHOR:
				logDoc.replaceItemValue("AccessLevel", "Author");
				break;
			case ACL.LEVEL_DEPOSITOR:
				logDoc.replaceItemValue("AccessLevel", "Depositor");
				break;
			case ACL.LEVEL_DESIGNER:
				logDoc.replaceItemValue("AccessLevel", "Designer");
				break;
			case ACL.LEVEL_EDITOR:
				logDoc.replaceItemValue("AccessLevel", "Editor");
				break;
			case ACL.LEVEL_MANAGER:
				logDoc.replaceItemValue("AccessLevel", "Manager");
				break;
			case ACL.LEVEL_NOACCESS:
				logDoc.replaceItemValue("AccessLevel", "No access");
				break;
			case ACL.LEVEL_READER:
				logDoc.replaceItemValue("AccessLevel", "Reader");
			}
			Item roles = logDoc.replaceItemValue("Roles", "");
			
			for (Object role : dbCurrent.queryAccessRoles(userName)) {
				roles.appendToTextList(role.toString());
			}
			logDoc.replaceItemValue("ApplicationName", dbCurrent.getTitle());
			
			LogBuffer buffer = (LogBuffer) LogFactory.getFactory().getAttribute("buffer");
			logDoc.replaceItemValue("Subject", buffer.getClassNames().remove(logEntries.hashCode()));
			RichTextItem body = logDoc.createRichTextItem("Body");
			RichTextItem stacktrace = logDoc.createRichTextItem("StackTrace");
			RichTextParagraphStyle style = createStyle();
			body.appendParagraphStyle(style);
			
			int counter = 0;
			for (LogEntry entry : logEntries) {
				body.appendText(entry.getLogEntry());
				body.addNewLine();
				if (entry.isError()) {
					stacktrace.setValueString(StringUtils.EMPTY);
					for (String line : entry.getStackTrace()) {
						stacktrace.appendText(line);
						stacktrace.addNewLine();
					}
					logDoc.replaceItemValue("Exception", "1");
				}
				
				counter++;
				
				if (counter % 100 == 0) {
					logDoc.save(true); //avoid caching large documents
				}
			}
			logDoc.computeWithForm(false, false);
			logDoc.replaceItemValue("CreatedBy", userName);
			logDoc.save(true);
			style.recycle();
			body.recycle();
			logDoc.recycle();
			
		} catch (NotesException e) {
			throw new RuntimeException(e);
		}
	}

	private RichTextParagraphStyle createStyle() throws NotesException {
		RichTextParagraphStyle r = session.createRichTextParagraphStyle();
		r.setAlignment(RichTextParagraphStyle.ALIGN_LEFT);
		r.setSpacingAbove(RichTextParagraphStyle.SPACING_SINGLE);
		r.setSpacingBelow(RichTextParagraphStyle.SPACING_ONE_POINT_50);
		r.setInterLineSpacing(RichTextParagraphStyle.SPACING_SINGLE);
		r.setLeftMargin(RichTextParagraphStyle.RULER_ONE_INCH);
		r
				.setFirstLineLeftMargin((int) (RichTextParagraphStyle.RULER_ONE_INCH * 1.5));
		r.setRightMargin(0);
		return r;
	}
}
