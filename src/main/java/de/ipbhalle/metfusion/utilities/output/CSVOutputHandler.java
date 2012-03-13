/**
 * created by Michael Gerlich, Feb 24, 2012 - 2:30:05 PM
 */ 

package de.ipbhalle.metfusion.utilities.output;

import java.util.List;

import de.ipbhalle.metfusion.web.controller.ResultExtGroupBean;
import de.ipbhalle.metfusion.wrapper.Result;
import de.ipbhalle.metfusion.wrapper.ResultExt;

public class CSVOutputHandler implements IOutputHandler {

	private String filename;
	private boolean append;
	
	private final String DEFAULT_ENDING = ".csv";

	/**
	 * Default constructor, uses filename and does not append to this file.
	 * 
	 * @param filename - name/path of the file to write
	 */
	public CSVOutputHandler(String filename) {
		this.filename = filename.endsWith(DEFAULT_ENDING) ? filename : filename + DEFAULT_ENDING;
		this.append = Boolean.FALSE;
	}
	
	/**
	 * Default constructor, uses filename and optionally appends to this file.
	 * 
	 * @param filename - name/path of the file to write
	 * @param append - append the file (TRUE) or not (FALSE)
	 */
	public CSVOutputHandler(String filename, boolean append) {
		this.filename = filename.endsWith(DEFAULT_ENDING) ? filename : filename + DEFAULT_ENDING;
		this.append = append;
	}
	
	@Override
	public boolean writeRerankedResults(List<ResultExt> results) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean writeAllResults(List<Result> originalFragmenter,
			List<Result> originalDatabase, List<ResultExt> newlyRanked,
			List<ResultExtGroupBean> cluster) {
		// TODO Auto-generated method stub
		return false;
	}

}
