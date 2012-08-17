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
	MplusMethanolplus(-33.033489d, "[M+CH3OH+H]+"),
	MplusClplus(-34.969402d, "[M+Cl]+"),
	MplusFAplus(-44.998201d, "[M+FA-H]+"),
	Mplus2Naplus(-44.971160d, "[M+2Na-H]+"),
	MplusIsoPropplus(-61.06534d, "[M+IsoProp+H]+"),
	MplusACNplus(-42.033823d, "[M+ACN+H]+"),
	
	Mminus(-0.00054858d, "M-"),
	MminusHminus(1.007276455d, "[M-H]-"),
	MplusClminus(-34.969402d, "[M+Cl]-"),
	MplusNaminus(-20.974666d, "[M+Na-2H]-"),
	MplusKminus(-36.948606d, "[M+K-2H]-"),
	MplusFAminus(-44.998201d, "[M+FA-H]-"),
	MminusWater(19.01839d, "[M-H2O-H]-"),
	MplusHCOOminus(-45.0174, "[M+HCOO]-");
	
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
