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

	/** default whitespace character to separate mz from intensity values */
	private static final String DEFAULT_WHITESPACE = " ";
	
	WsLibrarySearchLocator locator;
	WsGoBioSpaceLocator locatorGoBio;
	WsLibrarySearchSoap libSearchSoap;
	
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
	private String stringColumn = selectedColumn.getValue();
	
	private String inputSpectrum;
	
	// GenericDatabase interface fields
	private String databaseName = "GMD";
	private String serverUrl = "http://gmd.mpimp-golm.mpg.de/";
	
	private final String analyteURL = "http://gmd.mpimp-golm.mpg.de/Analytes/";	// + analyteID.aspx
	private final String metaboliteURL = "http://gmd.mpimp-golm.mpg.de/Metabolites/";	// + metaboliteID.aspx
	private final String molURL = "http://gmd.mpimp-golm.mpg.de/webservices/GetMolFile.ashx?id=";
	private final String webpageEnding = ".aspx";
	
	private boolean done = Boolean.FALSE;
	private boolean showResult = Boolean.FALSE;
	private String sessionPath;
	private int searchProgress = 0;
	private List<Result> results;
	private List<Result> unused;
	private int searchCounter = 0;
    private boolean isRunning = false;
    private String missingEntriesNote = "Note: Some entries from the original GMD query are left out because of missing structure information!";
    private boolean showNote = Boolean.FALSE;
    
    private boolean uniqueInchi = Boolean.FALSE;
    
	private AnnotatedMatch[] originalResults;
    
	
	public GMDBean() {
		// GMD library search locator
		locator = new WsLibrarySearchLocator();
		// GMD GoBioSpace locator
		locatorGoBio = new WsGoBioSpaceLocator();
		
		// GMD library seach
		try {
			libSearchSoap = locator.getwsLibrarySearchSoap();
		} catch (ServiceException e) {
			libSearchSoap = null;
		}
		
		setupAvailableColumns();
	}
	
	private void setupAvailableColumns() {
		availableColumns = new SelectItem[3];
		
		availableColumns[0] = new SelectItem(AlkaneRetentionIndexGcColumnComposition.MDN35, AlkaneRetentionIndexGcColumnComposition._MDN35);
		availableColumns[1] = new SelectItem(AlkaneRetentionIndexGcColumnComposition.VAR5, AlkaneRetentionIndexGcColumnComposition._VAR5);
		availableColumns[2] = new SelectItem(AlkaneRetentionIndexGcColumnComposition.none, AlkaneRetentionIndexGcColumnComposition._none);
	}
	
	private String buildAnalyteURL(String id) {
		return analyteURL + id + webpageEnding;
	}
	
	private String buildMetaboliteUrl(String id) {
		return metaboliteURL + id + webpageEnding;
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
				//sb.append(Integer.parseInt(line[0])).append(DEFAULT_WHITESPACE).append(Integer.parseInt("1000")).append(DEFAULT_WHITESPACE);
				double d = Double.parseDouble(line[0]);
				int mass = (int) d;
				sb.append(mass).append(DEFAULT_WHITESPACE).append(Integer.parseInt("1000")).append(DEFAULT_WHITESPACE);
			}
			else if(line.length == 2) {
				//sb.append(Integer.parseInt(line[0])).append(DEFAULT_WHITESPACE).append(Integer.parseInt(line[1])).append(DEFAULT_WHITESPACE);
				double d = Double.parseDouble(line[0]);
				int mass = (int) d;
				d = Double.parseDouble(line[1]);
				int intensity = (int) d;
				sb.append(mass).append(DEFAULT_WHITESPACE).append(intensity).append(DEFAULT_WHITESPACE);
			}
			else if(line.length == 3) {
				// assume that first mz, then int, then rel.int
				//sb.append(Integer.parseInt(line[0])).append(DEFAULT_WHITESPACE).append(Integer.parseInt(line[2])).append(DEFAULT_WHITESPACE);
				double d = Double.parseDouble(line[0]);
				int mass = (int) d;
				d = Double.parseDouble(line[2]);
				int intensity = (int) d;
				sb.append(mass).append(DEFAULT_WHITESPACE).append(intensity).append(DEFAULT_WHITESPACE);
			}
			else {
				// assume that first mz, then rel.int, then int
				//sb.append(Integer.parseInt(line[0])).append(DEFAULT_WHITESPACE).append(Integer.parseInt(line[2])).append(DEFAULT_WHITESPACE);
				double d = Double.parseDouble(line[0]);
				int mass = (int) d;
				d = Double.parseDouble(line[2]);
				int intensity = (int) d;
				sb.append(mass).append(DEFAULT_WHITESPACE).append(intensity).append(DEFAULT_WHITESPACE);
			}
		}
		
		return sb.toString().trim();
	}
	
	@Override
	public void run() {
		setDone(Boolean.FALSE);
		setRunning(Boolean.TRUE);
		
		ResultOfAnnotatedMatch roam = null;
		try {
			roam = libSearchSoap.librarySearch(ri, riWindow, selectedColumn, formatSpectrum());
		} catch (RemoteException e) {
			System.err.println("Error invoking GMD library search remotely!");
			// return empty result list if library search has an error
			this.results = new ArrayList<Result>();
			return;			
		}
		AnnotatedMatch[] matches = roam.getResults();
		setOriginalResults(matches);
		
		if(matches == null) {
			setShowResult(false);
			setRunning(false);
			notifyDone();
			this.results = new ArrayList<Result>();
			return;	
		}
		
		System.out.println("GMD results#: " + matches.length);
		wrapResults();
		
		setShowResult(true);
		setRunning(false);
		notifyDone();
	}
	
	private void wrapResults() {
		List<Result> results = new ArrayList<Result>();
		List<Result> unused = new ArrayList<Result>();
		double score = 1.0f;	// default to highest score, so each result is treated equally
		
		String relImagePath = getSessionPath(); 	//sep + "temp" + sep + sessionString + sep;
		String tempPath = relImagePath.substring(relImagePath.indexOf("/temp"));
		if(!tempPath.endsWith("/"))
			tempPath += "/";
		
		List<String> uniqueMetabolites = new ArrayList<String>();
		
		//String molURL = "http://gmd.mpimp-golm.mpg.de/webservices/GetMolFile.ashx?id=";
		int limitCounter = 0;
        int resultLimit = originalResults.length;
		for (int i = 0; i < originalResults.length; i++) {
			//String name = originalResults[i].getAnalyteName();
			String spectrumName = originalResults[i].getSpectrumName();
			String name = spectrumName;
			String id = originalResults[i].getMetaboliteID();
			//String analyteID = originalResults[i].getAnalyteID();
			//String u = molURL + analyteID;
			String u = molURL + id;
			
			// build analyte metabolite url
			//String analyteUrl = buildAnalyteURL(analyteID);
			String metaboliteUrl = buildMetaboliteUrl(id);
			
			try {
				String clearName = libSearchSoap.MPIMP_Quad_Name(originalResults[i].getSpectrumID());
				String[] split = clearName.split("\\[");	// [0]AnalyteName   [1]QuadName
				id = split[1].substring(0, split[1].indexOf("-"));
			} catch (RemoteException e2) {
				id = originalResults[i].getMetaboliteID();
			}
			
			// build image path
			String imagePath = tempPath + id + ".png";
			
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
	        	if(is == null) throw new CDKException("Empty InputStream from GMD.");
	        	
	        	String temp = IOUtils.toString(is);
	        	if(temp == null || temp.isEmpty() || temp.contains("no chemical information available")) {
	        		unused.add(new Result(databaseName, id, name, score, null));
	        		continue;
	        	}
	        	
	        	MDLReader mr = new MDLReader(IOUtils.toInputStream(temp));
	        	IChemFile chemFile = new ChemFile();
	    		IAtomContainer container = null;
				chemFile = (IChemFile) mr.read(chemFile);
				container = ChemFileManipulator.getAllAtomContainers(chemFile).get(0);
	        	
		        if(container != null) {
		        	// hydrogen handling, required for Metlin mol files!
					try {
						AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(container);
						CDKHydrogenAdder hAdder = CDKHydrogenAdder.getInstance(container.getBuilder());
				        hAdder.addImplicitHydrogens(container);
				        AtomContainerManipulator.convertImplicitToExplicitHydrogens(container);
					}
					catch (CDKException e) { }
					
                	// compute molecular formula
					IMolecularFormula iformula = MolecularFormulaManipulator.getMolecularFormula(container);
					String formula = MolecularFormulaManipulator.getHTML(iformula);
					
					// compute molecular mass
					double emass = 0.0d;
					if(!formula.contains("R"))	// compute exact mass from formula only if NO residues "R" are present
						emass = MolecularFormulaManipulator.getTotalExactMass(iformula);
					else emass = AtomContainerManipulator.getTotalExactMass(container);
					
					// skip non-unique entries
					if(isUniqueInchi() && uniqueMetabolites.contains(id)) {
						unused.add(new Result(databaseName, id, name, score, container, 
								buildAnalyteURL(id), imagePath, formula, emass));
					}
					else {
						results.add(new Result(databaseName, id, name, score, container, 
								metaboliteUrl, imagePath, formula, emass));
						
						limitCounter++;
						
						uniqueMetabolites.add(id);
					}
    			}
    			else unused.add(new Result(databaseName, id, name, score, container, metaboliteUrl, imagePath));
		        
			} catch (MalformedURLException e1) {
				System.err.println("Error processing URL [" + u + "]");
				unused.add(new Result(databaseName, id, name, score, null, metaboliteUrl, imagePath));
			} catch (IOException e) {
				System.err.println("Error processing URL [" + u + "]");
				unused.add(new Result(databaseName, id, name, score, null, metaboliteUrl, imagePath));
			} catch (CDKException e) {
				System.err.println("Error reading IAtomContainer from InputStream!");
				unused.add(new Result(databaseName, id, name, score, null, metaboliteUrl, imagePath));
			}
		}
		
		 // ensure progress bar set to 100% - can be lower if not all results had moldata, thus not increasing limitCounter
        this.searchCounter = resultLimit; 	//results.size();	//resultLimit;
        //this.limit = results.size();
        updateSearchProgress();	// update progress bar
        
        this.results = results;
        this.unused = unused;
        
        if(unused.size() > 0) {	// entries are left out because of missing structure information
        	System.out.println(missingEntriesNote);
        	setShowNote(Boolean.TRUE);
        }
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

	public float getRi() {
		return ri;
	}

	public void setRi(float ri) {
		this.ri = ri;
	}

	public float getRiWindow() {
		return riWindow;
	}

	public void setRiWindow(float riWindow) {
		this.riWindow = riWindow;
	}

	public SelectItem[] getAvailableColumns() {
		return availableColumns;
	}

	public void setAvailableColumns(SelectItem[] availableColumns) {
		this.availableColumns = availableColumns;
	}

	public AlkaneRetentionIndexGcColumnComposition getSelectedColumn() {
		return selectedColumn;
	}

	public void setSelectedColumn(AlkaneRetentionIndexGcColumnComposition selectedColumn) {
		this.selectedColumn = selectedColumn;
	}

	public void setStringColumn(String stringColumn) {
		this.stringColumn = stringColumn;
		// update selected AlkaneRetentionIndexGcColumnComposition accordingly 
		this.selectedColumn = AlkaneRetentionIndexGcColumnComposition.fromString(stringColumn);
	}

	public String getStringColumn() {
		return stringColumn;
	}

	public String getInputSpectrum() {
		return inputSpectrum;
	}

	public void setInputSpectrum(String inputSpectrum) {
		this.inputSpectrum = inputSpectrum;
	}

	public void setShowNote(boolean showNote) {
		this.showNote = showNote;
	}

	public boolean isShowNote() {
		return showNote;
	}

	public String getMissingEntriesNote() {
		return missingEntriesNote;
	}

	public void setMissingEntriesNote(String missingEntriesNote) {
		this.missingEntriesNote = missingEntriesNote;
	}

	public void setUniqueInchi(boolean uniqueInchi) {
		this.uniqueInchi = uniqueInchi;
	}

	public boolean isUniqueInchi() {
		return uniqueInchi;
	}
}
