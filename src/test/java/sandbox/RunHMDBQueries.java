/**
 * created by Michael Gerlich, Nov 18, 2013 - 4:15:22 PM

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

package sandbox;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import de.ipbhalle.enumerations.Ionizations;
import de.ipbhalle.io.FileNameFilterImpl;
import de.ipbhalle.metfusion.main.MetFusionBatchFileHandler;
import de.ipbhalle.metfusion.main.MetFusionBatchSettings;
import de.ipbhalle.metfusion.utilities.output.SDFOutputHandler;
import de.ipbhalle.metfusion.web.controller.HMDBBean;
import de.ipbhalle.metfusion.web.controller.HMDBBean.searchType;
import de.ipbhalle.metfusion.wrapper.Result;

public class RunHMDBQueries {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) {
		// input file
		// output dir
		// mode -> searchType enum
		// submode -> 1H, 13C, HSQC, TOCSY
		// tolerance
		
		if(args == null || args.length == 0) {
			System.err.println("Missing arguments: input file, output dir, searchType mode, [submode], [tolerance]");
			System.exit(-1);
		}
		
		String file = args[0];
		File inputFile = new File(file);
		String outputDir = args[1];
		searchType type = HMDBBean.searchType.valueOf(args[2]);
		
		String mode = "", tolerance = "";
		if(args.length >= 4) {
			mode = args[3];
		}
		if(args.length >= 5) {
			tolerance = args[4];
		}
		
		String dir = "/home/mgerlich/Downloads/HMDB/HMDB_filtered_run/MSMS/";
		File[] files = new File(dir).listFiles(new FileNameFilterImpl("", "mf"));
		for (int i = 0; i < files.length; i++) {
			// check if present
			String check = files[i].getName();
			check = check.replace(".mf", ".sdf");
			File fcheck = new File(outputDir, check);
			if(fcheck.exists())
				continue;
			//
			
			MetFusionBatchFileHandler mfbh = new MetFusionBatchFileHandler(files[i]);
			try {
				mfbh.readFile();
			} catch (IOException e) {
				System.err.println("Error reading settings file [" + inputFile + "]");
				continue;
			}
			MetFusionBatchSettings settings = mfbh.getBatchSettings();
			String peaks = settings.getPeaks();
			String[] split = peaks.split("\n");
			StringBuilder sb = new StringBuilder();
			for (int j = 0; j < split.length; j++) {
				String[] splitPeak = split[j].trim().split(" ");
				if(splitPeak.length == 2)
					sb.append(splitPeak[0]).append("\n");
			}
			int queryPeaks = split.length;	// number of peaks in query spectrum
			HMDBBean hb = new HMDBBean();
			if(type.equals(searchType.NMR1D)) {
				if(!mode.equals("C") && !mode.equals("H")) {
					System.err.println("Wrong mode, either H or C for 1D NMR!");
					System.exit(-1);
				}
				hb.setSelectedLibNMR1D(mode);
				hb.setToleranceNMR1D(0.02f);
				hb.setPeaksNMR1D(sb.toString());
			}
			else if(type.equals(searchType.NMR2D)) {
				hb.setSelectedLibNMR1D(mode);
				hb.setToleranceNMR1D(0.02f);
				hb.setPeaksNMR1D(sb.toString());
			}
			else if(type.equals(searchType.MSMS)) {
				hb.setPrecursorMSMS((float) settings.getMfExactMass());
				hb.setToleranceMSMSprecursor(0.1f);
				
				Ionizations ion = settings.getMbIonization();
				if(ion.equals(Ionizations.pos))
					hb.setModeMSMS("Positive");
				else if(ion.equals(Ionizations.neg))
					hb.setModeMSMS("Negative");
				else hb.setModeMSMS("");
				
				hb.setCeMSMS("");	// empty entry == All, HMDBBean.msmsCE.Low.toString()
				
				hb.setPeaksMSMS(peaks);
				
				hb.setToleranceMSMSmz(0.5f);
			}
			
			// run HMDB query
			List<Result> results = hb.performQuery(type);
			
			boolean gotResults = false;
			if(results.size() > 0)
				gotResults = true;
			
			if(gotResults) {
//				System.out.println("Found [" + results.size() + "] results");
//				System.out.println("ID\tName\tFormula\tWeight\tScore");
				for (Result result : results) {
					System.out.println(result.getId() + "\t" + result.getName() + "\t" + result.getSumFormula() + 
							"\t" + result.getExactMass() + "\t" + result.getScore());
					
					result.getMol().setProperty("numQueryPeaks", queryPeaks);
					result.getMol().setProperty("numMatchingPeaks", result.getMatchingPeaks());
					result.getMol().setProperty("numDatabasePeaks", result.getTiedRank());
				}
				
				String filename = files[i].getName();
				filename = filename.replace(".mf", ".sdf");
				SDFOutputHandler oh = new SDFOutputHandler(outputDir + filename);
				oh.writeOriginalResults(results, false);
				
				// store missing entries due to wrongly matched InChIKeys at HMDB
				List<String> missing = hb.getMissing();
				if(!missing.isEmpty()) {
					filename = files[i].getName();
					filename = filename.replace(".mf", ".missing");
					
					FileWriter fw;
					try {
						fw = new FileWriter(new File(outputDir, filename));
						for (String miss : missing) {
							fw.write(miss + "\n");
						}
						fw.close();
					} catch (IOException e) {
						System.err.println("Error writing to [" + filename + "]");
					}
				}
			}
			else {
				System.err.println("No results for [" + inputFile.getAbsolutePath() + "].");
			}
			
			try {
				System.out.println("Waiting for 5 seconds...");
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				System.err.println("Error interupting thread.");
			}
		}
	}
}
