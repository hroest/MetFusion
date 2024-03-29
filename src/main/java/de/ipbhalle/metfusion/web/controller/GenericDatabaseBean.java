/**
 * created by Michael Gerlich, Aug 23, 2012 - 10:34:22 AM
 */ 

package de.ipbhalle.metfusion.web.controller;

import java.util.List;

import de.ipbhalle.metfusion.wrapper.Result;

public interface GenericDatabaseBean extends Runnable {

	public void setDatabaseName(String name);
	public String getDatabaseName();
	
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
	
	public void setServerUrl(String serverUrl);
	public String getServerUrl();
	
	public void setShowNote(boolean showNote);
	public boolean isShowNote();
	
	public void setUniqueInchi(boolean uniqueInchi);
	public boolean isUniqueInchi();
}
