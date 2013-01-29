/**
 * created by Michael Gerlich, Oct 5, 2011 - 3:18:13 PM
 */ 

package de.ipbhalle.enumerations;

/** Enumeration that holds all available/allowed parameter keywords. */
public enum AvailableParameters {
	mbLimit, mbCutoff, mbIonization, mbInstruments,		// MassBank parameters
	mfDatabase, mfDatabaseIDs, mfFormula, mfLimit, mfParentIon, mfAdduct, mfExactMass, mfSearchPPM, mfMZabs, mfMZppm,	// MetFrag parameters 
	clustering, peaks, onlyCHNOPS,					// MetFusion parameters
	substrucPresent, substrucAbsent,		// substructure search includes/excludes
	spectralDB;								// name of spectral DB, MassBank, Metlin, GMD, SDF
}
