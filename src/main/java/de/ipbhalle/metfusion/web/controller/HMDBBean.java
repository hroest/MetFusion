/**
 * created by Michael Gerlich, Jan 29, 2013 - 1:34:44 PM
 */ 

package de.ipbhalle.metfusion.web.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openscience.cdk.interfaces.IAtomContainer;

import de.ipbhalle.metfusion.utilities.MassBank.MassBankUtilities;
import de.ipbhalle.metfusion.wrapper.Result;

public class HMDBBean implements GenericDatabaseBean {

	private String databaseName = "HMDB";
	private String sessionPath;
	private String serverUrl;
	private List<Result> results;
	private List<Result> unused;
	private boolean done;
	private boolean showResult;
	private boolean showNote;
	private boolean uniqueInchi;
	private int searchProgress;
	
	private final String utf8 = "✓";	// %E2%9C%93
	
	/** HMDB search types */
	public enum searchType {MS, MSMS, GCMS, NMR1D, NMR2D};
	
	/** 1D NMR types */
	public enum nucleus {H, C};
	private final String prefixH = "1";
	private final String prefixC = "13";
	private String queryUrlNMR1D = "http://www.hmdb.ca/spectra/nmr_one_d/search?" +
			"utf8=" + utf8 + "&nucleus=%s" + "&peaks=%s" +
			"&cs_tolerance=%f" + "&commit=Search";
	private final String tableNMR1D = "nmr_one_d_search_results";
	
	/** 2D NMR types */
	public enum library {tocsy, hsqc};
	private String queryUrlNMR2D = "http://www.hmdb.ca/spectra/nmr_two_d/search?" +
			"utf8=" + utf8 + "&library=%s" + "&peaks=%s" +
			"&x_tolerance=%f" + "&y_tolerance=%f" +	"&commit=Search";
	private final String tableNMR2D = "nmr_two_d_search_results";
	
	/** MS search ionization modes */
	public enum msIon {positive, negative, neutral};
	/** MS query URL */
	private String queryUrlMS = "http://www.hmdb.ca/spectra/ms/search?" +
			"utf8=" + utf8 + "&query_masses=%s" + "&tolerance=%f" +	"&mode=%s" + "&commit=Search";
	private final String tableMS = "ms-search-result"; 	// ms-search-table, gibt mehrere je nach anfrage
	
	/** MSMS search ionization modes */
	public enum msmsIon {Positive, Negative, NA};
	/** MSMS search collision energies */
	public enum msmsCE {Low, Medium, High, All};
	/** MSMS query URL */
	private String queryUrlMSMS = "http://www.hmdb.ca/spectra/ms_ms/search?" +
			"utf8=" + utf8 + "&parent_ion_mass=%f" + "&parent_ion_mass_tolerance=%f" +
			"&ionization_mode=%s" +	"&collision_energy_level=%s" +
			"&peaks=%s" + "&mass_charge_tolerance=%f" +	"&commit=Search";
	private final String tableMSMS = "ms_ms_search_results";
	
	/** GCMS search types */
	public enum gcmsType {retention_index, retention_time};
	private String queryUrlGCMS = "http://www.hmdb.ca/spectra/c_ms/search?" +
			"utf8=" + utf8 + "&retention_type=%s" + "&retention=%f" + "&retention_tolerance=%f" +
			"&peaks=%s" + "&mass_charge_tolerence=%f" +	"&commit=Search";
	private final String tableGCMS = "index";		// suche nach class index oder tag table
	
	
	public HMDBBean() {
		
	}
	
	@Override
	public void run() {
		List<Result> results = performQuery(searchType.NMR2D);
		System.out.println("#results -> " + results.size());
	}

	private List<Result> performQuery(searchType type) {
		List<Result> results = new ArrayList<Result>();
		String query = formatURL(type);
		
		results = parseGCandNMRResults(query, type);
		
//		switch(type) {
//		case MS:
//			results = parseGCandNMRResults(query, type);
//			break;
//			
//		case MSMS: results = parseGCandNMRResults(query, type);
//			break;
//			
//		case GCMS: results = parseGCandNMRResults(query, type);
//			break;
//			
//		case NMR1D: results = parseGCandNMRResults(query, type);
//			break;
//			
//		case NMR2D: results = parseGCandNMRResults(query, type);
//			break;
//			
//		default: System.err.println("No defined search type!");		
//		}
			
		return results;
	}
	
	private String formatURL(searchType type) {
		String formatted = "";
		
		switch(type) {
		case MS:
			formatted = String.format(queryUrlMS, "Enter one mass per line", 0.1d, msIon.positive);
			break;
			
		case MSMS:
			formatted = String.format(queryUrlMSMS, 272.0d, 0.1d, msmsIon.Positive, msmsCE.Low, "M/Z RI; One peak per line", 0.5d);
			break;

		case GCMS:
			formatted = String.format(queryUrlGCMS, gcmsType.retention_index, 1072d, 1d, "one peak per line", 1d);
			break;

		case NMR1D:
			String lib = "";
			if("C".equals(nucleus.C))
				lib = prefixC + "C";
			else if("H".equals(nucleus.H)) {
				lib = prefixH + "H";
			}
			else lib = prefixH + "H";	// default to H
			
			formatted = String.format(queryUrlNMR1D, lib, "One peak per line", 0.02d);
			break;

		case NMR2D:
			formatted = String.format(queryUrlNMR2D, library.tocsy, "One Co-ordinate per line with the numbers seperated by a space",
					0.02d, 0.02d);
			break;

		default: System.err.println("No defined search type!");		
		}
		
		return formatted;
	}
	
	private List<Result> parseGCandNMRResults(String url, searchType type) {
		List<Result> results = new ArrayList<Result>();
		// parse HTML with jsoup
		Document doc = null;
		try {
			doc = Jsoup.connect(url).get();
		} catch (IOException e) {
			System.err.println("Error connecting to HMDB!");
			return results;
		}
		
		// table rows
		//Elements rows = doc.getElementsByTag("table");	// first row contains header <th>, afterwards only data <td>
		//Element table = doc.getElementById("nmr_two_d_search_results");	// first row contains header <th>, afterwards only data <td>
		
		// GCMS, NMR1D + NMR2D provide the same result table style (5 columns)
		// MSMS has 7 columns
		// MS has 6 columns but multiple tables
		Element table = null;
		switch(type) {
		
		case MS: table = doc.getElementById(tableMS); break;
		case MSMS: table = doc.getElementById(tableMSMS); break;
		case NMR1D: table = doc.getElementById(tableNMR1D); break;
		case NMR2D: table = doc.getElementById(tableNMR2D); break;
		case GCMS: table = doc.getElementsByClass(tableGCMS).first(); break;
		default: System.err.println("Unsupported search type!"); break;
		}

		if(table == null)
			return results;
		
		Elements rows = table.getElementsByTag("tr");
		String rowDivider = "<hr />";
		String thStart = "<th>";
		String thEnd = "</th>";
		String structurePath = "/home/mgerlich/Downloads/HMDB/structures/";
		String urlHMDB = "http://www.hmdb.ca/";
		MassBankUtilities mbu = new MassBankUtilities(structurePath);
		
		for (Element element : rows) {
			Elements header = element.getElementsByTag("th");
			if(!header.isEmpty()) {
				for (Element head : header) {
					String text = head.toString();
					if(text.contains(rowDivider)); {
						String[] split = text.split(rowDivider);
						String s = "";
						for (int i = 0; i < split.length; i++) {
							s = split[i].trim();
							if(s.startsWith(thStart))
								s= s.replace(thStart, "");
							if(s.endsWith(thEnd))
								s = s.replace(thEnd, "");
						}
					}
				}
			}
			
			Elements data = element.getElementsByTag("td");
			int columnCounter = 0;
			String id = "";				// HMDB ID
			String name = "";			// name
			String cas = "";			// CAS ID
			String formula = "";		// molecular formula
			double weight = 0.0d;		// molecular weight
			double score = 0.0d;		// library matches
			String link = "";
			if(!data.isEmpty()) {
				for (Element d : data) {
					String s = d.toString();
					// link + ID
					if(columnCounter == 0 && s.contains("<a")) {
						id = d.text();
						link = d.getElementsByTag("a").get(0).attr("href");
					}
					
					// name + CAS
					if(columnCounter == 1 && s.contains("<strong>")) {
						String[] split = d.text().trim().split(" ");
						if(split.length > 2) {	// name has two or more entries
							for (int i = 0; i < split.length - 1; i++) {
								name += split[i];
							}
							cas = split[split.length-1];
						}
						else if(split.length == 2) {
							name = split[0];
							cas = split[1];
						}
					}
					
					// formula + weight
					if(columnCounter == 2 && s.contains("<sub>")) {
						String[] split = d.text().trim().split(" ");
						if(split.length == 2) {
							weight = Double.parseDouble(split[0]);
							formula = split[1];
						}
					}
					
					// image - > columnCounter == 3
					
					// score in the form of x/y
					if(columnCounter == 4) {
						String s2 = d.text().trim();
						if(s2.contains("/")) {
							String[] split = s2.split("/");
							if(split.length == 2) {
								double d1 = Double.parseDouble(split[0]);
								double d2 = Double.parseDouble(split[1]);
								score = d1 / d2;
							}
						}
						else score = Double.parseDouble(d.text().trim());
					}
					columnCounter++;
				}
				
				IAtomContainer ac = mbu.getContainer(id, structurePath);
				
				Result r = new Result("HMDB", id, name, score, ac, urlHMDB + link, "image", formula, weight);
				results.add(r);
			}
		}
		
		return results;
	}
	
	public static void main(String[] args) throws IOException {
		String MS = "http://www.hmdb.ca/spectra/ms/search?" +
				"utf8=%E2%9C%93" +
				"&query_masses=175%0D%0A209.98%0D%0A2011.971" +
				"&tolerance=0.05" +
				"&mode=positive" +		// negative or neutral
				"&commit=Search";
		
		String MSMS = "http://www.hmdb.ca/spectra/ms_ms/search?" +
				"utf8=%E2%9C%93" +
				"&parent_ion_mass=146.0" +
				"&parent_ion_mass_tolerance=0.1" +
				"&ionization_mode=Positive" +	// Negative or "" if NA
				"&collision_energy_level=Low" +		// Medium or High or "" if All
				"&peaks=40.948+0.174%0D%0A56.022+0.424%0D%0A84.37+53.488%0D%0A101.50+8.285%0D%0A102.401+0.775%0D%0A129.670+100.000%0D%0A146.966+20.070" +
				"&mass_charge_tolerance=0.5" +
				"&commit=Search";
		
		String GCMS = "http://www.hmdb.ca/spectra/c_ms/search?" +
				"utf8=%E2%9C%93" +
				"&retention_type=retention_index" +		// oder retention_time
				"&retention=1072" +
				"&retention_tolerance=1" +
				"&peaks=73%0D%0A147%0D%0A117%0D%0A190%0D%0A191%0D%0A148%0D%0A66%0D%0A75" +
				"&mass_charge_tolerence=" +
				"&commit=Search";
		
		String NMR1D = "http://www.hmdb.ca/spectra/nmr_one_d/search?" +
				"utf8=%E2%9C%93" +
				"&nucleus=1H" +		// 13C
				"&peaks=3.81%0D%0A3.82%0D%0A3.83%0D%0A3.85%0D%0A3.89%0D%0A3.90%0D%0A3.91%0D%0A4.25%0D%0A4.26%0D%0A4.27%0D%0A4.41%0D%0A8.19%0D%0A8.31" +
				"&cs_tolerance=0.02" +
				"&commit=Search";
		
		String NMR2D = "http://www.hmdb.ca/spectra/nmr_two_d/search?" +
				"utf8=%E2%9C%93" +
				"&library=tocsy" +	// hsqc
				"&peaks=3.76+2.126%0D%0A3.76+2.446%0D%0A3.76+3.76%0D%0A2.446+2.126%0D%0A2.446+2.446%0D%0A2.446+3.76%0D%0A2.126+2.126%0D%0A2.126+2.446%0D%0A2.126+3.76" +
				"&x_tolerance=0.02" +
				"&y_tolerance=0.02" +
				"&commit=Search";
		
		String utf8 = "✓";	// %E2%9C%93
		System.out.printf("http://www.hmdb.ca/spectra/nmr_two_d/search?" +
				"utf8=" + utf8 + 
				"&library=%s" +	// hsqc
				"&peaks=%s" +
				"&x_tolerance=%f" +
				"&y_tolerance=$f" +
				"&commit=Search", "tocsy", "3.76+2.126%0D%0A3.76+2.446%0D%0A3.76+3.76%0D%0A2.446+2.126%0D%0A2.446+2.446%0D%0A2.446+3.76%0D%0A2.126+2.126%0D%0A2.126+2.446%0D%0A2.126+3.76",
								0.02, 0.02);
		
		// parse HTML with jsoup
		Document doc = Jsoup.connect(GCMS).get();	//Jsoup.parse(s);
		
		// table rows
		//Elements rows = doc.getElementsByTag("table");	// first row contains header <th>, afterwards only data <td>
		Element table = doc.getElementById("nmr_two_d_search_results");	// first row contains header <th>, afterwards only data <td>
		Elements rows = table.getElementsByTag("tr");
		String rowDivider = "<hr />";
		String thStart = "<th>";
		String thEnd = "</th>";
		String structurePath = "/home/mgerlich/Downloads/HMDB/structures/";
		String urlHMDB = "http://www.hmdb.ca/";
		MassBankUtilities mbu = new MassBankUtilities(structurePath);
		List<Result> results = new ArrayList<Result>();
		
		for (Element element : rows) {
			Elements header = element.getElementsByTag("th");
			if(!header.isEmpty()) {
				for (Element head : header) {
					String text = head.toString();
					if(text.contains(rowDivider)); {
						String[] split = text.split(rowDivider);
						String s = "";
						for (int i = 0; i < split.length; i++) {
							s = split[i].trim();
							if(s.startsWith(thStart))
								s= s.replace(thStart, "");
							if(s.endsWith(thEnd))
								s = s.replace(thEnd, "");
						}
					}
				}
			}
			
			Elements data = element.getElementsByTag("td");
			int columnCounter = 0;
			String id = "";				// HMDB ID
			String name = "";			// name
			String cas = "";			// CAS ID
			String formula = "";		// molecular formula
			double weight = 0.0d;		// molecular weight
			double score = 0.0d;		// library matches
			String link = "";
			if(!data.isEmpty()) {
				for (Element d : data) {
					String s = d.toString();
					// link + ID
					if(columnCounter == 0 && s.contains("<a")) {
						id = d.text();
						link = d.getElementsByTag("a").get(0).attr("href");
					}
					
					// name + CAS
					if(columnCounter == 1 && s.contains("<strong>")) {
						String[] split = d.text().trim().split(" ");
						if(split.length > 2) {	// name has two or more entries
							for (int i = 0; i < split.length - 1; i++) {
								name += split[i];
							}
							cas = split[split.length-1];
						}
						else if(split.length == 2) {
							name = split[0];
							cas = split[1];
						}
					}
					
					// formula + weight
					if(columnCounter == 2 && s.contains("<sub>")) {
						String[] split = d.text().trim().split(" ");
						if(split.length == 2) {
							weight = Double.parseDouble(split[0]);
							formula = split[1];
						}
					}
					
					// image - > columnCounter == 3
					
					// score in the form of x/y
					if(columnCounter == 4) {
						String s2 = d.text().trim();
						if(s2.contains("/")) {
							String[] split = s2.split("/");
							if(split.length == 2) {
								double d1 = Double.parseDouble(split[0]);
								double d2 = Double.parseDouble(split[1]);
								score = d1 / d2;
							}
						}
						else score = Double.parseDouble(d.text().trim());
					}
					columnCounter++;
				}
				
				IAtomContainer ac = mbu.getContainer(id, structurePath);
				
				Result r = new Result("HMDB", id, name, score, ac, urlHMDB + link, "image", formula, weight);
				results.add(r);
			}
		}
		
		System.out.println("Found [" + results.size() + "] results");
		System.out.println("ID\tName\tFormula\tWeight\tScore");
		for (Result result : results) {
			System.out.println(result.getId() + "\t" + result.getName() + "\t" + result.getSumFormula() + 
					"\t" + result.getExactMass() + "\t" + result.getScore());
		}
	}
	
	@Override
	public String getDatabaseName() {
		return databaseName;
	}

	@Override
	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	@Override
	public String getSessionPath() {
		return sessionPath;
	}

	@Override
	public void setSessionPath(String sessionPath) {
		this.sessionPath = sessionPath;
	}

	@Override
	public String getServerUrl() {
		return serverUrl;
	}

	@Override
	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	@Override
	public List<Result> getResults() {
		return results;
	}

	@Override
	public void setResults(List<Result> results) {
		this.results = results;
	}

	@Override
	public List<Result> getUnused() {
		return unused;
	}

	@Override
	public void setUnused(List<Result> unused) {
		this.unused = unused;
	}

	@Override
	public boolean isDone() {
		return done;
	}

	@Override
	public void setDone(boolean done) {
		this.done = done;
	}

	@Override
	public boolean isShowResult() {
		return showResult;
	}

	@Override
	public void setShowResult(boolean showResult) {
		this.showResult = showResult;
	}

	@Override
	public boolean isShowNote() {
		return showNote;
	}

	@Override
	public void setShowNote(boolean showNote) {
		this.showNote = showNote;
	}

	@Override
	public boolean isUniqueInchi() {
		return uniqueInchi;
	}

	@Override
	public void setUniqueInchi(boolean uniqueInchi) {
		this.uniqueInchi = uniqueInchi;
	}

	@Override
	public int getSearchProgress() {
		return searchProgress;
	}

	@Override
	public void setSearchProgress(int searchProgress) {
		this.searchProgress = searchProgress;
	}


}
