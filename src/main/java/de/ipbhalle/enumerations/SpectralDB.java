/**
 * created by Michael Gerlich, Aug 17, 2012 - 4:18:55 PM
 */ 

package de.ipbhalle.enumerations;

public enum SpectralDB {

	MassBank("MassBank", "massbankSettings"),
	GMD("GMD", "gmdSettings"),
	Metlin("Metlin", "metlinSettings");
	
	private String label;
	private String panel;
	
	private SpectralDB(String label, String panel) {
		this.label = label;
		this.panel = panel;
	}

	public String getLabel() {
		return label;
	}

	public String getPanel() {
		return panel;
	}
}
