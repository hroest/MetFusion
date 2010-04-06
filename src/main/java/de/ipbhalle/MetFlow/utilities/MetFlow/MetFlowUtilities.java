/** 
 * created 23.11.2009 - 10:43:49 by Michael Gerlich
 * email: mgerlich@ipb-halle.de
 */

package de.ipbhalle.MetFlow.utilities.MetFlow;

import gov.nih.nlm.ncbi.pubchem.CompressType;
import gov.nih.nlm.ncbi.pubchem.FormatType;
import gov.nih.nlm.ncbi.pubchem.PCIDType;
import gov.nih.nlm.ncbi.pubchem.PUGLocator;
import gov.nih.nlm.ncbi.pubchem.PUGSoap;
import gov.nih.nlm.ncbi.pubchem.StatusType;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.rmi.RemoteException;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.xml.rpc.ServiceException;

import org.openscience.cdk.ChemFile;
import org.openscience.cdk.ChemObject;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

import com.chemspider.www.MassSpecAPISoapProxy;

import keggapi.KEGGLocator;
import keggapi.KEGGPortType;

/**
 * The Class MetFlowUtilities. An utility class for calling DB webservices of KEGG, PubChem and ChemSpider to retrieve moldata via corresponding key.
 */
public class MetFlowUtilities {
	
	/** The Constant security token required by ChemSpider. */
	private static final String token = "a1004d0f-9d37-47e0-acdd-35e58e34f603";
	
	/**
	 * KEGG get mol from id.
	 * 
	 * @param id the KEGG compound id
	 * 
	 * @return the i atom container
	 */
	public static IAtomContainer KEGGGetMolFromID(String id) {
		if(!id.matches("C[0-9]{5}"))
			return null;
		
		KEGGLocator locator = new KEGGLocator();
		try {
			KEGGPortType serv = locator.getKEGGPort();
			//get molecule by accession ID
            String str = "-f m cpd:" + id;
            String ret = serv.bget(str);

            MDLV2000Reader reader;
    		List<IAtomContainer> containersList;
    		
            reader = new MDLV2000Reader(new StringReader(ret));
            ChemFile chemFile = (ChemFile)reader.read((ChemObject)new ChemFile());
            containersList = ChemFileManipulator.getAllAtomContainers(chemFile);
            IAtomContainer molecule = containersList.get(0);
            
            return molecule;
		} catch (ServiceException e) {
			e.printStackTrace();
			System.err.println("Service Exception occured for KEGG!");
			return null;
		} catch (RemoteException e) {
			e.printStackTrace();
			System.err.println("Remote Exception occured for KEGG!");
			return null;
		} catch (CDKException e) {
			e.printStackTrace();
			System.err.println("CDK Exception occured for KEGG!");
			return null;
		}
	}
	
	/**
	 * Chem spider get mol from id.
	 * 
	 * @param id the id
	 * 
	 * @return the i atom container
	 */
	public static IAtomContainer ChemSpiderGetMolFromID(String id) {
		MassSpecAPISoapProxy chemSpiderProxy = new MassSpecAPISoapProxy();
		String mol = "";
		
		try {
			mol = chemSpiderProxy.getRecordMol(id, false, token);
		} catch (RemoteException e) {
			e.printStackTrace();
			System.err.println("Remote Exception occured for ChemSpider!");
			return null;
		}
		
		MDLV2000Reader reader;
		List<IAtomContainer> containersList;
		
        reader = new MDLV2000Reader(new StringReader(mol));
        ChemFile chemFile = null;
		try {
			chemFile = (ChemFile) reader.read((ChemObject)new ChemFile());
		} catch (CDKException e) {
			e.printStackTrace();
			System.err.println("CDK Exception occured for ChemSpider!");
			return null;
		}
        containersList = ChemFileManipulator.getAllAtomContainers(chemFile);
        IAtomContainer molecule = containersList.get(0);
        
        return molecule;
	}
	
	/**
	 * Pub chem get mol from id.
	 * 
	 * @param id the id
	 * 
	 * @return the i atom container
	 */
	public static IAtomContainer PubChemGetMolFromID(String id) {
		try {
			int cid = Integer.parseInt(id);
			return PubChemGetMolFromID(cid);
		}
		catch (NumberFormatException e) {
			e.printStackTrace();
			System.err.println("Error converting String id into integer format for PubChem CID!");
			return null;
		}
	}
	
	/**
	 * Pub chem get mol from id.
	 * 
	 * @param id the id
	 * 
	 * @return the i atom container
	 */
	public static IAtomContainer PubChemGetMolFromID(int id) {
		if(id < 0)
			return null;
		
		int[] cid = new int[1];
		cid[0] = id;
		return PubChemGetMolsFromIDs(cid);
	}
	
	/**
	 * Pub chem get mols from i ds.
	 * 
	 * @param ids the ids
	 * 
	 * @return the i atom container
	 */
	public static IAtomContainer PubChemGetMolsFromIDs(int[] ids) {
		PUGLocator locator = new PUGLocator();
		PUGSoap soap = null;
		
		IAtomContainer ac = null;

		try {
			soap = locator.getPUGSoap();
		} catch (ServiceException e) {
			e.printStackTrace();
			System.err.println("Service Exception occured for PubChem PUG!");
			return null;
		}

		String listKey = "";
		try {
			listKey = soap.inputList(ids, PCIDType.eID_CID);
		} catch (RemoteException e1) {
			e1.printStackTrace();
			System.err.println("Remote Exception occured for PubChem PUG!");
			return null;
		}
		
		System.out.println("ListKey = " + listKey);
	    try {
			System.out.println("number of compounds = " + soap.getListItemsCount(listKey));
		} catch (RemoteException e1) {
			e1.printStackTrace();
			System.err.println("Remote Exception occured for PubChem PUG!");
			return null;
		}
	        
	    // Initialize the download; request SDF with gzip compression
		String downloadKey = "";
		try {
			downloadKey = soap.download(listKey,FormatType.eFormat_SDF, CompressType.eCompress_GZip, false);
		} catch (RemoteException e1) {
			e1.printStackTrace();
			System.err.println("Remote Exception occured for PubChem PUG!");
			return null;
		}
		System.out.println("DownloadKey = " + downloadKey);
	        
	    // Wait for the download to be prepared
		StatusType status = null;
		try {
			while ((status = soap.getOperationStatus(downloadKey)) == StatusType.eStatus_Running || 
			           status == StatusType.eStatus_Queued) {

				System.out.println("Waiting for download to finish...");
			    Thread.sleep(10000);
			}
		} catch (RemoteException e1) {
			e1.printStackTrace();
			System.err.println("Remote Exception occured for PubChem PUG!");
			return null;
		} catch (InterruptedException e1) {
			e1.printStackTrace();
			System.err.println("Interrupted Exception occured for PubChem PUG!");
		}
        
        // On success, get the download URL, save to local file
        if (status == StatusType.eStatus_Success) {
        	// PROXY
        	try {
        		System.setProperty("ftp.proxySet", "true");
        		System.setProperty("ftp.proxyHost", "www.ipb-halle.de");
        		System.setProperty("ftp.proxyPort", "3128");
        	}
        	catch (SecurityException  e) {
        		e.printStackTrace();
        		System.err.println("Security Exception while setting Proxy parameters!");
        		//return null;
        	}
        	
            URL url = null;
			try {
				url = new URL(soap.getDownloadUrl(downloadKey));
			} catch (MalformedURLException e1) {
				e1.printStackTrace();
				System.err.println("Malformed URL Exception occured for PubChem PUG!");
				return null;
			} catch (RemoteException e1) {
				e1.printStackTrace();
				System.err.println("Remote Exception occured for PubChem PUG!");
				return null;
			}
            System.out.println("Success! Download URL = " + url.toString());
            
            // get input stream from URL
            URLConnection fetch;
			try {
				fetch = url.openConnection();
			} catch (IOException e1) {
				e1.printStackTrace();
				System.err.println("IO Exception occured for PubChem PUG!");
				return null;
			}
            InputStream input;
			try {
				input = fetch.getInputStream();
			} catch (IOException e1) {
				e1.printStackTrace();
				System.err.println("IO Exception occured for PubChem PUG!");
				return null;
			}
            
            // open local file based on the URL file name
            String filename = "/temp" + url.getFile().substring(url.getFile().lastIndexOf('/'));
            FileOutputStream output;
			try {
				output = new FileOutputStream(filename);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				System.err.println("File not Found Exception occured for PubChem PUG!");
				return null;
			}
            System.out.println("Writing data to " + filename);
            
            // buffered read/write
            byte[] buffer = new byte[10000];
            int n;
            try {
				while ((n = input.read(buffer)) > 0)
				    output.write(buffer, 0, n);
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("IO Exception occured for PubChem PUG!");
				return null;
			}
			
			//now read in the file
			FileInputStream in = null;
			GZIPInputStream gin = null;
	        try {
				in = new FileInputStream(filename);
				gin = new GZIPInputStream(in);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				System.err.println("File not found Exception occured for PubChem PUG!");
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("IO Exception occured for PubChem PUG!");
				return null;
			}
	        
	        //IChemObjectReader cor = null;
	        //cor = new ReaderFactory().createReader(in);
	       
	        MDLV2000Reader reader = new MDLV2000Reader(gin);
	        ChemFile fileContents = null;
			try {
				fileContents = (ChemFile) reader.read(new ChemFile());
			} catch (CDKException e) {
				e.printStackTrace();
				System.err.println("CDK Exception occured for PubChem PUG!");
				return null;
			}
	        System.out.println("Got " + fileContents.getChemSequence(0).getChemModelCount() + " atom containers");
	        ac = fileContents.getChemSequence(0).getChemModel(0).getMoleculeSet().getAtomContainer(0);
	        
	        return ac;
        }
        else {
            try {
				System.out.println("Error: " + soap.getStatusMessage(downloadKey));
			} catch (RemoteException e) {
				e.printStackTrace();
				System.err.println("Remote Exception occured for PubChem PUG!");
				return null;
			}            
        }

		
		return null;
	}
}
