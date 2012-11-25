package org.openntf.logging.listeners;

import java.util.Map;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import org.apache.commons.logging.LogFactory;
import org.openntf.logging.LogBuffer;
import org.openntf.logging.LoggerService;
import org.openntf.logging.ResourceHandler;
import org.openntf.logging.scheduler.Scheduler;
import org.openntf.utils.ApplicationScopeUtils;
import org.openntf.utils.JSFUtils;

public class LogEventListener implements PhaseListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5597217179112322644L;

	public void afterPhase(PhaseEvent event) {
		try {
			if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
				ApplicationScopeUtils.checkAndSet();

				LogFactory fac = LogFactory.getFactory();
				if (fac instanceof ResourceHandler) {
					((ResourceHandler) fac).commit();
				}
				LogBuffer buf = (LogBuffer) fac.getAttribute("buffer");
				/*
				 * If there are log messages start a new thread in 5 seconds,
				 * only if the thread is not already running. If the thread is
				 * running but the JobChangeListener hasn't been notified yet,
				 * wait until the notification is complete, then attempt to
				 * start the job again.
				 */
				if (buf != null && !buf.getQueue().isEmpty()) {
					Map<?, ?> applicationScope = (Map<?, ?>) JSFUtils
							.getVariable("applicationScope");
					boolean async = Boolean.valueOf(applicationScope.get(
							"async").toString());
					Double dbl = (Double) applicationScope.get("delay");
					long delay = dbl.longValue();
					if (async) {
						if (!Scheduler.start(delay)) {
							Scheduler.waitAndStart(delay);
						}
					} else {
						LoggerService.processBuffer(JSFUtils
								.getCurrentSession());
					}
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public void beforePhase(PhaseEvent event) {

	}

	public PhaseId getPhaseId() {
		return PhaseId.ANY_PHASE;
	}

}
