/**
 * created by Michael Gerlich, Feb 24, 2012 - 1:06:41 PM
 */ 

package de.ipbhalle.metfusion.utilities.output;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import org.openscience.cdk.Molecule;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.io.SDFWriter;
import org.openscience.cdk.layout.StructureDiagramGenerator;

import de.ipbhalle.metfusion.web.controller.ResultExtGroupBean;
import de.ipbhalle.metfusion.wrapper.Result;
import de.ipbhalle.metfusion.wrapper.ResultExt;

public class SDFOutputHandler implements IOutputHandler, Runnable{

	private String filename;
	private boolean append;
	
	private final String DEFAULT_ENDING = ".sdf";
	private final String GZIP_ENDING = ".gz";
	
	/**
	 * Default constructor, uses filename and does not append to this file.
	 * 
	 * @param filename - name/path of the file to write
	 */
	public SDFOutputHandler(String filename) {
		this.filename = filename.endsWith(DEFAULT_ENDING) ? filename : filename + DEFAULT_ENDING;
		this.append = Boolean.FALSE;
	}
	
	/**
	 * Default constructor, uses filename and optionally appends to this file.
	 * 
	 * @param filename - name/path of the file to write
	 * @param append - append the file (TRUE) or not (FALSE)
	 */
	public SDFOutputHandler(String filename, boolean append) {
		this.filename = filename.endsWith(DEFAULT_ENDING) ? filename : filename + DEFAULT_ENDING;
		this.append = append;
	}
	
	/**
	 * Fetch properties for original result.
	 * 
	 * @param r - the original result
	 * @return a map of keys and properties
	 */
	private Map<Object, Object> fetchProperties(Result r) {
		Map<Object, Object> props = new HashMap<Object, Object>();

		props.put(properties.origin, r.getPort());			// Origin of result
		props.put(properties.id, r.getId());				// ID
		props.put(properties.name, r.getName());			// Name
		props.put(properties.origscore, r.getScore());		// original Score
		props.put(properties.peaksExplained, r.getMatchingPeaks());	// number of peaks explained
		if(r.getSmiles() != null && !r.getSmiles().isEmpty())
			props.put(properties.smiles, r.getSmiles());		// SMILES
		
		return props;
	}
	
	/**
	 * Fetch properties for extended result.
	 * 
	 * @param r - the extended result
	 * @return a map of keys and properties
	 */
	private Map<Object, Object> fetchProperties(ResultExt r) {
		Map<Object, Object> props = new HashMap<Object, Object>();

		props.put(properties.origin, r.getPort());			// Origin of result
		props.put(properties.id, r.getId());				// ID
		props.put(properties.name, r.getName());			// Name
		props.put(properties.origscore, r.getScore());		// original Score
		props.put(properties.newscore, r.getResultScore());	// resulting Score
		props.put(properties.smiles, r.getSmiles());		// SMILES
		props.put(properties.peaksExplained, r.getMatchingPeaks());	// number of peaks explained
		props.put(properties.URL, r.getUrl());				// URL of the compound
		props.put(properties.tiedRank, r.getTiedRank());	// add tied rank
		props.put(properties.clusterRank, r.getClusterRank());	// add cluster rank
		
		return props;
	}
	
	public boolean writeClusterResults(List<ResultExt> results, boolean compress) {
		boolean success = Boolean.FALSE;
		OutputStream os = null;
		try {
			if(!compress)
				os = new FileOutputStream(new File(filename), append);
			else os = new GZIPOutputStream(new FileOutputStream(new File(filename + GZIP_ENDING), append));
		} catch (FileNotFoundException e) {
			System.err.println("Could not find file [" + filename + "]!");
			return success;
		} catch (IOException e) {
			System.err.println("Error creating GZIP output stream for file [" + filename + "]!");
			return success;
		}
		
		SDFWriter sdfwriter = new SDFWriter(os);
		StructureDiagramGenerator sdg = new StructureDiagramGenerator();
		for (ResultExt result : results) {
			IAtomContainer container = result.getMol();
			Map<Object, Object> props = fetchProperties(result);
			IMolecule temp = new Molecule(container);
			sdg.setMolecule(temp);
			IMolecule layedOutMol = null;
			// try to generate 2D coordinates
		    try {
				sdg.generateCoordinates();
				layedOutMol = sdg.getMolecule();
				layedOutMol.setProperties(props);
				layedOutMol.setID((String) props.get(properties.name));
				sdfwriter.write(layedOutMol);
			} catch (CDKException e) {		// else use AtomContainer without 2D information
				System.err.println("Error generating 2D coordinates for molecule [" + result.getId() + "], trying container without 2D coordinates!");
				try {
					container.setProperties(props);
					container.setID((String) props.get(properties.name));
					sdfwriter.write(container);
				} catch (CDKException e1) {
					System.err.println("Error writing container for molecule [" + result.getId() + "]");
				}
			} catch(ArrayIndexOutOfBoundsException e) {
		    	System.err.println("Error generating 2D coordinates for molecule [" + result.getId() + "], trying container without 2D coordinates!");
				try {
					container.setProperties(props);
					container.setID((String) props.get(properties.name));
					sdfwriter.write(container);
				} catch (CDKException e1) {
					System.err.println("Error writing container for molecule [" + result.getId() + "]");
				}
		    }
		}
		
		try {
			sdfwriter.close();
			os.close();
		} catch (IOException e) {
			System.err.println("Could not close connection to file [" + filename + "]!");
			return success;
		}
		success = Boolean.TRUE;
		
		return success;
	}
	
	public boolean writeOriginalResults(List<Result> results, boolean compress) {
		boolean success = Boolean.FALSE;
		if(results == null || results.isEmpty())
			return true;
		
		OutputStream os = null;
		try {
			if(!compress)
				os = new FileOutputStream(new File(filename), append);
			else os = new GZIPOutputStream(new FileOutputStream(new File(filename + GZIP_ENDING), append));
		} catch (FileNotFoundException e) {
			System.err.println("Could not find file [" + filename + "]!");
			return success;
		} catch (IOException e) {
			System.err.println("Error creating GZIP output stream for file [" + filename + "]!");
			return success;
		}
		
		SDFWriter sdfwriter = new SDFWriter(os);
		StructureDiagramGenerator sdg = new StructureDiagramGenerator();
		for (Result result : results) {
			IAtomContainer container = result.getMol();
			Map<Object, Object> props = fetchProperties(result);
			IMolecule temp = new Molecule(container);
			sdg.setMolecule(temp);
			IMolecule layedOutMol = null;
			// try to generate 2D coordinates
		    try {
				sdg.generateCoordinates();
				layedOutMol = sdg.getMolecule();
				layedOutMol.setProperties(props);
				layedOutMol.setID((String) props.get(properties.name));
				sdfwriter.write(layedOutMol);
			} catch (CDKException e) {		// else use AtomContainer without 2D information
				System.err.println("Error generating 2D coordinates for molecule [" + result.getId() + "], trying container without 2D coordinates!");
				try {
					container.setProperties(props);
					container.setID((String) props.get(properties.name));
					sdfwriter.write(container);
				} catch (CDKException e1) {
					System.err.println("Error writing container for molecule [" + result.getId() + "]");
				}
			} catch(ArrayIndexOutOfBoundsException e) {
		    	System.err.println("Error generating 2D coordinates for molecule [" + result.getId() + "], trying container without 2D coordinates!");
				try {
					container.setProperties(props);
					container.setID((String) props.get(properties.name));
					sdfwriter.write(container);
				} catch (CDKException e1) {
					System.err.println("Error writing container for molecule [" + result.getId() + "]");
				}
		    }
		}
		
		try {
			sdfwriter.close();
			os.close();
		} catch (IOException e) {
			System.err.println("Could not close connection to file [" + filename + "]!");
			return success;
		}
		success = Boolean.TRUE;
		
		return success;
	}
	
	@Override
	public boolean writeRerankedResults(List<ResultExt> results) {
		boolean success = Boolean.FALSE;
		OutputStream os = null;
		try {
			os = new FileOutputStream(new File(filename), append);
		} catch (FileNotFoundException e) {
			System.err.println("Could not find file [" + filename + "]!");
			return success;
		}
		
		SDFWriter sdfwriter = new SDFWriter(os);
		StructureDiagramGenerator sdg = new StructureDiagramGenerator();
		for (ResultExt result : results) {
			IAtomContainer container = result.getMol();
			Map<Object, Object> props = fetchProperties(result);
			IMolecule temp = new Molecule(container);
			sdg.setMolecule(temp);
			IMolecule layedOutMol = null;
			// try to generate 2D coordinates
		    try {
				sdg.generateCoordinates();
				layedOutMol = sdg.getMolecule();
				layedOutMol.setProperties(props);
				layedOutMol.setID((String) props.get(properties.name));
				sdfwriter.write(layedOutMol);
			} catch (CDKException e) {		// else use AtomContainer without 2D information
				System.err.println("Error generating 2D coordinates for molecule [" + result.getId() + "], trying container without 2D coordinates!");
				try {
					container.setProperties(props);
					container.setID((String) props.get(properties.name));
					sdfwriter.write(container);
				} catch (CDKException e1) {
					System.err.println("Error writing container for molecule [" + result.getId() + "]");
				}
			} catch(ArrayIndexOutOfBoundsException e) {
		    	System.err.println("Error generating 2D coordinates for molecule [" + result.getId() + "], trying container without 2D coordinates!");
				try {
					container.setProperties(props);
					container.setID((String) props.get(properties.name));
					sdfwriter.write(container);
				} catch (CDKException e1) {
					System.err.println("Error writing container for molecule [" + result.getId() + "]");
				}
		    }
		}
		
		try {
			sdfwriter.close();
			os.close();
		} catch (IOException e) {
			System.err.println("Could not close connection to file [" + filename + "]!");
			return success;
		}
		success = Boolean.TRUE;
		
		return success;
	}

	@Override
	public boolean writeAllResults(List<Result> originalFragmenter, List<Result> originalDatabase, 
			List<ResultExt> newlyRanked, List<ResultExtGroupBean> cluster) {
		boolean success = Boolean.FALSE;
		OutputStream os = null;
		try {
			os = new FileOutputStream(new File(filename), append);
		} catch (FileNotFoundException e) {
			System.err.println("Could not find file [" + filename + "]!");
			return success;
		}
		
		SDFWriter sdfwriter = new SDFWriter(os);
		StructureDiagramGenerator sdg = new StructureDiagramGenerator();
		// TODO write all results
		for (ResultExt result : newlyRanked) {
			IAtomContainer container = result.getMol();
			Map<Object, Object> props = fetchProperties(result);
			IMolecule temp = new Molecule(container);
			sdg.setMolecule(temp);
		    try {
				sdg.generateCoordinates();
			} catch (CDKException e1) {
				System.err.println("Error generating 2D coordinates for molecule [" + result.getId() + "]");
				continue;
			} catch(ArrayIndexOutOfBoundsException e) {
		    	System.err.println("Error generating 2D coordinates for molecule [" + result.getId() + "], trying container without 2D coordinates!");
				try {
					container.setProperties(props);
					container.setID((String) props.get(properties.name));
					sdfwriter.write(container);
				} catch (CDKException e1) {
					System.err.println("Error writing container for molecule [" + result.getId() + "]");
				}
		    }
		    IMolecule layedOutMol = sdg.getMolecule();
//			container.setProperties(props);
//			container.setID((String) props.get(properties.name));
		    layedOutMol.setProperties(props);
		    layedOutMol.setID((String) props.get(properties.name));
			
			try {
				sdfwriter.write(layedOutMol);
			} catch (CDKException e) {
				System.err.println("Error writing container for molecule [" + result.getId() + "]");
			}
		}
		
		try {
			sdfwriter.close();
			os.close();
		} catch (IOException e) {
			System.err.println("Could not close connection to file [" + filename + "]!");
			return success;
		}
		success = Boolean.TRUE;
		
		return false;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
}
