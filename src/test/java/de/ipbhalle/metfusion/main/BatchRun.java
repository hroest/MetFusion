/**
 * created by Michael Gerlich on Jun 7, 2010
 * last modified Jun 7, 2010 - 10:20:10 AM
 * email: mgerlich@ipb-halle.de
 */
package de.ipbhalle.metfusion.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.output.LockableFileWriter;
import org.apache.commons.math.linear.RealMatrix;

import de.ipbhalle.MassBank.MassBankLookupBean;
import de.ipbhalle.enumerations.Ionizations;
import de.ipbhalle.metfusion.utilities.MassBank.MassBankUtilities;
import de.ipbhalle.metfusion.web.controller.MetFragBean;
import de.ipbhalle.metfusion.wrapper.Result;
import de.ipbhalle.metfusion.wrapper.ResultExt;
import de.ipbhalle.metfusion.integration.Similarity.SimilarityMetFusion;
import de.ipbhalle.metfusion.integration.Tanimoto.TanimotoIntegrationWeighted;
import de.ipbhalle.metfusion.integration.Tanimoto.TanimotoSimilarity;
import de.ipbhalle.metfusion.main.MassBankBatchMode;
import de.ipbhalle.metfusion.main.MetFragBatchMode;


public class BatchRun {

	String oldResultsPath = "/home/mgerlich/workspace-3.5/MassBankComparison/MassBankQueries";
	
																		// für japanische MassBank Anfrage
	static String serverUrl = "http://www.massbank.jp/";
		//"http://massbankInternal.ipb-sub.ipb-halle.de/MassBank/"; 
		//"http://msbi.ipb-halle.de/MassBank/";		//"http://www.massbank.jp/";
	
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
		
		if(inputFile.isDirectory() || !outputDir.isDirectory() || !inputFile.exists()) {
			System.out.println("Program requires path/to/spectra_file and path/to/output/.");
			System.exit(-1);
		}

		/**
		 * read statistics file
		 */
		/**
		 * uralt Werte !!!
		 */
		//File statistics = new File("/home/mgerlich/workspace-3.5/MassBankComparison/MetFragResults/2009-08-04_15-43-23_histRealScoring_BioCpd_NoCH_PAPER_TD2_86AVG.csv");
		
		/**
		 * neuere Werte fuer HILL mit MetFrag Paper Algorithmus fuer 102 Hill-Mergespektren
		 */
		//old //File statistics = new File("/home/mgerlich/workspace-3.5/MetFusion2/testdata/MFstatistic.csv");
		//File statistics = new File("/home/mgerlich/Desktop/MetFrag_results/HILL/scoring_paper/2010-07-20_17-15-50_results.txt_mzabs0_ppm10_MICHA_ScoringPaper");
		//File statistics = new File("/home/mgerlich/evaluation/statistics/paper_Hill_sorted");

		/**
		 * Hill Werte mit MetFrag Paper Algorithmus fuer 510 Hill-Einzelspektren
		 */
		//File statistics = new File("/home/mgerlich/evaluation/statistics/paper_Hill_single_Spectra");
		
		/**
		 * MetFrag alte Formel auf RIKEN - 
		 */
		// old //File statistics = new File("/home/swolf/MassBankData/MetFragSunGrid/RikenDataMerged/CHONPS/useable/logs/2010-07-06_09-54-08_results.txt_ScoringFunctionPaper");
		//File statistics = new File("/home/swolf/MassBankData/MetFragSunGrid/RikenDataMerged/CHONPS/useable/logs/2010-07-07_13-25-50_results.txt_ScoringFunctionPaper_MICHA");
		//new File("/home/swolf/MassBankData/MetFragSunGrid/RikenDataMerged/CHONPS/useable/logs/2010-06-23_10-37-01_results.txt_alteFormel");
		//File statistics = new File("/home/mgerlich/evaluation/statistics/paper_Riken_merged_filtered");
		
		/**
		 * IPB QSTAR Single Spektren mit MetFrag Paper Formel
		 */
		//File statistics = new File("/home/mgerlich/evaluation/statistics/paper_QSTAR_single");
		
		/**
		 * IPB QSTAR Merged Spektren mit MetFrag Paper Formel
		 */
		//File statistics = new File("/home/mgerlich/evaluation/statistics/paper_QSTAR_merged");
		
		/**
		 * ALLE Spektren mit MetFrag Paper Formel
		 */
		File statistics = new File("/home/mgerlich/evaluation/statistics/eval_allSpectra.txt");
		
		if(!statistics.exists()) {
			System.err.println("Statistics file does not exist! -> " + statistics.getAbsolutePath() + "\naborting...");
			System.exit(-1);
		}
		
		BufferedReader br = new BufferedReader(new FileReader(statistics));
		String line = "";
		//line = br.readLine();	// skip header
		//  CID      worstRank
		Map<String, String> statMap = new HashMap<String, String>();
		while((line = br.readLine()) != null) {
			if(line.isEmpty() || line.length() < 10)
				continue;
			
			String[] split = line.split("\t");
//			String name = split[0];
//			name = name.replaceAll("-", "_");
//			name = name.replaceAll(" ", "_");
			/**
			 * modification for HILL data - 102 merged
			 */
			//String key = split[0].split("_10")[0];
			
			/**
			 * modification for HILL data - 510 single
			 */
			String key = split[0].substring(0, split[0].indexOf("."));

			/**
			 * modification for RIKEN data
			 */
			//String key = split[0].substring(0, split[0].indexOf("."));
			key = key.replaceAll(" ", "_");
			key = key.replaceAll("-", "_");
			statMap.put(key, line);	// mergeName	CID		worstRank	mass	time
		}
		/**
		 * 
		 */
		
		// instantiate new MetFragBean
		//MetFragBean mfb = new MetFragBean();
		MetFragBatchMode mfb = new MetFragBatchMode(outputDir.getAbsolutePath());
		
		// instantiate new MassBankLookupBean with designated MassBank serverUrl
		//MassBankLookupBean mblb = new MassBankLookupBean(serverUrl);
		MassBankBatchMode mblb = new MassBankBatchMode(outputDir.getAbsolutePath(), Ionizations.pos);
		//selectedInstruments = mblb.getSelectedInstruments();
		
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
        sb = sb.append("ESI-IT-(MS)n,ESI-IT-MS/MS,ESI-QTOF-MS/MS,ESI-QqIT-MS/MS,ESI-QqQ-MS/MS,ESI-QqTOF-MS/MS," +
        		"LC-ESI-IT-MS/MS,LC-ESI-QTOF-MS/MS,LC-ESI-QqQ-MS/MS");
        String[] sI = new String[] {"ESI-IT-(MS)n", "ESI-IT-MS/MS", "ESI-QTOF-MS/MS", "ESI-QqIT-MS/MS",
        		"ESI-QqQ-MS/MS", "ESI-QqTOF-MS/MS", "LC-ESI-IT-MS/MS", "LC-ESI-QTOF-MS/MS", "LC-ESI-QqQ-MS/MS"};
        
        /**
         * use all availables ESI instruments
         */
//        sb = sb.append("CE-ESI-TOF-MS,ESI-IT-(MS)n,ESI-IT-MS/MS,ESI-QTOF-MS/MS,ESI-QqIT-MS/MS,ESI-QqQ-MS/MS,ESI-QqTOF-MS/MS," +
//        		"LC-ESI-IT-MS/MS,LC-ESI-QTOF-MS/MS,LC-ESI-QqQ-MS/MS,LC-ESI-FT-MS,LC-ESI-IT-TOF-MS,LC-ESI-Q-MS,LC-ESI-QqQ-MS," +
//        		"LC-ESI-QqTOF-MS/MS,LC-ESI-TOF-MS");
//        String[] sI = new String[] {"CE-ESI-TOF-MS", "ESI-IT-(MS)n", "ESI-IT-MS/MS", "ESI-QTOF-MS/MS", "ESI-QqIT-MS/MS",
//        		"ESI-QqQ-MS/MS", "ESI-QqTOF-MS/MS", "LC-ESI-IT-MS/MS", "LC-ESI-QTOF-MS/MS", "LC-ESI-QqQ-MS/MS", "LC-ESI-FT-MS",
//        		"LC-ESI-IT-TOF-MS", "LC-ESI-Q-MS", "LC-ESI-QqQ-MS", "LC-ESI-QqTOF-MS/MS", "LC-ESI-TOF-MS"};
        
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
		/**
		 * modification for RIKEN data
		 */
		//String current = fileName.substring(0, fileName.indexOf("."));
		/**
		 * modification for HILL data - using 102 merged spectra
		 */
		//String current = fileName.substring(0, fileName.indexOf("_10"));
		/**
		 * modification for HILL data - using 510 single spectra
		 */
		String current = fileName;//.substring(0, fileName.indexOf("_"));
//		if(current.lastIndexOf("_") > 3)	// if name contains both _ and something like _10, be sure to cut the latter one out
//			current = fileName.substring(0, fileName.lastIndexOf("_"));
		
		// parse file for information
		// String[] info = getPeaklistFromFile(inputFile);
		String[] info = MassBankUtilities.getPeaklistFromFile(inputFile);
		
//		if(fileName.matches("[A-Z]{2}[0-9]{6}") || fileName.matches("[A-Z]{3}[0-9]{5}"));
		current = info[2];	// change from filename to compound name or ID
		// derzeitigen Record setzen für Ergebnisfilterung (richtige compounds rauswerfen -> konservativ)
		//mblb.setCurrentRecord(current);
		
		// RIKEN merged filtered MOD
		if(fileName.length() > 12 && fileName.contains("PR1"))	// merged compound name/ID
			current = fileName.substring(0, fileName.indexOf("."));	// current ID mismatches fileName, as filenames contains multiple IDs
			// current would match the last ID in filename, but the first ID or the complete String is needed!
		
		System.out.println("current file -> " + fileName + "\tcurrent ID/name -> " + current);
		String cid = "", worstRank = "";
		if(statMap.containsKey(current)) {
			System.out.println("statMap contains key!");
			String stat = statMap.get(current);
			String[] split = stat.split("\t");
			cid = split[1];
			worstRank = split[3];
			System.out.println("CID: " + cid + "\tworstRank: " + worstRank + "\tclusterRank: " + split[4]);
		}
		else {
			System.err.println("Found no matching entry for " + current);
			System.exit(-1);
		}
		
		//String mbPeaks = formatPeaksForMassBank(info[0]);
		String mbPeaks = MassBankUtilities.formatPeaksForMassBank(info[0]);
		mfb.setInputSpectrum(info[0]);
		mfb.setExactMass(Double.parseDouble(info[1]));
		// let MetFrag search in PubChem
		mfb.setSelectedDB("pubchem");
		//String cname = info[2];
		String cname = fileName.substring(0, fileName.indexOf("."));
		
		mblb.setInputSpectrum(info[0]);
        mblb.setSelectedIon(selectedIon);
        mblb.setSelectedInstruments(sb.toString());

        String param = "quick=true&CEILING=1000&WEIGHT=SQUARE&NORM=SQRT&START=1&TOLUNIT=unit"
				+ "&CORTYPE=COSINE&FLOOR=0&NUMTHRESHOLD=3&CORTHRESHOLD=0.8&TOLERANCE=0.3"
				+ "&CUTOFF=5" + "&NUM=0&VAL=" + mbPeaks;
		param += inst;
		
		
		Thread.sleep(10000);	// put thread to sleep to prevent hammering/DOS
		
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
		List<String> result = mblb.getQueryResults();
		
		// check if original MassBank result set or filtered result set is empty - stop if so
		if(result == null || result.size() == 0 || mblb.getQueryResults() == null || mblb.getQueryResults().size() == 0) {
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
		
		System.out.println("\n log file written -> " + log + "\n");
	//}
	
		// create tanimoto matrix and perform chemical-similarity based integration
		List<Result> listMassBank = mblb.getResults();
		List<Result> listMetFrag = mfb.getResults();
		
		TanimotoSimilarity sim = new TanimotoSimilarity(listMassBank, listMetFrag, 3, 0.5f);
		/**
		 * nur matrix und log file erstellen
		 */
		sim.writeMatrix(sim.getMatrix(), new File(outputDir, cname + ".mat"));	// changed from current to cname
//		System.out.println("Written similarity matrix - exiting!");
//		System.exit(0);
		
//		TanimotoIntegration integration = new TanimotoIntegration(sim);
		//setNewOrder(integration.computeNewOrdering());
		//integration.setCid(cid);
		
		TanimotoIntegrationWeighted tiw = new TanimotoIntegrationWeighted(sim);
		//tiw.setCid(cid);
		//TanimotoIntegration integration = new TanimotoIntegration(sim);
//		System.out.println("### Begin threshold approach ###");
//		List<ResultExt> newOrder = integration.computeNewOrdering();
//		System.out.println("### Finished threshold approach ###");
		
		//int[] second = integration.weightedApproach();
		//List<ResultExt> secondOrder = integration.computeNewOrderingFromIndices(second);
		System.out.println("### Begin weighted approach ###");
		List<ResultExt> secondOrder = tiw.computeNewOrdering();
		System.out.println("### Finished weighted approach ###");
		
		
		/**
		 * write new integration result lists
		 */
		File newRankIntWeight = new File(outputDir, cname + "_integration_weight.txt");
//		File newRankIntThresh = new File(outputDir, cname + "_integration_thresh.txt");
//		integration.writeResult(newRankIntThresh);
		tiw.writeResult(newRankIntWeight);
		/**
		 * 
		 */
		
		/**
		 * MetFrag cluster ranks
		 */
		File newRankWeight = new File(outputDir, cname + "_rank_weight.txt");
//		File newRankThresh = new File(outputDir, cname + "_rank_thresh.txt");
//		System.out.println("### Begin cluster ranking - threshold approach ###");
//		List<ResultExt> clusterThresh = SimilarityMetFusion.computeScores(newOrder, newRankThresh);
//		System.out.println("### Finished cluster ranking - threshold approach ###");
		System.out.println("### Begin cluster ranking - weighted approach ###");
		List<ResultExt> clusterWeight =	SimilarityMetFusion.computeScores(secondOrder, newRankWeight);
		System.out.println("### Finished cluster ranking - weighted approach ###");
		/**
		 * 
		 */
		
//		RealMatrix rm = sim.getMatrix();
		//ColorcodedMatrix ccm = new ColorcodedMatrix(rm, listMassBank, listMetFrag);
		
		// write tanimoto matrix
//		sim.writeMatrix(rm, new File(outputDir, cname + ".mat"));	// changed from current to cname

		/**
		 * TODO: rank bevor/danach
		 *    tiedRank
		 *    mean, median, sd
		 *    rangänderung
		 */
//		String wRank = "", tRank = "";
//		double[] wScores = new double[secondOrder.size()];
//		double[] tScores = new double[newOrder.size()];
//		double scoreW = 0, scoreT = 0;
//		int idxw = 0, idxt = 0;
//		// assume that both result lists have the same size
//		for (int i = 0; i < newOrder.size(); i++) {
//			// threshold data
//			if(newOrder.get(i) instanceof ResultExt) {
//				ResultExt temp = (ResultExt) newOrder.get(i);
//				tScores[i] = temp.getResultScore();
//			}
//			
////			if(newOrder.get(i).getId().equals(cid)) {
////				tRank = String.valueOf(i+1);
////				idxt = i+1;
////				if(newOrder.get(i) instanceof ResultExt) {
////					ResultExt temp = (ResultExt) newOrder.get(i);
////					scoreT = temp.getResultScore();
////				}
////			}
//				
//			// weighted data
//			if(secondOrder.get(i) instanceof ResultExt) {
//				ResultExt temp = (ResultExt) secondOrder.get(i);
//				wScores[i] = temp.getResultScore();
//			}
//			
////			if(secondOrder.get(i).getId().equals(cid)) {
////				wRank = String.valueOf(i+1);
////				idxw = i+1;
////				if(newOrder.get(i) instanceof ResultExt) {
////					ResultExt temp = (ResultExt) secondOrder.get(i);
////					scoreW = temp.getResultScore();
////				}
////			}
//		}
//		
//		File scoresT = new File(outputDir, cname + "_threshScores.vec");
//		sim.writeVector(tScores, scoresT);
//		
//		File scoresW = new File(outputDir, cname + "_weightScores.vec");
//		sim.writeVector(wScores, scoresW);
//		
		
		/**
		 * write out final statistics for tied and cluster ranks
		 * for both threshold and weighted approach
		 */
		String content = "";
		//File newStats = new File(outputDir, "HILL.stats");
		//File newStats = new File(outputDir, "RIKEN.stats");
		
//		File f = new File(outputDir, cname + "_thresh.list_tied");
//		try {
//			fw = new FileWriter(f);
//			for (int j = 0; j < newOrder.size(); j++) {
//				ResultExt r = newOrder.get(j);
//				fw.write(r.getId() + "\t" + r.getScore() + "\t" + r.getResultScore() + "\t" + r.getPosBefore() + "\t" + r.getPosAfter() + "\n");
//				
//				if(statMap.containsKey(cname) && statMap.get(cname).equals(r.getId())) {
//					//fws.write(cname + "\t" + r.getId() + "\t" +r.getPosAfter() + "\n");
//					//fws.write(cname + "\t" + r.getId() + "\t" + r.getPosAfter());
//					content = cname + "\t" + r.getId() + "\t" + r.getPosAfter();
//				}
//			}
//			fw.flush();
//			fw.close();
//		} catch (IOException e) {
//			System.err.println("Error writing file " + f.getAbsolutePath());
//			e.printStackTrace();
//		}
		
		File f = new File(outputDir, cname + "_weight.list_tied");
		try {
			fw = new FileWriter(f);
			for (int j = 0; j < secondOrder.size(); j++) {
				ResultExt r = secondOrder.get(j);
				fw.write(r.getId() + "\t" + r.getScore() + "\t" + r.getResultScore() + "\t" + r.getPosBefore() + "\t" + r.getPosAfter() + "\n");
				
				if(statMap.containsKey(cname) && statMap.get(cname).equals(r.getId())) {
					//fws.write(cname + "\t" + r.getId() + "\t" +r.getPosAfter() + "\n");
					//fws.write("\t" + r.getPosAfter());
					content += "\t" + r.getPosAfter();
				}
			}
			fw.flush();
			fw.close();
		} catch (IOException e) {
			System.err.println("Error writing file " + f.getAbsolutePath());
			e.printStackTrace();
		}
		
		// write cluster ranks to stats file
//		for (int j = 0; j < clusterThresh.size(); j++) {
//			ResultExt r = clusterThresh.get(j);
//			
//			if(statMap.containsKey(cname) && statMap.get(cname).equals(r.getId())) {
//				//fws.write(cname + "\t" + r.getId() + "\t" +r.getPosAfter() + "\n");
//				//fws.write("\t" + r.getClusterRank());
//				content += "\t" + r.getClusterRank();
//			}
//		}
		for (int j = 0; j < clusterWeight.size(); j++) {
			ResultExt r = clusterWeight.get(j);
			
			if(statMap.containsKey(cname) && statMap.get(cname).equals(r.getId())) {
				//fws.write(cname + "\t" + r.getId() + "\t" +r.getPosAfter() + "\n");
				//fws.write("\t" + r.getClusterRank() + "\n");
				content += "\t" + r.getClusterRank() + "\n";
			}
		}
		
		//writeToFile(newStats.getName(), content, outputDir.getAbsolutePath());
		/**
		 * 
		 */
		
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
		System.out.println("\n\n#############");
		System.out.println("Finished run!");
		System.out.println("#############");
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
					System.err.println("Writing to file " + file + " was interrupted!");
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
