package de.ipbhalle.MetFlow.wrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openscience.cdk.interfaces.IAtomContainer;


public class WorkflowOutputAlignment implements Comparable<WorkflowOutputAlignment> {

	public static final String KEGG = "KEGG compound";
	public static final String PubChem = "PUBCHEM";
	public static final String PubChemC = "pccompound";
	public static final String PubChemS = "pcsubstance";
	public static final String NIKKAJI = "NIKKAJI";
	public static final String INCHI = "InChI";
	public static final String SMILES = "SMILES";
	public static final String CAS = "CAS";
	public static final String KNAPSACK = "KNAPSACK";
	public static final String KAPPAVIEW = "KAPPAVIEW";
	public static final String CHEBI = "CHEBI";
	public static final String KEIO = "KEIO";
	public static final String LIPIDBANK = "LIPIDBANK";
	public static final String CAYMAN = "CAYMAN";
	public static final String CHEMSPIDER = "CHEMSPIDER";
	public static final String CHEMPDB = "CHEMPDB";
	public static final String SIRIUS = "SIRIUS";
	public static final String SCORE = "SCORE";		// use a score instead of a DB
	/** The Constant NONE. */
	public static final String NONE = "none";
	
	/**
	 * list of allowed databases used for alignment
	 */
	private static final List<String> allowables;
	static {
		allowables = new ArrayList<String>();
		allowables.add(KEGG);
		allowables.add(PubChem);
		allowables.add(PubChemC);
		allowables.add(PubChemS);
		allowables.add(NIKKAJI);
		allowables.add(INCHI);
		allowables.add(SMILES);
		allowables.add(CAS);
		allowables.add(KNAPSACK);
		allowables.add(KAPPAVIEW);
		allowables.add(CHEBI);
		allowables.add(KEIO);
		allowables.add(LIPIDBANK);
		allowables.add(CAYMAN);
		allowables.add(CHEMSPIDER);
		allowables.add(CHEMPDB);
		allowables.add(SIRIUS);
		allowables.add(SCORE);
		allowables.add(NONE);
	}
	
	/** The identifier inserted if something is missing. */
	public static final String missing = "-----";
	
	/** Key identifier if there are no identifiers.	 */
	public static final String noID = "no identifiers";

	/**
	 * String representation of the Taverna workflow output port associated to this result
	 */
	private String portName;
	
	/**
	 * String representation of the chemical compound, ideally its common name 
	 */
	private String compound;
	
	/**
	 * a float representing the score of the compound inside the whole result set
	 * range from 0 to 1
	 * used for both MassBank and MetFrag score
	 */
	private double score;
	
	/**
	 * the current database used for its corresponding identifier, which is stored in field id
	 */
	private String db;
	
	/**
	 * the identifier for the current compound, associated to the current value of field db
	 */
	private String id;
	
	/** record identifier for MassBank, 8 tokens, first 2-3 letters, remaining are numbers	 */
	private String record;
	
	/**
	 * the chosen database for the alignment
	 */
	private String alignDB;
		
	/**
	 * a map representation (HashMap) of all available DB links for one result/compound
	 * Key is the DB name (KEGG, NIKKAJI, pccompound,...), Value is the corresponding identifier
	 */
	private Map<String, String> dbLinks;
	
	/**
	 * the whole key set from dbLinks, used to access the Map inside JSF
	 */
	private Set<String> dbKeySet;
	
	private List<String> dbKeyList;
	
	/**
	 * list of all elements stored inside this port
	 */
	private List<WorkflowOutput> elements;
	
	/**
	 * list of WorkfloOutputAlignment objects that were successfully
	 * aligned to this current object (via same DB id, score or name)
	 */
	private List<WorkflowOutputAlignment> alignHits;
	
	/**
	 * states if the current object is inside alignHits of another object (true) or not
	 */
	private boolean isHit;
	
	/**
	 * states if the current object has alignment hits made of other objects (true) or not
	 */
	private boolean hasHits;
	
	/**
	 * states if this object has been positioned (exactly once) inside the alignment list
	 */
	private boolean aligned;
	
	/** is this item rendered in JSF (true) or not */
	private boolean rendered;
	
	/** CDK IAtomContainer holding the molfile information of the current alignment object */
	private IAtomContainer container;
	
	/** boolean indicating whether corresponding molfile has been fetched or not */
	private boolean aquiredMol;
	
	
	/**
	 * standard contructor
	 */
	public WorkflowOutputAlignment() {
		this.portName = missing;
		this.alignDB = missing;
		this.score = 0;
//		this.db = "no entry";
//		this.id = "no valid id";
		this.db = missing;
		this.id = missing;
		this.compound = missing;
		this.dbLinks = new HashMap<String, String>();
		this.dbKeySet = dbLinks.keySet();
		this.dbKeyList = new ArrayList<String>();
		dbKeyList.addAll(dbKeySet);
		this.alignHits = new ArrayList<WorkflowOutputAlignment>();
		this.record = missing;
		this.rendered = false;
	}


//	/**
//	 * one argument contructor
//	 * 
//	 * @param content String content contains a valid XML representation of a BioMoby output
//	 */
//	public WorkflowOutputAlignment(String content) {
//		this.content = (content.equals(null) ? "" : content);
//		this.hash = new HashMap<String, String>();
//		
//		//extractInfo();
//		//print();
//	}
	public WorkflowOutputAlignment(String portName) {
		this();
		//this.portName = (portName.isEmpty() ? missing : portName);
		this.portName = portName;
	}
	
	public WorkflowOutputAlignment(boolean empty) {
		if(empty) {
			this.portName = "";
			this.alignDB = "";
			this.score = 0;
			this.db = "";
			this.id = "";
			this.compound = "";
			this.dbLinks = new HashMap<String, String>();
			this.dbKeySet = dbLinks.keySet();
			this.dbKeyList = new ArrayList<String>();
			dbKeyList.addAll(dbKeySet);
			this.alignHits = new ArrayList<WorkflowOutputAlignment>();
			this.record = "";
			this.rendered = true;
		}
		else {
			this.portName = missing;
			this.alignDB = missing;
			this.score = 0;
			this.db = missing;
			this.id = missing;
			this.compound = missing;
			this.dbLinks = new HashMap<String, String>();
			this.dbKeySet = dbLinks.keySet();
			this.dbKeyList = new ArrayList<String>();
			dbKeyList.addAll(dbKeySet);
			this.alignHits = new ArrayList<WorkflowOutputAlignment>();
			this.record = missing;
			this.rendered = false;
		}
	}

	public WorkflowOutputAlignment(String portName, String alignDB, Double score, String db, String id, String compound) {
		this.portName = (portName.isEmpty() ? missing : portName);
		this.alignDB = (alignDB.isEmpty() ? missing : alignDB);
		this.score = (score >= 0.0 & score <= 1.0 ? score : 0);
		this.db = (allowables.contains(db) ? db : missing);
		this.id = (allowables.contains(db) ? id : missing);
		this.compound = (compound.isEmpty() ? missing : compound);
		this.dbLinks = new HashMap<String, String>();
		this.dbKeySet = dbLinks.keySet();
		this.dbKeyList = new ArrayList<String>();
		dbKeyList.addAll(dbKeySet);
		this.record = missing;
		this.rendered = true;
	}
	
	public WorkflowOutputAlignment(String portName, String alignDB, Double score, String db, String id, 
			String compound, Map<String, String> dbs, boolean rend) {
		this(portName, alignDB, score, db, id, compound);
		this.dbLinks = (dbs.isEmpty() ? new HashMap<String, String>() : dbs);
		this.dbKeySet = (dbs.isEmpty() ? new HashSet<String>() : dbs.keySet());
		this.dbKeyList =  new ArrayList<String>();
		dbKeyList.addAll(dbs.keySet());
		this.rendered = rend;
	}
	
	public WorkflowOutputAlignment(String portName, String alignDB, Double score, String db, String id, 
			String compound, Map<String, String> dbs, boolean rend, String rec) {
		this(portName, alignDB, score, db, id, compound, dbs, rend);
		this.record = (rec.isEmpty() ? missing : rec);
	}
	
	public WorkflowOutputAlignment(String portName, String alignDB, Double score, String db, String id, 
			String compound, Map<String, String> dbs, boolean rend, String rec, IAtomContainer container) {
		this(portName, alignDB, score, db, id, compound, dbs, rend);
		this.record = (rec.isEmpty() ? missing : rec);
		this.container = container;
	}
	
	public WorkflowOutputAlignment(String portName, String alignDB, List<WorkflowOutput> portElements) {
		this.portName = (portName.isEmpty() ? missing : portName);
		this.alignDB = (alignDB.isEmpty() ? missing : alignDB);
		this.elements = (portElements.isEmpty() ? new ArrayList<WorkflowOutput>() : portElements);
	}
	
	@Override
	public int compareTo(WorkflowOutputAlignment woa) {
		//return this.id.compareToIgnoreCase(woa.getId());
		return this.record.compareToIgnoreCase(woa.getRecord());
	}
	
	/**
	 * converts a String output from a T2Reference into a Biomoby object and extracts its information, based on ArrayString datatype
	 */
//	public void extractInfo() {
//		MobyContentInstance mci = null;
//		try {
//			mci = MobyDataUtils.fromXMLDocument(content);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		Set<String> keys = mci.keySet();
//		for (Iterator<String> iterator = keys.iterator(); iterator.hasNext();) {
//			String key = (String) iterator.next();
//			System.out.println("Key -> " + key);
//
//			MobyDataJob job = mci.get(key);
//			System.out.println("ID -> " + job.getID());
//
//			Set<String> jobKeys = job.keySet();
//			for (Iterator<String> iterator2 = jobKeys.iterator(); iterator2
//					.hasNext();) {
//				String jobKey = (String) iterator2.next();
//				System.out.println("job key -> " + jobKey);
//
//				ArrayList<String> namesVal = new ArrayList<String>();
//				namesVal.add(jobKey);
//				namesVal.add("Element");
//				namesVal.add("Value");
//				// retrieve all moby:Value from output
//				ArrayList<String> mobyValues = ParseMobyXML
//						.getContentForDataType(namesVal, -30, content,
//								"http://moby.ucalgary.ca/moby/MOBY-Central.pl");
//				mobyValues.trimToSize();
//				for (String val : mobyValues) {
//					System.out.println("jobOut Value -> " + val);
//				}
//
//				ArrayList<String> namesID = new ArrayList<String>();
//				namesID.add(jobKey);
//				namesID.add("Element");
//				// retrieve all moby:ID from output
//				ArrayList<String> mobyIDs = ParseMobyXML.getContentForDataType(
//						namesID, -20, content,
//						"http://moby.ucalgary.ca/moby/MOBY-Central.pl");
//				mobyIDs.trimToSize();
//				for (String id : mobyIDs) {
//					System.out.println("jobOut ID -> " + id);
//				}
//				
//				/**
//				 * mobyValues always contains DB identifiers, mobyIDs contains
//				 * scores for each identifier when MetFragOut contains DB names
//				 * for each identifier when MassBankOut ArrayString has empty ID
//				 * for MetFragOut has compound name; score = ... for MassBankOut
//				 */
//
//				System.out.println("primary data objects# = " + job.getPrimaryDataObjects().length);
//				
//				if (job.getPrimaryDataObjects().length > 0
//						& job.getPrimaryDataObjects() != null) {
//					
//					System.out.println("datatype = " + job.getPrimaryDataObjects()[0].getDataType().getName());
//
//					if (job.getPrimaryDataObjects()[0].getDataType().getName().equalsIgnoreCase("ArrayString")
//							& (mobyIDs.size() == mobyValues.size())) {
//						// ArrayString info
//						System.out.println("id = " + job.getPrimaryDataObjects()[0].getId());
//						String asID = job.getPrimaryDataObjects()[0].getId();
//						if (asID.contains(";")) {		// MassBankOut
//							String[] split = asID.split(";");	//split[1] contains score, split[0] contains compound name
//							for (int i = 0; i < split.length; i++) {
//								split[i] = split[i].trim();
//								System.out.println("split -> " + split[i]);
//							}
//							
//							//String score = split[1].replace("score=", "");
//							String score = asID.substring(asID.indexOf("=") + 1).trim();
//							for (int i = 0; i < mobyValues.size(); i++) {
//								//this.hash.put(mobyIDs.get(i) + ":" + mobyValues.get(i), score);
//								this.hash.put(mobyValues.get(i), score);
//							}
//						}
//						else {							// MetFragOut
//							for (int i = 0; i < mobyIDs.size(); i++) {
//								String[] split = mobyIDs.get(i).split(" ");	// score = ... -> [0]score, [1]=, [2]...
//								this.hash.put(mobyValues.get(i), split[2]);	
//							}
//						}
//					}
//				}
//
//				System.out.println("primary data objects set# = " + job.getPrimaryDataObjectSets().length);
//				if (job.getPrimaryDataObjectSets().length > 0 & job.getPrimaryDataObjectSets() != null) {
//					MobyDataObjectSet[] set = job.getPrimaryDataObjectSets();
//					for (int i = 0; i < set.length; i++) {
//						System.out.println("set ID = " + set[i].getId());
//					}
//				}
//			}
//		}
//		this.hashKeys = this.hash.keySet();
//		this.hashVals = this.hash.values();
//	}

	
	/**
	 * Adds a matching WorkflowOutputAlignment to the list of alignment hits.
	 * 
	 * @param hit the WorkflowOutputAlignment object which fits to this one
	 */
	public void addHit(WorkflowOutputAlignment hit) {
		if(this.alignHits == null) {
			this.alignHits = new ArrayList<WorkflowOutputAlignment>();
			this.alignHits.add(hit);
		}
		else this.alignHits.add(hit);
	}
	
	/**
	 * Removes a matching WorkflowOutputAlignment from list of alignment hits.
	 * 
	 * @param hit the WorkflowOutputAlignment object which should be removed
	 * 
	 * @return true, if removal was successful, else return false
	 */
	public boolean removeHit(WorkflowOutputAlignment hit) {
		boolean success = false;
		
		if(this.alignHits == null)
			success = false;
		else if(this.alignHits.contains(hit))
			success =  this.alignHits.remove(hit);
		
		return success;
	}
	
	/**
	 * prints the information stored in the hash, result of the BioMoby output parsing
	 */
	public void print() {
		System.out.println("PortName -> " + this.portName);
		System.out.println("alignDB -> " + this.alignDB);
		System.out.println("Compound -> " + this.compound);
		System.out.println("DB = " + this.db + "\tID = " + this.id);
		System.out.println("Score -> " + this.score);
		System.out.println("Record -> " + this.record);
	}


	public void setPortName(String portName) {
		this.portName = portName;
	}


	public String getPortName() {
		return portName;
	}


	public void setCompound(String compound) {
		this.compound = compound;
	}


	public String getCompound() {
		return compound;
	}


	public void setScore(double score) {
		this.score = score;
	}


	public double getScore() {
		return score;
	}


	public void setDb(String db) {
		this.db = db;
	}


	public String getDb() {
		return db;
	}


	public void setId(String id) {
		this.id = id;
	}


	public String getId() {
		return id;
	}


	public void setDbLinks(Map<String, String> dbLinks) {
		this.dbLinks = dbLinks;
	}


	public Map<String, String> getDbLinks() {
		return dbLinks;
	}


	public void setDbKeySet(Set<String> dbKeySet) {
		this.dbKeySet = dbKeySet;
		this.dbKeyList.addAll(dbKeySet);
	}


	public Set<String> getDbKeySet() {
		return dbKeySet;
	}


	public void setAlignDB(String alignDB) {
		this.alignDB = alignDB;
	}


	public String getAlignDB() {
		return alignDB;
	}


	public void setElements(List<WorkflowOutput> elements) {
		this.elements = elements;
	}


	public List<WorkflowOutput> getElements() {
		return elements;
	}


	public void setDbKeyList(List<String> dbKeyList) {
		this.dbKeyList = dbKeyList;
		this.dbKeySet.addAll(dbKeyList);
	}


	public List<String> getDbKeyList() {
		return dbKeyList;
	}


	public void setAlignHits(List<WorkflowOutputAlignment> alignHits) {
		this.alignHits = alignHits;
	}


	public List<WorkflowOutputAlignment> getAlignHits() {
		return alignHits;
	}


	public void setHit(boolean isHit) {
		this.isHit = isHit;
	}


	public boolean isHit() {
		return isHit;
	}


	public void setHasHits(boolean hasHits) {
		this.hasHits = hasHits;
	}


	public boolean isHasHits() {
		return hasHits;
	}


	public void setAligned(boolean aligned) {
		this.aligned = aligned;
	}


	public boolean isAligned() {
		return aligned;
	}


	public void setRecord(String record) {
		this.record = record;
	}


	public String getRecord() {
		return record;
	}


	public void setRendered(boolean rendered) {
		this.rendered = rendered;
	}


	public boolean isRendered() {
		return rendered;
	}


	public void setContainer(IAtomContainer container) {
		this.container = container;
	}


	public IAtomContainer getContainer() {
		return container;
	}


	public void setAquiredMol(boolean aquiredMol) {
		this.aquiredMol = aquiredMol;
	}


	public boolean isAquiredMol() {
		return aquiredMol;
	}

}
