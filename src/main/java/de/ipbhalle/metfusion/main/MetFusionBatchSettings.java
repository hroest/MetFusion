/**
 * created by Michael Gerlich, Oct 4, 2011 - 3:07:06 PM
 */ 

package de.ipbhalle.metfusion.main;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.ipbhalle.enumerations.Adducts;
import de.ipbhalle.enumerations.AvailableParameters;
import de.ipbhalle.enumerations.Databases;
import de.ipbhalle.enumerations.Ionizations;

/**
 * Support class that holds all parameter settings for MassBank (database), MetFrag (fragmenter) and MetFusion.
 * @author mgerlich
 *
 */
public class MetFusionBatchSettings {

	/** defaults */
	private int mbLimit = 100;
	private int mbCutoff = 5;
	private Ionizations mbIonization = Ionizations.pos;
	private String mbInstruments = "CE-ESI-TOF,ESI-IT-MS/MS,ESI-QqIT-MS/MS,ESI-QqQ-MS/MS,ESI-QqTOF-MS/MS,LC-ESI-IT,LC-ESI-ITFT," +
									"LC-ESI-ITTOF,LC-ESI-Q,LC-ESI-QIT,LC-ESI-QQ,LC-ESI-QTOF";
	private Databases mfDatabase = Databases.pubchem;
	private String mfDatabaseIDs = "";
	private String mfFormula = "";
	private int mfLimit = 10000;
	private double mfParentIon = 272.06847d;
	private Adducts mfAdduct = Adducts.Neutral;
	private double mfExactMass = mfAdduct.getDifference() + mfParentIon;
	private double mfSearchPPM = 10.0d;
	private double mfMZabs = 0.01d;
	private double mfMZppm = 10.0d;
	private boolean clustering = Boolean.TRUE;
	private String peaks = "273.096 22\n289.086 107\n290.118 14\n291.096 999\n292.113 162\n293.054 34\n579.169 37\n580.179 15";
	
	private Map<AvailableParameters, Object> storedSettings;
	
	/**
	 * Default constructor - loads and stores default settings.
	 */
	public MetFusionBatchSettings() {
		// default settings
		storeSettings();
	}
	
	/**
	 * Private method to store settings from the settings map into the appropriate fields.
	 */
	private void storeSettings() {
		this.storedSettings = new HashMap<AvailableParameters, Object>();
		storedSettings.put(AvailableParameters.mbLimit, mbLimit);
		storedSettings.put(AvailableParameters.mbCutoff, mbCutoff);
		storedSettings.put(AvailableParameters.mbIonization, mbIonization);
		storedSettings.put(AvailableParameters.mbInstruments, mbInstruments);
		storedSettings.put(AvailableParameters.mfDatabase, mfDatabase);
		storedSettings.put(AvailableParameters.mfDatabaseIDs, mfDatabaseIDs);
		storedSettings.put(AvailableParameters.mfFormula, mfFormula);
		storedSettings.put(AvailableParameters.mfLimit, mfLimit);
		storedSettings.put(AvailableParameters.mfParentIon, mfParentIon);
		storedSettings.put(AvailableParameters.mfAdduct, mfAdduct);
		storedSettings.put(AvailableParameters.mfExactMass, mfExactMass);
		storedSettings.put(AvailableParameters.mfSearchPPM, mfSearchPPM);
		storedSettings.put(AvailableParameters.mfMZabs, mfMZabs);
		storedSettings.put(AvailableParameters.mfMZppm, mfMZppm);
		storedSettings.put(AvailableParameters.clustering, clustering);
		storedSettings.put(AvailableParameters.peaks, peaks);
	}
	
	/**
	 * Method for transfering the settings map.
	 * 
	 * @return The map of available parameters with their corresponding settings.
	 */
	public Map<AvailableParameters, Object> transferSettings() {
		storeSettings();
		return storedSettings;
	}
	
	/**
	 * Method to restore/load settings from a given map. This method stores both the map and the
	 * available parameter values.
	 *  
	 * @param settings The map of available parameters and settings to be loaded.
	 */
	public void loadSettings(Map<AvailableParameters, Object> settings) {
		this.storedSettings = settings;
		Set<AvailableParameters> keys = settings.keySet();
		for (AvailableParameters key : keys) {
			switch(key) {
				case mbLimit: this.mbLimit = (Integer) settings.get(key);	break;
				case mbCutoff: this.mbCutoff = (Integer) settings.get(key);	break;
				case mbIonization: this.mbIonization = (Ionizations) settings.get(key); break;
				case mbInstruments: this.mbInstruments = (String) settings.get(key);	break;
					
				case mfDatabase: this.mfDatabase = (Databases) settings.get(key);	break;
				case mfDatabaseIDs: this.mfDatabaseIDs = (String) settings.get(key);	break;
				case mfFormula: this.mfFormula = (String) settings.get(key);	break;
				case mfLimit: this.mfLimit = (Integer) settings.get(key);	break;
				case mfParentIon: this.mfParentIon = (Double) settings.get(key);	break;
				case mfAdduct: this.mfAdduct = (Adducts) settings.get(key);	break;
				case mfExactMass: this.mfExactMass = (Double) settings.get(key);	break;
				case mfSearchPPM: this.mfSearchPPM = (Double) settings.get(key);	break;
				case mfMZabs: this.mfMZabs = (Double) settings.get(key);	break;
				case mfMZppm: this.mfMZppm = (Double) settings.get(key);	break;
				
				case clustering: this.clustering = (Boolean) settings.get(key);	break;
				case peaks: this.peaks = (String) settings.get(key);	break;
				default: ;	break;
			}
		}
	}
	
	/**
	 * Method to restore/load settings from a given map from a text file.
	 * Loading from a previously read in file requires explicit String casts in order to work properly.
	 * This method stores both the map and the available parameter values.
	 *  
	 * @param settings The map of available parameters and settings to be loaded.
	 */
	public void loadSettingsFromFile(Map<AvailableParameters, Object> settings) {
		this.storedSettings = settings;
		Set<AvailableParameters> keys = settings.keySet();
		for (AvailableParameters key : keys) {
			switch(key) {
				case mbLimit: this.mbLimit = (Integer.parseInt((String) settings.get(key)));	break;
				case mbCutoff: this.mbCutoff = (Integer.parseInt((String) settings.get(key)));	break;
				case mbIonization: this.mbIonization = Ionizations.valueOf((String) settings.get(key)); break;
				case mbInstruments: this.mbInstruments = (String) settings.get(key);	break;
					
				case mfDatabase: this.mfDatabase = (Databases.valueOf((String) settings.get(key)));	break;
				case mfDatabaseIDs: this.mfDatabaseIDs = (String) settings.get(key);	break;
				case mfFormula: this.mfFormula = (String) settings.get(key);	break;
				case mfLimit: this.mfLimit = (Integer.parseInt((String) settings.get(key)));	break;
				case mfParentIon: this.mfParentIon = (Double.parseDouble((String) settings.get(key)));	break;
				case mfAdduct: {
					Adducts[] values = Adducts.values();
					boolean found = false;
					for (int i = 0; i < values.length; i++) {
						if(values[i].getLabel().equals((String) settings.get(key))) {
							this.mfAdduct = values[i];
							found = true;
							break;
						}
					}
					// if no matching adduct was found, fall back to neutral adduct
					//this.mfAdduct = Adducts.valueOf(((String) settings.get(key)));
					if(!found) {
						this.mfAdduct = Adducts.Neutral;
						break;}
					else break;
					}
				case mfExactMass: this.mfExactMass = (Double.parseDouble((String) settings.get(key)));	break;
				case mfSearchPPM: this.mfSearchPPM = (Double.parseDouble((String) settings.get(key)));	break;
				case mfMZabs: this.mfMZabs = (Double.parseDouble((String) settings.get(key)));	break;
				case mfMZppm: this.mfMZppm = (Double.parseDouble((String) settings.get(key)));	break;
				
				case clustering: this.clustering = (Boolean.parseBoolean((String) settings.get(key)));	break;
				case peaks: this.peaks = (String) settings.get(key);	break;
				default: ;	break;
			}
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MetFusionBatchSettings def = new MetFusionBatchSettings();
		Map<AvailableParameters, Object> test = def.storedSettings;
		def.loadSettings(test);
		
		System.out.println(def.mbLimit);
		System.out.println(def.mbCutoff);
		System.out.println(def.mbIonization + "\t" + Ionizations.pos.getValue());
		System.out.println(def.mbInstruments);
		System.out.println(def.mfDatabase);
		System.out.println(def.mfDatabaseIDs);
		System.out.println(def.mfFormula);
		System.out.println(def.mfLimit);
		System.out.println(def.mfParentIon);
		System.out.println(def.mfAdduct);
		System.out.println(def.mfExactMass);
		System.out.println(def.mfSearchPPM);
		System.out.println(def.mfMZabs);
		System.out.println(def.mfMZppm);
		System.out.println(def.clustering);
		System.out.println(def.peaks);
		
		MetFusionBatchFileHandler mbfh = new MetFusionBatchFileHandler(new File("/home/mgerlich/Documents/metfusion_param_default.mf"));
		mbfh.writeFile(new File("/home/mgerlich/Documents/metfusion_param_default.mf"), def);
	}

	public int getMbLimit() {
		return mbLimit;
	}

	public void setMbLimit(int mbLimit) {
		this.mbLimit = mbLimit;
	}

	public int getMbCutoff() {
		return mbCutoff;
	}

	public void setMbCutoff(int mbCutoff) {
		this.mbCutoff = mbCutoff;
	}

	public String getMfDatabaseIDs() {
		return mfDatabaseIDs;
	}

	public void setMfDatabaseIDs(String mfDatabaseIDs) {
		this.mfDatabaseIDs = mfDatabaseIDs;
	}

	public String getMfFormula() {
		return mfFormula;
	}

	public void setMfFormula(String mfFormula) {
		this.mfFormula = mfFormula;
	}

	public int getMfLimit() {
		return mfLimit;
	}

	public void setMfLimit(int mfLimit) {
		this.mfLimit = mfLimit;
	}

	public double getMfParentIon() {
		return mfParentIon;
	}

	public void setMfParentIon(double mfParentIon) {
		this.mfParentIon = mfParentIon;
	}

	public Adducts getMfAdduct() {
		return mfAdduct;
	}

	public void setMfAdduct(Adducts mfAdduct) {
		this.mfAdduct = mfAdduct;
	}

	public double getMfExactMass() {
		return mfExactMass;
	}

	public void setMfExactMass(double mfExactMass) {
		this.mfExactMass = mfExactMass;
	}

	public double getMfSearchPPM() {
		return mfSearchPPM;
	}

	public void setMfSearchPPM(double mfSearchPPM) {
		this.mfSearchPPM = mfSearchPPM;
	}

	public double getMfMZabs() {
		return mfMZabs;
	}

	public void setMfMZabs(double mfMZabs) {
		this.mfMZabs = mfMZabs;
	}

	public double getMfMZppm() {
		return mfMZppm;
	}

	public void setMfMZppm(double mfMZppm) {
		this.mfMZppm = mfMZppm;
	}

	public boolean isClustering() {
		return clustering;
	}

	public void setClustering(boolean clustering) {
		this.clustering = clustering;
	}

	public String getPeaks() {
		return peaks;
	}

	public void setPeaks(String peaks) {
		this.peaks = peaks;
	}

	public void setMbInstruments(String mbInstruments) {
		this.mbInstruments = mbInstruments;
	}

	public String getMbInstruments() {
		return mbInstruments;
	}

	public Ionizations getMbIonization() {
		return mbIonization;
	}

	public void setMbIonization(Ionizations mbIonization) {
		this.mbIonization = mbIonization;
	}

	public Databases getMfDatabase() {
		return mfDatabase;
	}

	public void setMfDatabase(Databases mfDatabase) {
		this.mfDatabase = mfDatabase;
	}

}
