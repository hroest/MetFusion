package de.ipbhalle.MetFlow.web.controller;

public class WorkflowStackingBean {

	// default panel
	private String selectedGroup = "workflowOne";

	/**
	 * Gets the selected panel group.
	 * 
	 * @return the panel group id
	 */
	public String getSelectedGroup() {
		return selectedGroup;
	}

	/**
	 * Sets the selected panel group.
	 * 
	 * @param selectedGroup the new panel group id
	 */
	public void setSelectedGroup(String selectedGroup) {
		this.selectedGroup = selectedGroup;
	}

}
