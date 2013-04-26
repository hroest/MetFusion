/**
 * created by Michael Gerlich, Jan 31, 2013 - 3:48:39 PM

 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */ 

package de.ipbhalle.metfusion.main;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.xml.rpc.ServiceException;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.aromaticity.CDKHueckelAromaticityDetector;
import org.openscience.cdk.aromaticity.DoubleBondAcceptingAromaticityDetector;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.io.MDLV2000Writer;
import org.openscience.cdk.io.listener.PropertiesListener;
import org.openscience.cdk.io.setting.IOSetting;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.smiles.smarts.SMARTSQueryTool;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import com.chemspider.www.CommonSearchOptions;
import com.chemspider.www.EComplexity;
import com.chemspider.www.EIsotopic;
import com.chemspider.www.ERequestStatus;
import com.chemspider.www.ExtendedCompoundInfo;
import com.chemspider.www.MassSpecAPISoapProxy;
import com.chemspider.www.OpenBabelLocator;
import com.chemspider.www.OpenBabelSoap;
import com.chemspider.www.SearchSoapProxy;
import com.chemspider.www.SubstructureSearchOptions;

import de.ipbhalle.metfusion.utilities.MassBank.MassBankUtilities;
import de.ipbhalle.metfusion.utilities.output.SDFOutputHandler;
import de.ipbhalle.metfusion.wrapper.Result;
import de.ipbhalle.metfusion.wrapper.ResultSubstructure;

public class SubstructureSearch implements Runnable {

	private List<String> includes;
	private List<String> excludes;
	private String token;
	private String molecularFormula;
	public final boolean substrucPresent = Boolean.TRUE;
	public final boolean substrucAbsent = Boolean.FALSE;
	private ExtendedCompoundInfo[] chemspiderInfo;
	private List<ResultSubstructure> resultsOriginal;
	private List<ResultSubstructure> resultsRemaining;
	
	
	public SubstructureSearch(List<String> includes, List<String> excludes, String token, String formula) {
		this.includes = includes;
		this.excludes = excludes;
		this.setToken(token);
		this.setMolecularFormula(formula);
	}
	
	private void queryIncludes() {
		if(includes.size() == 1) {		// only one substructure filter
			resultsOriginal = queryDatabase(includes.get(0));
			//resultsOriginal = queryDatabaseWithFormula(molecularFormula);
			resultsRemaining = skipNonUsed(resultsOriginal);
			System.out.println("includes == 1 \toriginal = " + resultsOriginal.size());
			System.out.println("includes == 1 \tskipNonUsed = " + resultsRemaining.size());
		}
		else if(includes.size() > 1) {
			for (int i = 0; i < includes.size(); i++) {
				if(i == 0) {
					resultsOriginal = queryDatabase(includes.get(0));
					//resultsOriginal = queryDatabaseWithFormula(molecularFormula);
					resultsRemaining = skipNonUsed(resultsOriginal);
					
					System.out.println("\toriginal = " + resultsOriginal.size());
					System.out.println("\tskipNonUsed = " + resultsRemaining.size());
				}
				else {
					resultsRemaining = filterCandidates(resultsRemaining, includes.get(i), substrucPresent);
				}
				
				System.out.println("includes == " + includes.size());
				System.out.println("[" + i + "] -> remaining = " + resultsRemaining.size());
			}
		}
		else {
			System.err.println("Empty substructure!");
			return;
		}
	}
	
	private List<ResultSubstructure> skipNonUsed(List<ResultSubstructure> current) {
		List<ResultSubstructure> remaining = new ArrayList<ResultSubstructure>();
		for (ResultSubstructure rs : current) {
			if(rs.isUsed())
				remaining.add(rs);
		}
		return remaining;
	}
	
	private List<ResultSubstructure> queryDatabaseWithFormula(String formula) {
		List<ResultSubstructure> candidates = new ArrayList<ResultSubstructure>();
		
		MassSpecAPISoapProxy msp = new MassSpecAPISoapProxy();
		String[] ids = null;
		int[] CSIDs = null;
		try {
			ids = msp.searchByFormula2(formula);
			CSIDs = new int[ids.length];
			for (int i = 0; i < CSIDs.length; i++) {
				CSIDs[i] = Integer.parseInt(ids[i]);
			}
		} catch (RemoteException e) {
			System.err.println("Error querying with formula [" + formula + "]!");
			return candidates;
		}
		
		try {
			chemspiderInfo = msp.getExtendedCompoundInfoArray(CSIDs, token);
			System.out.println("# matches -> " + chemspiderInfo.length);
		} catch (RemoteException e) {
			System.err.println("Error retrieving compound info array!");
			return candidates;
		}
		
		SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
		for (int i = 0; i < chemspiderInfo.length; i++) {
			System.out.println(chemspiderInfo[i].getCSID() + "\t" + chemspiderInfo[i].getSMILES());
			IAtomContainer ac = null;
			boolean used = false;
			try {
				ac = sp.parseSmiles(chemspiderInfo[i].getSMILES());
				used = true;
			}
			catch(InvalidSmilesException ise) {
				ac = null;
				System.err.println("skipping " + chemspiderInfo[i].getCSID());
			}
			
			candidates.add(new ResultSubstructure(chemspiderInfo[i], ac, used));
		}
		
		return candidates;
	}
	
	private List<ResultSubstructure> queryDatabase(String substrucPresent) {
		List<ResultSubstructure> candidates = new ArrayList<ResultSubstructure>();
		
		// convert input SMILES to MOL format for ChemSpider service
		SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
//		sp.setPreservingAromaticity(false);
//		String mol = "";
//		String s = "";
//		try {
//			IMolecule temp = sp.parseSmiles(substrucPresent);
//			System.out.println("aromatic Hueckel? -> " + CDKHueckelAromaticityDetector.detectAromaticity(temp));
//			System.out.println("aromatic double bond? -> " + DoubleBondAcceptingAromaticityDetector.detectAromaticity(temp));
//			// create coordinates
//            StructureDiagramGenerator sdg = new StructureDiagramGenerator();
//            sdg.setMolecule(temp);
//            sdg.generateCoordinates();
//            IMolecule layedOutMol = sdg.getMolecule();
//            //
//            
//			byte[] b = null;
//			ByteArrayOutputStream bos = new ByteArrayOutputStream();
//			MDLV2000Writer writer = new MDLV2000Writer(bos);
//			IOSetting[] ios = writer.getIOSettings();
//			for (int i = 0; i < ios.length; i++) {
//				System.out.println(ios[i].getName() + "\t" + ios[i].getSetting());
//			}
//			Properties customSettings = new Properties();
//			customSettings.setProperty("ForceWriteAs2DCoordinates", "true");
//			customSettings.setProperty("WriteAromaticBondTypes", "true");
//			PropertiesListener listener = new PropertiesListener(customSettings);
//			writer.addChemObjectIOListener(listener);
//			 
//			writer.write(layedOutMol);
//			writer.close();
//			b = bos.toByteArray();
//			mol = new String(b, "UTF-8");
//			System.out.println(mol);
//			MassBankUtilities mbu = new MassBankUtilities();
//			IAtomContainer test2 = mbu.getContainer(mol);
//			//IAtomContainer test2 = mbu.getContainerUnmodified("c1cccc2nnnc12", "/home/mgerlich/projects/metfusion_tp/BTs/");
//			System.out.println("aromatic Hueckel? -> " + CDKHueckelAromaticityDetector.detectAromaticity(test2));
//			System.out.println("aromatic? -> " + DoubleBondAcceptingAromaticityDetector.detectAromaticity(test2));
//			SmilesGenerator sg = new SmilesGenerator(true);
//			s = sg.createSMILES(layedOutMol);
//			System.out.println("old smiles -> " + substrucPresent);
//			System.out.println("smiles -> " + s);
//		} catch (InvalidSmilesException e2) {
//			// TODO Auto-generated catch block
//			e2.printStackTrace();
//		} catch (CDKException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		OpenBabelLocator obl = new OpenBabelLocator();
		String obmol = "";
		try {
			OpenBabelSoap obsoap = obl.getOpenBabelSoap();
			obmol = obsoap.convert(substrucPresent, "smi", "mol");
			System.out.println("obmol\n" + obmol);
		} catch (ServiceException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		MassSpecAPISoapProxy chemSpiderProxy = new MassSpecAPISoapProxy();
		SearchSoapProxy ssp = new SearchSoapProxy();
		SubstructureSearchOptions sso = new SubstructureSearchOptions(substrucPresent, false);
		//sso.setMatchTautomers(false);
		//sso.setMolecule(substrucPresent);
		
		CommonSearchOptions cso = new CommonSearchOptions(EComplexity.Single, EIsotopic.NotLabeled, false, false);
		//cso.setComplexity(EComplexity.Single);
		//cso.setIsotopic(EIsotopic.NotLabeled);	// NotLabeled when using Formula search
//		cso.setComplexity(EComplexity.Any);
//		cso.setIsotopic(EIsotopic.Any);
		//cso.setHasSpectra(false);
		//cso.setHasPatents(false);
		String transactionID = "";
		ERequestStatus ers = null;
		
		try {
			transactionID = ssp.substructureSearch(sso, cso, token);
			System.out.println("transaction id -> " + transactionID);
			ers = ssp.getAsyncSearchStatus(transactionID, token);
			while(ers.equals(ERequestStatus.Processing))  {
				Thread.sleep(2000);
				ers = ssp.getAsyncSearchStatus(transactionID, token);
			}
		} catch (RemoteException e1) {
			e1.printStackTrace();
			return candidates;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return candidates;
		}
		
		if(ers.equals(ERequestStatus.Failed)) {
			System.out.println("failed");
			return candidates;
		}
		
		if(ers.equals(ERequestStatus.ResultReady)) {
			int[] CSIDs = null;
			System.out.println("woohoo");
			try {
				CSIDs = ssp.getAsyncSearchResult(transactionID, token); 
			}
			catch (RemoteException e) {
				System.err.println("Error retrieving information and parsing results.");
				
				String resultURL = "http://www.chemspider.com/Search.asmx/GetAsyncSearchResult?rid=%s&token=%s";
				String format = String.format(resultURL, transactionID, token);
				try {
					URL u = new URL(format);
					URLConnection con = u.openConnection();
					InputStream is = con.getInputStream();
					String ids = IOUtils.toString(is);
					is.close();
					
					Document doc = Jsoup.parse(ids);
					Elements elem = doc.getElementsByTag("int");
					CSIDs = new int[elem.size()];
					for (int i = 0; i < CSIDs.length; i++) {
						CSIDs[i] = Integer.parseInt(elem.get(i).text().trim());
					}
				} catch (MalformedURLException e1) {
					System.err.println("Wrong URL for retrieving results!\n" + format);
				} catch (IOException e1) {
					System.err.println("Error parsing results!");
				}
			}
				
			if(CSIDs == null || CSIDs.length == 0)
				return candidates;
			
			System.out.println("#CSIDs -> " + CSIDs.length);
			int arrLength = CSIDs.length;
			int splitLength = 1000;
//				if(CSIDs.length > splitLength)
//					CSIDs = Arrays.copyOf(CSIDs, splitLength);
			int[] temp = new int[1];
			int numSplits = arrLength / splitLength;
			int remaining = arrLength % splitLength;
			if(numSplits == 0) {
				try {
					chemspiderInfo = chemSpiderProxy.getExtendedCompoundInfoArray(CSIDs, token);
				} catch (RemoteException e) {
					System.err.println("Error retrieving information and parsing results.");
					return candidates;
				}
			}
			else {
				int pos = 0;
				int current = 0;
				List<ExtendedCompoundInfo> eci = new ArrayList<ExtendedCompoundInfo>();
				for (int i = 0; i < numSplits; i++) {
					System.out.println("split [" + i + "] from " + numSplits);
					temp = Arrays.copyOfRange(CSIDs, pos, pos+splitLength);
					ExtendedCompoundInfo[] part;
					try {
						part = chemSpiderProxy.getExtendedCompoundInfoArray(temp, token);
					} catch (RemoteException e1) {
						System.err.println("Error retrieving information and parsing results for split [" + i + "].");
						pos = pos+splitLength;
						continue;
					}
					for (int j = 0; j < part.length; j++) {
						eci.add(part[j]);
						//chemspiderInfo[current] = part[j];
						current++;
					}
					pos = pos+splitLength;
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						System.err.println("Error while thread sleep!");
					}
				}
				// add remaining stuff
				if(remaining > 0) {
					temp = Arrays.copyOfRange(CSIDs, pos, pos+remaining);
					ExtendedCompoundInfo[] part;
					try {
						part = chemSpiderProxy.getExtendedCompoundInfoArray(temp, token);
					} catch (RemoteException e) {
						System.err.println("Error retrieving information and parsing results.");
						return candidates;
					}
					for (int j = 0; j < part.length; j++) {
						eci.add(part[j]);
						//chemspiderInfo[current] = part[j];
						current++;
					}
				}
				
				// copy list into array
				chemspiderInfo = new ExtendedCompoundInfo[eci.size()];
				for (int i = 0; i < chemspiderInfo.length; i++) {
					chemspiderInfo[i] = eci.get(i);
				}
			}
			
//			chemspiderInfo = chemSpiderProxy.getExtendedCompoundInfoArray(CSIDs, token);
//			chemspiderInfo = new ExtendedCompoundInfo[CSIDs.length];
//			for (int i = 0; i < chemspiderInfo.length; i++) {
//				chemspiderInfo[i] = chemSpiderProxy.getExtendedCompoundInfo(CSIDs[i], token);
//			}
			System.out.println("# matches -> " + chemspiderInfo.length);
			for (int i = 0; i < chemspiderInfo.length; i++) {
				System.out.println(chemspiderInfo[i].getCSID() + "\t" + chemspiderInfo[i].getSMILES());
				IAtomContainer ac = null;
				boolean used = false;
				try {
					ac = sp.parseSmiles(chemspiderInfo[i].getSMILES());
					used = true;
				}
				catch(InvalidSmilesException ise) {
					ac = null;
					used = false;
					System.err.println("skipping " + chemspiderInfo[i].getCSID());
				}
				
				candidates.add(new ResultSubstructure(chemspiderInfo[i], ac, used));
			}
			
		}
		
		return candidates;
	}
	
	private List<ResultSubstructure> filterCandidates(List<ResultSubstructure> candidates, String substructure, boolean include) {
		System.out.println("substructure filter -> " + substructure);
		System.out.println("include -> " + include);
		
		SMARTSQueryTool sqt = null;
		try {
			sqt = new SMARTSQueryTool(substructure);
		} catch (CDKException e) {
			System.err.println("Wrong smarts -> " + substructure);
			return candidates;
		}
		
		List<ResultSubstructure> remaining = new ArrayList<ResultSubstructure>();
		// filter out container that contain strucAbsent 
		boolean matches = false;
		for (ResultSubstructure rs : candidates) {
			if(!rs.isUsed())
				continue;
			
			// SMARTS matching
			try {
				matches = sqt.matches(rs.getMol());
				//System.out.println("matches -> " + matches);
				if((matches && include) || (!matches && !include)) {		// keep container
					remaining.add(rs);
				}
				// else discard container
			} catch (CDKException e) {
				System.err.println("error while matching");
				continue;
			} catch (NullPointerException e) {
				System.err.println("[" + rs.getId() + "] -> container is null?");
				continue;
			}
		}
		System.out.println("#candidates -> " + candidates.size());
		System.out.println("#remaining -> "  + remaining.size());
		return remaining;
	}
	
	private List<ResultSubstructure> filterCandidatesByMolecularFormula(List<ResultSubstructure> candidates) {
		if(molecularFormula.isEmpty())		// return unmodified candidate list if no molecular formula is present
			return candidates;
		
		IMolecularFormula filter = MolecularFormulaManipulator.getMolecularFormula(molecularFormula, DefaultChemObjectBuilder.getInstance());
		List<ResultSubstructure> remaining = new ArrayList<ResultSubstructure>();
		for (ResultSubstructure rs : candidates) {
			IAtomContainer ac = rs.getMol();
			try {
				AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(ac);
//				CDKHydrogenAdder hAdder = CDKHydrogenAdder.getInstance(ac.getBuilder());
//		        hAdder.addImplicitHydrogens(ac);
//		        AtomContainerManipulator.convertImplicitToExplicitHydrogens(ac);
			} catch (CDKException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			IMolecularFormula toCheck = MolecularFormulaManipulator.getMolecularFormula(ac);
//			if(MolecularFormulaManipulator.compare(filter, toCheck))
//				remaining.add(rs);
//			else {
//				System.out.println("Filter formula [" + molecularFormula + "] does not match candidate formula [" + 
//						MolecularFormulaManipulator.getHillString(toCheck) + "].");
//			}
			
			String csFormula = rs.getInfo().getMF();
			if(csFormula == null)
				csFormula = "";
			
			csFormula = csFormula.replaceAll("[_{}]+", "");
			
			//if(MolecularFormulaManipulator.getHillString(filter).equals(MolecularFormulaManipulator.getHillString(toCheck))) {
			//if(molecularFormula.trim().equals(rs.getInfo().getMF().trim())) {
			if(molecularFormula.trim().compareTo(csFormula) == 0) { 
				remaining.add(rs);
//				System.out.println("[" + rs.getId() + "] -> filter formula [" + molecularFormula + "] does match candidate formula [" + 
//						MolecularFormulaManipulator.getHillString(toCheck) + "].");
			}
			else {
//				System.err.println(rs.getId() + " formula " + MolecularFormulaManipulator.getHillString(toCheck) +
//						" does not match " + MolecularFormulaManipulator.getHillString(filter));
				System.err.println(molecularFormula + " does not match " + csFormula);
			}
			// alternative: mit elements und getAtomCount/getElementCount prüfen ob alle Elemente in filter
			// <= Elemente in toCheck sind
		}
		return remaining;
	}
			
	@Override
	public void run() {
		// retrieve candidates and filter candidates -> substrucPresent
		queryIncludes();
		
		// filter remaining candidates -> substrucAbsent
		List<ResultSubstructure> current = resultsRemaining;
		System.out.println("begin exclude filtering");
		System.out.println("original -> " + resultsOriginal.size());
		System.out.println("remaining -> " + resultsRemaining.size());
		if(excludes != null && excludes.size() > 0) {
			for (String ex : excludes) {
				current = filterCandidates(current, ex, substrucAbsent);
			}
			System.out.println("finally remaining filtered -> " + current.size());
		}
		if(current.isEmpty()) {
			System.err.println("Nothing left!");
			//return;
		}
		// filter remaining stuff for molecular formula, if present
		if(!molecularFormula.isEmpty()) {
			current = filterCandidatesByMolecularFormula(current);
			System.out.println("#remaining after molecular formula filter -> " + current.size());
		}
		
		this.resultsRemaining = current;	// after substrucPresent and substrucAbsent filtering
		System.out.println("resultsRemaining -> " + resultsRemaining.size());
		// use remaining candidates as intermediate entry to MetFrag?
		// or create SDF and invoke MetFrag SDF fragmentation?
		/**
		 * TODO: ChemSpider IDs als Input für MetFrag, danach regulär MetFusionBatchMode
		 */
	}
	
	public static void main(String[] args) {
		String token = "eeca1d0f-4c03-4d81-aa96-328cdccf171a";
		//String token = "a1004d0f-9d37-47e0-acdd-35e58e34f603";
		//test();
		
		//File file = new File("/home/mgerlich/projects/metfusion_tp/BTs/MetFusion_ChemSp_mfs/136m0498_MSMS.mf");
		//File file = new File("/home/mgerlich/projects/metfusion_tp/BTs/MetFusion_ChemSp_mfs/148m0859_MSMS.mf");
//		File file = new File("/home/mgerlich/projects/metfusion_tp/BTs/MetFusion_ChemSp_mfs/164m0445a_MSMS.mf");
		//File file = new File("/home/mgerlich/projects/metfusion_tp/BTs/MetFusion_ChemSp_mfs/192m0757a_MSMS.mf");
		//File file = new File("/home/mgerlich/projects/metfusion_tp/BTs/MetFusion_ChemSp_mfs/naringenin.mf");
		
		//File file = new File("/home/mgerlich/projects/metfusion_tp/BTs/Known_BT_MSMS_ChemSp/1MeBT_MSMS.mf");
		
		File file = new File("/home/mgerlich/projects/metfusion_tp/BTs/Unknown_BT_MSMS_ChemSp/mf_with_substruct/150m0655a_MSMS.mf");
		
		MetFusionBatchFileHandler mbf = new MetFusionBatchFileHandler(file);
		try {
			mbf.readFile();
		} catch (IOException e) {
			System.err.println("Error reading from MetFusion settings file [" + file.getAbsolutePath() + "]. Aborting!");
			System.exit(-1);
		}
		
		MetFusionBatchSettings settings = mbf.getBatchSettings();
		List<String> absent = settings.getSubstrucAbsent();
		List<String> present = settings.getSubstrucPresent();
		for (String s : present) {
			System.out.println("present -> " + s);
		}
		for (String s : absent) {
			System.out.println("absent -> " + s);
		}
		String formula = settings.getMfFormula();
		System.out.println("formula -> " + formula);
		
		SubstructureSearch ss = new SubstructureSearch(present, absent, token, formula);
		ss.run();
		List<ResultSubstructure> remaining = ss.getResultsRemaining();
		List<Result> resultsForSDF = new ArrayList<Result>();
		
		StringBuilder sb = new StringBuilder();
		String sep = ",";
		for (ResultSubstructure rs : remaining) {
			sb.append(rs.getId()).append(sep);
			
			Result r = new Result(rs.getPort(), rs.getId(), rs.getName(), rs.getScore());
			r.setMol(rs.getMol());
			r.setSmiles(rs.getSmiles());
			r.setInchi(rs.getInchi());
			r.setInchikey(rs.getInchikey());
			resultsForSDF.add(r);
		}
		String ids = sb.toString();
		
		if(!ids.isEmpty()) {
			ids = ids.substring(0, ids.length()-1);
			System.out.println("ids -> " + ids);
			settings.setMfDatabaseIDs(ids);
			String filename = file.getName();
			String prefix = filename.substring(0, filename.lastIndexOf("."));
			filename = filename.replace(prefix, prefix + "_ids");
			File output = new File(file.getParent(), filename);
			mbf.writeFile(output, settings);
			
			SDFOutputHandler so = new SDFOutputHandler(prefix + ".sdf");
			so.writeOriginalResults(resultsForSDF, false);
		}
	}
	
	public static void test() {
		String token = "eeca1d0f-4c03-4d81-aa96-328cdccf171a";
		MassSpecAPISoapProxy chemSpiderProxy = new MassSpecAPISoapProxy();
		try {
			ExtendedCompoundInfo cpdInfo = chemSpiderProxy.getExtendedCompoundInfo(905, token);
			System.out.println(cpdInfo.getCommonName());
			chemSpiderProxy.searchByMass2(272.04d, 0.001d);
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	
		
		SearchSoapProxy ssp = new SearchSoapProxy();
		SubstructureSearchOptions sso = new SubstructureSearchOptions();
		sso.setMatchTautomers(false);
		//sso.setMolecule("CC(=O)Oc1ccccc1C(=O)O");
		//sso.setMolecule("O=C(\\C1=C(/O)\\C(=C(\\O)C(C1=O)(C\\C=C(/C)C)C\\C=C(/C)C)C\\C=C(/C)C)C(C)C");
		sso.setMolecule("Cc1cccc2nnnc12");
		
		CommonSearchOptions cso = new CommonSearchOptions();
		cso.setComplexity(EComplexity.Any);
		cso.setIsotopic(EIsotopic.Any);		// NotLabeled when using Formula search
		cso.setHasSpectra(false);
		cso.setHasPatents(false);
		String transactionID = "";
		ERequestStatus ers = null;
		try {
			transactionID = ssp.substructureSearch(sso, cso, token);
			System.out.println("transaction id -> " + transactionID);
			ers = ssp.getAsyncSearchStatus(transactionID, token);
			while(ers.equals(ERequestStatus.Processing))  {
				Thread.sleep(2000);
				ers = ssp.getAsyncSearchStatus(transactionID, token);
			}
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(ers.equals(ERequestStatus.Failed)) {
			System.out.println("failed");
			System.exit(-1);
		}
		
		//String strucAbsent = "CC=C(C)C";
		String strucAbsent = "O=CO";		// O=CO[H]
		SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
		IAtomContainer out = null;
		try {
			out = sp.parseSmiles(strucAbsent);
		} catch (InvalidSmilesException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		if(ers.equals(ERequestStatus.ResultReady)) {
			System.out.println("woohoo");
			try {
				int[] CSIDs = ssp.getAsyncSearchResult(transactionID, token);
				ExtendedCompoundInfo[] info = chemSpiderProxy.getExtendedCompoundInfoArray(CSIDs, token);
				System.out.println("# matches -> " + info.length);
				List<IAtomContainer> containersList = new ArrayList<IAtomContainer>();
				for (int i = 0; i < info.length; i++) {
					System.out.println(info[i].getCSID() + "\t" + info[i].getCommonName() + "\t" + info[i].getSMILES());
					IAtomContainer ac = null;
					try {
						ac = sp.parseSmiles(info[i].getSMILES());
					}
					catch(InvalidSmilesException ise) {
						continue;
					}
					
					containersList.add(ac);
				}
//				String sdf = chemSpiderProxy.getRecordsSdf(transactionID, token);
//				InputStream in = IOUtils.toInputStream(sdf);
//				MDLReader reader = new MDLReader(in);
//				ChemFile chemFile = (ChemFile) reader.read((ChemObject) new ChemFile());
//				List<IAtomContainer> containersList = ChemFileManipulator.getAllAtomContainers(chemFile);
				System.out.println("# mols -> " + containersList.size());
				SMARTSQueryTool sqt = new SMARTSQueryTool(strucAbsent);
				
				// filter out container that contain strucAbsent 
				for (IAtomContainer container : containersList) {
		            
					// MCSS search
					List<IAtomContainer> mcsslist = UniversalIsomorphismTester.getOverlaps( container, out );
					int maxmcss = -9999999;
					IAtomContainer maxac = null;
					for (int j = 0; j < mcsslist.size(); j++){
					    IAtomContainer a = (IAtomContainer) mcsslist.get(j);
					    if (a.getAtomCount() > maxmcss) {		// TODO: leave out candidates that match the substructure !!!
					        maxmcss = a.getAtomCount();
					        maxac = a;
					    }
					    
					    if(a.getAtomCount() == out.getAtomCount()) {	// matching number of atoms between MCSS and structure to leave out
					    	System.out.println("#atoms in MCSS matches substrucAbsent -> filter out");
					    	break;
					    }
					}
					System.out.println("maxac -> " + maxac.getAtomCount());
					
					// SMARTS matching
					boolean matches = sqt.matches(container);	// , true
					if(matches) {		// leave out container
						System.out.println("matches");
						int nmatch = sqt.countMatches();
						List<List<Integer>> mappings = sqt.getMatchingAtoms();
						for (int i = 0; i < nmatch; i++) {
							List<Integer> atomIndices = mappings.get(i);
							System.out.println("#atom indices -> " + atomIndices.size());
						}
					}
					else {				// keep container
						System.out.println("no match");
					}
				}
				
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CDKException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else System.out.println("oh no");
	}

	public List<String> getIncludes() {
		return includes;
	}

	public void setIncludes(List<String> includes) {
		this.includes = includes;
	}

	public List<String> getExcludes() {
		return excludes;
	}

	public void setExcludes(List<String> excludes) {
		this.excludes = excludes;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public ExtendedCompoundInfo[] getChemspiderInfo() {
		return chemspiderInfo;
	}

	public void setChemspiderInfo(ExtendedCompoundInfo[] chemspiderInfo) {
		this.chemspiderInfo = chemspiderInfo;
	}

	public List<ResultSubstructure> getResultsOriginal() {
		return resultsOriginal;
	}

	public void setResultsOriginal(List<ResultSubstructure> resultsOriginal) {
		this.resultsOriginal = resultsOriginal;
	}

	public List<ResultSubstructure> getResultsRemaining() {
		return resultsRemaining;
	}

	public void setResultsRemaining(List<ResultSubstructure> resultsRemaining) {
		this.resultsRemaining = resultsRemaining;
	}

	public String getMolecularFormula() {
		return molecularFormula;
	}

	public void setMolecularFormula(String molecularFormula) {
		this.molecularFormula = molecularFormula;
	}

}
