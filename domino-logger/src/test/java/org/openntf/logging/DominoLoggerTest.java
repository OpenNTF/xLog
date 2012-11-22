package org.openntf.logging;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.text.StringContains.containsString;
import static org.junit.Assert.assertThat;

import java.util.List;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.View;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openntf.logging.LogBuffer;
import org.openntf.logging.entity.LogEntry;
import org.openntf.utils.DominoProvider;


@RunWith(JMock.class)
public class DominoLoggerTest {

	private final Mockery context = new JUnit4Mockery();
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testLogMessageAdded() throws Exception {
		final Database db = context.mock(Database.class);
		final DominoProvider provider = context.mock(DominoProvider.class);
		final View view = context.mock(View.class);
		final Document doc = context.mock(Document.class);
		
		LogFactory fac = LogFactory.getFactory();
		fac.setAttribute("provider", provider);
		
		context.checking(new Expectations() {
			{
				oneOf(provider).getCurrentDatabase();
				will(returnValue(db));
				oneOf(db).getView("allByForm.LU");
				will(returnValue(view));
				oneOf(view).getDocumentByKey("LoggerConfig");
				will(returnValue(doc));
				oneOf(doc).getItemValueString("LogLevel");
				will(returnValue("1"));
				oneOf(doc).recycle();
			}
		});
		Log logger = LogFactory.getLog(DominoLoggerTest.class);
		foo(logger);
		LogBuffer buf = (LogBuffer) LogFactory.getFactory().getAttribute("buffer");
		buf.saveLogDocument();
		assertThat(buf.getQueue().size(), is(1));
		buf.getQueue().clear();
	}
	
	private void foo(Log logger) {
		logger.debug("hej");
	}

	@Test
	public void testLogMessageContains() throws Exception {
		Log logger = LogFactory.getLog(DominoLoggerTest.class);
		logger.debug("hej");
		LogBuffer buf = (LogBuffer) LogFactory.getFactory().getAttribute("buffer");
		buf.saveLogDocument();
		
		List<LogEntry> entries = buf.getQueue().get("Test user").iterator().next();
		System.out.println("log message: " + entries.get(0).getLogEntry());
		assertThat(entries.get(0).getLogEntry(), containsString("testLogMessageContains"));
		buf.getQueue().clear();
	}
	
	@SuppressWarnings("null")
	@Test
	public void testError() throws Exception {
		Log logger = LogFactory.getLog(DominoLoggerTest.class);
		String s = null;
		try {
			s.charAt(0);
		} catch (NullPointerException e) {
			logger.error(null, e);
		}
		LogBuffer buf = (LogBuffer) LogFactory.getFactory().getAttribute("buffer");
		buf.saveLogDocument();
		List<LogEntry> entries = buf.getQueue().get("Test user").iterator().next();
		assertThat(entries.get(0).getStackTrace(), notNullValue());
		buf.getQueue().clear();
	}
}
