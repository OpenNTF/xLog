package org.openntf.logging.listeners;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

public class RestoreViewListener implements PhaseListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4801967787689617537L;

	@Override
	public void afterPhase(PhaseEvent event) {
		
	}

	@Override
	public void beforePhase(PhaseEvent event) {
		if (event.getPhaseId() == PhaseId.RESTORE_VIEW) {
			System.out.println("before restore view phase");
		}
	}

	@Override
	public PhaseId getPhaseId() {
		return PhaseId.ANY_PHASE;
	}

}
