/**
 * created by Michael Gerlich, Apr 8, 2013 - 3:30:46 PM

 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */ 

package de.ipbhalle.metfusion.threading;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.ipbhalle.enumerations.AvailableParameters;
import de.ipbhalle.enumerations.OutputFormats;
import de.ipbhalle.metfusion.integration.Similarity.SimilarityMetFusion;
import de.ipbhalle.metfusion.integration.Tanimoto.TanimotoIntegrationWeighted;
import de.ipbhalle.metfusion.integration.Tanimoto.TanimotoSimilarity;
import de.ipbhalle.metfusion.main.MetFusionBatchSettings;
import de.ipbhalle.metfusion.utilities.output.CSVOutputHandler;
import de.ipbhalle.metfusion.utilities.output.ODFOutputHandler;
import de.ipbhalle.metfusion.utilities.output.SDFOutputHandler;
import de.ipbhalle.metfusion.utilities.output.XLSOutputHandler;
import de.ipbhalle.metfusion.wrapper.Result;
import de.ipbhalle.metfusion.wrapper.ResultExt;
import de.ipbhalle.metfusion.wrapper.SDFDatabase;

/**
 * Thread class that relies on two provided SDF files to replace the generic fragmenter and spectral database queries with 
 * previously generated or user-defined result lists. SDF files need to provide a Score, ID, and Name property to properly 
 * fill the result lists.
 *  
 * @author mgerlich
 *
 */
public class MetFusionThreadSDFOnly implements Runnable {

	private MetFusionBatchSettings settings;
	private SDFDatabase fragmenter;
	private SDFDatabase database;
	private OutputFormats format;
	private String tempPath;
	private String prefix;
	private final String prefixSeparator = "_";
	private final String DEFAULT_SEPARATOR = "\t";
	private final String DEFAULT_NEWLINE = "\n";
	private final String lineSeparator = System.getProperty("line.separator");
	
	/** indicator for using ChemAxon fingerprints instead of CDK ones */
	private boolean useECFP = Boolean.FALSE;
	
	/** indicator for compression */
	private boolean compress = Boolean.FALSE;
	
	/** indicator for verbosity */
	private boolean verbose = Boolean.FALSE;
	
	private boolean active = Boolean.FALSE;

	
	public MetFusionThreadSDFOnly(MetFusionBatchSettings settings, String tempPath, String prefix, OutputFormats format) {
		this.setSettings(settings);
		this.setPrefix(prefix);
		this.setFormat(format);
		this.setTempPath(tempPath);
	}
	
	private boolean setupSDFs() {
		boolean success = false;
		
		String fragmenterSDFPath = settings.getSdfFile();
		String databaseSDFPath = settings.getSpectralSDF();
		
		File fragmenterSDF = new File(fragmenterSDFPath);
		File databaseSDF = new File(databaseSDFPath);
		
		if(fragmenterSDF.exists() && fragmenterSDF.isFile() && databaseSDF.exists() && databaseSDF.isFile()) {
			fragmenter = new SDFDatabase("fragmenterSDF", fragmenterSDFPath);
			database = new SDFDatabase("databaseSDF", databaseSDFPath);

			success = true;
		}
		
		return success;
	}
	
	@Override
	public void run() {
		long time1 = System.currentTimeMillis();
		setActive(Boolean.TRUE);
		
		boolean success = setupSDFs();
		if(!success) {
			System.err.println("Error setting up SD files! Aborting.");
			return;
		}
		// create Result lists from each SDF file
		// then go ahead like MetFusionThreadBatchMode with 
		
		/**
		 * threading via Threads and ThreadExecutor
		 */
		// create new Threads from Runnable-implementing classes
		Thread r1 = new Thread(database);
		Thread r2 = new Thread(fragmenter);
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
		
		List<Result> resultDatabase = database.getResults();
		List<Result> resultFragmenter = fragmenter.getResults();
		if(resultDatabase.isEmpty()) {
			System.err.println("EMPTY Database results! Aborting.");
			System.exit(-1);
		}
		else if(resultFragmenter.isEmpty()) {
			System.err.println("EMPTY Fragmenter results! Aborting.");
			System.exit(-1);
		}
		else {
			System.out.println("Got [" + resultDatabase.size() + "] Database results.");
			System.out.println("Got [" + resultFragmenter.size() + "] Fragmenter results.");
		}
		
		/** write out original results everytime, despite of verbose or not */
		File origOut = new File(tempPath, addPrefixToFile("resultsOrig.log"));
		try {
			FileWriter fw = new FileWriter(origOut);
			fw.write("## " + database.getDatabaseName() + "\n");
			for (int i = 0; i < resultDatabase.size(); i++) {
				Result result = resultDatabase.get(i);
				fw.write(formatResultOutput(result, false));
			}
			
			fw.write("## " + fragmenter.getDatabaseName() + "\n");
			for (int i = 0; i < resultFragmenter.size(); i++) {
				Result result = resultFragmenter.get(i);
				fw.write(formatResultOutput(result, false));
			}
			
			fw.flush();
			fw.close();
		} catch (IOException e1) {
			System.err.println("Error writing original results in [" + origOut.getAbsolutePath() + "]!");
		}
		
		TanimotoSimilarity sim = new TanimotoSimilarity(resultDatabase, resultFragmenter, isUseECFP());
		/** write original similarity matrix */
		sim.writeMatrix(sim.getMatrix(), new File(tempPath, addPrefixToFile("sim.mat")));
		TanimotoIntegrationWeighted tiw = new TanimotoIntegrationWeighted(sim);
        tiw.run();
        
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
		TanimotoSimilarity after = new TanimotoSimilarity(resultDatabase, redraw, isUseECFP());	//, 3, 0.5f);
        
		SimilarityMetFusion sm = new SimilarityMetFusion();
		System.out.println("Started clustering");
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
        	
	        XLSOutputHandler xlsHandler = new XLSOutputHandler(tempPath + prefix + ".xls");
	        xlsHandler.writeAllResults(resultFragmenter, resultDatabase, resultingOrder, null);
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
        	
	        XLSOutputHandler xlsHandler = new XLSOutputHandler(tempPath + prefix + ".xls");
	        xlsHandler.writeAllResults(resultFragmenter, resultDatabase, resultingOrder, null);
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

	private String addPrefixToFile(String filename) {
		return prefix + prefixSeparator + filename;
	}
	
	private Map<AvailableParameters, Object> fetchSettings() {
		return settings.transferSettings();
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
	
	public MetFusionBatchSettings getSettings() {
		return settings;
	}

	public void setSettings(MetFusionBatchSettings settings) {
		this.settings = settings;
	}

	public SDFDatabase getFragmenter() {
		return fragmenter;
	}

	public void setFragmenter(SDFDatabase fragmenter) {
		this.fragmenter = fragmenter;
	}

	public SDFDatabase getDatabase() {
		return database;
	}

	public void setDatabase(SDFDatabase database) {
		this.database = database;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public OutputFormats getFormat() {
		return format;
	}

	public void setFormat(OutputFormats format) {
		this.format = format;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getTempPath() {
		return tempPath;
	}

	public void setTempPath(String tempPath) {
		this.tempPath = tempPath;
	}

	public boolean isUseECFP() {
		return useECFP;
	}

	public void setUseECFP(boolean useECFP) {
		this.useECFP = useECFP;
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
