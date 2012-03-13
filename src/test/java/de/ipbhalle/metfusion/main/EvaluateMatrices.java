package de.ipbhalle.MetFusion.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.output.LockableFileWriter;

import de.ipbhalle.metfusion.wrapper.ResultExt;
import de.ipbhalle.io.FileNameFilterImpl;
import de.ipbhalle.metfusion.integration.Similarity.SimilarityMetFusion;
import de.ipbhalle.metfusion.integration.Tanimoto.TanimotoIntegration;
import de.ipbhalle.metfusion.integration.Tanimoto.TanimotoIntegrationWeighted;
import de.ipbhalle.metfusion.integration.Tanimoto.TanimotoSimilarity;


/**
 * TODO:
 * ergebnismatrizen einlesen und schwellwert sowie gewichteten ansatz drueberjagen.
 * neue ergebnislisten abspeichern -> *.list
 * neue tied-rank listen abspeichern -> *.tied
 * neue cluster-rank listen abspeichern -> *.cluster
 */

public class EvaluateMatrices {

	/**
	 * HILL run
	 * 
	 * /home/mgerlich/workspace-3.5/MetFusion2/testdata/Hill/results/2010-06-30_14-14-48/ 
	 * /home/mgerlich/workspace-3.5/MetFusion2/testdata/Hill/results/2010-06-30_14-14-48/
	 */
	
	/**
	 * RIKEN run
	 * 
	 * /home/mgerlich/workspace-3.5/MetFusion2/testdata/Hill/results/2010-07-07_14-54-46/
	 * /home/mgerlich/workspace-3.5/MetFusion2/testdata/Hill/results/2010-07-07_14-54-46/
	 */
	public final String logDir = "/home/mgerlich/workspace-3.5/MetFusion2/testdata/Hill/results/2010-07-07_14-54-46/";
	public final String matDir = "/home/mgerlich/workspace-3.5/MetFusion2/testdata/Hill/results/2010-07-07_14-54-46/";
	public final static String statsFile = "/home/swolf/MassBankData/MetFragSunGrid/RikenDataMerged/CHONPS/useable/logs/2010-07-07_13-25-50_results.txt_ScoringFunctionPaper_MICHA";
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		if(args == null || args.length < 2) {
			System.out.println("Program requires path/to/spectra/ and path/to/output/");
			System.exit(-1);
		}
		
		File inputFile = new File(args[0]);
		File outputDir = new File(args[1]);
		
		if(inputFile.isDirectory() || !outputDir.isDirectory()) {
			System.out.println("Program requires path/to/spectra_file and path/to/output/");
			System.exit(-1);
		}

		/**
		 * read statistics file
		 */
		//File statistics = new File("/home/mgerlich/Desktop/MetFrag_results/RIKEN/scoring_paper/2010-07-07_13-25-50_results.txt_ScoringFunctionPaper_MICHA");// scoring paper
		//File statistics = new File("/home/mgerlich/Desktop/MetFrag_results/HILL/scoring_paper/2010-07-07_15-24-35_results.txt");
		File statistics = new File("/home/mgerlich/evaluation/statistics/paper_Hill_sorted");
		
		BufferedReader br = null;
		Map<String, String> statMap = new HashMap<String, String>();
		try {
			br = new BufferedReader(new FileReader(statistics));
			String line = "";
			line = br.readLine();	// skip header
			//  CID      worstRank
			while((line = br.readLine()) != null) {
				if(line.isEmpty() || line.equals("\n"))
					continue;
				
				String[] split = line.split("\t");
//				String name = split[0];
//				name = name.replaceAll("-", "_");
//				name = name.replaceAll(" ", "_");
				String key = split[0].split("_10")[0];
				//String key = split[0];		// e.g. PR100001PR100002.txt
//				if(key.length() > 8)
//					key = key.substring(0, key.indexOf("."));
				key = key.replaceAll(" ", "_");
				key = key.replaceAll("-", "_");
				String cid = split[1];
				statMap.put(key, cid);	// mergeName	CID		worstRank	mass	time
			}
			
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		/**
		 * 
		 */
		File input = new File(args[0]);
		String logfile = input.getName().replace(".mat", ".log");
//		if(logfile.length() > 12)
//			logfile = logfile.substring(0, 8) + ".log";
		
		String dir = input.getAbsolutePath().substring(0, input.getAbsolutePath().lastIndexOf("/"));
		File input_log = new File(dir, logfile);
		String cname = input.getName().substring(0, input.getName().indexOf("."));
		File newStats = new File(outputDir, "HILL.stats");
		String content = "";
		
		try {
			EvalLog log = new EvalLog(input_log, true);
			TanimotoSimilarity sim = new TanimotoSimilarity(input, true);
			
			// start both threads in parallel
	        ExecutorService threadExecutor = null;
	        threadExecutor = Executors.newFixedThreadPool(2);
	        threadExecutor.execute(log);
	        threadExecutor.execute(sim);
	        threadExecutor.shutdown();
	        
	        do {
	        	Thread.sleep(5000);
	        }while(!threadExecutor.isTerminated());
	        
			sim.setCandidates(log.getCandidates());
			sim.setPrimaries(log.getPrimaries());
			
			TanimotoIntegration integration = new TanimotoIntegration(sim);
			List<ResultExt> newOrder = integration.computeNewOrdering();
			
			TanimotoIntegrationWeighted tiw = new TanimotoIntegrationWeighted(sim);
			List<ResultExt> newOrder2 = tiw.computeNewOrdering();
			
			File persistentSave = new File(outputDir, cname + "_thresh.save");
			integration.persistentWrite(persistentSave.getAbsolutePath());
			
			persistentSave = new File(outputDir, cname + "_weight.save");
			tiw.persistentWrite(persistentSave.getAbsolutePath());
			
			/**
			 * MetFrag cluster ranks
			 */
			File newRankWeight = new File(outputDir, cname + "_clusterRank_weight.txt");
			File newRankThresh = new File(outputDir, cname + "_clusterRank_thresh.txt");
			SimilarityMetFusion sm = new SimilarityMetFusion();
			List<ResultExt> clusterThresh =	sm.computeScores(newOrder, newRankThresh);
			List<ResultExt> clusterWeight =	sm.computeScores(newOrder2, newRankWeight);
			/**
			 * 
			 */
			
			File f = new File(outputDir, cname + "_thresh.list_tied");
			try {
				FileWriter fw = new FileWriter(f);
				for (int j = 0; j < newOrder.size(); j++) {
					ResultExt r = newOrder.get(j);
					fw.write(r.getPosBefore() + "\t" + r.getId() + "\t" + r.getScore() + "\t" + r.getResultScore() + "\t" + r.getPosAfter() + "\n");
					
					if(statMap.containsKey(cname) && statMap.get(cname).equals(r.getId())) {
						//fws.write(cname + "\t" + r.getId() + "\t" +r.getPosAfter() + "\n");
						//fws.write(cname + "\t" + r.getId() + "\t" + r.getPosAfter());
						content = cname + "\t" + r.getId() + "\t" + r.getPosAfter();
					}
				}
				fw.flush();
				fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			f = new File(outputDir, cname + "_weight.list_tied");
			try {
				FileWriter fw = new FileWriter(f);
				for (int j = 0; j < newOrder2.size(); j++) {
					ResultExt r = newOrder2.get(j);
					fw.write(r.getPosBefore() + "\t" + r.getId() + "\t" + r.getScore() + "\t" + r.getResultScore() + "\t" + r.getPosAfter() + "\n");
					
					if(statMap.containsKey(cname) && statMap.get(cname).equals(r.getId())) {
						//fws.write(cname + "\t" + r.getId() + "\t" +r.getPosAfter() + "\n");
						//fws.write("\t" + r.getPosAfter());
						content += "\t" + r.getPosAfter();
					}
				}
				fw.flush();
				fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// write cluster ranks to stats file
			for (int j = 0; j < clusterThresh.size(); j++) {
				ResultExt r = clusterThresh.get(j);
				
				if(statMap.containsKey(cname) && statMap.get(cname).equals(r.getId())) {
					//fws.write(cname + "\t" + r.getId() + "\t" +r.getPosAfter() + "\n");
					//fws.write("\t" + r.getClusterRank());
					content += "\t" + r.getClusterRank();
				}
			}
			for (int j = 0; j < clusterWeight.size(); j++) {
				ResultExt r = clusterWeight.get(j);
				
				if(statMap.containsKey(cname) && statMap.get(cname).equals(r.getId())) {
					//fws.write(cname + "\t" + r.getId() + "\t" +r.getPosAfter() + "\n");
					//fws.write("\t" + r.getClusterRank() + "\n");
					content += "\t" + r.getClusterRank() + "\n";
				}
			}
			
		} catch (NumberFormatException e) {
			e.printStackTrace();
			System.err.println("NumberFormatException while creating new TanimotoSimilarity from file!");
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("IOException while creating new TanimotoSimilarity from file!");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		writeToFile(newStats.getAbsolutePath(), content, outputDir.getAbsolutePath());
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
}
