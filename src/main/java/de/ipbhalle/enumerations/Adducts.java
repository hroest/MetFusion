/**
 * created by Michael Gerlich, Oct 4, 2011 - 3:16:06 PM
 */ 

package de.ipbhalle.enumerations;


public enum Adducts {
	
	Neutral(0d, "Neutral"),
	
	Mplus(0.00054858d, "M+"),
	MplusHplus(-1.007276455d, "[M+H+]"),
	MplusNH4plus(-18.033823d, "[M+NH4]+"),
	MplusNaplus(-22.989218d, "[M+Na]+"),
	MplusKplus(-38.963158d, "[M+K]+"),
	//MplusMethanolplus(-33.033489d, "[M+CH3OH+H]+"),
	//MplusIsoPropplus(-61.06534d, "[M+IsoProp+H]+"),
	//MplusACNplus(-42.033823d, "[M+ACN+H]+"),
	
	Mminus(-0.00054858d, "M-"),
	MminusHminus(1.007276455d, "[M-H]-"),
	MplusClminus(-34.969402d, "[M+Cl]-"),
	//MplusNaminus(-20.974666d, "[M+Na-2H]-"),
	//MplusKminus(-36.948606d, "[M+K-2H]-"),
	MplusFAminus(-44.998201d, "[M+HCOO]-"),
	//MminusWater(19.01839d, "[M-H2O-H]-"),
	MplusCH3OOminus(-59.0133, "[M+CH3COO]-");
	
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
