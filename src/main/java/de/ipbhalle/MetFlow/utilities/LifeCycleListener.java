package de.ipbhalle.MetFlow.utilities;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;


public class LifeCycleListener implements PhaseListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void beforePhase(PhaseEvent event) {
		System.out.println("BeforePhase: " + event.getPhaseId());
	}

	public void afterPhase(PhaseEvent event) {
		System.out.println("AfterPhase: " + event.getPhaseId());
	}

	public PhaseId getPhaseId() {
		return PhaseId.ANY_PHASE;
	}
}
