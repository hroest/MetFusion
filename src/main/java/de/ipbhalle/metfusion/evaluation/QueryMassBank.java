package de.ipbhalle.metfusion.evaluation;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.ipbhalle.enumerations.Dataset;
import de.ipbhalle.enumerations.DatasetUsage;
import de.ipbhalle.enumerations.Instruments;
import de.ipbhalle.metfusion.wrapper.Result;

import massbank.GetConfig;
import massbank.GetInstInfo;
import massbank.MassBankCommon;

public class QueryMassBank implements Runnable{

	private final static String massbankJP = "http://www.massbank.jp/";
	private String serverUrl;
	private MassBankCommon mbCommon;
	private GetConfig config;
	private GetInstInfo instInfo;
	
	private int limit = 100;
	
	private String selectedIon = "1";
	
	private List<String> queryResults;
	private boolean showResult;
	private List<Result> results;
	private List<String> originalResults;
	
	private List<Result> unused;
	
	private Thread t;
	private static final String cacheMassBank = "/vol/massbank/Cache/";
	
	/**
	 * construct query for MassBank
	 */
	public QueryMassBank() {
		
	}
	
	public QueryMassBank(String serverUrl, Instruments inst, Dataset ds, DatasetUsage usage) {
		// set MassBank parameters
		this.serverUrl = (serverUrl.isEmpty() | !serverUrl.startsWith("http://") ? massbankJP : serverUrl);
		this.setMbCommon(new MassBankCommon());
		this.setConfig(new GetConfig(this.serverUrl));
		this.setInstInfo(new GetInstInfo(this.serverUrl));
		Map<String, List<String>> instGroup = instInfo.getTypeGroup();
        Iterator<String> it = instGroup.keySet().iterator();
        StringBuilder sb = new StringBuilder();
        
        // iterate over instrument groups
        while(it.hasNext()) {
        	String group = it.next();							// instrument group - currently one of EI, ESI and Others
        	List<String> items = instGroup.get(group);			// retrieve instruments from current instrument group
        	System.out.println(group);
        	for (String string : items) {
				System.out.print(string + " ");
			}
        	System.out.println();
        	
        	if(group.toUpperCase().equals(inst.toString().toUpperCase())) {		// same MassBank group as parameter -> EI, ESI
        		for (int i = 0; i < items.size(); i++) {
        			sb.append(items.get(i)).append(",");
        		}
        		System.out.println(group + " -> " + sb.toString());
        	}
        	else if(inst.equals(Instruments.ESIMS2) && group.toUpperCase().startsWith(Instruments.ESI.toString())) {		// ESIMS2 group
        		for (int i = 0; i < items.size(); i++) {
        			String instrument = items.get(i);
        			if(instrument.contains("MS/MS") || instrument.contains("(MS)n"))
        				sb.append(instrument).append(",");
        		}
        		System.out.println(group + " -> " + sb.toString());
    		}
    		else if(inst.equals(Instruments.OTHER) && group.toUpperCase().equals(Instruments.OTHER.toString().toUpperCase())) {		// Other group
    			for (int i = 0; i < items.size(); i++) {
        			sb.append(items.get(i)).append(",");
        		}
    			System.out.println(group + " -> " + sb.toString());
    		}
        	else {
        		System.out.println("Instrument group [" + inst.toString() + "] not defined in MassBank!");
        		// TODO
        	}
        }
        
        String temp = sb.toString();
        if(temp.endsWith(","))
        	temp = temp.substring(0, temp.length() - 1);
        
        setT(new Thread(this, "massbank"));
	}
	
	public void runQuery(boolean useData) {
		
	}
	
	private void queryHill(boolean useData) {
		
	}
	
	private void queryRiken(boolean useData) {
		
	}
	
	private void queryBoth(boolean useData) {
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		QueryMassBank qmb = new QueryMassBank(massbankJP, Instruments.ESIMS2, Dataset.HILL, DatasetUsage.no);
		System.out.println(qmb.getLimit());
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

	public void setMbCommon(MassBankCommon mbCommon) {
		this.mbCommon = mbCommon;
	}

	public MassBankCommon getMbCommon() {
		return mbCommon;
	}

	public void setConfig(GetConfig config) {
		this.config = config;
	}

	public GetConfig getConfig() {
		return config;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public int getLimit() {
		return limit;
	}

	public void setSelectedIon(String selectedIon) {
		this.selectedIon = selectedIon;
	}

	public String getSelectedIon() {
		return selectedIon;
	}

	public GetInstInfo getInstInfo() {
		return instInfo;
	}

	public void setInstInfo(GetInstInfo instInfo) {
		this.instInfo = instInfo;
	}

	public void setQueryResults(List<String> queryResults) {
		this.queryResults = queryResults;
	}

	public List<String> getQueryResults() {
		return queryResults;
	}

	public void setShowResult(boolean showResult) {
		this.showResult = showResult;
	}

	public boolean isShowResult() {
		return showResult;
	}

	public void setResults(List<Result> results) {
		this.results = results;
	}

	public List<Result> getResults() {
		return results;
	}

	public void setOriginalResults(List<String> originalResults) {
		this.originalResults = originalResults;
	}

	public List<String> getOriginalResults() {
		return originalResults;
	}

	public void setUnused(List<Result> unused) {
		this.unused = unused;
	}

	public List<Result> getUnused() {
		return unused;
	}

	public void setT(Thread t) {
		this.t = t;
	}

	public Thread getT() {
		return t;
	}

	public static String getCachemassbank() {
		return cacheMassBank;
	}
}
