package de.ipbhalle.MetFlow.wrapper;

public class WorkflowInput {

	private String name;
	private int depth;
	private String value;
	private String message;
	private boolean shown;
	
	public WorkflowInput() {
		this.name = "";
		this.depth = 0;
		this.value = "";
		this.message = "";
	}

	public WorkflowInput(String name, int depth, String value) {
		this.name = name;
		this.depth = depth;
		this.value = value;
		if(depth == 0) {
			this.message = "Only one single value allowed!";
		}
		else if (depth == 1) {
			this.message = "This is a list of values! Separate entries via newline or ;";
		}
		else if (depth > 1) {
			this.message = "This is a " + depth + "-deep list! Separate lists via ; and list entries via newline";
		}
	}
	
	public WorkflowInput(String name, int depth, String value, boolean shown) {
		this.name = name;
		this.depth = depth;
		this.value = value;
		if(depth == 0) {
			this.message = "Only one single value allowed!";
		}
		else if (depth == 1) {
			this.message = "This is a list of values! Separate entries via newline or ;";
		}
		else if (depth > 1) {
			this.message = "This is a " + depth + "-deep list! Separate lists via ; and list entries via newline";
		}
		this.shown = shown;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public int getDepth() {
		return depth;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setShown(boolean shown) {
		this.shown = shown;
	}

	public boolean isShown() {
		return shown;
	}
}
