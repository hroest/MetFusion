/**
 * created by Michael Gerlich, Nov 10, 2010 - 3:54:23 PM
 */ 

package de.ipbhalle.metfusion.integration.Tanimoto;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.ipbhalle.MassBank.MassBankLookupBean;
import de.ipbhalle.metfusion.utilities.MassBank.MassBankUtilities;
import de.ipbhalle.metfusion.web.controller.MetFragBean;
import de.ipbhalle.metfusion.wrapper.Result;
import de.ipbhalle.metfusion.wrapper.ResultExt;
import de.ipbhalle.metfrag.similarity.SimilarityCompound;
import de.ipbhalle.metfrag.similarity.SimilarityGroup;
import de.ipbhalle.metfusion.integration.Similarity.SimilarityMetFusion;

public class ClusteringTest {
	
static String serverUrl = "http://msbi.ipb-halle.de/MassBank/";		//"http://www.massbank.jp/";
	
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
	 */
	public static void main(String[] args) throws InterruptedException {
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

		// instantiate new MetFragBean
		MetFragBean mfb = new MetFragBean();
		// set limit for testing purposes
		mfb.setLimit(100);
		
		// instantiate new MassBankLookupBean with designated MassBank serverUrl
		MassBankLookupBean mblb = new MassBankLookupBean(serverUrl);
		selectedInstruments = mblb.getSelectedInstruments();
		
		//String typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_SEARCH];
        StringBuilder sb = new StringBuilder();
        sb = sb.append("&INST=");
        /**
         * only use (LC)-ESI-Tandem-MS instruments
         */
        sb = sb.append("ESI-IT-(MS)n,ESI-IT-MS/MS,ESI-QTOF-MS/MS,ESI-QqIT-MS/MS,ESI-QqQ-MS/MS,ESI-QqTOF-MS/MS," +
        		"LC-ESI-IT-MS/MS,LC-ESI-QTOF-MS/MS,LC-ESI-QqQ-MS/MS");
        String[] sI = new String[] {"ESI-IT-(MS)n", "ESI-IT-MS/MS", "ESI-QTOF-MS/MS", "ESI-QqIT-MS/MS",
        		"ESI-QqQ-MS/MS", "ESI-QqTOF-MS/MS", "LC-ESI-IT-MS/MS", "LC-ESI-QTOF-MS/MS", "LC-ESI-QqQ-MS/MS"};
        
        String inst = sb.toString();
        if(inst.endsWith(","))		// remove trailing comma
        	inst = inst.substring(0, inst.length() - 1);
        
		selectedIon = "1";
		
        /**
         * build up parameter string for MassBank search
         */
        String ions = "&ION=" + selectedIon;
        inst += ions;
		
        // start both threads in parallel
        ExecutorService threadExecutor = null;
        
        MassBankUtilities mbu = new MassBankUtilities();
        String[] info = mbu.getPeaklistFromFile(inputFile);
        String mbPeaks = mbu.formatPeaksForMassBank(info[0]);
		mfb.setInputSpectrum(info[0]);
		mfb.setExactMass(Double.parseDouble(info[1]));
		// let MetFrag search in PubChem
		mfb.setSelectedDB("pubchem");
		//String cname = info[2];
		
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
        	Thread.sleep(1000);
        }while(!threadExecutor.isTerminated());
        
        // create tanimoto matrix and perform chemical-similarity based integration
		List<Result> listMassBank = mblb.getResults();
		List<Result> listMetFrag = mfb.getResults();
		
		System.out.println("MetFrag Original Ergebnisliste:");
		for (Result result : listMetFrag) {
			System.out.println(result.getId() + "  " + result.getScore());
		}
		
		TanimotoSimilarity sim = new TanimotoSimilarity(listMassBank, listMetFrag, 3, 0.5f);
		TanimotoIntegrationWeighted tiw = new TanimotoIntegrationWeighted(sim);
		List<ResultExt> secondOrder = tiw.computeNewOrdering();
		
		System.out.println("MetFusion Ergebnisse VOR clustern");
		for (ResultExt r : secondOrder) {
			System.out.println(r.getId() + "\t" + r.getResultScore() + "\t" + r.getPosAfter());
		}
		
		SimilarityMetFusion sm = new SimilarityMetFusion();
		List<ResultExt> clusterWeight =	sm.computeScores(secondOrder);
		
		System.out.println("MetFusion Ergebnisse NACH clustern:");
		for (ResultExt r : clusterWeight) {
			System.out.println(r.getId() + "\t" + r.getResultScore() + "\t" + r.getClusterRank());
		}
		
		List<SimilarityGroup> groupedCandidates = sm.getGroupedCandidates();
		int rankTanimotoGroup = 0;
		List<String> groups = new ArrayList<String>();
		for (SimilarityGroup group : groupedCandidates) {
			List<SimilarityCompound> tempSimilar = group.getSimilarCompounds();
			for (int k = 0; k < tempSimilar.size(); k++) {
				groups.add(tempSimilar.get(k).getCompoundID());
			}	
			rankTanimotoGroup++;
		}
		System.out.println("\nTanimoto clustering:");
		for (String string : groups) {
			//DAS IST ENDERGEBNISTABELLE
			System.out.println(string + "\t" + rankTanimotoGroup);
		}
		
		//RealMatrix rm = sim.getMatrix();
		//ColorcodedMatrix ccm = new ColorcodedMatrix(rm, listMassBank, listMetFrag);
	}

}
