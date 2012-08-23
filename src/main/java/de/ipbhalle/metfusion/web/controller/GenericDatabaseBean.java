/**
 * created by Michael Gerlich, Aug 23, 2012 - 10:34:22 AM
 */ 

package de.ipbhalle.metfusion.web.controller;

import java.util.List;

import de.ipbhalle.metfusion.wrapper.Result;

public interface GenericDatabaseBean extends Runnable {

//	boolean done = false;
//	boolean showResult = false;
//	String sessionPath = "";
//	int searchProgress = 0;
		
	public void setResults(List<Result> results);
	public List<Result> getResults();
	
	public void setUnused(List<Result> unused);
	public List<Result> getUnused();
	
	public void setDone(boolean done);
	public boolean isDone();

	public void setShowResult(boolean showResult);
	public boolean isShowResult();
	
	public void setSessionPath(String sessionPath);
	public String getSessionPath();
	
	public void setSearchProgress(int searchProgress);
	public int getSearchProgress();
}
