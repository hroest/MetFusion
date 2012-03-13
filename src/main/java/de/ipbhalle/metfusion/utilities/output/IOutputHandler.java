/**
 * created by Michael Gerlich, Feb 24, 2012 - 2:32:30 PM
 */ 

package de.ipbhalle.metfusion.utilities.output;

import java.util.List;

import de.ipbhalle.metfusion.web.controller.ResultExtGroupBean;
import de.ipbhalle.metfusion.wrapper.Result;
import de.ipbhalle.metfusion.wrapper.ResultExt;

public interface IOutputHandler {

	/** enumeration of allowed properties for output annotation */
	enum properties {origin, name, id, origscore, newscore, rank, smiles, peaksExplained};
	String filename = "";
	
	/** output only the newly ranked list of fragmenter candidates */
	public boolean writeRerankedResults(List<ResultExt> results);
	
	/** output all result lists, including original ones from both 
	 * fragmenter and database as well as newly ranked list from fragmenter candidates */
	public boolean writeAllResults(List<Result> originalFragmenter, List<Result> originalDatabase, 
			List<ResultExt> newlyRanked, List<ResultExtGroupBean> cluster);
	
}
