/**
 * created by Michael Gerlich, Aug 24, 2012 - 1:26:45 PM
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
import javax.faces.model.SelectItem;
import javax.xml.rpc.ServiceException;

import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.IOUtils;
import org.openscience.cdk.ChemFile;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.io.MDLReader;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import de.ipbhalle.metfusion.wrapper.Result;
import de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.WsGoBioSpaceLocator;
import de.mpg.mpimp_golm.gmd.webservices.wsLibrarySearch_asmx.AlkaneRetentionIndexGcColumnComposition;
import de.mpg.mpimp_golm.gmd.webservices.wsLibrarySearch_asmx.AnnotatedMatch;
import de.mpg.mpimp_golm.gmd.webservices.wsLibrarySearch_asmx.ResultOfAnnotatedMatch;
import de.mpg.mpimp_golm.gmd.webservices.wsLibrarySearch_asmx.WsLibrarySearchLocator;
import de.mpg.mpimp_golm.gmd.webservices.wsLibrarySearch_asmx.WsLibrarySearchSoap;


@ManagedBean(name="gmdBean")
@SessionScoped
public class GMDBean implements Runnable, Serializable, GenericDatabaseBean {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	WsLibrarySearchLocator locator;
	WsGoBioSpaceLocator locatorGoBio;
	
	// create new Post method for spectrum image
	PostMethod methodSpectrum = new PostMethod("http://gmd.mpimp-golm.mpg.de/webservices/SpectrumImage.ashx");
	// create new Post method for mol file
	PostMethod methodMol = new PostMethod("http://gmd.mpimp-golm.mpg.de/webservices/GetMolFile.ashx");
	// create new Post method for msp file
	PostMethod methodMsp = new PostMethod("http://gmd.mpimp-golm.mpg.de/webservices/SpectrumMsp.ashx");
	
	private float ri = 1899.06f;
	private float riWindow = 1f;
	
	private SelectItem[] availableColumns;
	private AlkaneRetentionIndexGcColumnComposition selectedColumn = AlkaneRetentionIndexGcColumnComposition.VAR5;
	
	private String inputSpectrum;
	
	// GenericDatabase interface fields
	private String databaseName = "GMD";
	private String serverUrl = "http://gmd.mpimp-golm.mpg.de/";
	
	private final String analyteURL = "http://gmd.mpimp-golm.mpg.de/Analytes/";	// + analyteID.aspx
	private final String webpageEnding = ".aspx";
	
	private boolean done = Boolean.FALSE;
	private boolean showResult = Boolean.FALSE;
	private String sessionPath;
	private int searchProgress = 0;
	private List<Result> results;
	private List<Result> unused;
	private int searchCounter = 0;
    private boolean isRunning = false;
	
	private AnnotatedMatch[] originalResults;
    
	
	public GMDBean() {
		// GMD library search locator
		locator = new WsLibrarySearchLocator();
		// GMD GoBioSpace locator
		locatorGoBio = new WsGoBioSpaceLocator();
		
		setupAvailableColumns();
	}
	
	private void setupAvailableColumns() {
		availableColumns = new SelectItem[3];
		
		availableColumns[0] = new SelectItem(AlkaneRetentionIndexGcColumnComposition.MDN35, AlkaneRetentionIndexGcColumnComposition.MDN35.toString());
		availableColumns[1] = new SelectItem(AlkaneRetentionIndexGcColumnComposition.VAR5, AlkaneRetentionIndexGcColumnComposition.VAR5.toString());
		availableColumns[2] = new SelectItem(AlkaneRetentionIndexGcColumnComposition.none, AlkaneRetentionIndexGcColumnComposition.none.toString());
	}
	
	private String buildAnalyteURL(String id) {
		return analyteURL + id + webpageEnding;
	}
	
	private String formatSpectrum() {
		StringBuilder sb = new StringBuilder();
		String[] split = inputSpectrum.split("\n");
		for (int i = 0; i < split.length; i++) {	// one mz and intensity pair per row
			String[] line = split[i].split("\\s");
			if(line.length == 0) {
				System.err.println("Error parsing peaklist!");
				continue;
			}
			else if(line.length == 1) {
				// assume that only mz values are given
				sb.append(Integer.parseInt(line[0]));
				sb.append(Integer.parseInt("1000"));
			}
			else if(line.length == 2) {
				sb.append(Integer.parseInt(line[0]));
				sb.append(Integer.parseInt(line[1]));
			}
			else if(line.length == 3) {
				// assume that first mz, then int, then rel.int
				sb.append(Integer.parseInt(line[0]));
				sb.append(Integer.parseInt(line[2]));
			}
			else {
				// assume that first mz, then rel.int, then int
				sb.append(Integer.parseInt(line[0]));
				sb.append(Integer.parseInt(line[2]));
			}
		}
		
		return sb.toString();
	}
	
	@Override
	public void run() {
		setDone(Boolean.FALSE);
		setRunning(Boolean.TRUE);
		
		WsLibrarySearchSoap libSearchSoap;
		try {
			libSearchSoap = locator.getwsLibrarySearchSoap();
		} catch (ServiceException e1) {
			System.err.println("Error instantiating GMD webservice!");
			return;			// return if library search can't be instantiated
		}
		
		ResultOfAnnotatedMatch roam = null;
		try {
			roam = libSearchSoap.librarySearch(ri, riWindow, selectedColumn, formatSpectrum());
		} catch (RemoteException e) {
			System.err.println("Error invoking GMD library search remotely!");
			
			// return empty result list if library search has an error
			
		}
		AnnotatedMatch[] matches = roam.getResults();
		setOriginalResults(matches);
		
		wrapResults();
		
		setRunning(false);
		notifyDone();
	}
	
	private void wrapResults() {
		List<Result> results = new ArrayList<Result>();
		List<Result> unused = new ArrayList<Result>();
		double score = 1.0f;	// default to highest score, so each result is treated equally
		
		String relImagePath = getSessionPath(); 	//sep + "temp" + sep + sessionString + sep;
		System.out.println("relImagePath -> " + relImagePath);
		String tempPath = relImagePath.substring(relImagePath.indexOf("/temp"));
		if(!tempPath.endsWith("/"))
			tempPath += "/";
		
		String molURL = "http://gmd.mpimp-golm.mpg.de/webservices/GetMolFile.ashx?id=";
		int limitCounter = 0;
        int resultLimit = originalResults.length;
		for (int i = 0; i < originalResults.length; i++) {
			String name = originalResults[i].getAnalyteName();
			String id = originalResults[i].getMetaboliteID();
			String analyteID = originalResults[i].getAnalyteID();
			String u = molURL + analyteID;
			
			this.searchCounter = limitCounter;
            updateSearchProgress();	// update progress bar
            
            /**
             *  create results only till the given limit
             */
            if(limitCounter == resultLimit) {
            	this.searchProgress = 100;
            	//updateSearchProgress();	// update progress bar
            	break;
            }
			
			try {
				URL url = new URL(u);
				URLConnection con = url.openConnection();
	        	InputStream is = con.getInputStream();
	        	
	        	String temp = IOUtils.toString(is);
//	        	if(!temp.endsWith("M END"))
//	        		temp += "\nM END\n";
	        	
	        	MDLReader mr = new MDLReader(IOUtils.toInputStream(temp));
	        	IChemFile chemFile = new ChemFile();
	    		IAtomContainer container = null;
				chemFile = (IChemFile) mr.read(chemFile);
				container = ChemFileManipulator.getAllAtomContainers(chemFile).get(0);
	        	
				// hydrogen handling, required for Metlin mol files!
				try {
					AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(container);
					CDKHydrogenAdder hAdder = CDKHydrogenAdder.getInstance(container.getBuilder());
			        hAdder.addImplicitHydrogens(container);
			        AtomContainerManipulator.convertImplicitToExplicitHydrogens(container);
				}
				catch (CDKException e) { }
				
		        if(container != null) {
                	// compute molecular formula
					IMolecularFormula iformula = MolecularFormulaManipulator.getMolecularFormula(container);
					String formula = MolecularFormulaManipulator.getHTML(iformula);
					
					// compute molecular mass
					double emass = 0.0d;
					if(!formula.contains("R"))	// compute exact mass from formula only if NO residues "R" are present
						emass = MolecularFormulaManipulator.getTotalExactMass(iformula);
					else emass = AtomContainerManipulator.getTotalExactMass(container);
					
					results.add(new Result(databaseName, id, name, score, container, 
							buildAnalyteURL(analyteID), tempPath + id + ".png", formula, emass));
    			}
    			else results.add(new Result(databaseName, id, name, score, container, buildAnalyteURL(analyteID), tempPath + id + ".png"));
		        
		        limitCounter++;
			} catch (MalformedURLException e1) {
				System.err.println("Error processing URL [" + u + "]");
				unused.add(new Result(databaseName, id, name, score, null));
			} catch (IOException e) {
				System.err.println("Error processing URL [" + u + "]");
				unused.add(new Result(databaseName, id, name, score, null));
			} catch (CDKException e) {
				System.err.println("Error reading IAtomContainer from InputStream!");
				unused.add(new Result(databaseName, id, name, score, null));
			}
		}
		
		 // ensure progress bar set to 100% - can be lower if not all results had moldata, thus not increasing limitCounter
        this.searchCounter = resultLimit; 	//results.size();	//resultLimit;
        //this.limit = results.size();
        updateSearchProgress();	// update progress bar
        
        this.results = results;
        this.unused = unused;
	}
	
	/**
	 * Updates counter for progress bar.
	 */
	public void updateSearchProgress() {
		int maximum = this.originalResults.length;
		int limit = maximum;		// Metlin currently has no way to limit number of results
		int border = (limit >= maximum) ? maximum : limit;
		float result = (((float) searchCounter / (float) border) * 100f);
		this.searchProgress = Math.round(result);
		
		// Ensure the new percent is within the valid 0-100 range
        if (searchProgress < 0) {
        	searchProgress = 0;
        }
        if (searchProgress > 100) {
        	searchProgress = 100;
        }
	}
	
	public static IAtomContainer getCDKAtomContainer(InputStream is) {
		MDLV2000Reader reader = new MDLV2000Reader(is);
		IChemFile chemFile = new ChemFile();
		IAtomContainer container = null;
		try {			
			chemFile = (IChemFile) reader.read(chemFile);
			container = ChemFileManipulator.getAllAtomContainers(chemFile).get(0);
			// hydrogen handling
			//container = hydrogenHandling(container);
		} catch (NumberFormatException e) {
			System.err.println("NumberFormatException occured while parsing mol file");
			return null;
		} catch (CDKException e) {
			System.err.println("CDKException occured!");
			return null;
		}
		
		return container;
	}
	
	public synchronized void notifyDone() {
    	done = Boolean.TRUE;
    	notifyAll();
    }
	
	@Override
	public void setDatabaseName(String name) {
		this.databaseName = name;		
	}
	
	@Override
	public String getDatabaseName() {
		return databaseName;
	}

	@Override
	public void setResults(List<Result> results) {
		this.results = results;
	}

	@Override
	public List<Result> getResults() {
		return results;
	}

	@Override
	public void setUnused(List<Result> unused) {
		this.unused = unused;		
	}

	@Override
	public List<Result> getUnused() {
		return unused;
	}

	@Override
	public void setDone(boolean done) {
		this.done = done;		
	}

	@Override
	public boolean isDone() {
		return done;
	}

	@Override
	public void setShowResult(boolean showResult) {
		this.showResult = showResult;		
	}

	@Override
	public boolean isShowResult() {
		return showResult;
	}

	@Override
	public void setSessionPath(String sessionPath) {
		this.sessionPath = sessionPath;
	}

	@Override
	public String getSessionPath() {
		return sessionPath;
	}

	@Override
	public void setSearchProgress(int searchProgress) {
		this.searchProgress = searchProgress;
	}

	@Override
	public int getSearchProgress() {
		return searchProgress;
	}

	@Override
	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	@Override
	public String getServerUrl() {
		return serverUrl;
	}

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}

	public boolean isRunning() {
		return isRunning;
	}

	public void setSearchCounter(int searchCounter) {
		this.searchCounter = searchCounter;
	}

	public int getSearchCounter() {
		return searchCounter;
	}

	public void setOriginalResults(AnnotatedMatch[] originalResults) {
		this.originalResults = originalResults;
	}

	public AnnotatedMatch[] getOriginalResults() {
		return originalResults;
	}

}
