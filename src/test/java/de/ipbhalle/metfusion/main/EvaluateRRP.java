/**
 * created by Michael Gerlich, Apr 20, 2011 - 3:32:07 PM
 */ 

package de.ipbhalle.MetFusion.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import de.ipbhalle.io.FileNameFilterImpl;

public class EvaluateRRP {

	public static double computeRRP(int BC, int WC, int TC) {
		double rrp = 0.0d;
		// (1/2 * (1 + ((BC - WC) / (TC - 1))));
		rrp = (1/2 * (1 + ((BC - WC) / (TC - 1))));
		return rrp;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File dir = new File("/home/mgerlich/evaluation/MetFusion/allSpectra/results/2010-12-15_16-32-02_ESIMS2_withCorrect_MSBI_MassBank/");
		File[] logs = dir.listFiles(new FileNameFilterImpl("", "log", "mat"));
		System.out.println("#logs -> " + logs.length);
		Arrays.sort(logs);
		for (int i = 0; i < logs.length; i++) {
			String correct = logs[i].getName().substring(0, logs[i].getName().indexOf("."));
			
			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(logs[i]));
				String line = "";
				
				int TC = 0;	// total number of candidates
				int BC = 0;	// total number of better candidates
				int WC = 0;	// total number of worse candidates
				while((line = br.readLine()) != null) {
					if(line.startsWith("##"))
						continue;
					
					TC++;
					
					if(line.startsWith(correct))
						BC = TC;
						
					if(line.isEmpty()) {
						WC = TC - BC;
						break;
					}
				}
			} catch (FileNotFoundException e) {
				System.err.println("Error: file not found!");
				e.printStackTrace();
			} catch (IOException e) {
				System.err.println("Error reading from file!");
				e.printStackTrace();
			}
			finally {
				try {
					br.close();
				} catch (IOException e) {
					System.err.println("Error closing file reader!");
					e.printStackTrace();
				}
			}
		}
	}

}
