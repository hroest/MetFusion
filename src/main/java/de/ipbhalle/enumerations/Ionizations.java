/**
 * created by Michael Gerlich, Oct 4, 2011 - 3:03:35 PM
 */ 

package de.ipbhalle.enumerations;

public enum Ionizations {
	pos(1), 	// positive mode
	neg(-1), 	// negative mode
	both(0);	// both modes
	
	private int value;
	
	private Ionizations(int value) {
		this.value = value;
	};	
		
	public int getValue() {
		return value;
	}
}
