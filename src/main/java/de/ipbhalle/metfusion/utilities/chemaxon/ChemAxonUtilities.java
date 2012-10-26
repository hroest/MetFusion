/**
 * created by Michael Gerlich, Jul 25, 2012 - 3:46:44 PM
 */ 

package de.ipbhalle.metfusion.utilities.chemaxon;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.fingerprint.Fingerprinter;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.similarity.Tanimoto;
import org.openscience.cdk.smiles.SmilesParser;

import chemaxon.calculations.hydrogenize.Hydrogenize;
import chemaxon.descriptors.ECFP;
import chemaxon.descriptors.ECFPFeature;
import chemaxon.descriptors.ECFPFeatureLookup;
import chemaxon.descriptors.ECFPParameters;
import chemaxon.descriptors.GenerateMD;
import chemaxon.descriptors.MDGeneratorException;
import chemaxon.formats.MolExporter;
import chemaxon.formats.MolFormatException;
import chemaxon.formats.MolImporter;
import chemaxon.license.LicenseManager;
import chemaxon.license.LicenseProcessingException;
import chemaxon.struc.Molecule;
import chemaxon.util.MolHandler;

/**
 * Utility class for dealing with ChemAxon ECFP and FCFP descriptors as 
 * alternative to CDK fingerprints.
 * 
 * @author mgerlich
 *
 */
public class ChemAxonUtilities {

	/** The parameter configuration for ECFP generation. */
	private ECFPParameters paramConfig;
	
	private ECFPParameters paramECFP;
	private ECFPParameters paramFCFP;
	
	public static void main(String[] args) {
		ChemAxonUtilities cau = new ChemAxonUtilities(true);
		URL url = cau.getClass().getResource("fcfp.xml");
		System.out.println("file -> " + url.getFile());
		System.out.println("path -> " + url.getPath());
		
		ECFP one = cau.generateECFPFromName("O=P(c1ccccc1)(c2ccccc2)C");
		ECFP two = cau.generateECFPFromName("O=P4(C1C3C2CC1C4C23)c5ccccc5");
		System.out.println("tan test -> " + (1-one.getTanimoto(two)));
		
		ECFPParameters params = new ECFPParameters(new File(url.getFile()));
		ECFPFeatureLookup lookup = new ECFPFeatureLookup(params);
		MolHandler mh = null;
		try {
			mh = new MolHandler("O=P(c1ccccc1)(c2ccccc2)C");
			Molecule m = mh.getMolecule();
			lookup.processMolecule(m);
			int[] arr = one.toIntArray();
			for (int i = 0; i < arr.length; i++) {
				for(ECFPFeature f : lookup.getFeaturesFromIdentifier(arr[i])) {
					//System.out.println(f.getSubstructure().toFormat("SMARTS"));
					try {
						System.out.println(MolExporter.exportToFormat(f.getSubstructure(), "SMARTS"));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		} catch (MolFormatException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
		try {
			Fingerprinter fp = new Fingerprinter();
			
			IAtomContainer ac1 = sp.parseSmiles("O=P(c1ccccc1)(c2ccccc2)C");
			IAtomContainer ac2 = sp.parseSmiles("O=P4(C1C3C2CC1C4C23)c5ccccc5");
			float tan = Tanimoto.calculate(fp.getFingerprint(ac1), fp.getFingerprint(ac2));
			System.out.println("CDK tanimoto -> " + tan);
		} catch (InvalidSmilesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ChemAxonUtilities cau2 = new ChemAxonUtilities(false);
		ECFP test = cau2.generateECFPFromMol(new File("/vol/massbank/Cache/PB/mol/PB000122.mol"));
		ECFP test2 = cau2.generateECFPFromMol(new File("/vol/massbank/Cache/PB/mol/PB000126.mol"));
		System.out.println("tanimoto dissimilarity -> " + test.getTanimoto(test2));
		System.out.println("tanimoto similarity -> " + (1-test.getTanimoto(test2)));
	}
	
	/**
	 * Generate a descriptor set with both ECFP and FCFP.
	 */
	public ChemAxonUtilities() {
		InputStream is = getClass().getResourceAsStream("fcfp.xml");
		InputStream is2 = getClass().getResourceAsStream("ecfp.xml");
		try {
			String xml = IOUtils.toString(is, "UTF-8");
			paramFCFP = new ECFPParameters(xml); // generate FCFP, default fcfp.xml
			
			xml = IOUtils.toString(is2, "UTF-8");
			paramECFP = new ECFPParameters(xml); // generate ECFP, default ecfp.xml
		} catch (IOException e) {
			System.err.println("Error reading config files.");
		}
		finally{
			try {
				is.close();
				is2.close();
			} catch (IOException e) {
			}
		}
	}
	
	/**
	 * The default constructor for this utility class.
	 * Specify whether to use ECFP or FCFP default configuration file.
	 * Default would be ecfp.xml. Alternative for FCFP generation would be fcfp.xml.
	 * 
	 * @param generateFCFP - indicates whether FCFP should be generated rather than
	 * default ECFP
	 */
	public ChemAxonUtilities(boolean generateFCFP) {
		String config = "ecfp.xml";		// default to ECFP generation
		if(generateFCFP) {		// generate FCFP, use fcfp.xml
			config = "fcfp.xml";
		}
		else {					// generate ECFP, use ecfp.xml
			config = "ecfp.xml";
		}
		
		InputStream is = getClass().getResourceAsStream(config);
		try {
			String xml = IOUtils.toString(is, "UTF-8");
			paramConfig = new ECFPParameters(xml); 	// parameter config
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			try {
				is.close();
			} catch (IOException e) {
			}
		}
		
		// read in license file if no property is given
		if(System.getProperty("chemaxon.license.url") == null) {
			is = getClass().getResourceAsStream("license.cxl");
			try {
				String xml = IOUtils.toString(is, "UTF-8");
				LicenseManager.setLicense(xml);
			} catch (LicenseProcessingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}finally {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	/**
	 * The default constructor for this utility class.
	 * It requires a valid filepath to a parameter XML file.
	 * Default would be ecfp.xml. Alternative for FCFP generation would be fcfp.xml.
	 * 
	 * @param pathConfigXML - the filepath to the configuration file
	 * @param generateFCFP - indicates whether FCFP should be generated rather than
	 * default ECFP
	 */
	public ChemAxonUtilities(String pathConfigXML) {
		File path = new File(pathConfigXML);
		if(pathConfigXML.isEmpty()) {		// default to standard ecfp configuration if path is empty
			URL url = this.getClass().getResource("ecfp.xml");
			path = new File(url.getFile());
		}

		paramConfig = new ECFPParameters(path); // generate ECFP
		
		// read in license file if no property is given
		if(System.getProperty("chemaxon.license.url") == null) {
			//URL license = this.getClass().getResource("license.cxl");
			InputStream is = getClass().getResourceAsStream("license.cxl");
			try {
				String xml = IOUtils.toString(is, "UTF-8");
				LicenseManager.setLicense(xml);
				//LicenseManager.setLicenseFile(license.getFile());
			} catch (LicenseProcessingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Generates an ECFP with the specified properties from a mol file.
	 * 
	 * @param f - the file pointing to a mol file
	 * @return the ECFP, or null if the generation failed.
	 */
	public ECFP generateECFPFromMol(File f) {
		ECFP ecfp = new ECFP(paramConfig);
		
		MolImporter mol;
		try {
			mol = new MolImporter(f.getAbsolutePath());
			
			Molecule m = mol.read();		// read molecule
			Hydrogenize.removeHAtoms(m);	// remove explicit hydrogens
			
			ecfp.generate(m);
		} catch (MolFormatException e) {
			System.err.println("Unregular format for file [" + f.getAbsolutePath() + "]");
			return null;
		} catch (IOException e) {
			System.err.println("Error reading from file [" + f.getAbsolutePath() + "]");
			return null;
		} catch (MDGeneratorException e) {
			System.err.println("Error generating ECFP for molecule [" + f.getAbsolutePath() + "]");
			return null;
		}

		return ecfp;
	}
	
	/**
	 * Generates an ECFP from a textual identifier (name, <b>SMILES</b>, InChI).
	 * 
	 * @param identifier - the textual identifier used for generating the ECFP
	 * @return the ECFP, or null if the generation failed.
	 */
	public ECFP generateECFPFromName(String identifier) {
		ECFP ecfp = new ECFP(paramConfig);
		
		try {
			MolHandler mh = new MolHandler(identifier);
			Molecule m = mh.getMolecule();
			//Molecule m = MolImporter.importMol(identifier, "name");		// read name
			Hydrogenize.removeHAtoms(m);								// remove explicit hydrogens
			
			ecfp.generate(m);
		} catch (MolFormatException e) {
			System.err.println("Unregular format for identifier [" + identifier + "]");
			return null;
		} catch (MDGeneratorException e) {
			System.err.println("Error generating ECFP for identifier [" + identifier + "]");
			return null;
		}
		
		return ecfp;
	}
	
	private void generateDescriptorSet() throws MDGeneratorException, IOException {
		GenerateMD generator = new GenerateMD(2);
		generator.setInput("molecules.sdf");
		generator.setSDfileInput(true);
		generator.setDescriptor(0, "molecules.ecfp", "ECFP", paramECFP, "");
		generator.setDescriptor(1, "molecules.fcfp", "ECFP", paramFCFP, "");
		generator.init();
		generator.run();
		generator.close();
	}
}
