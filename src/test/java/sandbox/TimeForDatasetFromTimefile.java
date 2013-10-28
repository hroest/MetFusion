/**
 * created by Michael Gerlich, Sep 16, 2013 - 5:16:48 PM

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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import de.ipbhalle.io.FileNameFilterImpl;

public class TimeForDatasetFromTimefile {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		//String logDir = "/home/mgerlich/projects/metfusion_HMDB/results_dualSDFs_1H_afterHMDBfix_allMatchingSpectra/2ndrun/logs/";
		//String logDir = "/home/mgerlich/projects/metfusion_HMDB/results_dualSDFs_afterHMDBfix/check/logs/";
		//String logDir ="/home/mgerlich/projects/metfusion_allspectra_withoutInChIKeyFilter/logs/";
		String logDir = "/home/mgerlich/projects/metfusion_allspectra_withInChIKeyFilter/logs/";
		File dir = new File(logDir);
		File[] list = dir.listFiles(new FileNameFilterImpl("", "time"));
		Arrays.sort(list);
		System.out.println("Found [" + list.length + "] files.");
		
		FileWriter resultFile = new FileWriter(new File(logDir, "runtime.info"));
		// write header
		resultFile.write("Accession\tRuntime (sec)\n");

		for (int i = 0; i < list.length; i++) {
			try {
				String[] info = getLogInfo(list[i]);
				resultFile.write(info[0] + "\t" + info[1] + "\n");
			} catch (IOException e) {
				System.err.println("Error writing time information for [" + list[i].getAbsolutePath() + "].");
			}
		}

		resultFile.flush();
		resultFile.close();
		System.out.println("done");
	}

	/**
	 * [0] - Accession
	 * [1] - runtime
	 * @param f
	 * @return
	 * @throws IOException 
	 */
	private static String[] getLogInfo(File f) throws IOException {
		String line = "";
		String acc = "";
		String runtime = "";
		
		BufferedReader br = new BufferedReader(new FileReader(f));
		while((line = br.readLine()) != null) {
			String[] split = line.trim().split("\\s");
			if(split.length == 2) {		// sys time + user time in seconds
				float time = Float.parseFloat(split[0]) + Float.parseFloat(split[1]);
				runtime = String.valueOf(time);
			}
		}
		br.close();
		
		// get accession from filename
		acc = f.getName();
		acc = acc.substring(0, acc.indexOf("."));
		
		String[] info = {acc, runtime};
		return info;
	}
}
