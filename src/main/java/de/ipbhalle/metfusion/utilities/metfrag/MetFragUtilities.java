package de.ipbhalle.metfusion.utilities.metfrag;

import java.util.Vector;

import de.ipbhalle.metfrag.spectrum.PeakMolPair;

public class MetFragUtilities {

	/**
	 * Utility function to create a single String containing the m/z and intensity 
	 * of all matched peaks from the query spectrum.
	 * 
	 * @param matchedFragments - the vector containing the PeakMolPair of the matched fragments
	 * @return the String representation of matched peaks, separated by a single white-space
	 */
	public static String retrievePeaksExplained(Vector<PeakMolPair> matchedFragments) {
		String explained = "";
		if(matchedFragments.size() > 0) {
			for (PeakMolPair pair : matchedFragments) {
				explained += pair.getPeak().getMass() + " " + pair.getPeak().getIntensity() + " ";
			}
		}
		
		return explained.trim();
	}
}
