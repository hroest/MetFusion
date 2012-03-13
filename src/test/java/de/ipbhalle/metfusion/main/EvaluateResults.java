/**
 * created by Michael Gerlich on Jun 8, 2010
 * last modified Jun 8, 2010 - 12:52:33 PM
 * email: mgerlich@ipb-halle.de
 */
package de.ipbhalle.metfusion.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.math.stat.StatUtils;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;


public class EvaluateResults {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		//String path = args[0];
		String path = "/home/mgerlich/workspace-3.5/MetFusion2/testdata/Hill/results/2010-06-15_16-49-42/";
		File dir = new File(path);
		//File[] list = dir.listFiles(new MyFileFilter(".vec"));
		File[] results = dir.listFiles(new MyFileFilter("_result.log"));
		
		Arrays.sort(results);
		
		//if (list.length != 102 || results.length != 102) {
		if (results.length != 102) {
			System.err.println("wrong number of results files - aborting...");
			System.err.println("expected 102 - was " + results.length + " for _result.log files.");
			//System.exit(-1);
		}
		else System.out.println("expected 102 results found :)");
		
		String[] cids = new String[results.length];
		int[] worstRanks = new int[results.length];
		int[] threshRanks = new int[results.length];
		int[] threshTiedRanks = new int[results.length];
		int[] weightRanks = new int[results.length];
		int[] weightTiedRanks = new int[results.length];
		
		for (int i = 0; i < results.length; i++) {
			File f = results[i];
			System.out.println(f);
			BufferedReader br = new BufferedReader(new FileReader(f));
			String line = "";
			while((line = br.readLine()) != null) {
				/**
				 * String header = "## CID\tworstRank\tthresholdRank\tweightedRank\tthresholdTiedRank\tweightedTiedRank\n";
				 */
				if(line.startsWith("##") || line.startsWith("CID"))
					continue;
				
				String[] split = line.split("\t");
				cids[i] = split[0];
				worstRanks[i] = Integer.parseInt(split[1]);
				threshRanks[i] = Integer.parseInt(split[2]);
				weightRanks[i] = Integer.parseInt(split[3]);
				threshTiedRanks[i] = Integer.parseInt(split[4]);
				weightTiedRanks[i] = Integer.parseInt(split[5]);
			}
		}
		
		
		// Get a DescriptiveStatistics instance
		DescriptiveStatistics stats = new DescriptiveStatistics();

		// Add the data from the array
		for( int i = 0; i < threshTiedRanks.length; i++) {
		        stats.addValue(threshTiedRanks[i]);
		}

		// Compute some statistics
		double mean = stats.getMean();
		double std = stats.getStandardDeviation();
		System.out.println("mean=" + mean + "\tsd=" + std);
		
//		double mean2 = StatUtils.mean(weightTiedRanks);
//		double std2 = StatUtils.variance(weightTiedRanks);
//		double median = StatUtils.percentile(weightTiedRanks, 0.5);
		//double median = stats.getMedian();

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
