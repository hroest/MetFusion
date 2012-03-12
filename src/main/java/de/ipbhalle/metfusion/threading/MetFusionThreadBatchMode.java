/**
 * created by Michael Gerlich, Sep 1, 2011 - 4:15:14 PM
 */ 

package de.ipbhalle.metfusion.threading;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;

import jxl.write.WriteException;

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
import de.ipbhalle.metfusion.web.controller.ResultExtGroupBean;
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
	
	@Override
	public void run() {
		long time1 = System.currentTimeMillis();
		setActive(Boolean.TRUE);
		
		massbank.run();
		metfrag.run();
		while(!massbank.isDone() && !metfrag.isDone()) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		if(massbank.getResults() == null || massbank.getResults().size() == 0) {
        	String errMessage = "Peak(s) not found in MassBank - check the settings and try again.";
            System.err.println(errMessage);
            FacesMessage curentMessage = new FacesMessage(errMessage, errMessage);
            curentMessage.setSeverity(FacesMessage.SEVERITY_WARN);
            //Mark as ERROR
    		setActive(Boolean.FALSE);
    		
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
            
            return;
        }
		
		// create tanimoto matrix and perform chemical-similarity based integration
		List<Result> listMassBank = massbank.getResults();
		List<Result> listMetFrag = metfrag.getResults();
		
		/**
		 * TODO: check output
		 */
		File origOut = new File(tempPath, addPrefixToFile("resultsOrig.log"));
		try {
			FileWriter fw = new FileWriter(origOut);
			fw.write("## MassBank\n");
			for (int i = 0; i < listMassBank.size(); i++) {
				Result result = listMassBank.get(i);
				fw.write(formatResultOutput(result));
			}
			
			fw.write("## MetFrag\n");
			for (int i = 0; i < listMetFrag.size(); i++) {
				Result result = listMetFrag.get(i);
				fw.write(formatResultOutput(result));
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
		
		TanimotoSimilarity sim = new TanimotoSimilarity(listMassBank, listMetFrag);	//, 3, 0.5f);
		sim.writeMatrix(sim.getMatrix(), new File(tempPath, addPrefixToFile("sim.mat")));

		String sessionPath = massbank.getSessionPath();
		// fork new thread for generating ColorCodedMatrix
		ColoredMatrixGeneratorThread cmT = new ColoredMatrixGeneratorThread(sim);
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
        cmT.run();
        //igT.run();
        //igT2.run();
        
        /**
		 * MetFrag cluster ranks
		 */
        while(!tiw.isDone() && !cmT.isDone()) { // && !igT.isDone() && !igT2.isDone()) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
        metfusion.setColorMatrix(cmT.getCcm());
        
        /**
		 * TODO: check output
		 */
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
				fw.write(formatResultOutput(r));
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
		 *  new colored similarity matrix after metfusion
		 */
		TanimotoSimilarity after = new TanimotoSimilarity(listMassBank, redraw);	//, 3, 0.5f);
		// fork new thread for generating ColorCodedMatrix
		ColoredMatrixGeneratorThread cmtAfter = new ColoredMatrixGeneratorThread(after);
		cmtAfter.run();
		while(!cmtAfter.isDone()) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
        metfusion.setColorMatrixAfter(cmtAfter.getCcm());
        
        cmT.getCcm().writeColorMatrix(new File(tempPath, addPrefixToFile("color.mat")));
        cmtAfter.getCcm().writeColorMatrix(new File(tempPath, addPrefixToFile("colorAfter.mat")));
        
        // write output files
        if(this.format.equals(OutputFormats.SDF)) {
        	SDFOutputHandler sdfhandler = new SDFOutputHandler(tempPath + prefix + ".sdf", Boolean.FALSE);
    		sdfhandler.writeRerankedResults(resultingOrder);
        }
        else if(this.format.equals(OutputFormats.XLS)) {
	        XLSOutputHandler xlsHandler = new XLSOutputHandler(tempPath + prefix + ".xls");
	        xlsHandler.writeAllResults(listMetFrag, listMassBank, resultingOrder, null);
	        xlsHandler.writeOriginalMatrix(cmT.getCcm(), "Original Matrix");
	        xlsHandler.writeModifiedMatrix(cmtAfter.getCcm(), "Reranked Matrix");
	        try {
				xlsHandler.finishWorkbook();
			} catch (WriteException e2) {
				System.err.println("Could not write xls file [" + tempPath + prefix + ".xls]");
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
        
        metfusion.setSecondOrder(resultingOrder);	// assign results to metfusion bean
		SimilarityMetFusion sm = new SimilarityMetFusion();
		System.out.println("Started clustering");
		//List<ResultExtGroupBean> clusters = sm.computeScoresCluster(resultingOrder, styleBean);
		List<ResultExtGroupBean> clusters = sm.computeScoresCluster(resultingOrder);
		/**
		 * TODO check
		 */
		File clusterOut = new File(tempPath, addPrefixToFile("resultsCluster.log"));
		try {
			fw = new FileWriter(clusterOut);
			fw.write("ID\tResultScore\tCluster\tName\tExactMass\tSMILES\tSumFormula\n");
		} catch (IOException e) {
			System.err.println("Error writing [header] to file [" + clusterOut.getAbsolutePath() + "]");
		}
		for (int i = 0; i < clusters.size(); i++) {
			ResultExtGroupBean r = clusters.get(i);
			try {
				fw.write(formatResultOutput(r));
			} catch (IOException e) {
				System.err.println("Error writing [parent cluster] to file [" + clusterOut.getAbsolutePath() + "]");
			}
			List<ResultExtGroupBean> l = r.getChildResultRows();
			if(l.size() > 1) {
				for (int j = 0; j < l.size(); j++) {
					ResultExtGroupBean child = clusters.get(i);
					try {
						fw.write(formatResultOutput(child));
					} catch (IOException e) {
						System.err.println("Error writing [child cluster] to file [" + clusterOut.getAbsolutePath() + "]");
					}
				}
			}
		}
		try {
			fw.flush();
			fw.close();
		} catch (IOException e1) {
			System.err.println("Error finalizing file [" + clusterOut.getAbsolutePath() + "]");
		}
		
		metfusion.setTanimotoClusters(clusters);
		System.out.println("Finished clustering");
		
		System.out.println("list size -> " + clusters.size());
		setActive(Boolean.FALSE);
		long time2 = System.currentTimeMillis() - time1;
		System.out.println("time spended -> " + time2 + " ms");
	}

	/**
	 * Utitily method to format the output String according to the passed Object, which is then written into a file.
	 * 
	 * @param o The Object for which the output string should be formatted. Only one of Result, ResultExt or ResultExtGroupBean will lead
	 * to formatted output strings.
	 * @return The correctly formatted String for output in a file.
	 */
	private String formatResultOutput(Object o) {
		if(o instanceof Result) {
			Result r = (Result) o;
			
			String result =  r.getId() + DEFAULT_SEPARATOR + r.getScore() + DEFAULT_SEPARATOR + r.getName() + 
				DEFAULT_SEPARATOR + r.getExactMass() + DEFAULT_SEPARATOR + r.getSmiles() + DEFAULT_SEPARATOR + r.getSumFormula() + DEFAULT_NEWLINE;
			return result;
		}
		else if(o instanceof ResultExt) {
			ResultExt r = (ResultExt) o;
			
			String result = r.getId() + DEFAULT_SEPARATOR + r.getResultScore() + DEFAULT_SEPARATOR + r.getName() + 
				DEFAULT_SEPARATOR + r.getExactMass() + DEFAULT_SEPARATOR + r.getSmiles() + DEFAULT_SEPARATOR + r.getSumFormula() + DEFAULT_NEWLINE;
			return result;
		}
		else if(o instanceof ResultExtGroupBean) {
			ResultExtGroupBean r = (ResultExtGroupBean) o;
			
			String result = r.getId() + DEFAULT_SEPARATOR + r.getResultScore() + DEFAULT_SEPARATOR + r.getClusterRank() + 
				DEFAULT_SEPARATOR +	r.getName() + DEFAULT_SEPARATOR + r.getExactMass() + DEFAULT_SEPARATOR + r.getSmiles() + 
				DEFAULT_SEPARATOR + r.getSumFormula() + DEFAULT_NEWLINE;
			return result;
		}
		
		String result = "";
		return result;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isActive() {
		return active;
	}

}
