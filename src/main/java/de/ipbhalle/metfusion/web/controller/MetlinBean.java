/**
 * created by Michael Gerlich, Aug 17, 2012 - 10:40:37 AM
 */ 

package de.ipbhalle.metfusion.web.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.xml.rpc.ServiceException;

import org.apache.commons.io.IOUtils;
import org.openscience.cdk.ChemFile;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.io.MDLReader;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

import Metlin.MetlinPortType;
import Metlin.MetlinServiceLocator;
import Metlin.SpectrumLineInfo;
import Metlin.SpectrumMatchRequest;
import chemaxon.formats.MolImporter;
import chemaxon.struc.Molecule;
import de.ipbhalle.metfusion.wrapper.Result;

@ManagedBean(name="metlinBean")
@SessionScoped
public class MetlinBean implements Runnable, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** the basic URL to the structure (mol) information at Metlin */
	private static final String structureURL = "http://metlin.scripps.edu/structure/";
	
	private MetlinServiceLocator locator;
	private MetlinPortType serv;
	
	private String securityToken;
	private String inputSpectrum;
	private String selectedIonization = "pos";
	
	private float[] specMasses;
	private float toleranceMSMS = 0.001f;
	private float precursorMass;
	
	private int[] intensities;
	private int tolerancePrecursor = 5;
	private int collisionEnergy = 40;
	
	private SpectrumMatchRequest parameters;
	private SpectrumLineInfo[] metlinResults;
	private List<Result> results;
	
	public MetlinBean() {
		this("");
	}
	
	public MetlinBean(String token) {
		setupMetlin(token);
	}
	
	@Override
	public void run() {
		// create new empty parameter container
		parameters = new SpectrumMatchRequest();
		
		// parse input spectrum
		formatPeaks();
		
		// set security token
		parameters.setToken(getSecurityToken());
		// set masses list
		parameters.setMass(getSpecMasses());
		// set intensities list
		parameters.setIntensity(getIntensities());
		// set ionization
		parameters.setMode(getSelectedIonization());
		// set tolerance of MSMS
		parameters.setToleranceMSMS(getToleranceMSMS());
		// set tolerance of precursor
		parameters.setTolerancePrecursor(getTolerancePrecursor());
		// set precursor mass
		parameters.setPrecursorMass(getPrecursorMass());
		// set collision energy
		parameters.setCollisionEnergy(collisionEnergy);
		
		// execute query and store results
		try {
			metlinResults = serv.spectrumMatch(getParameters());
		} catch (RemoteException e) {
			System.err.println("Error retrieving query results! Returning null results!");
			metlinResults = new SpectrumLineInfo[1];
			metlinResults[0] = new SpectrumLineInfo();
			
			return;
		}
		wrapResults();
	}

	private void wrapResults() {
		List<Result> results = new ArrayList<Result>();
		List<String> urls = new ArrayList<String>();
        
        for(int i = 0; i < metlinResults.length; i++){
            System.out.println(metlinResults[i].getMetlinID());
            System.out.println("molUrl -> " + structureURL + metlinResults[i].getMetlinID() + ".mol");
            String u = structureURL + metlinResults[i].getMetlinID() + ".mol";
            urls.add(u);
            System.out.println(metlinResults[i].getName());
            System.out.println(metlinResults[i].getMetlinScore());
            System.out.println(metlinResults[i].getPrecursor());
            System.out.println(metlinResults[i].getPrecursorPPM());
            System.out.println(metlinResults[i].getSpectrumMatching());
            
            try {
				URL url = new URL(u);
				URLConnection con = url.openConnection();
	        	InputStream is = con.getInputStream();
	        	
	        	String temp = IOUtils.toString(is);
	        	if(!temp.endsWith("M END"))
	        		temp += "\nM END\n";
	        	
	        	MDLReader mr = new MDLReader(IOUtils.toInputStream(temp));
	        	IChemFile chemFile = new ChemFile();
	    		IAtomContainer container = null;
    			chemFile = (IChemFile) mr.read(chemFile);
    			container = ChemFileManipulator.getAllAtomContainers(chemFile).get(0);
	        	
    			// only add new result if everything goes fine
    			double d = Double.valueOf(metlinResults[i].getMetlinScore());	// Metlin-score between 0 and 100
                d = d / 100d;	// break score down into range 0 to 1
                results.add(new Result("Metlin", metlinResults[i].getMetlinID(), metlinResults[i].getName(), d, container));
			} catch (MalformedURLException e) {
				System.err.println("Error processing URL [" + u + "]");
			} catch (IOException e) {
				System.err.println("Error processing URL [" + u + "]");
			} catch (CDKException e) {
				System.err.println("Error reading IAtomContainer from InputStream!");
			}
        }
        
        this.results = results;
	}
	
	private void setupMetlin(String token) {
		this.locator = new MetlinServiceLocator();
		try {
			this.serv = locator.getMetlinPort();
		} catch (ServiceException e) {
			System.err.println("Error creating MetlinPort for [" + locator.getMetlinPortAddress() + "]");
		}
		
		// retrieve application scoped PropertiesBean
		PropertiesBean pb = (PropertiesBean) FacesContext.getCurrentInstance().getExternalContext().getApplicationMap().get("propertiesBean");
		String propToken = pb.getProperty("metlinToken");		// read security token from properties file
		
		// if another server is not selected and if this property is set, use the designated server rather the default
		if(token.isEmpty() && propToken != null && !propToken.isEmpty())	// use propertyToken if provided token is empty		
			setSecurityToken(propToken);
		else setSecurityToken(token);		// else use provided token
	}
	
	private String formatIonization() {
		String ion = "pos";	// default to positive
		if(selectedIonization == "1")
			ion = "pos";
		else if (selectedIonization == "-1")
			ion = "neg";
		else ion = "pos";
		
		return ion;
	}
	
	private void formatPeaks() {
		String[] split = inputSpectrum.trim().split("\n");
		float[] mzs = new float[split.length];
		int[] ints = new int[split.length];
		
		for (int i = 0; i < split.length; i++) {
			String[] line = split[i].split("\\s");
			if(line.length == 0) {
				System.err.println("Error parsing peaklist!");
				continue;
			}
			else if(line.length == 1) {
				// assume that only mz values are given
				mzs[i] = Float.parseFloat(line[0]);
				ints[i] = 1000;
			}
			else if(line.length == 2) {
				mzs[i] = Float.parseFloat(line[0]);
				ints[i] = Integer.parseInt(line[1]);
			}
			else if(line.length == 3) {
				// assume that first mz, then rel.int, then int
				mzs[i] = Float.parseFloat(line[0]);
				ints[i] = Integer.parseInt(line[2]);
			}
			else {
				// assume that first mz, then rel.int, then int
				mzs[i] = Float.parseFloat(line[0]);
				ints[i] = Integer.parseInt(line[2]);
			}
		}
		
		// set parsed values into corresponding fields
		specMasses = mzs;
		intensities = ints;
	}
	
	public static void main(String[] args) {
		try {
        	long time1 = System.currentTimeMillis();
        	
            MetlinServiceLocator locator = new MetlinServiceLocator();
            MetlinPortType serv = locator.getMetlinPort();
            String token = "tXSy1wXlgMiMGKih";
            float specMass[] = new float[] { 83.0597f, 86.0895f, 95.0542f, 110.0641f, 159.0819f, 195.0797f };
            int intensity[] = new int[] { 2700, 1200, 1200, 10000, 3900, 700 };
            String mode = "pos";
            int ce = 20;
            float tolMS = 0.01f;
            int tolPrec = 20;
            float prec = 398.17025f;
            SpectrumMatchRequest parameters = new SpectrumMatchRequest(token, specMass, intensity, mode, ce, tolMS, tolPrec, prec);
            SpectrumLineInfo resultsData[]= serv.spectrumMatch(parameters);
            
            List<String> urls = new ArrayList<String>();
            
            for(int i = 0; i < resultsData.length; i++){
                System.out.println(resultsData[i].getMetlinID());
                System.out.println("molUrl -> " + structureURL + resultsData[i].getMetlinID() + ".mol");
                urls.add(structureURL + resultsData[i].getMetlinID() + ".mol");
                System.out.println(resultsData[i].getName());
                System.out.println(resultsData[i].getMetlinScore());
                System.out.println(resultsData[i].getPrecursor());
                System.out.println(resultsData[i].getPrecursorPPM());
                System.out.println(resultsData[i].getSpectrumMatching());
            }
            
            for (String s : urls) {
            	URL url = new URL(s);
            	URLConnection con = url.openConnection();
            	InputStream is = con.getInputStream();
            	
            	String temp = IOUtils.toString(is);
            	if(!temp.endsWith("M END"))
            		temp += "\nM END\n";
            	MolImporter mi = new MolImporter(IOUtils.toInputStream(temp));
            	Molecule mol = mi.createMol();
			}
            
            long time2 = System.currentTimeMillis();
            System.out.println("time -> " + (time2-time1) / 1000l);
        } catch (Exception e) {
             e.printStackTrace();
        }
	}

	public void setSecurityToken(String securityToken) {
		this.securityToken = securityToken;
	}

	public String getSecurityToken() {
		return securityToken;
	}

	public void setSpecMasses(float[] specMasses) {
		this.specMasses = specMasses;
	}

	public float[] getSpecMasses() {
		return specMasses;
	}

	public void setIntensities(int[] intensities) {
		this.intensities = intensities;
	}

	public int[] getIntensities() {
		return intensities;
	}

	public String getSelectedIonization() {
		return selectedIonization;
	}

	public void setSelectedIonization(String selectedIonization) {
		this.selectedIonization = selectedIonization;
	}

	public void setToleranceMSMS(float toleranceMSMS) {
		this.toleranceMSMS = toleranceMSMS;
	}

	public float getToleranceMSMS() {
		return toleranceMSMS;
	}

	public void setTolerancePrecursor(int tolerancePrecursor) {
		this.tolerancePrecursor = tolerancePrecursor;
	}

	public int getTolerancePrecursor() {
		return tolerancePrecursor;
	}

	public void setPrecursorMass(float precursorMass) {
		this.precursorMass = precursorMass;
	}

	public float getPrecursorMass() {
		return precursorMass;
	}

	public String getInputSpectrum() {
		return inputSpectrum;
	}

	public void setInputSpectrum(String inputSpectrum) {
		this.inputSpectrum = inputSpectrum;
	}

	public int getCollisionEnergy() {
		return collisionEnergy;
	}

	public void setCollisionEnergy(int collisionEnergy) {
		this.collisionEnergy = collisionEnergy;
	}

	public SpectrumMatchRequest getParameters() {
		return parameters;
	}

	public void setParameters(SpectrumMatchRequest parameters) {
		this.parameters = parameters;
	}

	public void setMetlinResults(SpectrumLineInfo[] metlinResults) {
		this.metlinResults = metlinResults;
	}

	public SpectrumLineInfo[] getMetlinResults() {
		return metlinResults;
	}

	public void setResults(List<Result> results) {
		this.results = results;
	}

	public List<Result> getResults() {
		return results;
	}
	
}
