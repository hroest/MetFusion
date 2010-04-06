/** 
 * created 19.11.2009 - 13:39:50 by Michael Gerlich
 * email: mgerlich@ipb-halle.de
 */

package de.ipbhalle.MetFlow.utilities.MassBank;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import massbank.GetConfig;
import massbank.MassBankCommon;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.openscience.cdk.ChemFile;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

/**
 * The Class MassBankUtilities.
 */
public class MassBankUtilities {

	/** The Constant baseUrl. */
	private final static String baseUrl = "http://www.massbank.jp/";	//  http://msbi.ipb-halle.de/MassBank/
	
	/** The server url. */
	private static String serverUrl;
	
	/**
	 * Write mol file.
	 * 
	 * @param id the MassBank id
	 * @param mol the mol string
	 * 
	 * @return true, if successful
	 */
	public static boolean writeMolFile(String id, String mol, String basePath) {
		boolean success = false;
		File f = new File(basePath + id + ".mol");
		if(f.exists())
			return false;
		
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(f));
			bw.write(mol);
			bw.flush();
			bw.close();
			success = true;
		} catch (IOException e) {
			e.printStackTrace();
			success = false;
		}
		return success;
	}
	
	/**
	 * Retrieve molfile.
	 * 
	 * @param compound the name of the MassBank compound
	 * @param site the site number of the MassBank record
	 * 
	 * @return the molfile as string
	 */
	public static String retrieveMol(String compound, String site, String record) {
		String reqStr = "";
		if(record.startsWith("CO") || record.startsWith("PB")) {
			reqStr = "http://msbi.ipb-halle.de/MassBank/" + "jsp/" + MassBankCommon.DISPATCHER_NAME;
			if(record.startsWith("CO"))
				site = "1";
			else if(record.startsWith("PB"))
				site = "0";				
		}
		else reqStr = baseUrl + "jsp/" + MassBankCommon.DISPATCHER_NAME;
//		try {
//			compound = URLEncoder.encode(compound, "UTF-8");
//			System.out.println("compound encoded -> " + compound);
//		} catch (UnsupportedEncodingException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//			compound = compound.replaceAll(" ", "%20");
//		}
		HttpClient client = new HttpClient();
		PostMethod method = new PostMethod(reqStr);
		method.addParameter("type", "mol");
		method.addParameter("qtype", "n");
		method.addParameter("otype", "m");
		method.addParameter("query", compound);
		method.addParameter("site", site);
		
		String mol = "";
		try {
			client.executeMethod(method);
			mol = method.getResponseBodyAsString();
			if(mol.contains("M  EN") && !mol.contains("M  END")) {
				mol = mol.replace("M  EN", "M  END");
			}
			if(!mol.equals("0\n")) {	// (do not) change molfile properties block (first 3 rows)
				Pattern p = Pattern.compile("[0-9]+\n[0-9]+\n");
				mol = p.matcher(mol).replaceFirst("");
			}
			if(mol.contains("<!DOCTYPE html PUBLIC")) {	// reset mol mol data if encountered error on server
				mol = "";
			}
			if(mol.equals("0\n")){		// found no molfile
				mol = "";		// reset corrupt/missing mol data to empty string
			}
		} catch (HttpException e) {
			//e.printStackTrace();
			System.err.println("Error for ["+compound+"] !!!");
			System.err.println("HttpException occured.");
		} catch (IOException e) {
			//e.printStackTrace();
			System.err.println("Error for ["+compound+"] !!!");
			System.err.println("IOException occured.");
		}
		
		// wait for one second to prevent DOS for server
		try {
			Thread.sleep(1000L);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return mol;
	}
	
	/**
	 * Retrieve the MassBank site of a compound.
	 * 
	 * @param id the MassBank record id
	 * 
	 * @return the site number as string, -1 if id not found, else >= 0
	 */
	public static String retrieveSite(String id) {
		MassBankCommon mbcommon = new MassBankCommon();
		GetConfig conf = new GetConfig(baseUrl);
		serverUrl = conf.getServerUrl();
		String typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_RECORD];
		ArrayList result = mbcommon.execMultiDispatcher( serverUrl, typeName, "id=" + id );
		String line = "";
		Boolean isFound = false;
		String[] val = null;
		for ( int i = 0; i < result.size(); i++ ) {
			line = (String)result.get(i);
			if ( !line.equals("") ) {
				val = line.split("\t");
				if ( val[0].equals(id) ) {
					isFound = true;
					break;
				}
			}
		}
		if ( !isFound ) {
			return "-1";
		}
		else {
			return val[1];
		}
	}
	
	public static String retrieveRecord(String id, String site) throws IOException {
		String content = "";
		
		File f = new File("/home/mgerlich/workspace-3.5/MassBankComparison/MBCache/" + id + ".txt");
		if(!f.exists()) {
			String reqStr = "http://msbi.ipb-halle.de/MassBank/";
			reqStr += "jsp/" + MassBankCommon.DISPATCHER_NAME;
			
			HttpClient client = new HttpClient();
			PostMethod method = new PostMethod( reqStr );
			method.addParameter("type", "disp");		// display record
			method.addParameter("id", id);				// specify record
			method.addParameter("site", site);
			
			client.executeMethod(method);
			String result1 = method.getResponseBodyAsString();
			String result = result1.substring(result1.indexOf("ACCESSION"), result1.indexOf("</pre>"));
			FileWriter fw = new FileWriter(f);
			fw.write(result);
			fw.flush();
			fw.close();
			
			return result;
		}
		
		BufferedReader br = new BufferedReader(new FileReader(f));
		String line = "";
		content = "";
		while((line = br.readLine()) != null) {
			content += line;
		}
		br.close();
		
		return content;
	}
	
	public static Map<String, String> retrieveLinks(String id, String site) {
		Map<String, String> dbs = new HashMap<String, String>();
		
		File f = new File("/home/mgerlich/workspace-3.5/MassBankComparison/MBCache/" + id + ".txt");
		if(!f.exists()) {
			String reqStr = "http://msbi.ipb-halle.de/MassBank/";
			reqStr += "jsp/" + MassBankCommon.DISPATCHER_NAME;
			
			HttpClient client = new HttpClient();
			PostMethod method = new PostMethod( reqStr );
			method.addParameter("type", "disp");		// display record
			method.addParameter("id", id);				// specify record
			method.addParameter("site", site);
			
			try {
				client.executeMethod(method);
				String result1 = method.getResponseBodyAsString();
				String result = result1.substring(result1.indexOf("ACCESSION"), result1.indexOf("</pre>"));
				FileWriter fw = new FileWriter(f);
				fw.write(result);
				fw.flush();
				fw.close();
			} catch (HttpException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(f));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String line = "";
		String db = "";
		String val = "";
		try {
			while((line = br.readLine()) != null) {
				if(line.startsWith("CH$LINK:")) {
					String[] split = line.split(" ");
					db = split[1];
					for (int i = 2; i < split.length; i++) {
						if(split[i].contains("</a>")) {
							val = split[i].substring(split[i].indexOf("\"_blank\">") + 9, split[i].indexOf("</a>"));
							break;
						}
					}
					dbs.put(db, val);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return dbs;
	}
	
	/**
	 * Retrieves the IAtomContainer for a moldata string.
	 * 
	 * @param mol the moldata as String
	 * 
	 * @return the container as IAtomContainer object, null if Exception occurs
	 */
	public static IAtomContainer getContainer(String mol) {
		if(mol.isEmpty() || mol.equals("0\n")) {
			return null;
		}
		InputStream is = new ByteArrayInputStream(mol.getBytes());
		MDLV2000Reader reader = new MDLV2000Reader(is);
		IChemFile chemFile = new ChemFile();
		IAtomContainer container = null;
		try {			
			chemFile = (IChemFile) reader.read(chemFile);
			container = ChemFileManipulator.getAllAtomContainers(chemFile).get(0);
		} catch (CDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return container;
	}
}
