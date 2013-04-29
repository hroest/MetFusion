/**
 * created by Michael Gerlich, Oct 4, 2011 - 2:09:22 PM
 */ 

package de.ipbhalle.metfusion.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openscience.cdk.ChemFile;
import org.openscience.cdk.ChemObject;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

import de.ipbhalle.enumerations.AvailableParameters;


public class MetFusionBatchFileHandler {

	private File batchFile;
	private MetFusionBatchSettings batchSettings;
	
	private final String prefix = "#";
	private final String separator = ":";
	private final String DEFAULT_SEPARATOR = " ";
	
	public MetFusionBatchFileHandler(File file) {
		this.batchFile = file;
	}
	
	@SuppressWarnings("unchecked")
	public void readFile() throws IOException {
		if(batchFile.exists() && batchFile.isFile() && batchFile.canRead())
			System.out.println("Found file [" + batchFile.getAbsolutePath() + "]");
		else {
			String message = "Error opening file [" + batchFile.getAbsolutePath() + "] - aborting!";
			System.err.println(message);
			throw new IOException(message);
		}
		
		batchSettings = new MetFusionBatchSettings();
		Map<AvailableParameters, Object> settings = new HashMap<AvailableParameters, Object>();
		BufferedReader br = new BufferedReader(new FileReader(batchFile));
		String line = "", peaks = "";
		AvailableParameters[] ap = AvailableParameters.values();
		List<String> params = new ArrayList<String>();
		for (int i = 0; i < ap.length; i++) {
			params.add(ap[i].toString());
		}
		while((line = br.readLine()) != null) {
			if(line.startsWith(prefix)) {		// found parameter
				int position = line.indexOf(separator);	// find position of separator (after which parameter value follows)
				String param = line.substring(1, position).trim();	// get name of parameter
				if(!params.contains(param)) {		// skip unknown property
					System.err.println("Unknown property [" + param + "]. Ignoring it!");
					continue;
				}
				AvailableParameters av = AvailableParameters.valueOf(param);	// check for parameter name in enumeration of available parameters
				Object value = line.substring(position+1).trim();	// get value of parameter
				if(av.equals(AvailableParameters.substrucAbsent) | av.equals(AvailableParameters.substrucPresent)) {	// one of the list/multiple items
					List<String> l = null;
					if(settings.containsKey(av)) {		// update list
						l = (List<String>) settings.get(av);
						if(!l.contains((String) value))
							l.add((String) value);
					}
					else {								// create new list
						l = new ArrayList<String>();
						if(!((String) value).isEmpty())	// only store item if not empty
							l.add((String) value);
					}
					settings.put(av, l);				// store list
				}
				else settings.put(av, value);
			}
			else if(!line.isEmpty()) {	// assume peaklist
				peaks += line.trim() + "\n";
			}
		}
		br.close();
		String normalizedPeaks = normalizePeaks(peaks.trim());
		settings.put(AvailableParameters.peaks, normalizedPeaks);
		
		batchSettings.loadSettingsFromFile(settings);
	}
	
	private String normalizePeaks(String peaks) {
		double maxInt = 0d;
		String[] lines = peaks.split("\n");	// each m/z int pair one line
		String[] mz = new String[lines.length];
		String[] ints = new String[lines.length];
		boolean onlyMZ = false;
		for (int i = 0; i < lines.length; i++) {
			lines[i] = lines[i].replaceAll("[ \t\\x0B\f\r]+", DEFAULT_SEPARATOR);	// replace each white space by default one
			String[] split = lines[i].split(DEFAULT_SEPARATOR);
			if(split.length == 1) {		// only m/z, set relative intensity to 500
				mz[i] = split[0];
				ints[i] = "500";
				onlyMZ = true;
			}
			else if(split.length == 2) {		// m/z intensity pair
				double intensity = Double.parseDouble(split[1]);
				if(intensity > maxInt)
					maxInt = intensity;
				mz[i] = split[0];
				ints[i] = split[1];
				onlyMZ = false;
			}
			else if(split.length == 3) {// m/z intensity rel.intensity triple
				double intensity = Double.parseDouble(split[2]);
				if(intensity > maxInt)
					maxInt = intensity;
				mz[i] = split[0];
				ints[i] = split[2];
				onlyMZ = false;
			}
			else {	// unknown split - assume length 3 split
				System.err.println("Unusual size of split!");
				mz[i] = split[0];
				ints[i] = split[2];
				onlyMZ = false;
			}
		}
		if(onlyMZ)
			maxInt = 500;
		
		StringBuilder sb = new StringBuilder();
		//calculate the rel. intensity
		for (int i = 0; i < lines.length; i++) {
			sb.append(mz[i]).append(DEFAULT_SEPARATOR).append(Math.round(((Double.valueOf(ints[i]) / maxInt) * 999))).append("\n");
		}
		
		return sb.toString();
	}
	
	public void printSettings() {
		System.out.println("Settings for file [" + batchFile + "]");
		if(batchSettings != null) {
			Map<AvailableParameters, Object> settings = batchSettings.transferSettings();
			Set<AvailableParameters> keys = settings.keySet();
			for (AvailableParameters key : keys) {
				Object o = settings.get(key);
				if(o instanceof List) {
					@SuppressWarnings("unchecked")
					List<String> l = (List<String>) o;
					if(l.isEmpty()) 
						System.out.println("[" + key + "] -> ");
					else {
						for (String s : l) {
							System.out.println("[" + key + "] -> " + s);
						}
					}
				}
				else System.out.println("[" + key + "] -> " + o);
			}
		}
		else System.err.println("Empty settings!");
	}
	
	public boolean writeFile(File output, MetFusionBatchSettings settings) {
		boolean success = false;
		Map<AvailableParameters, Object> currentSettings = settings.transferSettings();
		Set<AvailableParameters> parameters = currentSettings.keySet();
		FileWriter fw = null;
		try {
			fw = new FileWriter(output);
		} catch (IOException e) {
			System.err.println("Error creating new batch file [" + output.getAbsolutePath() + "] - Aborting!");
			return success;
		}
		
		boolean peaksAvailable = Boolean.FALSE;
		for (AvailableParameters param : parameters) {
			if(param.equals(AvailableParameters.peaks)) {	// ensure that peak list is written last
				peaksAvailable = Boolean.TRUE;
				continue;
			}
			if(param.equals(AvailableParameters.substrucAbsent) | param.equals(AvailableParameters.substrucPresent)) {	// one of the list/multiple items
				@SuppressWarnings("unchecked")
				List<String> list = (List<String>) currentSettings.get(param);
				if(list == null)
					continue;
				
				for (String string : list) {
					try {
						fw.write(formatSetting(param, string));
					} catch (IOException e) {
						System.err.println("Error writing batch file [" + output.getAbsolutePath() + "]!");
						return success;
					}
				}
			}
			else {
				try {
					fw.write(formatSetting(param, currentSettings.get(param)));
				} catch (IOException e) {
					System.err.println("Error writing batch file [" + output.getAbsolutePath() + "]!");
					return success;
				}
			}
		}
		
		if(peaksAvailable) {
			try {
				fw.write(formatSetting(AvailableParameters.peaks, currentSettings.get(AvailableParameters.peaks)));
			} catch (IOException e) {
				System.err.println("Error writing peaks into batch file [" + output.getAbsolutePath() + "]!");
				return success;
			}		
		}
		
		try {
			fw.flush();
			fw.close();
		} catch (IOException e) {
			System.err.println("Error finishing batch file [" + output.getAbsolutePath() + "]!");
			return success;
		}
		success = true;
		return success;
	}
	
	private String formatSetting(AvailableParameters parameter, Object setting) {
		String formatted = prefix + DEFAULT_SEPARATOR + parameter + ":" + DEFAULT_SEPARATOR;
		if(parameter.equals(AvailableParameters.peaks))
			formatted += "\n" + setting + "\n";
		else formatted += setting + "\n";
		return formatted;
	}
	
	public List<IAtomContainer> consumeSDF(String path) {
		List<IAtomContainer> containersList = new ArrayList<IAtomContainer>();
		try {
			MDLV2000Reader reader = new MDLV2000Reader(new FileInputStream(new File(path)));
			ChemFile chemFile =  (ChemFile) reader.read((ChemObject) new ChemFile());
			containersList = ChemFileManipulator.getAllAtomContainers(chemFile);
		} catch (FileNotFoundException e) {
			System.err.println("File [" + path + "] not found! - returning null!");
			e.printStackTrace();
			return null;
		} catch (CDKException e) {
			System.err.println("CDK error while reading [" + path + "] - returning null!");
			e.printStackTrace();
			return null;
		}
		
		return containersList;
	}
	
	public static void main(String[] args) {
		//MetFusionBatchFileHandler mbfr = new MetFusionBatchFileHandler(new File("/home/mgerlich/Documents/metfusion_param_default.mf"));
		//MetFusionBatchFileHandler mbfr = new MetFusionBatchFileHandler(new File("/home/sneumann/CASMI/msbi.ipb-halle.de/contest/metfrag/metfrag-category2-challenge1.mf"));
		//MetFusionBatchFileHandler mbfr = new MetFusionBatchFileHandler(new File("/home/mgerlich/Downloads/skype_transfer/136m0498_MSMS.mf"));
		MetFusionBatchFileHandler mbfr = new MetFusionBatchFileHandler(
				new File("/home/mgerlich/projects/metfusion_tp/BTs/MetFusion_ChemSp_mfs/192m0757a_MSMS.mf"));
		
		try {
			mbfr.readFile();
		} catch (IOException e) {
			//System.err.println("Error while reading/accessing batch file [" + arg_mf + "]!");
			e.printStackTrace();
			//System.exit(-1);
		}

		mbfr.printSettings();
		
		try {
			//MDLV2000Reader reader = new MDLV2000Reader(new FileInputStream(new File("/home/mgerlich/Desktop/7_C7H5ClO2.sdf")));
			MDLV2000Reader reader = new MDLV2000Reader(new FileInputStream(new File("/home/mgerlich/Downloads/MetFragResults_1318338437763.sdf")));
			ChemFile chemFile =  (ChemFile) reader.read((ChemObject) new ChemFile());
			List<IAtomContainer> containersList = ChemFileManipulator.getAllAtomContainers(chemFile);
			for (IAtomContainer container: containersList) {
				System.out.println(container.getAtomCount());
				System.out.println("Mass -> " + container.getProperty("Mass"));
				Map<Object, Object> props = container.getProperties();
				Set<Object> keys = props.keySet();
				for (Object key : keys) {
					System.out.println("Key [" + key + "] -> " + props.get(key));
				}
			}
		} catch (FileNotFoundException e) {
			//System.err.println("Error while reading/accessing sdf file [" + arg_sdf + "]!");
			e.printStackTrace();
			//System.exit(-1);
		} catch (CDKException e) {
			//System.err.println("CDKError while processing sdf file [" + arg_sdf + "]!");
			e.printStackTrace();
			//System.exit(-1);
		}
	}

	public File getBatchFile() {
		return batchFile;
	}

	public void setBatchFile(File batchFile) {
		this.batchFile = batchFile;
	}

	public MetFusionBatchSettings getBatchSettings() {
		return batchSettings;
	}

	public void setBatchSettings(MetFusionBatchSettings batchSettings) {
		this.batchSettings = batchSettings;
	}
}

