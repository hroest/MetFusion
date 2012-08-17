/**
 * created by Michael Gerlich, Jul 24, 2012 - 1:05:03 PM
 */ 

package sandbox;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.interfaces.IAtomContainer;

import de.ipbhalle.io.FileNameFilterImpl;
import de.ipbhalle.metfusion.utilities.MassBank.MassBankUtilities;

public class UniqueCompounds {

	static String cacheMassBank = "/vol/massbank/Cache/";
	
	/**
	 * @param args
	 * @throws CDKException 
	 */
	public static void main(String[] args) throws CDKException {
		File spectraDir = new File("/home/mgerlich/Datasets/allSpectra");
		File[] spectra = spectraDir.listFiles(new FileNameFilterImpl("", ".txt"));
		System.out.println("#spectra -> " + spectra.length);
		
		MassBankUtilities mbu = new MassBankUtilities();
		InChIGeneratorFactory igf = InChIGeneratorFactory.getInstance();
		
		int currentPeaks = 0;
		Map<String, Integer> idToNumPeaks = new HashMap<String, Integer>();
		Map<String, IAtomContainer> idToContainer = new HashMap<String, IAtomContainer>();
		Map<String, String> idToKey = new HashMap<String, String>();
		Map<String, List<String>> keyToIDs = new HashMap<String, List<String>>();
		List<String> interestingSpectra = new ArrayList<String>();
		Map<String, Integer> idToIndex = new HashMap<String, Integer>();
		
		for (int i = 0; i < spectra.length; i++) {
			String id = spectra[i].getName().substring(0, 8);
			idToIndex.put(id, i);
			
			String prefix = "";
    		if(id.matches("[A-Z]{3}[0-9]{5}"))
    			prefix = id.substring(0, 3);
    		else prefix = id.substring(0, 2);
            File dir = new File(cacheMassBank);
            String[] institutes = dir.list();
            File f = null;
            String basePath = "";
            for (int j = 0; j < institutes.length; j++) {
                if(institutes[j].equals(prefix)) {
                    f = new File(dir, institutes[j] + "/mol/");
                    basePath = f.getAbsolutePath();
                    if(!basePath.endsWith("/"))
                            basePath += "/";
                    break;
                }
            }
			
            String[] info = mbu.getPeaklistFromFile(spectra[i]);
			currentPeaks = info[0].split("\n").length;
            idToNumPeaks.put(id, currentPeaks);
            
            IAtomContainer container = null;
            container = mbu.getContainer(id, basePath);
            idToContainer.put(id, container);
            
            InChIGenerator ig = igf.getInChIGenerator(container);
            String inchiKey = ig.getInchiKey();
            inchiKey = inchiKey.split("-")[0];
            idToKey.put(id, inchiKey);
            
            if(keyToIDs.containsKey(inchiKey)) {
            	List<String> ids = keyToIDs.get(inchiKey);
            	if(!ids.contains(id))
            		ids.add(id);
            	
            	keyToIDs.put(inchiKey, ids);
            }
            else {
            	List<String> ids = new ArrayList<String>();
            	ids.add(id);
            	keyToIDs.put(inchiKey, ids);
            }
		}
		
		Set<String> inchiKeys = keyToIDs.keySet();
		for (String key : inchiKeys) {
			int currentMaxPeaks = 0;
			List<String> ids = keyToIDs.get(key);
			String maxId = "";
			for (String s : ids) {
				currentPeaks = idToNumPeaks.get(s);
				if(currentPeaks > currentMaxPeaks)
					maxId = s;
			}
			interestingSpectra.add(maxId);
		}
		
		String outDir = "/home/mgerlich/Datasets/christoph/";
		for (String s : interestingSpectra) {
			File spec = spectra[idToIndex.get(s)];
			try {
				IOUtils.copy(new FileReader(spec), new FileOutputStream(new File(outDir, spec.getName())));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("# interesting spectra -> " + interestingSpectra.size());
	}

}
