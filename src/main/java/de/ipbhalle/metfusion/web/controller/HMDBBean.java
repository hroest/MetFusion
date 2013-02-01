/**
 * created by Michael Gerlich, Jan 29, 2013 - 1:34:44 PM
 */ 

package de.ipbhalle.metfusion.web.controller;

import java.util.List;

import de.ipbhalle.metfusion.wrapper.Result;

public class HMDBBean implements GenericDatabaseBean {

	private String databaseName = "HMDB";
	private String sessionPath;
	private String serverUrl;
	private List<Result> results;
	private List<Result> unused;
	private boolean done;
	private boolean showResult;
	private boolean showNote;
	private boolean uniqueInchi;
	private int searchProgress;
	
	public HMDBBean() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

	public static void main(String[] args) {
		String MS = "http://www.hmdb.ca/spectra/ms/search?" +
				"utf8=%E2%9C%93" +
				"&query_masses=175%0D%0A209.98%0D%0A2011.971" +
				"&tolerance=0.05" +
				"&mode=positive" +		// negative or neutral
				"&commit=Search";
		
		String MSMS = "http://www.hmdb.ca/spectra/ms_ms/search?" +
				"utf8=%E2%9C%93" +
				"&parent_ion_mass=146.0" +
				"&parent_ion_mass_tolerance=0.1" +
				"&ionization_mode=Positive" +	// Negative or "" if NA
				"&collision_energy_level=Low" +		// Medium or High or "" if All
				"&peaks=40.948+0.174%0D%0A56.022+0.424%0D%0A84.37+53.488%0D%0A101.50+8.285%0D%0A102.401+0.775%0D%0A129.670+100.000%0D%0A146.966+20.070" +
				"&mass_charge_tolerance=0.5" +
				"&commit=Search";
		
		String GCMS = "http://www.hmdb.ca/spectra/c_ms/search?" +
				"utf8=%E2%9C%93" +
				"&retention_type=retention_index" +		// oder retention_time
				"&retention=1072" +
				"&retention_tolerance=1" +
				"&peaks=73%0D%0A147%0D%0A117%0D%0A190%0D%0A191%0D%0A148%0D%0A66%0D%0A75" +
				"&mass_charge_tolerence=" +
				"&commit=Search";
		
		String NMR1D = "http://www.hmdb.ca/spectra/nmr_one_d/search?" +
				"utf8=%E2%9C%93" +
				"&nucleus=1H" +		// 13C
				"&peaks=3.81%0D%0A3.82%0D%0A3.83%0D%0A3.85%0D%0A3.89%0D%0A3.90%0D%0A3.91%0D%0A4.25%0D%0A4.26%0D%0A4.27%0D%0A4.41%0D%0A8.19%0D%0A8.31" +
				"&cs_tolerance=0.02" +
				"&commit=Search";
		
		String NMR2D = "http://www.hmdb.ca/spectra/nmr_two_d/search?" +
				"utf8=%E2%9C%93" +
				"&library=tocsy" +	// hsqc
				"&peaks=3.76+2.126%0D%0A3.76+2.446%0D%0A3.76+3.76%0D%0A2.446+2.126%0D%0A2.446+2.446%0D%0A2.446+3.76%0D%0A2.126+2.126%0D%0A2.126+2.446%0D%0A2.126+3.76" +
				"&x_tolerance=0.02" +
				"&y_tolerance=0.02" +
				"&commit=Search";
		
		String utf8 = "✓";	// %E2%9C%93
		System.out.printf("http://www.hmdb.ca/spectra/nmr_two_d/search?" +
				"utf8=" + utf8 + 
				"&library=%s" +	// hsqc
				"&peaks=%s" +
				"&x_tolerance=%f" +
				"&y_tolerance=$f" +
				"&commit=Search", "tocsy", "3.76+2.126%0D%0A3.76+2.446%0D%0A3.76+3.76%0D%0A2.446+2.126%0D%0A2.446+2.446%0D%0A2.446+3.76%0D%0A2.126+2.126%0D%0A2.126+2.446%0D%0A2.126+3.76",
								0.02, 0.02);
	}
	
	@Override
	public String getDatabaseName() {
		return databaseName;
	}

	@Override
	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	@Override
	public String getSessionPath() {
		return sessionPath;
	}

	@Override
	public void setSessionPath(String sessionPath) {
		this.sessionPath = sessionPath;
	}

	@Override
	public String getServerUrl() {
		return serverUrl;
	}

	@Override
	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	@Override
	public List<Result> getResults() {
		return results;
	}

	@Override
	public void setResults(List<Result> results) {
		this.results = results;
	}

	@Override
	public List<Result> getUnused() {
		return unused;
	}

	@Override
	public void setUnused(List<Result> unused) {
		this.unused = unused;
	}

	@Override
	public boolean isDone() {
		return done;
	}

	@Override
	public void setDone(boolean done) {
		this.done = done;
	}

	@Override
	public boolean isShowResult() {
		return showResult;
	}

	@Override
	public void setShowResult(boolean showResult) {
		this.showResult = showResult;
	}

	@Override
	public boolean isShowNote() {
		return showNote;
	}

	@Override
	public void setShowNote(boolean showNote) {
		this.showNote = showNote;
	}

	@Override
	public boolean isUniqueInchi() {
		return uniqueInchi;
	}

	@Override
	public void setUniqueInchi(boolean uniqueInchi) {
		this.uniqueInchi = uniqueInchi;
	}

	@Override
	public int getSearchProgress() {
		return searchProgress;
	}

	@Override
	public void setSearchProgress(int searchProgress) {
		this.searchProgress = searchProgress;
	}


}
