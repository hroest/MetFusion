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

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.smiles.smarts.SMARTSQueryTool;

import com.chemspider.www.CommonSearchOptions;
import com.chemspider.www.EComplexity;
import com.chemspider.www.EIsotopic;
import com.chemspider.www.ERequestStatus;
import com.chemspider.www.ExtendedCompoundInfo;
import com.chemspider.www.MassSpecAPISoapProxy;
import com.chemspider.www.SearchSoapProxy;
import com.chemspider.www.SubstructureSearchOptions;

import de.ipbhalle.metfusion.wrapper.ResultSubstructure;

public class SubstructureSearch implements Runnable {

	private List<String> includes;
	private List<String> excludes;
	private String token;
	public final boolean substrucPresent = Boolean.TRUE;
	public final boolean substrucAbsent = Boolean.FALSE;
	private ExtendedCompoundInfo[] chemspiderInfo;
	private List<ResultSubstructure> resultsOriginal;
	private List<ResultSubstructure> resultsRemaining;
	
	
	public SubstructureSearch(List<String> includes, List<String> excludes, String token) {
		this.includes = includes;
		this.excludes = excludes;
		this.setToken(token);
	}
	
	private void queryIncludes() {
		if(includes.size() == 1) {		// only one substructure filter
			try {
				resultsOriginal = queryDatabase(includes.get(0));
				resultsRemaining = skipNonUsed(resultsOriginal);
				System.out.println("includes == 1 \toriginal = " + resultsOriginal.size());
			} catch (CDKException e) {
				resultsOriginal = new ArrayList<ResultSubstructure>();
				e.printStackTrace();
			}
		}
		else if(includes.size() > 1) {
			for (int i = 0; i < includes.size(); i++) {
				if(i == 0) {
					try {
						resultsOriginal = queryDatabase(includes.get(0));
						resultsRemaining = skipNonUsed(resultsOriginal);
					} catch (CDKException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
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
	
	private List<ResultSubstructure> queryDatabase(String substrucPresent) throws CDKException {
		List<ResultSubstructure> candidates = new ArrayList<ResultSubstructure>();
		
		MassSpecAPISoapProxy chemSpiderProxy = new MassSpecAPISoapProxy();
		SearchSoapProxy ssp = new SearchSoapProxy();
		SubstructureSearchOptions sso = new SubstructureSearchOptions();
		sso.setMatchTautomers(false);
		sso.setMolecule(substrucPresent);
		
		CommonSearchOptions cso = new CommonSearchOptions();
		cso.setComplexity(EComplexity.Single);
		cso.setIsotopic(EIsotopic.NotLabeled);
		cso.setHasSpectra(false);
		cso.setHasPatents(false);
		String transactionID = "";
		ERequestStatus ers = null;
		SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
		
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
			System.out.println("woohoo");
			try {
				int[] CSIDs = ssp.getAsyncSearchResult(transactionID, token);
				chemspiderInfo = chemSpiderProxy.getExtendedCompoundInfoArray(CSIDs, token);
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
						System.err.println("skipping " + chemspiderInfo[i].getCSID());
					}
					
					candidates.add(new ResultSubstructure(chemspiderInfo[i], ac, used));
				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
			// SMARTS matching
			try {
				matches = sqt.matches(rs.getContainer());
				//System.out.println("matches -> " + matches);
				if((matches && include) || (!matches && !include)) {		// keep container
					remaining.add(rs);
				}
				// else discard container
			} catch (CDKException e) {
				System.err.println("error while matching");
				continue;
			}
		}
		System.out.println("#candidates -> " + candidates.size());
		System.out.println("#remaining -> "  + remaining.size());
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
		// TODO
		this.resultsRemaining = new ArrayList<ResultSubstructure>();
		for (ResultSubstructure rs : resultsOriginal) {
			if(rs.isUsed())
				resultsRemaining.add(rs);
		}
		
		System.out.println("resultsRemaining -> " + resultsRemaining.size());
		// use remaining candidates as intermediate entry to MetFrag?
		// or create SDF and invoke MetFrag SDF fragmentation?
	}
	
	public static void main(String[] args) {
		String token = "eeca1d0f-4c03-4d81-aa96-328cdccf171a";
		//test();
		//File file = new File("/home/mgerlich/projects/metfusion_tp/BTs/MetFusion_ChemSp_mfs/192m0757a_MSMS.mf");
		File file = new File("/home/mgerlich/projects/metfusion_tp/BTs/MetFusion_ChemSp_mfs/164m0445a_MSMS.mf");
		MetFusionBatchFileHandler mbf = new MetFusionBatchFileHandler(file);
		try {
			mbf.readFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		SubstructureSearch ss = new SubstructureSearch(present, absent, token);
		ss.run();
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
		cso.setComplexity(EComplexity.Single);
		cso.setIsotopic(EIsotopic.NotLabeled);
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

}
