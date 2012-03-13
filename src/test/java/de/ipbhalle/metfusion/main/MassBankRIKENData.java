/**
 * created by Michael Gerlich on Jun 7, 2010
 * last modified Jun 7, 2010 - 10:20:10 AM
 * email: mgerlich@ipb-halle.de
 */
package de.ipbhalle.metfusion.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.output.LockableFileWriter;
import org.apache.commons.math.linear.RealMatrix;

import de.ipbhalle.MassBank.MassBankLookupBean;
import de.ipbhalle.metfusion.utilities.MassBank.MassBankUtilities;
import de.ipbhalle.metfusion.web.controller.MetFragBean;
import de.ipbhalle.metfusion.wrapper.ColorcodedMatrix;
import de.ipbhalle.metfusion.wrapper.Result;
import de.ipbhalle.metfusion.wrapper.ResultExt;
import de.ipbhalle.metfusion.integration.MatrixUtils;
import de.ipbhalle.metfusion.integration.Similarity.SimilarityMetFusion;
import de.ipbhalle.metfusion.integration.Tanimoto.TanimotoIntegration;
import de.ipbhalle.metfusion.integration.Tanimoto.TanimotoIntegrationWeighted;
import de.ipbhalle.metfusion.integration.Tanimoto.TanimotoSimilarity;

import massbank.MassBankCommon;

public class MassBankRIKENData {

	String oldResultsPath = "/home/mgerlich/workspace-3.5/MassBankComparison/MassBankQueries";
	
	static String serverUrl = "http://www.massbank.jp/";
	
	Date current;
	
	/** The Constant CHEMSPIDER. */
	public static final String CHEMSPIDER = "CHEMSPIDER";
	
	/** The Constant KEGG. */
	public static final String KEGG = "KEGG compound";
	
	/** The Constant PubChemC. */
	public static final String PubChemC = "pccompound";
	
	public static String[] selectedInstruments;
	
	public static String selectedIon = "0";
	/**
	 * @param args
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws InterruptedException, IOException {
		if(args == null || args.length < 2) {
			System.out.println("Program requires path/to/spectra/ and path/to/output/");
			System.exit(-1);
		}
		
		File inputFile = new File(args[0]);
		File outputDir = new File(args[1]);
		
		if(inputFile.isDirectory() || !outputDir.isDirectory()) {
			System.out.println("Program requires path/to/spectra_file and path/to/output/.");
			System.exit(-1);
		}

		/**
		 * read statistics file
		 */
		//File statistics = new File("/home/mgerlich/workspace-3.5/MassBankComparison/MetFragResults/2009-08-04_15-43-23_histRealScoring_BioCpd_NoCH_PAPER_TD2_86AVG.csv");
		File statistics = new File("/home/mgerlich/workspace-3.5/MetFusion2/testdata/MFstatistic.csv");
		

		BufferedReader br = new BufferedReader(new FileReader(statistics));
		String line = "";
		line = br.readLine();	// skip header
		//  CID      worstRank
		Map<String, String> statMap = new HashMap<String, String>();
		while((line = br.readLine()) != null) {
			String[] split = line.split("\t");
//			String name = split[0];
//			name = name.replaceAll("-", "_");
//			name = name.replaceAll(" ", "_");
			//String key = split[0].split("_10")[0];
			String key = split[0];
			key = key.replaceAll(" ", "_");
			key = key.replaceAll("-", "_");
			statMap.put(key, line);	// mergeName	CID		worstRank	mass	time
		}
		/**
		 * 
		 */
		
		MetFragBean mfb = new MetFragBean();
		MassBankLookupBean mblb = new MassBankLookupBean(serverUrl);
		selectedInstruments = mblb.getSelectedInstruments();
		
		//String typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_SEARCH];
        StringBuilder sb = new StringBuilder();
        sb = sb.append("&INST=");
//        if(selectedInstruments != null && selectedInstruments.length > 0) {	// use chosen instruments
//        	for (int i = 0; i < selectedInstruments.length; i++) {
//    			sb = sb.append(selectedInstruments[i]).append(",");
//    		}
//        }
        
        /**
         * only use (LC)-ESI-Tandem-MS instruments
         */
//        sb = sb.append("ESI-IT-(MS)n,ESI-IT-MS/MS,ESI-QTOF-MS/MS,ESI-QqIT-MS/MS,ESI-QqQ-MS/MS,ESI-QqTOF-MS/MS,LC-ESI-IT-MS/MS,LC-ESI-QTOF-MS/MS,LC-ESI-QqQ-MS/MS");
//        String[] sI = new String[] {"ESI-IT-(MS)n", "ESI-IT-MS/MS", "ESI-QTOF-MS/MS", "ESI-QqIT-MS/MS",
//        		"ESI-QqQ-MS/MS", "ESI-QqTOF-MS/MS", "LC-ESI-IT-MS/MS", "LC-ESI-QTOF-MS/MS", "LC-ESI-QqQ-MS/MS"};
        sb = sb.append("CE-ESI-TOF-MS,ESI-IT-(MS)n,ESI-IT-MS/MS,ESI-QTOF-MS/MS,ESI-QqIT-MS/MS,ESI-QqQ-MS/MS,ESI-QqTOF-MS/MS," +
        		"LC-ESI-IT-MS/MS,LC-ESI-QTOF-MS/MS,LC-ESI-QqQ-MS/MS,LC-ESI-FT-MS,LC-ESI-IT-TOF-MS,LC-ESI-Q-MS,LC-ESI-QqQ-MS," +
        		"LC-ESI-QqTOF-MS/MS,LC-ESI-TOF-MS");
        String[] sI = new String[] {"CE-ESI-TOF-MS", "ESI-IT-(MS)n", "ESI-IT-MS/MS", "ESI-QTOF-MS/MS", "ESI-QqIT-MS/MS",
        		"ESI-QqQ-MS/MS", "ESI-QqTOF-MS/MS", "LC-ESI-IT-MS/MS", "LC-ESI-QTOF-MS/MS", "LC-ESI-QqQ-MS/MS", "LC-ESI-FT-MS",
        		"LC-ESI-IT-TOF-MS", "LC-ESI-Q-MS", "LC-ESI-QqQ-MS", "LC-ESI-QqTOF-MS/MS", "LC-ESI-TOF-MS"};
        
        String inst = sb.toString();
        if(inst.endsWith(","))		// remove trailing comma
        	inst = inst.substring(0, inst.length() - 1);
        
        // set ionization mode to positive if none selected
//        if(selectedIon == null || selectedIon.isEmpty() || selectedIon.length() == 0)
//        	selectedIon = "0";
		selectedIon = "1";
		
        /**
         * build up parameter string for MassBank search
         */
        String ions = "&ION=" + selectedIon;
        inst += ions;
        
		/**
		 * 
		 */
		
        // start both threads in parallel
        ExecutorService threadExecutor = null;
        
		//File[] list = inputDir.listFiles(new MyFileFilter(".txt"));
		//System.out.println("Hill files #txt -> " + list.length);
		//Arrays.sort(list);
		//for (File f : list) {
        String fileName = inputFile.getName();
		System.out.println(fileName);
		//String current = fileName.substring(0, fileName.indexOf("."));
		String current = fileName.substring(0, fileName.indexOf("_10"));
		System.out.println(current);
		String cid = "", worstRank = "";
		if(statMap.containsKey(current)) {
			System.out.println("statMap contains key!");
			String stat = statMap.get(current);
			String[] split = stat.split("\t");
			cid = split[1];
			worstRank = split[2];
			System.out.println("CID: " + cid + "\tworstRank: " + worstRank);
		}
		else {
			System.err.println("Found no matching entry for " + current);
			System.exit(-1);
		}
		
		String[] info = getPeaklistFromFile(inputFile);
		
		String mbPeaks = formatPeaksForMassBank(info[0]);
		mfb.setInputSpectrum(info[0]);
		mfb.setExactMass(Double.parseDouble(info[1]));
		// let MetFrag search in PubChem
		mfb.setSelectedDB("pubchem");
		String cname = info[2];
		
		mblb.setInputSpectrum(info[0]);
        mblb.setSelectedIon(selectedIon);
        mblb.setSelectedInstruments(sI);

        String param = "quick=true&CEILING=1000&WEIGHT=SQUARE&NORM=SQRT&START=1&TOLUNIT=unit"
				+ "&CORTYPE=COSINE&FLOOR=0&NUMTHRESHOLD=3&CORTHRESHOLD=0.8&TOLERANCE=0.3"
				+ "&CUTOFF=5" + "&NUM=0&VAL=" + mbPeaks;
		param += inst;
		
		
		/**
		 * threading
		 */
		threadExecutor = Executors.newFixedThreadPool(2);
        threadExecutor.execute(mblb);
        threadExecutor.execute(mfb);
        threadExecutor.shutdown();
        
        do {
        	Thread.sleep(5000);
        }while(!threadExecutor.isTerminated());
		
//	        MassBankCommon mbcommon = new MassBankCommon();
//			// retrieve result list
		//ArrayList<String> result = mbcommon.execMultiDispatcher(serverUrl, typeName, param);
//			Map<String, String> molData = getMolFile(result, serverUrl);
		List<String> result = mblb.getOriginalResults();
		
		if(result == null || result.size() == 0) {
			System.err.println("Empty MassBank result set for " + current);
			System.err.println("No log file, matrix or vectors written for " + current);
			System.exit(-1);
		}
			
		File log = new File(outputDir, cname + ".log");
		System.out.println("START writing log file -> " + log);
		FileWriter fw = new FileWriter(log);
		fw.write("## MassBank\n");
		for (int i = 0; i < result.size(); i++) {
			String rec = (String) result.get(i);
			fw.write(rec);
			fw.write("\n");
		}
		
		fw.write("\n");
		fw.write("## MetFrag\n");
		List<Result> mfResults = mfb.getResults();
		
		if(mfResults == null || mfResults.size() == 0) {
			fw.flush();
			fw.close();
			System.err.println("Empty MetFrag result set for " + current);
			System.exit(-1);
		}
		
		for (int i = 0; i < mfResults.size(); i++) {
			Result r = mfResults.get(i);
			fw.write(r.getName() + "\t" + r.getId() + "\t" + r.getScore() + "\n");
			
			File mol = new File("/home/mgerlich/workspace-3.5/MetFusion2/testdata/Hill/mol/", r.getId() + ".mol");
			boolean success = MassBankUtilities.writeContainer(mol, r.getMol());
			System.out.println("writing mol for " + mol + " successful ? " + success);
		}
		
		fw.flush();
		fw.close();
		
		System.out.println("\n log file written -> " + log);
	//}
	
		// create tanimoto matrix and perform chemical-similarity based integration
		List<Result> listMassBank = mblb.getResults();
		List<Result> listMetFrag = mfb.getResults();
		
		TanimotoSimilarity sim = new TanimotoSimilarity(listMassBank, listMetFrag, 3, 0.5f);
		
		TanimotoIntegration integration = new TanimotoIntegration(sim);
		//setNewOrder(integration.computeNewOrdering());
		//integration.setCid(cid);
		
		TanimotoIntegrationWeighted tiw = new TanimotoIntegrationWeighted(sim);
		//tiw.setCid(cid);
		//TanimotoIntegration integration = new TanimotoIntegration(sim);
		List<ResultExt> newOrder = integration.computeNewOrdering();
		
		//int[] second = integration.weightedApproach();
		//List<ResultExt> secondOrder = integration.computeNewOrderingFromIndices(second);
		List<ResultExt> secondOrder = tiw.computeNewOrdering();
		
		
		/**
		 * write new integration result lists
		 */
		File newRankIntWeight = new File(outputDir, cname + "_integration_weight.txt");
		File newRankIntThresh = new File(outputDir, cname + "_integration_thresh.txt");
		integration.writeResult(newRankIntThresh);
		tiw.writeResult(newRankIntWeight);
		/**
		 * 
		 */
		
		/**
		 * MetFrag cluster ranks
		 */
		File newRankWeight = new File(outputDir, cname + "_rank_weight.txt");
		File newRankThresh = new File(outputDir, cname + "_rank_thresh.txt");
		// List<ResultExt> clusterThresh = 
		SimilarityMetFusion.computeScores(newOrder, newRankThresh);
		// List<ResultExt> clusterWeight = 
		SimilarityMetFusion.computeScores(secondOrder, newRankWeight);
		/**
		 * 
		 */
		
		RealMatrix rm = sim.getMatrix();
		//ColorcodedMatrix ccm = new ColorcodedMatrix(rm, listMassBank, listMetFrag);
		
		// write tanimoto matrix
		sim.writeMatrix(rm, new File(outputDir, cname + ".mat"));		// changed from current to cname

		/**
		 * TODO: rank bevor/danach
		 *    tiedRank
		 *    mean, median, sd
		 *    rangänderung
		 */
		String wRank = "", tRank = "";
		double[] wScores = new double[secondOrder.size()];
		double[] tScores = new double[newOrder.size()];
		double scoreW = 0, scoreT = 0;
		int idxw = 0, idxt = 0;
		// assume that both result lists have the same size
		for (int i = 0; i < newOrder.size(); i++) {
			// threshold data
			if(newOrder.get(i) instanceof ResultExt) {
				ResultExt temp = (ResultExt) newOrder.get(i);
				tScores[i] = temp.getResultScore();
			}
			
//			if(newOrder.get(i).getId().equals(cid)) {
//				tRank = String.valueOf(i+1);
//				idxt = i+1;
//				if(newOrder.get(i) instanceof ResultExt) {
//					ResultExt temp = (ResultExt) newOrder.get(i);
//					scoreT = temp.getResultScore();
//				}
//			}
				
			// weighted data
			if(secondOrder.get(i) instanceof ResultExt) {
				ResultExt temp = (ResultExt) secondOrder.get(i);
				wScores[i] = temp.getResultScore();
			}
			
//			if(secondOrder.get(i).getId().equals(cid)) {
//				wRank = String.valueOf(i+1);
//				idxw = i+1;
//				if(newOrder.get(i) instanceof ResultExt) {
//					ResultExt temp = (ResultExt) secondOrder.get(i);
//					scoreW = temp.getResultScore();
//				}
//			}
		}
		
		File scoresT = new File(outputDir, current + "_threshScores.vec");
		sim.writeVector(tScores, scoresT);
		
		File scoresW = new File(outputDir, current + "_weightScores.vec");
		sim.writeVector(wScores, scoresW);
		
//		int rankw = MatrixUtils.getTiedRank(wScores, scoreW);
//		int rankt = MatrixUtils.getTiedRank(tScores, scoreT);

//		File resultsLog = new File(outputDir, cname + "_result.log");
//		fw = new FileWriter(resultsLog);
//		
//		String header = "## CID\tworstRank\tthresholdRank\tweightedRank\tthresholdTiedRank\tweightedTiedRank\n";
//		fw.write(header);
//		fw.write(cid + "\t" + worstRank + "\t" + idxt + "\t" + idxw + "\t" + rankt + "\t" + rankw);
//		fw.flush();
//		fw.close();
		
	}
	
	private static String formatPeaksForMassBank(String peaks) {
		peaks = peaks.replaceAll(" ", ",");
		peaks = peaks.replaceAll("\n", "@");
		return peaks;
	}
	
	private static String[] getPeaklistFromFile(File f) {
		StringBuilder sb = new StringBuilder();
		String[] data = new String[3];
		String mass = "";
		String compound = "";
		try {
			BufferedReader br = new BufferedReader(new FileReader(f));
			String line = "";
			int counter = 0;
			while((line = br.readLine()) != null) {
				/**
				 * RIKEN spectra
				 */
//				if(line.startsWith("ACCESSION:"))
//					compound = line.substring(line.indexOf(":") + 1).trim();
				
				/**
				 * Hill spectra
				 */
				if(line.startsWith("CH$NAME:")) {
					compound = line.substring(line.indexOf(":") + 1).trim();
				}
				
				if(line.startsWith("CH$EXACT_MASS:")) {
					mass = line.substring(line.indexOf(":") + 1).trim();
				}
				
				if(line.startsWith("PK$NUM_PEAK:")) {
					counter = Integer.parseInt(line.substring(line.indexOf(":") + 1).trim());
					// skip peak header line
					br.readLine();
					
					for (int i = 0; i < counter; i++) {
						String temp = br.readLine().trim();
						String[] split = temp.split(" ");
						if(split.length == 3 && i < (counter-1)) {
							sb.append(split[0]).append(" ").append(split[2]).append("\n");
						}
						else sb.append(split[0]).append(" ").append(split[2]);
					}
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String result = sb.toString();
		if(result.endsWith("\n")) {
			result = result.substring(0, result.length() - 1);
		}
		
		data[0] = result;
		data[1] = mass;
		data[2] = compound;
		
		return data;
	}
	
	private static Map<String,String> getMolFile(ArrayList list, String serverUrl) {
		String prevName = "";
		String param = "";
		for ( int i = 0; i < list.size(); i++ ) {
			String rec = (String)list.get(i);
			String[] fields = rec.split(";");
			String name    = fields[0];  
			if ( !name.equals(prevName) ) {
				String ename = "";
				try {
					ename = URLEncoder.encode( name, "utf-8" );
				}
				catch ( UnsupportedEncodingException e ) {
					e.printStackTrace();
				}
				param += ename + "@";
			}
			prevName = name;
		}
		if ( !param.equals("") ) {
			param = param.substring(0, param.length()-1);
			param = "&names=" + param;
		}
		MassBankCommon mbcommon = new MassBankCommon();
		String typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_GETMOL];
		ArrayList result = mbcommon.execMultiDispatcher( serverUrl, typeName, param );

		Map<String, String> map = new HashMap();
		boolean isStart = false;
		int cnt = 0;
		String key = "";
		String moldata = "";
		for ( int i = 0; i < result.size(); i++ ) {
			String temp = (String)result.get(i);
			String[] item = temp.split("\t");
			String line = item[0];
			if ( line.indexOf("---NAME:") >= 0 ) {
				if ( !key.equals("") && !map.containsKey(key) && !moldata.trim().equals("") ) {
					// Molfileデータ格納
					map.put(key, moldata);
				}
				// 次のデータのキー名
				key = line.substring(8).toLowerCase();
				moldata = "";
			}
			else {
				// JME Editor 
				if ( line.indexOf("M  CHG") >= 0 ) {
					continue;
				}
				/**
				 * modified to work with CDK instread of JME editor
				 */
				//moldata += line + "|\n";
				moldata += line + "\n";
			}
		}
		if ( !map.containsKey(key) && !moldata.trim().equals("") ) {
			map.put(key, moldata);
		}
		return map;
	}
	
	public static void writeToFile(String file, String content, String dir)
	 {
        try
        {
                LockableFileWriter lfw = new LockableFileWriter(file, true, dir);
                lfw.write(content);
                lfw.flush();
                lfw.close();
        }
        catch (Exception e) {                        
                try {
					Thread.sleep(10);
					writeToFile(file, content, dir);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
        }
    }
	
	/**
	 * inner class which provides implementation of FilenameFilter interface
	 * 
	 * @author mgerlich
	 *
	 */
	private static class MyFileFilter implements FilenameFilter {
		
		private String suffix = "";
		private String prefix = "";
		
		public MyFileFilter() {
			suffix = ".txt";
		}
		
		public MyFileFilter(String suffix) {
			this.suffix = (suffix.isEmpty() ? ".txt" : suffix);
		}

		public MyFileFilter(String prefix, String suffix) {
			this(suffix);
			this.prefix = prefix;
		}
		
		@Override
		public boolean accept(File dir, String name) {
			if(suffix.isEmpty() && prefix.isEmpty())
				return false;
			else if(!suffix.isEmpty() && name.endsWith(suffix))
				return true;
			else if(!suffix.isEmpty() && !prefix.isEmpty() && 
					name.startsWith(prefix) && name.endsWith(suffix))
				return true;
			else return false;
		}
	}

}
