/**
 * created by Michael Gerlich, Apr 25, 2013 - 3:09:01 PM

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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.ipbhalle.io.FileNameFilterImpl;
import de.ipbhalle.metfusion.main.MetFusionBatchFileHandler;
import de.ipbhalle.metfusion.main.MetFusionBatchSettings;
import de.ipbhalle.metfusion.main.SubstructureSearch;
import de.ipbhalle.metfusion.utilities.output.SDFOutputHandler;
import de.ipbhalle.metfusion.wrapper.Result;
import de.ipbhalle.metfusion.wrapper.ResultSubstructure;

public class RunSubstructureSearch {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String token = "eeca1d0f-4c03-4d81-aa96-328cdccf171a";
		//String mfDir = "/home/mgerlich/projects/metfusion_tp/BTs/Unknown_BT_MSMS_ChemSp/mf_with_substruct/";
		if(args == null || args.length == 0) {	// specified path, formula and SDF, but no path to SDF
			System.out.println("updated query and SD files are stored in subfolder [output]");
			System.err.println("Provide path to mf files for ChemSpider Substructure Search! Aborting.");
			System.out.println("list of arguments:\n\t[0] -> path to mf files [REQUIRED]" );
			System.out.println("\t[1] -> use formula as query rather than first substrucPresent [OPTIONAL], values {true,false}, default is true");
			System.out.println("\t[2] -> use SDF file for substructure search [OPTIONAL], values {true,false}, default is false");
			System.out.println("\t[3] -> path to SDF file [REQUIRED] if [2] = true");
			System.exit(-1);
		}
		
		/**
		 * [0] path to mf files
		 * [1] use formula as query rather than first substrucPresent
		 * [2] use SDF as input
		 * [3] path to SDF
		 */
		String mfDir = args[0];
		boolean useFormulaAsQuery = true;
		boolean useSDFFile = false;
		String sdfFile = "";
		if(args.length == 2)
			useFormulaAsQuery = Boolean.parseBoolean(args[1]);
		if(args.length == 3) {
			useSDFFile = Boolean.parseBoolean(args[2]);
			if(useSDFFile) {
				System.err.println("SDF use enabled, but no path to SDF file specified! Aborting.");
				System.out.println("list of arguments:\n\t[0] -> path to mf files [REQUIRED]" );
				System.out.println("\t[1] -> use formula as query rather than first substrucPresent [OPTIONAL], values {true,false}, default is true");
				System.out.println("\t[2] -> use SDF file for substructure search [OPTIONAL], values {true,false}, default is false");
				System.out.println("\t[3] -> path to SDF file [REQUIRED] if [2] = true");
				System.exit(-1);
			}
			else {
				System.err.println("No SDF file specified, running online query!");
				useSDFFile = false;
			}
		}
		if(args.length == 4) {
			useSDFFile = Boolean.parseBoolean(args[2]);
			sdfFile = args[3];
			if(useSDFFile & sdfFile.isEmpty()) {
				System.err.println("No SDF file specified, running online query!");
				useSDFFile = false;
			}
		}
		
		File[] mfFiles = new File(mfDir).listFiles(new FileNameFilterImpl("", "mf"));
		Arrays.sort(mfFiles);
		for (int i = 0; i < mfFiles.length; i++) {
			File file = mfFiles[i];
			System.out.println("Running "  + file.getName());
			
			MetFusionBatchFileHandler mbf = new MetFusionBatchFileHandler(file);
			try {
				mbf.readFile();
			} catch (IOException e) {
				System.err.println("Error reading from MetFusion settings file [" + file.getAbsolutePath() + "]. Skipping!");
				//System.exit(-1);
				continue;
			}
			
			MetFusionBatchSettings settings = mbf.getBatchSettings();
			List<String> absent = settings.getSubstrucAbsent();
			List<String> present = settings.getSubstrucPresent();
			for (String s : present) {
				System.out.println("present -> " + s);
			}
			for (String s : absent) {
				System.out.println("absent -> " + s);
			}
			String formula = settings.getMfFormula();
			System.out.println("formula -> " + formula);
			
			if(absent.isEmpty() && present.isEmpty()) {
				System.err.println("Neither includes nor excludes specified for [" + mbf.getBatchFile().getAbsolutePath() + "], skipping it.");
				continue;
			}
			
			SubstructureSearch ss = new SubstructureSearch(present, absent, token, formula, mbf, useFormulaAsQuery, useSDFFile, sdfFile);
			ss.run();
			List<ResultSubstructure> remaining = ss.getResultsRemaining();
			List<Result> resultsForSDF = new ArrayList<Result>();
			
			StringBuilder sb = new StringBuilder();
			String sep = ",";
			for (ResultSubstructure rs : remaining) {
				sb.append(rs.getId()).append(sep);
				
				Result r = new Result(rs.getPort(), rs.getId(), rs.getName(), rs.getScore());
				r.setMol(rs.getMol());
				r.setSmiles(rs.getSmiles());
				r.setInchi(rs.getInchi());
				r.setInchikey(rs.getInchikey());
				resultsForSDF.add(r);
			}
			String ids = sb.toString();
			
			File dir = new File(mfDir, "output");
			dir.mkdir();
			if(!ids.isEmpty()) {
				ids = ids.substring(0, ids.length()-1);
				System.out.println("ids -> " + ids);
				settings.setMfDatabaseIDs(ids);
				String filename = file.getName();
				String prefix = filename.substring(0, filename.lastIndexOf("."));
				filename = filename.replace(prefix, prefix + "_ids");
				File output = new File(dir, filename);
				mbf.writeFile(output, settings);
				
				SDFOutputHandler so = new SDFOutputHandler(dir.getAbsolutePath() + "/" + prefix + ".sdf");
				so.writeOriginalResults(resultsForSDF, false);
			}
		}

	}

}
