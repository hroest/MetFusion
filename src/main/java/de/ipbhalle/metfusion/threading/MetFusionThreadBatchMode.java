/**
 * created by Michael Gerlich, Sep 1, 2011 - 4:15:14 PM
 */ 

package de.ipbhalle.metfusion.threading;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.faces.application.FacesMessage;

import jxl.write.WriteException;

import de.ipbhalle.enumerations.AvailableParameters;
import de.ipbhalle.enumerations.OutputFormats;
import de.ipbhalle.metfusion.integration.Similarity.SimilarityMetFusion;
import de.ipbhalle.metfusion.integration.Tanimoto.TanimotoIntegrationWeighted;
import de.ipbhalle.metfusion.integration.Tanimoto.TanimotoSimilarity;
import de.ipbhalle.metfusion.main.MassBankBatchMode;
import de.ipbhalle.metfusion.main.MetFragBatchMode;
import de.ipbhalle.metfusion.main.MetFusionBatchMode;
import de.ipbhalle.metfusion.utilities.output.CSVOutputHandler;
import de.ipbhalle.metfusion.utilities.output.ODFOutputHandler;
import de.ipbhalle.metfusion.utilities.output.SDFOutputHandler;
import de.ipbhalle.metfusion.utilities.output.XLSOutputHandler;
import de.ipbhalle.metfusion.wrapper.Result;
import de.ipbhalle.metfusion.wrapper.ResultExt;

public class MetFusionThreadBatchMode implements Runnable {

	private MetFusionBatchMode metfusion;
	private MassBankBatchMode massbank;
	private MetFragBatchMode metfrag;
	private String tempPath;
	private String prefix;
	private OutputFormats format;
	
	private boolean active = Boolean.FALSE;
	private final String prefixSeparator = "_";
	private final String DEFAULT_SEPARATOR = "\t";
	private final String DEFAULT_NEWLINE = "\n";
	
	private final String os = System.getProperty("os.name");
	private final String fileSeparator = System.getProperty("file.separator");
	private final String userHome = System.getProperty("user.home");
	private final String currentDir = System.getProperty("user.dir");
	private final String lineSeparator = System.getProperty("line.separator");
	private final String fileEncoding = System.getProperty("file.encoding");
	
	/** indicator for using ChemAxon fingerprints instead of CDK ones */
	private boolean useECFP = Boolean.FALSE;
	
	/** indicator for compression */
	private boolean compress = Boolean.FALSE;
	
	/** indicator for verbosity */
	private boolean verbose = Boolean.FALSE;
	
	
	/**
	 * 
	 * @param app the MetFusionBatchMode instance
	 * @param database the MassBankBatchMode instance
	 * @param fragmenter the MetFragBatchMode instance
	 * @param tempPath the output path
	 * @param prefix the prefix used for each output file
	 */
	public MetFusionThreadBatchMode(MetFusionBatchMode app, MassBankBatchMode database, MetFragBatchMode fragmenter, 
			String tempPath, String prefix, OutputFormats format) {
		this.metfusion = app;
		this.massbank = database;
		this.metfrag = fragmenter;
		this.tempPath = tempPath;
		this.prefix = prefix;
		this.format = format;
	}
	
	private String addPrefixToFile(String filename) {
		return prefix + prefixSeparator + filename;
	}
	
	private void writeUnused(List<Result> unused, File out) {
		if(!unused.isEmpty()) {
			try {
				FileWriter fw = new FileWriter(out);
				for (Result r : unused) {
					fw.write(r.getId() + "\t" + r.getName() + "\t" + r.getScore() + "\n");
				}
				fw.flush();
				fw.close();
			} catch (IOException e) {
				System.err.println("Error writing list of unused MassBank records!");
			}
		}
	}
	
	@Override
	public void run() {
		long time1 = System.currentTimeMillis();
		setActive(Boolean.TRUE);
		
		/**
		 * threading via Threads and ThreadExecutor
		 */
		// create new Threads from Runnable-implementing classes
		Thread r1 = new Thread(massbank);
		Thread r2 = new Thread(metfrag);
		ExecutorService threadExecutor = Executors.newFixedThreadPool(4);
		// execute the threads in parallel
		threadExecutor.execute(r1);
		threadExecutor.execute(r2);
		threadExecutor.shutdown();
		//wait until all threads are finished
		while(!threadExecutor.isTerminated())
		{
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				r1.interrupt();
				r2.interrupt();
				String errMessage = "Error performing queries.";
				System.err.println(errMessage);
				System.err.println("Aborting run.");
				
	            return;
			}
		}
		/**
		 * threading via runnable()
		 */
		
		// both result lists are empty
		if((massbank.getResults() == null || massbank.getResults().size() == 0) && 
				(metfrag.getResults() == null || metfrag.getResults().size() == 0)) {
			System.err.println("No results at all, both MassBank and MetFrag returned no results or had errors!");
			return;
		}
		
		if(massbank.getResults() == null || massbank.getResults().size() == 0) {
        	String errMessage = "Peak(s) not found in MassBank - check the settings and try again.";
            System.err.println(errMessage);
            FacesMessage curentMessage = new FacesMessage(errMessage, errMessage);
            curentMessage.setSeverity(FacesMessage.SEVERITY_WARN);
            //Mark as ERROR
    		setActive(Boolean.FALSE);
    		
    		// write out MetFrag results
    		List<Result> listMetFrag = metfrag.getResults();
    		if(listMetFrag.size() > 0) {
    			File incomplete = new File(tempPath, addPrefixToFile("incomplete.log"));
        		try {
        			FileWriter fw = new FileWriter(incomplete);    			
        			fw.write("## MetFrag\n");
        			for (int i = 0; i < listMetFrag.size(); i++) {
        				Result result = listMetFrag.get(i);
        				fw.write(formatResultOutput(result, false));
        			}
        			fw.flush();
        			fw.close();
        		} catch (IOException e1) {
        			System.err.println("Error writing original results in [" + incomplete.getAbsolutePath() + "]!");
        		}
    		}
    		
    		// write original MetFrag result SDF only if verbose
    		if(isVerbose() && listMetFrag.size() > 0) {
    			File incompleteSDF = new File(tempPath, addPrefixToFile("incomplete_metfrag.sdf"));
        		SDFOutputHandler sdfHandler = new SDFOutputHandler(incompleteSDF.getAbsolutePath());
        		sdfHandler.writeOriginalResults(listMetFrag, isCompress());
        		
        		if(this.format.equals(OutputFormats.SDF_XLS) || 
        				this.format.equals(OutputFormats.XLS)) {
        			File incompleteXLS = new File(tempPath, addPrefixToFile("incomplete_metfrag.xls"));
        			de.ipbhalle.metfusion.utilities.output.XLSOutputHandler xlsHandler = 
        					new de.ipbhalle.metfusion.utilities.output.XLSOutputHandler(incompleteXLS.getAbsolutePath());
        			xlsHandler.writeAllResults(listMetFrag, null, null, null);
        	        xlsHandler.writeSettings(fetchSettings());
        	        try {
						xlsHandler.finishWorkbook(false);
					} catch (IOException e) {
						System.err.println("Could not write xls file [" + incompleteXLS.getAbsolutePath() + "]");
					}
        		}
    		}
    		
            return;
        }
        else if(massbank.getResults() != null) {
            System.out.println("# MassBank results: " + massbank.getResults().size());
            //setShowResultsDatabase(true);
        }
        else {      // abort run and return
            //String errMessage = "EMPTY MassBank result! - Check settings.";
        	String errMessage = "Peak(s) not found in MassBank - check the settings and try again.";
            //this.errorMessage = errMessage;
            System.err.println(errMessage);
            FacesMessage curentMessage = new FacesMessage(errMessage, errMessage);
            curentMessage.setSeverity(FacesMessage.SEVERITY_WARN);
            //Mark as ERROR
    		setActive(Boolean.FALSE);
    		
    		// write out MetFrag results
    		List<Result> listMetFrag = metfrag.getResults();
    		if(listMetFrag.size() > 0) {
    			File incomplete = new File(tempPath, addPrefixToFile("incomplete.log"));
        		try {
        			FileWriter fw = new FileWriter(incomplete);    			
        			fw.write("## MetFrag\n");
        			for (int i = 0; i < listMetFrag.size(); i++) {
        				Result result = listMetFrag.get(i);
        				fw.write(formatResultOutput(result, false));
        			}
        			fw.flush();
        			fw.close();
        		} catch (IOException e1) {
        			System.err.println("Error writing original results in [" + incomplete.getAbsolutePath() + "]!");
        		}
    		}
    		
    		// write original MetFrag result SDF only if verbose
    		if(isVerbose() && listMetFrag.size() > 0) {
    			File incompleteSDF = new File(tempPath, addPrefixToFile("incomplete_metfrag.sdf"));
        		SDFOutputHandler sdfHandler = new SDFOutputHandler(incompleteSDF.getAbsolutePath());
        		sdfHandler.writeOriginalResults(listMetFrag, isCompress());
        		
        		if(this.format.equals(OutputFormats.SDF_XLS) || 
        				this.format.equals(OutputFormats.XLS)) {
        			File incompleteXLS = new File(tempPath, addPrefixToFile("incomplete_metfrag.xls"));
        			de.ipbhalle.metfusion.utilities.output.XLSOutputHandler xlsHandler = 
        					new de.ipbhalle.metfusion.utilities.output.XLSOutputHandler(incompleteXLS.getAbsolutePath());
        			xlsHandler.writeAllResults(listMetFrag, null, null, null);
        	        xlsHandler.writeSettings(fetchSettings());
        	        try {
						xlsHandler.finishWorkbook(false);
					} catch (IOException e) {
						System.err.println("Could not write xls file [" + incompleteXLS.getAbsolutePath() + "]");
					}
        		}
    		}
    		
            return;
        }
                
        if(metfrag.getResults() == null || metfrag.getResults().size() == 0) {
        	String errMessage = "EMPTY MetFrag result! - Check settings.";
        	//this.errorMessage = errMessage;
            System.err.println(errMessage);
            FacesMessage curentMessage = new FacesMessage(errMessage, errMessage);
            curentMessage.setSeverity(FacesMessage.SEVERITY_WARN);
            //Mark as ERROR
    		setActive(Boolean.FALSE);
    		setActive(Boolean.FALSE);
    		
    		// write out MassBank results
    		List<Result> listMassBank = massbank.getResults();
    		if(listMassBank.size() > 0) {
    			File incomplete = new File(tempPath, addPrefixToFile("incomplete.log"));
        		try {
        			FileWriter fw = new FileWriter(incomplete);    			
        			fw.write("## MassBank\n");
        			for (int i = 0; i < listMassBank.size(); i++) {
        				Result result = listMassBank.get(i);
        				fw.write(formatResultOutput(result, false));
        			}
        			fw.flush();
        			fw.close();
        		} catch (IOException e1) {
        			System.err.println("Error writing original results in [" + incomplete.getAbsolutePath() + "]!");
        		}
    		}
    		
    		// write original MassBank result SDF only if verbose
    		if(isVerbose() && listMassBank.size() > 0) {
    			File incompleteSDF = new File(tempPath, addPrefixToFile("incomplete_massbank.sdf"));
        		SDFOutputHandler sdfHandler = new SDFOutputHandler(incompleteSDF.getAbsolutePath());
        		sdfHandler.writeOriginalResults(listMassBank, isCompress());
        		
        		if(this.format.equals(OutputFormats.SDF_XLS) || 
        				this.format.equals(OutputFormats.XLS)) {
        			File incompleteXLS = new File(tempPath, addPrefixToFile("incomplete_massbank.xls"));
        			de.ipbhalle.metfusion.utilities.output.XLSOutputHandler xlsHandler = 
        					new de.ipbhalle.metfusion.utilities.output.XLSOutputHandler(incompleteXLS.getAbsolutePath());
        			xlsHandler.writeAllResults(listMassBank, null, null, null);
        	        xlsHandler.writeSettings(fetchSettings());
        	        try {
						xlsHandler.finishWorkbook(false);
					} catch (IOException e) {
						System.err.println("Could not write xls file [" + incompleteXLS.getAbsolutePath() + "]");
					}
        		}
    		}
    		
            return;
        }
        else if(metfrag.getResults() != null) {
        	System.out.println("# MetFrag results: " + metfrag.getResults().size());
        }
        else {      // abort run and return
            String errMessage = "EMPTY MetFrag result! - Check settings.";
            //this.errorMessage = errMessage;
            System.err.println(errMessage);
            FacesMessage curentMessage = new FacesMessage(errMessage, errMessage);
            curentMessage.setSeverity(FacesMessage.SEVERITY_WARN);
            //Mark as ERROR
    		setActive(Boolean.FALSE);
            
    		// write out MassBank results
    		List<Result> listMassBank = massbank.getResults();
    		if(listMassBank.size() > 0) {
    			File incomplete = new File(tempPath, addPrefixToFile("incomplete.log"));
        		try {
        			FileWriter fw = new FileWriter(incomplete);    			
        			fw.write("## MassBank\n");
        			for (int i = 0; i < listMassBank.size(); i++) {
        				Result result = listMassBank.get(i);
        				fw.write(formatResultOutput(result, false));
        			}
        			fw.flush();
        			fw.close();
        		} catch (IOException e1) {
        			System.err.println("Error writing original results in [" + incomplete.getAbsolutePath() + "]!");
        		}
    		}
    		
    		// write original MassBank result SDF only if verbose
    		if(isVerbose() && listMassBank.size() > 0) {
    			File incompleteSDF = new File(tempPath, addPrefixToFile("incomplete_massbank.sdf"));
        		SDFOutputHandler sdfHandler = new SDFOutputHandler(incompleteSDF.getAbsolutePath());
        		sdfHandler.writeOriginalResults(listMassBank, isCompress());
        		
        		if(this.format.equals(OutputFormats.SDF_XLS) || 
        				this.format.equals(OutputFormats.XLS)) {
        			File incompleteXLS = new File(tempPath, addPrefixToFile("incomplete_massbank.xls"));
        			de.ipbhalle.metfusion.utilities.output.XLSOutputHandler xlsHandler = 
        					new de.ipbhalle.metfusion.utilities.output.XLSOutputHandler(incompleteXLS.getAbsolutePath());
        			xlsHandler.writeAllResults(listMassBank, null, null, null);
        	        xlsHandler.writeSettings(fetchSettings());
        	        try {
						xlsHandler.finishWorkbook(false);
					} catch (IOException e) {
						System.err.println("Could not write xls file [" + incompleteXLS.getAbsolutePath() + "]");
					}
        		}
    		}
    		
            return;
        }
		
		// create tanimoto matrix and perform chemical-similarity based integration
		List<Result> listMassBank = massbank.getResults();
		List<Result> listMetFrag = metfrag.getResults();
		
		/** write unused spectral DB log only if verbose */
		if(isVerbose()) {
			File unused = new File(tempPath, addPrefixToFile("unused.log"));
			writeUnused(massbank.getUnused(), unused);
		}
		
		/** write SDFs out only if verbose */
		if(isVerbose()) {
			File massbankSDF = new File(tempPath, addPrefixToFile("massbank.sdf"));
			SDFOutputHandler sdfHandlerMB = new SDFOutputHandler(massbankSDF.getAbsolutePath());
			sdfHandlerMB.writeOriginalResults(listMassBank, isCompress());
			
			File metfragSDF = new File(tempPath, addPrefixToFile("metfrag.sdf"));
			SDFOutputHandler sdfHandlerMF = new SDFOutputHandler(metfragSDF.getAbsolutePath());
			sdfHandlerMF.writeOriginalResults(listMetFrag, isCompress());
			
			File unusedSDF = new File(tempPath, addPrefixToFile("unused.sdf"));
			SDFOutputHandler sdfHandlerU = new SDFOutputHandler(unusedSDF.getAbsolutePath());
			sdfHandlerU.writeOriginalResults(massbank.getUnused(), isCompress());
		}
		
		/** write out original results everytime, despite of verbose or not */
		File origOut = new File(tempPath, addPrefixToFile("resultsOrig.log"));
		try {
			FileWriter fw = new FileWriter(origOut);
			fw.write("## MassBank\n");
			for (int i = 0; i < listMassBank.size(); i++) {
				Result result = listMassBank.get(i);
				fw.write(formatResultOutput(result, false));
			}
			
			fw.write("## MetFrag\n");
			for (int i = 0; i < listMetFrag.size(); i++) {
				Result result = listMetFrag.get(i);
				fw.write(formatResultOutput(result, false));
			}
			
			fw.flush();
			fw.close();
		} catch (IOException e1) {
			System.err.println("Error writing original results in [" + origOut.getAbsolutePath() + "]!");
		}
		
		// cancel if one or both lists are empty -> check settings
		if(listMassBank.isEmpty() || listMetFrag.isEmpty()) {
			setActive(Boolean.FALSE);
			//metfusion.setPercentProgressFragmenter(100);

			return;
		}
		
		//TanimotoSimilarity sim = new TanimotoSimilarity(listMassBank, listMetFrag, true);	//, 3, 0.5f);
		TanimotoSimilarity sim = new TanimotoSimilarity(listMassBank, listMetFrag, isUseECFP());
		/** write original similarity matrix */
		sim.writeMatrix(sim.getMatrix(), new File(tempPath, addPrefixToFile("sim.mat")));

		String sessionPath = massbank.getSessionPath();
		// fork new thread for generating ColorCodedMatrix
//		ColoredMatrixGeneratorThread cmT = new ColoredMatrixGeneratorThread(sim);
		TanimotoIntegrationWeighted tiw = new TanimotoIntegrationWeighted(sim);
		// fork new thread for generating compound images
		//ImageGeneratorThread igT = new ImageGeneratorThread(listMetFrag, sessionPath, tempPath);
		//ImageGeneratorThread igT2 = new ImageGeneratorThread(listMassBank, sessionPath, tempPath);
		
//		ExecutorService threadExecutor = Executors.newFixedThreadPool(4);
//        threadExecutor.execute(tiw);
//        threadExecutor.execute(cmT);
//        threadExecutor.execute(igT);
//        threadExecutor.execute(igT2);
//        threadExecutor.shutdown();
        
        tiw.run();
//        cmT.run();
        //igT.run();
        //igT2.run();
        
        /**
		 * MetFrag cluster ranks
		 */
        while(!tiw.isDone() ) {//&& !cmT.isDone()) { // && !igT.isDone() && !igT2.isDone()) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
//        metfusion.setColorMatrix(cmT.getCcm());
        
        /** write newly ranked results */
		File newOut = new File(tempPath, addPrefixToFile("resultsNew.log"));
		FileWriter fw = null;
		try {
			fw = new FileWriter(newOut);
			fw.write("## MetFrag\n");
		} catch (IOException e1) {
			System.err.println("Error writing new results in [" + newOut.getAbsolutePath() + "]!");
		}
        
        List<ResultExt> resultingOrder = tiw.getResultingOrder();
		List<Result> redraw = new ArrayList<Result>();
		for (int i = 0; i < resultingOrder.size(); i++) {
			ResultExt r = resultingOrder.get(i);
			redraw.add(new Result(r));
			
			try {
				fw.write(formatResultOutput(r, false));
			} catch (IOException e) {
				System.err.println("Error writing to file [" + newOut.getAbsolutePath() + "]");
			}
		}
		
		try {
			fw.flush();
			fw.close();
		} catch (IOException e1) {
			System.err.println("Error finalizing file [" + newOut.getAbsolutePath() + "]");
		}
		
		/**
		 * write SMILES file if verbose
		 */
		if(isVerbose()) {
			File smilesOut = new File(tempPath, addPrefixToFile("smiles.txt"));
			try {
				fw = new FileWriter(smilesOut);
			} catch (IOException e1) {
				System.err.println("Error writing SMILES in [" + smilesOut.getAbsolutePath() + "]!");
			}
			for (int i = 0; i < resultingOrder.size(); i++) {
				ResultExt r = resultingOrder.get(i);
				try {
					fw.write(r.getSmiles() + " " + r.getResultScore() + lineSeparator);
				} catch (IOException e) {
					System.err.println("Error writing to file [" + smilesOut.getAbsolutePath() + "]");
				}
			}
			try {
				fw.flush();
				fw.close();
			} catch (IOException e1) {
				System.err.println("Error finalizing file [" + smilesOut.getAbsolutePath() + "]");
			}
		}
		/**
		 * 
		 */
		
		/**
		 *  new colored similarity matrix after metfusion
		 */
		TanimotoSimilarity after = new TanimotoSimilarity(listMassBank, redraw, isUseECFP());	//, 3, 0.5f);
		// fork new thread for generating ColorCodedMatrix
//		ColoredMatrixGeneratorThread cmtAfter = new ColoredMatrixGeneratorThread(after);
//		cmtAfter.run();
//		while(!cmtAfter.isDone()) {
//			try {
//				wait();
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
//        metfusion.setColorMatrixAfter(cmtAfter.getCcm());
        
        //cmT.getCcm().writeColorMatrix(new File(tempPath, addPrefixToFile("color.mat")));
        //cmtAfter.getCcm().writeColorMatrix(new File(tempPath, addPrefixToFile("colorAfter.mat")));
        
        metfusion.setSecondOrder(resultingOrder);	// assign results to metfusion bean
		SimilarityMetFusion sm = new SimilarityMetFusion();
		System.out.println("Started clustering");
//		List<ResultExtGroupBean> clustersBean = sm.computeScoresCluster(resultingOrder);	// original bean-like clustering
//		List<ResultExt> clusterResultExt = sm.computeScores(resultingOrder);	
		List<ResultExt> clusters = sm.computeClusterRank(resultingOrder);
		
		/** write cluster results */
		File clusterOut = new File(tempPath, addPrefixToFile("resultsCluster.log"));
		try {
			fw = new FileWriter(clusterOut);
			fw.write("ID\tResultScore\tCluster\tName\tExactMass\tSMILES\tSumFormula\n");
		} catch (IOException e) {
			System.err.println("Error writing [header] to file [" + clusterOut.getAbsolutePath() + "]");
		}
		for (int i = 0; i < clusters.size(); i++) {
			ResultExt r = clusters.get(i);
			try {
				fw.write(formatResultOutput(r, true));
			} catch (IOException e) {
				System.err.println("Error writing [parent cluster] to file [" + clusterOut.getAbsolutePath() + "]");
			}
		}
		try {
			fw.flush();
			fw.close();
		} catch (IOException e1) {
			System.err.println("Error finalizing file [" + clusterOut.getAbsolutePath() + "]");
		}
		
		//metfusion.setTanimotoClusters(clustersBean);
		metfusion.setClusterResults(clusters);
		System.out.println("Finished clustering");
		
		// write output files
		if(this.format.equals(OutputFormats.SDF_XLS)) {
			// write SDF
			SDFOutputHandler sdfhandler = new SDFOutputHandler(tempPath + prefix + ".sdf", Boolean.FALSE);
        	sdfhandler.writeClusterResults(clusters, isCompress());
        	
        	// and XLS
        	ColoredMatrixGeneratorThread cmT = new ColoredMatrixGeneratorThread(sim);
        	ColoredMatrixGeneratorThread cmtAfter = new ColoredMatrixGeneratorThread(after);
        	cmT.run();
    		cmtAfter.run();
        	while(!cmT.isDone() && !cmtAfter.isDone()) {
    			try {
    				wait();
    			} catch (InterruptedException e) {
    				e.printStackTrace();
    			}
    		}
        	metfusion.setColorMatrix(cmT.getCcm());
        	metfusion.setColorMatrixAfter(cmtAfter.getCcm());
        	
	        XLSOutputHandler xlsHandler = new XLSOutputHandler(tempPath + prefix + ".xls");
	        xlsHandler.writeAllResults(listMetFrag, listMassBank, resultingOrder, null);
	        xlsHandler.writeOriginalMatrix(cmT.getCcm(), "Original Matrix");
	        xlsHandler.writeModifiedMatrix(cmtAfter.getCcm(), "Reranked Matrix");
	        xlsHandler.writeSettings(fetchSettings());
	        try {
				xlsHandler.finishWorkbook(isCompress());
			} catch (IOException e2) {
				System.err.println("Could not write xls file [" + tempPath + prefix + ".xls]");
			}
		}
		else if(this.format.equals(OutputFormats.SDF)) {
        	SDFOutputHandler sdfhandler = new SDFOutputHandler(tempPath + prefix + ".sdf", Boolean.FALSE);
    		//sdfhandler.writeRerankedResults(resultingOrder);
        	sdfhandler.writeClusterResults(clusters, isCompress());
        }
        else if(this.format.equals(OutputFormats.XLS)) {
        	// run color matrix threads if needed
        	ColoredMatrixGeneratorThread cmT = new ColoredMatrixGeneratorThread(sim);
        	ColoredMatrixGeneratorThread cmtAfter = new ColoredMatrixGeneratorThread(after);
        	cmT.run();
    		cmtAfter.run();
        	while(!cmT.isDone() && !cmtAfter.isDone()) {
    			try {
    				wait();
    			} catch (InterruptedException e) {
    				e.printStackTrace();
    			}
    		}
        	metfusion.setColorMatrix(cmT.getCcm());
        	metfusion.setColorMatrixAfter(cmtAfter.getCcm());
        	
	        XLSOutputHandler xlsHandler = new XLSOutputHandler(tempPath + prefix + ".xls");
	        xlsHandler.writeAllResults(listMetFrag, listMassBank, resultingOrder, null);
	        xlsHandler.writeOriginalMatrix(cmT.getCcm(), "Original Matrix");
	        xlsHandler.writeModifiedMatrix(cmtAfter.getCcm(), "Reranked Matrix");
	        xlsHandler.writeSettings(fetchSettings());
	        try {
				xlsHandler.finishWorkbook(isCompress());
			} catch (IOException e2) {
				System.err.println("Could not write xls file [" + tempPath + prefix + ".xls]");
			}
        }
        else if(format.equals(OutputFormats.CSV)) {
        	CSVOutputHandler xscHandler = new CSVOutputHandler(tempPath + prefix + ".csv");
        }
        else if(format.equals(OutputFormats.ODF)) {
        	ODFOutputHandler odfHandler = new ODFOutputHandler();
        }
        else {
        	System.err.println("OutputFormat not recognized!");
        	System.err.println("Must be one of the following: ");
        	OutputFormats[] formats = OutputFormats.values();
        	for (OutputFormats of : formats) {
				System.err.print(of.toString() + "  ");
			}
        }
		
		System.out.println("list size -> " + clusters.size());
		setActive(Boolean.FALSE);
		long time2 = System.currentTimeMillis() - time1;
		time2 = time2 / 1000;	// from milliseconds to seconds
		System.out.println("time spent -> " + time2 + " s");
	}

	private Map<AvailableParameters, Object> fetchSettings() {
    	Map<AvailableParameters, Object> m = new HashMap<AvailableParameters, Object>();
    	m.put(AvailableParameters.clustering, true);
    	m.put(AvailableParameters.mbCutoff, massbank.getCutoff());
    	m.put(AvailableParameters.mbInstruments, massbank.getSelectedInstruments());
    	m.put(AvailableParameters.mbIonization, massbank.getSelectedIon());
    	m.put(AvailableParameters.mbLimit, massbank.getLimit());
    	m.put(AvailableParameters.mfAdduct, metfrag.getSelectedAdduct());
    	m.put(AvailableParameters.mfDatabase, metfrag.getSelectedDB());
    	m.put(AvailableParameters.mfDatabaseIDs, metfrag.getDatabaseID());
    	m.put(AvailableParameters.mfExactMass, metfrag.getExactMass());
    	m.put(AvailableParameters.mfFormula, metfrag.getMolecularFormula());
    	m.put(AvailableParameters.mfLimit, metfrag.getLimit());
    	m.put(AvailableParameters.mfMZabs, metfrag.getMzabs());
    	m.put(AvailableParameters.mfMZppm, metfrag.getMzppm());
    	m.put(AvailableParameters.mfParentIon, metfrag.getParentIon());
    	m.put(AvailableParameters.mfSearchPPM, metfrag.getSearchppm());
    	m.put(AvailableParameters.onlyCHNOPS, metfrag.isOnlyCHNOPS());
    	m.put(AvailableParameters.peaks, metfrag.getInputSpectrum());
    	m.put(AvailableParameters.substrucAbsent, "not yet implemented");
    	m.put(AvailableParameters.substrucPresent, "not yet implemented");
    	m.put(AvailableParameters.spectralDB, "MassBank");
    	
    	return m;
    }
	/**
	 * Utitily method to format the output String according to the passed Object, which is then written into a file.
	 * 
	 * @param o The Object for which the output string should be formatted. Only one of Result, ResultExt or ResultExtGroupBean will lead
	 * to formatted output strings.
	 * @return The correctly formatted String for output in a file.
	 */
	private String formatResultOutput(Object o, boolean cluster) {
		String result = "";
		
		if(cluster) {
			ResultExt r = (ResultExt) o;
			
			result = r.getId() + DEFAULT_SEPARATOR + r.getResultScore() + DEFAULT_SEPARATOR + r.getClusterRank() + 
				DEFAULT_SEPARATOR +	r.getName() + DEFAULT_SEPARATOR + r.getExactMass() + DEFAULT_SEPARATOR + r.getSmiles() + 
				DEFAULT_SEPARATOR + r.getSumFormula() + DEFAULT_NEWLINE;
			return result;
		}
		else if(o instanceof ResultExt) {
			ResultExt r = (ResultExt) o;
			
			result = r.getId() + DEFAULT_SEPARATOR + r.getResultScore() + DEFAULT_SEPARATOR + r.getName() + 
				DEFAULT_SEPARATOR + r.getExactMass() + DEFAULT_SEPARATOR + r.getSmiles() + DEFAULT_SEPARATOR + r.getSumFormula() + DEFAULT_NEWLINE;
			return result;
		}
		else if(o instanceof Result) {
			Result r = (Result) o;
			
			result =  r.getId() + DEFAULT_SEPARATOR + r.getScore() + DEFAULT_SEPARATOR + r.getName() + 
				DEFAULT_SEPARATOR + r.getExactMass() + DEFAULT_SEPARATOR + r.getSmiles() + DEFAULT_SEPARATOR + r.getSumFormula() + DEFAULT_NEWLINE;
			return result;
		}
		
		return result;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isActive() {
		return active;
	}

	public void setUseECFP(boolean useECFP) {
		this.useECFP = useECFP;
	}

	public boolean isUseECFP() {
		return useECFP;
	}

	public boolean isCompress() {
		return compress;
	}

	public void setCompress(boolean compress) {
		this.compress = compress;
	}

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

}
