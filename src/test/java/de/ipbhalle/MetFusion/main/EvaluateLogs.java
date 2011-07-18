/**
 * created by Michael Gerlich on Jun 14, 2010
 * last modified Jun 14, 2010 - 10:48:15 AM
 * email: mgerlich@ipb-halle.de
 */
package de.ipbhalle.MetFusion.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.ipbhalle.metfusion.wrapper.ResultExt;
import de.ipbhalle.io.FileNameFilterImpl;
import de.ipbhalle.metfusion.integration.Tanimoto.TanimotoIntegration;
import de.ipbhalle.metfusion.integration.Tanimoto.TanimotoIntegrationWeighted;
import de.ipbhalle.metfusion.integration.Tanimoto.TanimotoSimilarity;

public class EvaluateLogs {

	//public final String logDir = "/home/mgerlich/workspace-3.5/MetFusion2/testdata/Hill/results/2010-06-11_14-30-08/";
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
	public final String logDir = "/home/mgerlich/evaluation/MetFusion/HILL/results/2010-10-07_10-19-01_HILL_ESIMS2_wHill_MF+MBscore_mergedSpectra_MSBI_MassBank/";
		//"/home/mgerlich/workspace-3.5/MetFusion2/testdata/Hill/results/2010-07-07_14-54-46/";
	public final String matDir = logDir;
		//"/home/mgerlich/workspace-3.5/MetFusion2/testdata/Hill/results/2010-07-07_14-54-46/";
	public final static String statsFile = "/home/mgerlich/evaluation/statistics/paper_Hill_single_Spectra";
		//"/home/swolf/MassBankData/MetFragSunGrid/RikenDataMerged/CHONPS/useable/logs/2010-07-07_13-25-50_results.txt_ScoringFunctionPaper_MICHA";
	
	/**
	 * @param args args[0] denotes path to both *.mat and *.log files.
	 * args[1] denotes the path to the statistics file.
	 */
	public static void main(String[] args) {
		if(args == null || args.length != 2) {
			System.out.println("missing path of log files and mat files");
			System.exit(-1);
		}
			
		String path = args[0];
		String pathMat = args[1];
		FilenameFilter filterLog = new FileNameFilterImpl("", ".log", "_result");
		FilenameFilter filterMat = new FileNameFilterImpl("", ".mat", "Scores");
		
		// log files
		File dirLog = new File(path);
		File[] files = dirLog.listFiles(filterLog);
		
		// mat files
		File dirMat = new File(pathMat);
		File[] filesMat = dirMat.listFiles(filterMat);
		
		Arrays.sort(files);
		Arrays.sort(filesMat);
		System.out.println("#files = " + files.length + "\t#filesMat = " + filesMat.length);
		for (int i = 0; i < files.length; i++) {
			System.out.println(files[i]);
			System.out.println(filesMat[i]);
		}
		System.exit(-1);
		
		// start both threads in parallel
//        ExecutorService threadExecutor = null;
//        threadExecutor = Executors.newFixedThreadPool(2);
        
        
		List<EvalLog> logs = new ArrayList<EvalLog>();
		List<TanimotoSimilarity> sims = new ArrayList<TanimotoSimilarity>();
		
		File statistic = new File(statsFile);
		Map<String, String> original = new HashMap<String, String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(statistic));
			String line = "";
			while((line = br.readLine()) != null) {
				if(line.contains("CID"))	// skip header
					continue;
				
				String[] split = line.split("\t");
				String name = split[0];
				name = name.replaceAll("-", "_");
				name = name.replaceAll(" ", "_");
				if(name.contains(".txt"))
					name = name.substring(0, name.indexOf("."));
				String cid = split[1];
				original.put(name, cid);
			}
			
			br.close();
		} catch (FileNotFoundException e1) {
			System.err.println("Error reading statistics file.");
			System.exit(-1);
		} catch (IOException e) {
			System.err.println("Error reading statistics file.");
			System.exit(-1);
		}
		
		File newStats = new File("/home/mgerlich/Desktop/newStats.csv");
		FileWriter fws = null;
		try {
			fws = new FileWriter(newStats);
		} catch (IOException e1) {
			System.err.println("error creating new filewriter for new results file...");
			System.exit(-2);
		}
		
		for (int i = 0; i < files.length; i++) {
		//for (int i = 0; i < 1; i++) {
			String cname = files[i].getName().substring(0, files[i].getName().indexOf("."));
			System.out.println(files[i] + "\t" + cname);
			//logs.add(new EvalLog(files[i]));
			EvalLog log = new EvalLog(files[i]);
			try {
				//sims.add(new TanimotoSimilarity(filesMat[i]));
				//TanimotoSimilarity sim = sims.get(i);
				TanimotoSimilarity sim = new TanimotoSimilarity(filesMat[i]);
				//sim.setCandidates(logs.get(i).getCandidates());
				//sim.setPrimaries(logs.get(i).getPrimaries());
				sim.setCandidates(log.getCandidates());
				sim.setPrimaries(log.getPrimaries());
				
				System.out.println("#primaries -> " + sim.getPrimaries().size());
				System.out.println("#candidates -> " + sim.getCandidates().size());
				System.out.println("matrix -> [" + sim.getMatrix().getRowDimension() 
						+ "x" + sim.getMatrix().getColumnDimension() + "]");
				
				
				TanimotoIntegration integration = new TanimotoIntegration(sim);
				List<ResultExt> newOrder = integration.computeNewOrdering();
				
				TanimotoIntegrationWeighted tiw = new TanimotoIntegrationWeighted(sim);
				List<ResultExt> newOrder2 = tiw.computeNewOrdering();
				
				File f = new File("/home/mgerlich/Desktop/result/result_thresh" + cname + ".txt");
				try {
					FileWriter fw = new FileWriter(f);
					for (int j = 0; j < newOrder.size(); j++) {
						ResultExt r = newOrder.get(j);
						fw.write(r.getPosBefore() + "\t" + r.getId() + "\t" + r.getScore() + "\t" + r.getResultScore() + "\t" + r.getPosAfter() + "\n");
						
						if(original.containsKey(cname) && original.get(cname).equals(r.getId())) {
							fws.write(cname + "\t" + r.getId() + "\t" +r.getPosAfter() + "\n");
						}
					}
					fw.flush();
					fw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				f = new File("/home/mgerlich/Desktop/result/result_weight" + cname + ".txt");
				try {
					FileWriter fw = new FileWriter(f);
					for (int j = 0; j < newOrder2.size(); j++) {
						ResultExt r = newOrder2.get(j);
						fw.write(r.getPosBefore() + "\t" + r.getId() + "\t" + r.getScore() + "\t" + r.getResultScore() + "\t" + r.getPosAfter() + "\n");
						
						if(original.containsKey(cname) && original.get(cname).equals(r.getId())) {
							fws.write(cname + "\t" + r.getId() + "\t" +r.getPosAfter() + "\n");
						}
					}
					fw.flush();
					fw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (NumberFormatException e) {
				//logs.remove(i);		// remove log from list as it is useless without corresponding matrix
				System.err.println("Error while loading Tanimoto Matrix for " + filesMat[i] + " -> removed from evaluation procedure.");

				e.printStackTrace();
			} catch (IOException e) {
				//logs.remove(i);
				System.err.println("Error while loading Tanimoto Matrix for " + filesMat[i] + " -> removed from evaluation procedure.");
				
				e.printStackTrace();
			}
			
		}
		
		try {
			fws.flush();
			fws.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.exit(0);
		
		TanimotoSimilarity sim = sims.get(0);
		sim.setCandidates(logs.get(0).getCandidates());
		sim.setPrimaries(logs.get(0).getPrimaries());
		
		System.out.println("#primaries -> " + sim.getPrimaries().size());
		System.out.println("#candidates -> " + sim.getCandidates().size());
		System.out.println("matrix -> [" + sim.getMatrix().getRowDimension() 
				+ "x" + sim.getMatrix().getColumnDimension() + "]");
		
		
		TanimotoIntegration integration = new TanimotoIntegration(sim);
		List<ResultExt> newOrder = integration.computeNewOrdering();
		
		TanimotoIntegrationWeighted tiw = new TanimotoIntegrationWeighted(sim);
		List<ResultExt> newOrder2 = tiw.computeNewOrdering();
		
		File f = new File("/home/mgerlich/Desktop/result_thresh.txt");
		try {
			FileWriter fw = new FileWriter(f);
			for (int i = 0; i < newOrder.size(); i++) {
				ResultExt r = newOrder.get(i);
				fw.write(r.getPosBefore() + "\t" + r.getId() + "\t" + r.getScore() + "\t" + r.getResultScore() + "\t" + r.getPosAfter() + "\n");
			}
			fw.flush();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		f = new File("/home/mgerlich/Desktop/result_weight.txt");
		try {
			FileWriter fw = new FileWriter(f);
			for (int i = 0; i < newOrder2.size(); i++) {
				ResultExt r = newOrder2.get(i);
				fw.write(r.getPosBefore() + "\t" + r.getId() + "\t" + r.getScore() + "\t" + r.getResultScore() + "\t" + r.getPosAfter() + "\n");
			}
			fw.flush();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		threadExecutor.execute(logs.get(0));
//		threadExecutor.execute(logs.get(1));
//		threadExecutor.shutdown();
	}

}
