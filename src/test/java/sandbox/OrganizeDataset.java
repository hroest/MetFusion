/**
 * created by Michael Gerlich, Jul 19, 2012 - 9:18:09 AM
 */ 

package sandbox;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.SDFWriter;

import de.ipbhalle.metfusion.utilities.MassBank.MassBankUtilities;

public class OrganizeDataset {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String cacheMassBank = "/vol/massbank/Cache/";
		String recDir = "/home/mgerlich/Datasets/allSpectra/";
		File recs = new File(recDir);
		File[] list = recs.listFiles();
		MassBankUtilities mbu = new MassBankUtilities();
		SDFWriter sdfw = new SDFWriter(new FileOutputStream(new File("/home/mgerlich/dataset.sdf")));
		
		for (int i = 0; i < list.length; i++) {
			String id = list[i].getName().substring(0, list[i].getName().indexOf("."));
			String prefix = "";
    		if(id.matches("[A-Z]{3}[0-9]{5}"))
    			prefix = id.substring(0, 3);
    		else prefix = id.substring(0, 2);
    		
    		String[] info = mbu.getPeaklistFromFile(list[i]);
    		id = info[2];
    		
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
            
            IAtomContainer container = null;
            // first look if container is present, then download if not
            container = mbu.getContainer(id, basePath);
            container.setProperty("cdk:Title", id);
            
            Map<String, String> links = mbu.retrieveLinks(id, "0");
            if(links.containsKey("KEGG"))
            	container.setProperty("KEGG", links.get("KEGG"));	// not all records have KEGG ID's
            
            container.setProperty("PUBCHEM", links.get("PUBCHEM")); // but all records do have PUBCHEM ID's
            
            try {
				sdfw.write(container);
			} catch (CDKException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		sdfw.close();
	}

}
