/**
 * created by Michael Gerlich on Jan 5, 2010 - 11:18:38 AM
 * email: mgerlich@ipb-halle.de
 */
package de.ipbhalle.MetFlow.wrapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * 
 * @author mgerlich
 *
 * Wrapper class which contains a MetFrag and MassBank object pair, realised via WorkflowOutputAlignment objects.
 * <br>
 * Also provides fields for corresponding rank of the object in their result set as well as their tanimoto distance.
 * 
 */
public class MBMFPair {

	/** the MassBank alignment object */
	private WorkflowOutputAlignment massbank;
	
	/** the MetFrag alignment object */
	private WorkflowOutputAlignment metfrag;
	
	/** the tanimoto distance between the MassBank and MetFrag alignment objects */
	private Float tanimoto;
	
	/** the rank of the MassBank object in its result set */
	private int rankMassBank;
	
	/** the rank of the MetFrag object in its result set */
	private int rankMetFrag;
	
	/** the SQL table name constant */
	public static final String TABLE = "";
	
	/** the SQL database name constant*/
	public static final String DB = "";
	
	/**
	 * MBMFPair Constructor
	 * 
	 * @param MB the MassBank object
	 * @param MF the MetFrag object
	 * @param rankMB the rank of the MassBank object in its result set
	 * @param rankMF the rank of the MetFrag object in its result set
	 * @param tanimoto the float valued tanimoto distance between the MassBank and MetFrag objects
	 */
	public MBMFPair(WorkflowOutputAlignment MB, WorkflowOutputAlignment MF, int rankMB, int rankMF, Float tanimoto) {
		massbank = MB;
		metfrag = MF;
		rankMassBank = rankMB;
		rankMetFrag = rankMF;
		this.tanimoto = tanimoto;
	}
	
	/**
	 * method which writes SQL INSERT code for this MBMFPair in the form of
	 * |MassBank ID | MassBank Rank | MetFrag ID | MetFrag Rank | Tanimoto Distance|
	 * 
	 * @throws ParseException
	 * @throws IOException 
	 */
	public void writeSQL() throws ParseException, IOException {
		String sql = "INSERT INTO " + DB + "." + TABLE + " VALUES (" + massbank.getRecord() + ", " + rankMassBank
				+ ", " + metfrag.getId() + ", " + rankMetFrag + ", " + tanimoto + ");";
		System.out.println("sql -> " + sql);
		
		// This is how to initialize a well-known Date (often used in unit test fixtures)
		DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
		dfm.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
		Date d = new Date();
		String out = dfm.format(d);
		System.out.println("Date: " + out);
		
		File f = new File("./sql/" + out + ".sql");
		FileWriter fw = new FileWriter(f);
		fw.write(sql);
		fw.flush();
		fw.close();
	}
	
	/**
	 * testing method
	 * 
	 * @param args
	 * @throws ParseException
	 * @throws IOException 
	 */
	public static void main(String[] args) throws ParseException, IOException {
		MBMFPair pair = new MBMFPair(new WorkflowOutputAlignment(true), new WorkflowOutputAlignment(true), 2, 3, 0.5f);
		pair.writeSQL();
	}
}
