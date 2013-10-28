/**
 * created by Michael Gerlich, Oct 22, 2013 - 4:09:56 PM

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
import java.util.HashMap;
import java.util.Map;

import de.ipbhalle.enumerations.Adducts;
import de.ipbhalle.enumerations.Databases;
import de.ipbhalle.enumerations.Ionizations;
import de.ipbhalle.io.FileNameFilterImpl;
import de.ipbhalle.metfusion.main.MetFusionBatchFileHandler;
import de.ipbhalle.metfusion.main.MetFusionBatchSettings;

public class GenerateQueryFiles {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String dir = "/home/mgerlich/projects/metfusion_tp/Unknowns_MetFusion/Top30_P_allSamples/";
		String outdir = "/home/mgerlich/projects/metfusion_tp/Unknowns_MetFusion/Top30_P_allSamples/queries/"; 
		String _HCD = "HCD";
		String _CID = "CID";
		String _MF = ".mf";
		String _TXT = ".txt";
		String _filter = _HCD;
		String _pos = "_P_";
		String _neg = "_N_";
		
		// reference table with entry to mass
		Map<String,String> table = readInfoTable("/home/mgerlich/projects/metfusion_tp/Unknowns_MetFusion/Top30_P.txt");
		// filter files
		File[] list = new File(dir).listFiles(new FileNameFilterImpl("", "txt"));
		System.out.println("#list -> " + list.length);
		for (int i = 0; i < list.length; i++) {
			System.out.println(list[i]);
			File output = new File(outdir, list[i].getName().replace(_TXT, _MF));
			MetFusionBatchFileHandler mbfh = new MetFusionBatchFileHandler(output);
			MetFusionBatchSettings settings = new MetFusionBatchSettings();
			String filename = list[i].getName();
			
			// fetch peak information
			String peaks = readPeaklist(list[i].getAbsolutePath());
			
			// ZWI_N_mz158_98_rt1_72_CID35_Top30_N05.txt
			filename = filename.replace(_TXT, "");	// remove trailing .txt
			String[] split = filename.split("_");
			/** [0] - sample name
			 *  [1] - ionization mode
			 *  [2] - nominal mass
			 *  [3] - two decimal places mass
			 *  [4] - nominal retention time
			 *  [5] - two decimal places rt
			 *  [6] - CID35 or HCD60
			 *  [7] - Top30 info table
			 *  [8] - entry in info table
			 */
			String entry = split[8];
			String mass = table.get(entry);
			// update settings
			if(filename.contains(_pos)) {
				settings.setMbIonization(Ionizations.pos);
				settings.setMfAdduct(Adducts.MplusHplus);
			}
			else if(filename.contains(_neg)) {
				settings.setMbIonization(Ionizations.neg);
				settings.setMfAdduct(Adducts.MminusHminus);
			}
			else {
				settings.setMbIonization(Ionizations.pos);
				settings.setMfAdduct(Adducts.Neutral);
			}
			
			settings.setMbInstruments("CE-ESI-TOF,ESI-ITFT,ESI-QIT,ESI-QQ,LC-ESI-IT,LC-ESI-ITFT," +
					"LC-ESI-ITTOF,LC-ESI-Q,LC-ESI-QIT,LC-ESI-QQ,LC-ESI-QTOF,LC-ESI-TOF," +
					"APCI-ITFT,LC-APCI-QTOF,LC-APCI-ITFT,LC-APCI-Q,LC-APPI-QQ,LC-APCI-QTOF,ESI-FTICR,HPLC-ESI-TOF,UPLC-ESI-QTOF");
			settings.setMfDatabase(Databases.chemspider);	// set chemspider as default DB
			settings.setPeaks(peaks);			// store peaks
			settings.setOnlyCHNOPS(false);		// set CHNOPS to false
			settings.setMfSearchPPM(5.0d);		// searchPPM
			settings.setMfMZppm(10.0d);			// mzppm
			settings.setMfParentIon(Double.parseDouble(mass));
			settings.setMfExactMass(Double.parseDouble(mass));	// exact mass is correctly calculated in MetFragBatchMode
			settings.setMbCutoff(1);	// set cutoff down to 1
			// HCD specific settings
			//if(_filter.equals(_HCD)) {
			if(filename.contains(_HCD)) {
				settings.setMfMZabs(0.001d);
			}
			// CID specific settings
			//else if(_filter.equals(_CID)) {
			else if(filename.contains(_CID)) {
				settings.setMfMZabs(0.2d);
			}
			
			// set settings
			mbfh.setBatchSettings(settings);
			mbfh.writeFile(output, settings);
		}
	}

	public static String readPeaklist(String file) {
		String peaks = "";
		File f = new File(file);
		try {
			BufferedReader br = new BufferedReader(new FileReader(f));
			String line = "";
			StringBuilder sb = new StringBuilder();
			while((line = br.readLine()) != null) {
				sb.append(line).append("\n");
			}
			br.close();
			
			peaks = sb.toString();
		} catch (FileNotFoundException e) {
			System.err.println("File [" + f.getAbsolutePath() + "] does not exist, aborting!");
			System.exit(-1);
		} catch (IOException e) {
			System.err.println("Error File [" + f.getAbsolutePath() + "] does not exist, aborting!");
			System.exit(-1);
		}
		return peaks;
	}
	
	public static Map<String,String> readInfoTable(String file) {
		File f = new File(file);
		Map<String,String> table = new HashMap<String, String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(f));
			String line = "", mass = "", name = "";
			br.readLine();	// skip header
			// mz RT Top30_Name ...
			while((line = br.readLine()) != null) {
				String[] split = line.split("\t");
				mass = split[0];
				name = split[2];	// "Top30_N01"
				if(name.contains("_")) {
					name = name.split("_")[1];		// discard "Top30"
				}
				table.put(name, mass);
			}
			br.close();
		} catch (FileNotFoundException e) {
			System.err.println("File [" + f.getAbsolutePath() + "] does not exist, aborting!");
			System.exit(-1);
		} catch (IOException e) {
			System.err.println("Error File [" + f.getAbsolutePath() + "] does not exist, aborting!");
			System.exit(-1);
		}
		return table;
	}
}
