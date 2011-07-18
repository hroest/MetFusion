/**
 * created by Michael Gerlich, Nov 8, 2010 - 12:47:12 PM
 * 
 * Utility class providing a batch mode for console to MetFusion functionality.
 */ 

package de.ipbhalle.metfusion.main;

import java.io.File;

public class MetFusionBatchMode {

	private final String os = System.getProperty("os.name");
	private final String fileSeparator = System.getProperty("file.separator");
	private final String userHome = System.getProperty("user.home");
	private final String currentDir = System.getProperty("user.dir");
	private final String lineSeparator = System.getProperty("line.separator");
	private final String fileEncoding = System.getProperty("file.encoding");
	
	private boolean doneCheck = false;
	private boolean doneSetup = false;
	
	
	public MetFusionBatchMode(String[] args) {
		
		this.doneSetup = setup();
		this.doneCheck = checkArguments(args);
	}
	
	/**
	 * Setup the current path environment to write result files.
	 * 
	 * @return <p><b>true</b> if the current directory where this class is being run is a directory
	 * and it is read-/writable.
	 * <p><b>false</b> if the current directory or path is not useable. Fallback to tmp dir.
	 */
	private boolean setup() {
		boolean success = false;
		File dir = new File(currentDir);
		if(dir.canRead() && dir.canWrite() && dir.isDirectory())
			success = true;
		return success;
	}
	
	private boolean checkArguments(String[] args) {
		boolean success = false;
		
		
		return success;
	}
	
	/**
	 * Provide arguments in the following way:
	 * 
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		MetFusionBatchMode mfbm = new MetFusionBatchMode(args);
		
		

	}

}
