/**
 * created by Michael Gerlich, Nov 19, 2013 - 10:40:34 AM

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;


import de.ipbhalle.enumerations.Adducts;
import de.ipbhalle.enumerations.Databases;
import de.ipbhalle.enumerations.Ionizations;
import de.ipbhalle.io.FileNameFilterImpl;
import de.ipbhalle.metfusion.main.MetFusionBatchFileHandler;
import de.ipbhalle.metfusion.main.MetFusionBatchSettings;

public class GenerateQueryFilesCRV {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String dirCRV_LIST2_N = "/home/mgerlich/projects/metfusion_tp/Unknowns_MetFusion/CRV_LIST2_N/";
		String dirCRV_LIST2_P = "/home/mgerlich/projects/metfusion_tp/Unknowns_MetFusion/CRV_LIST2_P/";
		String dirCRV_LIST3_N = "/home/mgerlich/projects/metfusion_tp/Unknowns_MetFusion/CRV_LIST3_N/";

		String subDirLogs = "logs";
		String subDirQueries = "queries";
		String subDirResults = "results";
		
		String suffixMS = "_MS.txt";
		String suffixMSMS = "_MSMS.txt";
		/*
		 * select working directory
		 */
		String selectedDir = dirCRV_LIST2_P;
		
		File[] filesMS = new File(selectedDir).listFiles(new FileNameFilterImpl("", suffixMS));
		File[] filesMSMS = new File(selectedDir).listFiles(new FileNameFilterImpl("", suffixMSMS));
		File f = new File(selectedDir, subDirLogs);
		f.mkdir();
		f = new File(selectedDir, subDirQueries);
		File outdir = f;	// define outputfolder
		f.mkdir();
		f = new File(selectedDir, subDirResults);
		f.mkdir();
		
		Arrays.sort(filesMS);
		Arrays.sort(filesMSMS);
		
		Ionizations mbIonization = Ionizations.pos;
		Adducts mfAdduct = Adducts.Neutral;
		if(selectedDir.endsWith("_P/")) {
			mbIonization = Ionizations.pos;
			mfAdduct = Adducts.MplusHplus;
		}
		else if(selectedDir.endsWith("_N/")) {
			mbIonization = Ionizations.neg;
			mfAdduct = Adducts.MminusHminus;
		}
		else {
			System.err.println("Path doesn't end with P or N - need information regarding ionization mode!");
			System.exit(-1);
		}
		
		for (int i = 0; i < filesMS.length; i++) {	// fetch precursor mass from MS run
			String filename = filesMSMS[i].getName();
			filename = filename.substring(0, filename.indexOf("."));
			
			String line = "";
			double emass = 0.0d;
			try {
				BufferedReader br = new BufferedReader(new FileReader(filesMS[i]));
				line = br.readLine().trim();	// precursor always first line in MS file
				String[] split = line.split("\t");
				emass = Double.parseDouble(split[0]);
				br.close();
			} catch (FileNotFoundException e) {
				System.err.println("File [" + filesMS[i].getAbsolutePath() + "] not found!");
				continue;
			} catch (IOException e) {
				System.err.println("Error reading from file [" + filesMS[i].getAbsolutePath() + "]");
				continue;
			}
			
			File msms = filesMSMS[i];
			if(!filesMSMS[i].getName().startsWith(filename)) {	// find correct MSMS file
				for (int j = 0; j < filesMSMS.length; j++) {
					if(filesMSMS[j].getName().startsWith(filename)) {
						msms = filesMSMS[j];
						break;
					}
				}
			}
			String peaks = "";
			StringBuilder sb = new StringBuilder();
			try {
				BufferedReader br = new BufferedReader(new FileReader(msms));
				while((line = br.readLine()) != null) {
					String[] split = line.trim().split("\\s");
					sb.append(split[0]).append("\t").append(split[split.length-1]).append("\n");
				}
				br.close();
			} catch (FileNotFoundException e) {
				System.err.println("File [" + msms.getAbsolutePath() + "] not found!");
				continue;
			} catch (IOException e) {
				System.err.println("Error reading from file [" + filesMS[i].getAbsolutePath() + "]");
				continue;
			}
			
			peaks = sb.toString().trim();
			MetFusionBatchSettings settings = new MetFusionBatchSettings();
			settings.setMfSearchPPM(5.0d);
			settings.setMfMZabs(0.001d);
			settings.setMfMZppm(5.0d);
			settings.setPeaks(peaks);
			settings.setMbIonization(mbIonization);
			settings.setMfAdduct(mfAdduct);
			settings.setMfParentIon(emass);
			settings.setMfExactMass(emass);
			settings.setMfDatabase(Databases.chemspider);
			settings.setOnlyCHNOPS(false);		// get eerything
			File output = new File(outdir, filename + ".mf");
			MetFusionBatchFileHandler mfbh = new MetFusionBatchFileHandler(output);
			mfbh.writeFile(output, settings);
		}
	}

}
