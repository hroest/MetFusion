/**
 * created by Michael Gerlich, Jan 28, 2013 - 11:14:23 AM
 */ 

package de.ipbhalle.metfusion.wrapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.openscience.cdk.ChemFile;
import org.openscience.cdk.ChemObject;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import de.ipbhalle.metfusion.web.controller.GenericDatabaseBean;

public class SDFDatabase implements GenericDatabaseBean {

	private String databaseName = "sdf";
	private String sessionPath;
	private String serverUrl;
	private List<Result> results;
	private List<Result> unused;
	private boolean done;
	private boolean showResult;
	private boolean showNote;
	private boolean uniqueInchi;
	private int searchProgress;
	
	private String SDFfile;
	
	
	public SDFDatabase(String databaseName, String sdfFilePath) {
		this.databaseName = databaseName;
		this.setSDFfile(sdfFilePath);
		
		this.results = new ArrayList<Result>();
		this.unused = new ArrayList<Result>();
	}
	
	@Override
	public void run() {
		
		// read in SDF file thread-wise
		MDLV2000Reader reader = null;
		List<IAtomContainer> containersList;
		List<IAtomContainer> ret = new ArrayList<IAtomContainer>();
		
		File f = new File(SDFfile);
		
		if(f.isFile())
		{
			try {
				reader = new MDLV2000Reader(new FileReader(f));
			} catch (FileNotFoundException e) {
				System.err.println("SD file [" + f.getAbsolutePath() + "] not found. Returning empty list!");
				this.results = new ArrayList<Result>();
				
				return;
			}
			ChemFile chemFile = null;
			try {
				chemFile = (ChemFile) reader.read((ChemObject) new ChemFile());
				containersList = ChemFileManipulator.getAllAtomContainers(chemFile);
				for (IAtomContainer container : containersList) {
					Map<Object, Object> properties =  container.getProperties();
					String id = "", name = "", origin = "", smiles = "";
					int peaksExplained = 0;
					double score = 0.0d;
					
					if(properties.containsKey("id")) {
						id = (String) properties.get("id");
					}
					if(properties.containsKey("origscore")) {
						score = Double.valueOf((String) properties.get("origscore"));
					}
					if(properties.containsKey("name")) {
						name = (String) properties.get("name");
					}
					if(properties.containsKey("peaksExplained")) {
						peaksExplained = Integer.valueOf((String) properties.get("peaksExplained"));
					}
					if(properties.containsKey("origin")) {
						origin = (String) properties.get("origin");
					}
					if(properties.containsKey("smiles")) {
						smiles = (String) properties.get("smiles");
					}
					
					IAtomContainer temp = hydrogenHandling(container);
					ret.add(temp);

					// compute molecular formula
					IMolecularFormula iformula = MolecularFormulaManipulator.getMolecularFormula(container);
					String formula = MolecularFormulaManipulator.getHillString(iformula);
					// compute molecular mass
					double emass = 0.0d;
					if (!formula.contains("R")) // compute exact mass from formula only if NO residues "R" are present
							emass = MolecularFormulaManipulator.getTotalExactMass(iformula);
					
					Result r = new Result(origin, id, name, score, temp, "", "", formula, emass);
					r.setMatchingPeaks(peaksExplained);
					if(smiles != null && !smiles.isEmpty())
						r.setSmiles(smiles);
					
					results.add(r);
				}
			} catch (CDKException e) {
				System.err.println("Error reading SDF file " + f.getAbsolutePath());
				this.results = new ArrayList<Result>();
				
				return;
			}
		}
		else {
			System.err.println("[" + f.getAbsolutePath() + "] is not a file! Please specify a valid SD file. Returning an empty list!");
			this.results = new ArrayList<Result>();
			
			return;
		}
		
		/**
		 * apply sorting after score ???
		 */
		// sort final result list based upon score value
//		Collections.sort(results, Collections.reverseOrder(new ResultComparator()));
		/**
		 * 
		 */
	}

	private IAtomContainer hydrogenHandling(IAtomContainer container) {
		// create deep copy
		IAtomContainer copy = null;
		try {
			 copy = container.clone();
		} catch (CloneNotSupportedException e1) {
			return container;
		}
		
		//add hydrogens
        try {
			AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(copy);
			CDKHydrogenAdder hAdder = CDKHydrogenAdder.getInstance(copy.getBuilder());
		    hAdder.addImplicitHydrogens(copy);
		    AtomContainerManipulator.convertImplicitToExplicitHydrogens(copy);
		} catch (CDKException e) {		// return original if adding H's fails
			return container;
		}
        
        return copy;
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

	@Override
	public void setShowNote(boolean showNote) {
		this.showNote = showNote;
	}

	@Override
	public boolean isShowNote() {
		return showNote;
	}

	@Override
	public void setUniqueInchi(boolean uniqueInchi) {
		this.uniqueInchi = uniqueInchi;
	}

	@Override
	public boolean isUniqueInchi() {
		return uniqueInchi;
	}

	public String getSDFfile() {
		return SDFfile;
	}

	public void setSDFfile(String sDFfile) {
		SDFfile = sDFfile;
	}

}
