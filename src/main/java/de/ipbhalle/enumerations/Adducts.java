/**
 * created by Michael Gerlich, Oct 4, 2011 - 3:16:06 PM
 */ 

package de.ipbhalle.enumerations;


public enum Adducts {
	
	Neutral(0d, "Neutral"),
	Mplus(0.00054858d, "M+"),
	MplusHplus(-1.007276455d, "[M+H+]"),
	MplusNaplus(-22.98921912d, "[M+Na]+"),
	MplusKplus(-38.96315882d, "[M+K]+"),
	MplusNH4plus(-18.0385d, "[M+NH4]+"),
	MplusHCOOminus(-45.0174, "[M+HCOO]-"),
	Mminus(-0.00054858d, "M-"),
	MminusHminus(1.007276455d, "[M-H]-"),
	MminusNaminus(22.98921912d, "[M-Na]-"),
	MminusKminus(38.96315882d, "[M-K]-");
	
	private double difference;
	private String label;
	
	private Adducts(double difference, String label) {
		this.difference = difference;
		this.label = label;
	}
	
	public double getDifference() {
		return difference;
	}
	
	public String getLabel() {
		return label;
	}
	
}
