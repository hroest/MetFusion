/**
 * created by Michael Gerlich, Nov 16, 2010 - 12:47:54 PM
 */ 

package de.ipbhalle.metfusion.threading;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.ipbhalle.metfrag.tools.renderer.StructureToFile;
import de.ipbhalle.metfusion.wrapper.Result;
import de.ipbhalle.metfusion.wrapper.ResultExt;

public class ImageGeneratorThread extends Thread {

	private List<? extends Result> compounds;
	private String outputFolder;
	private String tempPath;
	private Map<String, String> idToPath;
	public final String DEFAULT_ENDING = ".png";
	private boolean done = Boolean.FALSE;
	
	/**
	 * Constructor allowing List of Result (upper bound) or any subclass.
	 *
	 * @param compounds the list of compounds
	 * @param outputFolder the output folder
	 * @param tempPath the temp path
	 */
	public ImageGeneratorThread(List<? extends Result> compounds, String outputFolder, String tempPath) {
		this.compounds = compounds;
		this.outputFolder = outputFolder;
		this.tempPath = tempPath;
		this.idToPath = new HashMap<String, String>();
	}
	
	@Override
	public void run() {
		this.done = Boolean.FALSE;
		System.out.println("Start generating compound pictures!");
		
		StructureToFile stf = null;
		try {
			stf = new StructureToFile(200, 200, getOutputFolder(), false, false);
		} catch (Exception e) {
			System.err.println("Error creating StructureToFile object! - Aborting...");
			e.printStackTrace();
			return;
		}
		
		for (Object obj : this.compounds) {
			Result r = null;
			if(obj instanceof ResultExt) {
				r = (ResultExt) obj;
			}
			else if (obj instanceof Result) {
				r = (Result) obj;
			}
			else {
				System.err.println("Object not of required class!");
				System.err.println("found [" + obj.getClass() + "], but need [de.ipbhalle.metfusion.wrapper.Result] or extending class!");
				continue;
			}
			
			String filename = r.getId() + DEFAULT_ENDING;
			File image = new File(getOutputFolder(), filename);
			String path = getTempPath() + filename;
			if(stf != null && !image.exists()) {
				try {
					stf.writeMOL2PNGFile(r.getMol(), filename);
					r.setImagePath(path);
					idToPath.put(r.getId(), path);
				} catch (Exception e) {
					System.err.println("Exception occured for [" + filename + "] while generating compound image!");
					e.printStackTrace();
				}
			}
		}
		
		System.out.println("Finished generating compound pictures!");
		this.done = Boolean.TRUE;
	}
	
	public void setCompounds(List<? extends Result> compounds) {
		this.compounds = compounds;
	}

	public List<? extends Result> getCompounds() {
		return compounds;
	}

	public void setOutputFolder(String outputFolder) {
		this.outputFolder = outputFolder;
	}

	public String getOutputFolder() {
		return outputFolder;
	}

	public String getTempPath() {
		return tempPath;
	}

	public void setTempPath(String tempPath) {
		this.tempPath = tempPath;
	}

	public Map<String, String> getIdToPath() {
		return idToPath;
	}

	public void setIdToPath(Map<String, String> idToPath) {
		this.idToPath = idToPath;
	}

	public void setDone(boolean done) {
		this.done = done;
	}

	public boolean isDone() {
		return done;
	}
	
}
