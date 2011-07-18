/**
 * created by Michael Gerlich on Jun 11, 2010
 * last modified Jun 11, 2010 - 2:44:18 PM
 * email: mgerlich@ipb-halle.de
 */
package de.ipbhalle.MassBank;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import massbank.GetConfig;
import massbank.MassBankCommon;
import massbank.ResultList;
import massbank.ResultRecord;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;

public class IPB_MassBank_test {

	/**
	 * @param args
	 * @throws UnsupportedEncodingException 
	 */
	public static void main(String[] args) throws UnsupportedEncodingException {
		String baseUrl = "http://www.massbank.jp/";	//"http://msbi.ipb-halle.de/MassBank/";
		// String baseUrl = "http://www.massbank.jp/";
		GetConfig conf = new GetConfig(baseUrl);
		String serverUrl = conf.getServerUrl();
		String compoundName = "Serotonin"; // Naringenin
		String typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_GETMOL];
		System.out.println("typeName -> " + typeName);
		String param = "&query=" + URLEncoder.encode(compoundName, "UTF-8")
				+ "&qtype=n&otype=q";
		// param =
		// "&names=1-methoxyindole-3-carbaldehyde@1H-indole-3-carboxylic+acid@3-Acetylindole@3-Formylindole@4-Coumaroylcholine@4-Hydroxybenzoylcholine@4-methoxy-1H-indole-3-carbaldehyde@5-Hydroxyindole-3-acetic+acid@5-Hydroxytryptophan@5-methoxyindole-3-carbaldehyde@Acetylcholine@Alanine@Allocryptopine@Aminocaproic+acid@Arginine@Asparagine@Aspartic+acid@Benzoylcholine@Berberine@Betaine-Aldehyde@Biochanin+A@Boldine@Caffeine@Caffeoylcholine@Camalexin";
		param = "&query=" + URLEncoder.encode("Naringenin", "UTF-8")
				+ "&qtype=n&otype=q";
		MassBankCommon mbcommon = new MassBankCommon();
		ArrayList<String> result = mbcommon.execMultiDispatcher(serverUrl,
				typeName, param);
		System.out.println("result.length -> " + result.size());
		// serverUrl = "http://msbi.ipb-halle.de/MassBank/";
		// result = mbcommon.execMultiDispatcher(serverUrl, typeName, param);
		// System.out.println("result.length -> " + result.size());
		Map<String, String> map = getMolFile(result, serverUrl);

		// HttpClient test
		HttpClient client = new HttpClient();
		String m_url = "http://cents.m.u-tokyo.ac.jp/MassBank/cgi-bin/IndexCount.cgi";
		// String m_url =
		// "http://msbi.ipb-halle.de/MassBank/cgi-bin/ServerCheck.cgi";
		PostMethod method = new PostMethod(m_url);
		String strParam = "dsn=MassBank";
		method.addParameter("dsn", "MassBank");
		String msg = "";
		// typeName =
		// MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_IDXCNT];
		// // idxcnt

		boolean checkClient = false;
		if (checkClient) {
			try {
				//
				int statusCode = client.executeMethod(method);
				// 
				if (statusCode != HttpStatus.SC_OK) {
					// 
					msg = method.getStatusLine().toString() + "\n" + "URL  : "
							+ m_url;
					msg += "\nPARAM : " + strParam;
					System.out.println(msg);
					;
				}

				String web = "";
				// getResponseBodyAsStream()
				InputStream is = method.getResponseBodyAsStream();
				StringBuilder sb = new StringBuilder();
				String line = "";
				if (is != null) {
					try {
						BufferedReader reader = new BufferedReader(
								new InputStreamReader(is, "UTF-8"));
						while ((line = reader.readLine()) != null) {
							if (line.equals("") || line.equals("\n"))
								sb.append(line);
							else
								sb.append(line).append("\n"); // .append("\n");
						}
					} finally {
						is.close();
					}
					web = sb.toString();
				} else {
					web = "";
				}
				System.out.println(web);
			} catch (Exception e) {
				// 
				msg = e.toString() + "\n" + "URL  : " + m_url;
				msg += "\nPARAM : " + strParam;
				System.err.println(msg);
			} finally {
				// 
				method.releaseConnection();
			}
		}

		String searchParam = "";
		// searchParam += key + "=" + URLEncoder.encode(vals[i], "utf-8") + "&";
		searchParam = "sortKey=name&type=quick&qpeak=273.096,22@289.086,107@290.118,14@291.096,999@292.113,162@293.054,34@579.169,37@580.179,15&CUTOFF=5";
		// ResultList list;
		// typeName =
		// MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_QUICK];
		// list = mbcommon.execDispatcherResult( serverUrl, typeName,
		// searchParam, true, null, conf );
		// System.out.println("list# -> " + list.getResultNum());

		ResultList list2 = new ResultList(conf);
		System.out.println(list2.getResultNum());

		typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_SEARCH];
		String paramPeak = "147.044,20@153.019,30@273.076,999@274.083,30";
		param = "quick=true&CEILING=1000&WEIGHT=SQUARE&NORM=SQRT&START=1&TOLUNIT=unit"
			+ "&CORTYPE=COSINE&FLOOR=0&NUMTHRESHOLD=3&CORTHRESHOLD=0.8&TOLERANCE=0.3"
			+ "&CUTOFF=5" + "&NUM=0&VAL=" + paramPeak.toString();
		result = mbcommon.execMultiDispatcher(serverUrl, typeName, param);
		System.out.println("result# -> " + result.size());
		
		Map<String, String> mapMolData = getMolFile(result, serverUrl);
		System.out.println("keys# -> " + mapMolData.keySet().size());
		Set<String> keys = mapMolData.keySet();
		for (String k : keys) {
			System.out.println("key -> " + k);
			System.out.println(mapMolData.get(k));
		}
		
		typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_QUICK];
        ResultList list = mbcommon.execDispatcherResult( serverUrl, typeName, searchParam, true, null, conf );
        System.out.println("list.length -> " + list.getResultNum());
        
        result = mbcommon.execMultiDispatcher(serverUrl, typeName, searchParam);
        System.out.println("result# -> " + result.size());
        
        String reqStr = "http://msbi.ipb-halle.de/MassBank/";
		reqStr += "jsp/" + MassBankCommon.DISPATCHER_NAME;
		
		client = new HttpClient();
		method = new PostMethod( reqStr );
		method.addParameter("type", "disp");		// display record
		method.addParameter("id", "PB000122");				// specify record
		method.addParameter("site", "0");
		
		try {
			client.executeMethod(method);
			String result1 = method.getResponseBodyAsString();
			String rec = result1.substring(result1.indexOf("ACCESSION"), result1.indexOf("</pre>"));
			System.out.println(rec);
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static Map<String, String> getMolFile(ArrayList list,
			String serverUrl) {
		String prevName = "";
		String param = "";
		for (int i = 0; i < list.size(); i++) {
			String rec = (String) list.get(i);
			String[] fields = rec.split(";");
			String name = fields[0];
			if (!name.equals(prevName)) {
				String ename = "";
				try {
					ename = URLEncoder.encode(name, "utf-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				param += ename + "@";
			}
			prevName = name;
		}
		if (!param.equals("")) {
			param = param.substring(0, param.length() - 1);
			param = "&names=" + param;
		}

		MassBankCommon mbcommon = new MassBankCommon();
		String typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_GETMOL];
		ArrayList<String> result = mbcommon.execMultiDispatcher(serverUrl,
				typeName, param);
		for (String s : result) {
			System.out.println(s);
		}

		Map<String, String> map = new HashMap();
		boolean isStart = false;
		int cnt = 0;
		String key = "";
		String moldata = "";
		for (int i = 0; i < result.size(); i++) {
			String temp = (String) result.get(i);
			String[] item = temp.split("\t");
			String line = item[0];
			if (line.indexOf("---NAME:") >= 0) {
				if (!key.equals("") && !map.containsKey(key)
						&& !moldata.trim().equals("")) {
					// Molfileデータ格納
					map.put(key, moldata);
				}
				// 次のデータのキー名
				key = line.substring(8).toLowerCase();
				moldata = "";
			} else {
				// JME Editor
				if (line.indexOf("M  CHG") >= 0) {
					continue;
				}
				moldata += line + "|\n";
			}
		}
		if (!map.containsKey(key) && !moldata.trim().equals("")) {
			map.put(key, moldata);
		}
		return map;
	}

	private Map<String, String> getMolFile(ResultList list, int startIndex,
			int endIndex, String serverUrl) {

		String prevName = "";
		String param = "";
		for (int i = startIndex; i <= endIndex; i++) {
			ResultRecord rec = list.getRecord(i);
			String name = rec.getName();
			if (!name.equals(prevName)) {
				String ename = "";
				try {
					ename = URLEncoder.encode(name, "utf-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				param += ename + "@";
			}
			prevName = name;
		}
		if (!param.equals("")) {
			param = param.substring(0, param.length() - 1);
			param = "&names=" + param;
		}
		MassBankCommon mbcommon = new MassBankCommon();
		String typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_GETMOL];
		System.out.println("Result.jsp: param -> " + param);
		ArrayList result = mbcommon.execMultiDispatcher(serverUrl, typeName, param);
		System.out.println("result size -> " + result.size());
		Map<String, String> map = new HashMap();
		boolean isStart = false;
		int cnt = 0;
		String key = "";
		String moldata = "";
		for (int i = 0; i < result.size(); i++) {
			String temp = (String) result.get(i);
			String[] item = temp.split("\t");
			String line = item[0];
			if (line.indexOf("---NAME:") >= 0) {
				if (!key.equals("") && !map.containsKey(key)
						&& !moldata.trim().equals("")) {
					// Molfileデータ格納
					map.put(key, moldata);
				}
				// 次のデータのキー名
				key = line.substring(8).toLowerCase();
				moldata = "";
			} else {
				// JME Editor
				if (line.indexOf("M  CHG") >= 0) {
					continue;
				}
				moldata += line + "|\n";
			}
		}
		if (!map.containsKey(key) && !moldata.trim().equals("")) {
			map.put(key, moldata);
		}
		return map;
	}

}
