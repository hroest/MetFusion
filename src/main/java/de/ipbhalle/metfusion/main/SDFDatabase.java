/**
 * created by Michael Gerlich, Jan 28, 2013 - 11:14:23 AM
 */ 

package de.ipbhalle.metfusion.main;

import java.util.List;

import de.ipbhalle.metfrag.main.MetFrag;
import de.ipbhalle.metfusion.web.controller.GenericDatabaseBean;
import de.ipbhalle.metfusion.wrapper.Result;

public class SDFDatabase implements GenericDatabaseBean {

	private String databaseName = "sdf";
	private String sessionPath;
	private String serverUrl;
	private List<Result> results;
	private List<Result> unused;
	private boolean done;
	private boolean showResult;
	private boolean showNote;
	private boolean uniqueInchi;
	private int searchProgress;
	
	public SDFDatabase() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		//MetFrag.startConvenienceSDF(null, done, searchProgress, searchProgress, searchProgress, done, done, searchProgress, done, done, done, done, searchProgress, done, databaseName);
	}

	@Override
	public void setDatabaseName(String name) {
		this.databaseName = name;
	}

	@Override
	public String getDatabaseName() {
		return databaseName;
	}

	@Override
	public void setResults(List<Result> results) {
		this.results = results;
	}

	@Override
	public List<Result> getResults() {
		return results;
	}

	@Override
	public void setUnused(List<Result> unused) {
		this.unused = unused;
	}

	@Override
	public List<Result> getUnused() {
		return unused;
	}

	@Override
	public void setDone(boolean done) {
		this.done = done;
	}

	@Override
	public boolean isDone() {
		return done;
	}

	@Override
	public void setShowResult(boolean showResult) {
		this.showResult = showResult;
	}

	@Override
	public boolean isShowResult() {
		return showResult;
	}

	@Override
	public void setSessionPath(String sessionPath) {
		this.sessionPath = sessionPath;
	}

	@Override
	public String getSessionPath() {
		return sessionPath;
	}

	@Override
	public void setSearchProgress(int searchProgress) {
		this.searchProgress = searchProgress;
	}

	@Override
	public int getSearchProgress() {
		return searchProgress;
	}

	@Override
	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	@Override
	public String getServerUrl() {
		return serverUrl;
	}

	@Override
	public void setShowNote(boolean showNote) {
		this.showNote = showNote;
	}

	@Override
	public boolean isShowNote() {
		return showNote;
	}

	@Override
	public void setUniqueInchi(boolean uniqueInchi) {
		this.uniqueInchi = uniqueInchi;
	}

	@Override
	public boolean isUniqueInchi() {
		return uniqueInchi;
	}

}
