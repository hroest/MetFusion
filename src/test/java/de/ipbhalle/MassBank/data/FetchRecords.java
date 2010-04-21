/**
 * created by Michael Gerlich on Apr 14, 2010
 * last modified Apr 14, 2010 - 10:43:41 AM
 * email: mgerlich@ipb-halle.de
 */
package de.ipbhalle.MassBank.data;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import massbank.GetConfig;
import massbank.MassBankCommon;

import de.ipbhalle.MetFlow.utilities.MassBank.MassBankUtilities;


public class FetchRecords {

	private static final String[] indexType = { "site", "inst", "ion", "cmpd" };
	private static final String[] tblName = { "Contributor", "Instrument Type", "Ionization Mode", "Compound Name" };
	private static final String[] header = { "INSTRUMENT", "ION", "COMPOUND" };
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// new instance of MassBank API
		MassBankCommon mbcommon = new MassBankCommon();
		// configure MassBank API instance
		String baseUrl = "http://www.massbank.jp/"; //"http://www.massbank.jp/";	//http://msbi.ipb-halle.de/MassBank/
		int pos = "http://www.massbank.jp/jsp".indexOf("/jsp");
		GetConfig conf = new GetConfig(baseUrl);
		String serverUrl = conf.getServerUrl();
		String[] siteNameList = conf.getSiteLongName();
		
		// typeName set to index count
		String typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_IDXCNT];
		// result list
		List result = mbcommon.execMultiDispatcher( serverUrl, typeName, "" );
		
		Map<String, Integer>[] countMap = new HashMap[siteNameList.length];
		ArrayList countList = new ArrayList();
		List<String> keyList = new ArrayList<String>();
		String[] siteCount = new String[siteNameList.length];
		
		for ( int siteNum = 0; siteNum < siteNameList.length; siteNum++ ) {
			countMap[siteNum] = new HashMap<String, Integer>();
		}
		
		String inst = "&inst_grp=ESI&inst=ESI-QqTOF-MS/MS"; // instrument parameter
		
//		String param = "quick=true&CEILING=1000&WEIGHT=SQUARE&NORM=SQRT&START=1&TOLUNIT=unit"
//			 + "&CORTYPE=COSINE&FLOOR=0&NUMTHRESHOLD=3&CORTHRESHOLD=0.8&TOLERANCE=0.3"
//			 + "&CUTOFF=" + pCutoff + "&NUM="+ num + "&VAL=" + massBankRecord.getPeakQuery()
//			 + inst + "&ION=" + mode;
		
		int[] numRows = new int[header.length + 1];
		numRows[0] = siteNameList.length;
		for ( int i = 0; i < result.size(); i++ ) {
			String line = (String)result.get(i);
			if ( !line.equals("") ) {
				String[] fields = line.split("\t");
				int siteNum = Integer.parseInt(fields[fields.length - 1]);
				String key = fields[0];
				
				int count = Integer.parseInt( fields[1] );
				if ( countMap[siteNum].get( key ) != null ) {
					count = count + countMap[siteNum].get( key );
				}
				countMap[siteNum].put( key, count );

				boolean isFound = false;
				for ( int j = 0; j < keyList.size(); j++ ) {
					if ( keyList.get(j).equals(key) ) {
						isFound = true;
						break;
					}
				}
				if ( !isFound ) {
					keyList.add( key );
					
					for ( int j = 0; j < header.length; j++ ) {
						if ( key.indexOf( header[j] ) >= 0 ) {
							numRows[j+1]++;
							break;
						}
					}
				}
			}
		}
		List<String> adjustKeyList = new ArrayList<String>();
		adjustKeyList.add( "//" );
		adjustKeyList.add( keyList.get(0) );
		adjustKeyList.add( "//" );
		for ( int i = 0; i < header.length; i++ ) {
			for ( int j = 1; j < keyList.size(); j++ ) {
				String key = keyList.get(j);
				pos = key.indexOf(":");
				String keyHead = key.substring( 0, pos );
				if ( header[i].equals( keyHead ) ) {
					adjustKeyList.add( key );
				}
			}
			adjustKeyList.add( "//" );
		}
		
		for (int i = 0; i < countMap.length; i++) {
			Set<String> keys = countMap[i].keySet();
			for (String key : keys) {
				if(key.startsWith("INSTRUMENT:"))
						System.out.println(key);
			}
		}
	}

	static void copy( InputStream in, OutputStream out ) throws IOException 
	  { 
	    byte[] buffer = new byte[ 0xFFFF ]; 
	    for ( int len; (len = in.read(buffer)) != -1; ) 
	      out.write( buffer, 0, len ); 
	  } 
	
	static void copyFile(String src, String dest) {
		FileInputStream fis = null;
		FileOutputStream fos = null;

		try {
			fis = new FileInputStream(src);
			fos = new FileOutputStream(dest);

			copy(fis, fos);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fis != null)
				try {
					fis.close();
				} catch (IOException e) {
				}
			if (fos != null)
				try {
					fos.close();
				} catch (IOException e) {
				}
		}
	}
}
