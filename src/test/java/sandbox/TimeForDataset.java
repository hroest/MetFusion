/**
 * created by Michael Gerlich, Sep 6, 2012 - 12:47:58 PM
 */ 

package sandbox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

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
		String logDir = "/home/mgerlich/SGE/output";
		File dir = new File(logDir);
		File[] list = dir.listFiles(new FileNameFilterImpl("KEGG", ""));
		
		FileWriter resultFile = new FileWriter(new File("/home/mgerlich/projects/metfusion_paper/runtime_kegg.txt"));
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
			
			// get fragmenter results
			if(!gotFragmenterResults & line.startsWith("#") & line.contains("results") & line.contains("MetFrag")) {
				fragmenterResults = line.substring(line.indexOf(":") + 1).trim();
				gotFragmenterResults = true;
			}
			
			// get runtime
			if(!gotRuntime & line.contains("time spent")) {
				runtime = line.substring(line.indexOf(">") +2, line.lastIndexOf("s")).trim();
				gotRuntime = true;
			}
		}
		br.close();
		
		String[] info = {acc, dbResults, fragmenterResults, runtime};
		return info;
	}
}
