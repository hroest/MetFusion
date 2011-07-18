/**
 * created by Michael Gerlich on May 7, 2010
 * last modified May 7, 2010 - 11:06:55 AM
 * email: mgerlich@ipb-halle.de
 */
package de.ipbhalle.GridEngine.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openscience.cdk.ChemFile;
import org.openscience.cdk.ChemObject;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

import de.ipbhalle.metfusion.wrapper.Result;

public class CompareHillCompounds {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws CDKException 
	 */
	public static void main(String[] args) throws IOException, CDKException {
		//String dirAnno = "/vol/www/msbi/DocumentRoot/MassBankOld/DB/annotation/MassBankUconn";
		String dirMol = "/vol/www/msbi/DocumentRoot/MassBankOld/DB/molfile/MassBankUconn";
		String assoc = "/home/mgerlich/workspace-3.5/MetFlowICE/testdata/uconnResult.txt";
		
		File fAssoc = new File(assoc);
		Map<String, String> mapping = new HashMap<String, String>();
		
		BufferedReader br = new BufferedReader(new FileReader(fAssoc));
		String line = "";
		while((line = br.readLine()) != null) {
			System.out.println(line);
			String[] split = line.split("\t");
			mapping.put(split[0], split[1]);
		}
		br.close();
		
		List<Result> primaries = new ArrayList<Result>();
		List<Result> cands = new ArrayList<Result>();
		
		File fMol = new File(dirMol);
		File[] files = fMol.listFiles();
		for (File file : files) {
			String id = file.getName().substring(0, file.getName().indexOf("."));
			System.out.println(id);
			String name = mapping.get(id);
			System.out.println(name);
			
			IAtomContainer ac = null;
			MDLV2000Reader reader;
    		List<IAtomContainer> containersList;
            reader = new MDLV2000Reader(new FileReader(file));
            ChemFile chemFile = (ChemFile)reader.read((ChemObject)new ChemFile());
            containersList = ChemFileManipulator.getAllAtomContainers(chemFile);
            ac = containersList.get(0);
//            new WorkflowOutputAlignment(port, KEGG, score, db, id, compoundName, dbs, true, record, ac)
            primaries.add(new Result(name, id, name, 0, ac));
            cands.add(new Result(name, id, name, 0, ac));
            
            System.out.println(primaries.size());
		}
		
		//SimilarityTanimoto sim = new SimilarityTanimoto(primaries, cands);
		//TanimotoSimilarity sim;
		//File temp = new File("/home/mgerlich/workspace-3.5/MetFlowICE/testdata/HillMatrix.tsv");
		
	}

}
