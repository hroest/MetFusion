/**
 * created by Michael Gerlich, Sep 6, 2012 - 12:47:58 PM
 */ 

package sandbox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import de.ipbhalle.io.FileNameFilterImpl;

/**
 * Class which fetches the runtime from the log files of a run.
 * Therefore, *.log files are REQUIRED.
 * 
 * @author mgerlich
 *
 */
public class TimeForDataset {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		//String logDir = "/home/mgerlich/SGE/output";
		//String logDir = "/home/mgerlich/projects/metfusion_allspectra_withoutInChIKeyFilter/logs/";
//		String logDir = "/home/mgerlich/projects/metfusion_allspectra_withInChIKeyFilter/logs/";
		//String logDir = "/home/mgerlich/projects/metfusion_HMDB/results_dualSDFs_1H_afterHMDBfix_allMatchingSpectra/2ndrun/logs/";
		//String logDir = "/home/mgerlich/projects/metfusion_HMDB/results_dualSDFs_afterHMDBfix/check/logs/";
		String logDir = "/home/mgerlich/SGE/output/HMDB/";
		File dir = new File(logDir);
		//File[] list = dir.listFiles(new FileNameFilterImpl("KEGG", ""));
		//File[] list = dir.listFiles(new FileNameFilterImpl("", "log"));
		File[] list = dir.listFiles(new FileNameFilterImpl("HMDB_13C_MSMS_sge_metfusion.q.o5421", ""));
		Arrays.sort(list);
		
		//FileWriter resultFile = new FileWriter(new File("/home/mgerlich/projects/metfusion_paper/runtime_kegg.txt"));
		//FileWriter resultFile = new FileWriter(new File("/home/mgerlich/projects/metfusion_allspectra_withoutInChIKeyFilter/logs/runtime_nonunique.txt"));
//		FileWriter resultFile = new FileWriter(new File("/home/mgerlich/projects/metfusion_allspectra_withInChIKeyFilter/logs/runtime_unique.txt"));
		//FileWriter resultFile = new FileWriter(new File("/home/mgerlich/projects/metfusion_HMDB/results_dualSDFs_1H_afterHMDBfix_allMatchingSpectra/2ndrun/logs/runtime_1H_allmatching.txt"));
		//FileWriter resultFile = new FileWriter(new File("/home/mgerlich/projects/metfusion_HMDB/results_dualSDFs_afterHMDBfix/check/logs/runtime_13C.txt"));
		FileWriter resultFile = new FileWriter(new File("/home/mgerlich/projects/metfusion_HMDB/runtime_all13C.txt"));
		
		// write header
		resultFile.write("Accession\tMassBank\tMetFrag\tRuntime (sec)\n");
		
		for (int i = 0; i < list.length; i++) {
			try {
				String[] info = getLogInfo(list[i]);
				resultFile.write(info[0] + "\t" + info[1] + "\t" + info[2] + "\t" + info[3] + "\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		resultFile.flush();
		resultFile.close();
	}

	/**
	 * [0] - Accession
	 * [1] - #MassBank results
	 * [2] - #MetFrag results
	 * [3] - runtime
	 * @param f
	 * @return
	 * @throws IOException 
	 */
	private static String[] getLogInfo(File f) throws IOException {
		String line = "";
		String acc = "";
		String dbResults = "";
		String fragmenterResults = "";
		String runtime = "";
		boolean gotAcc = false, gotDBResults = false, gotFragmenterResults = false, gotRuntime = false;
		
		BufferedReader br = new BufferedReader(new FileReader(f));
		while((line = br.readLine()) != null) {
			// retrieve Accession
			if(!gotAcc & line.contains(".txt")) {
				String[] split = line.split(".txt");
				acc = split[0].substring(split[0].lastIndexOf("/") + 1).trim();
				gotAcc = true;
			}
			
			// get database results
			if(!gotDBResults & line.startsWith("#") & line.contains("results") & !line.contains("MetFrag")) {
				dbResults = line.substring(line.indexOf(":") + 1).trim();
				gotDBResults = true;
			}
			else if(!gotDBResults & line.contains("Database results.")) {
				dbResults = line.substring(line.indexOf("[") + 1, line.indexOf("]"));
				gotDBResults = true;
			}
			
			// get fragmenter results
			if(!gotFragmenterResults & line.startsWith("#") & line.contains("results") & line.contains("MetFrag")) {
				fragmenterResults = line.substring(line.indexOf(":") + 1).trim();
				gotFragmenterResults = true;
			}
			else if(!gotFragmenterResults & line.contains("Fragmenter results.")) {
				fragmenterResults = line.substring(line.indexOf("[") + 1, line.indexOf("]"));
				gotFragmenterResults = true;
			}
			
			// get runtime
			if(!gotRuntime & line.contains("time spent")) {
				runtime = line.substring(line.indexOf(">") +2, line.lastIndexOf("s")).trim();
				gotRuntime = true;
			}
		}
		br.close();
		
		if(!gotAcc) {
			acc = f.getName();
			acc = acc.substring(0, acc.indexOf("."));
			gotAcc = true;
		}
		String[] info = {acc, dbResults, fragmenterResults, runtime};
		return info;
	}
}
