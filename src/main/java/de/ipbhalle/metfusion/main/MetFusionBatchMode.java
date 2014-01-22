/**
 * created by Michael Gerlich, Nov 8, 2010 - 12:47:12 PM
 * 
 * Utility class providing a batch mode for console to MetFusion functionality.
 */ 

package de.ipbhalle.metfusion.main;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openscience.cdk.interfaces.IAtomContainer;

import de.ipbhalle.enumerations.Adducts;
import de.ipbhalle.enumerations.Databases;
import de.ipbhalle.enumerations.Fingerprints;
import de.ipbhalle.enumerations.Ionizations;
import de.ipbhalle.enumerations.OutputFormats;
import de.ipbhalle.metfusion.threading.MetFusionThreadBatchMode;
import de.ipbhalle.metfusion.threading.MetFusionThreadSDFOnly;
import de.ipbhalle.metfusion.threading.MetFusionThreadSpectralSDFBatchMode;
import de.ipbhalle.metfusion.utilities.MassBank.MassBankUtilities;
import de.ipbhalle.metfusion.web.controller.ResultExtGroupBean;
import de.ipbhalle.metfusion.wrapper.ColorcodedMatrix;
import de.ipbhalle.metfusion.wrapper.ResultExt;
import de.ipbhalle.metfusion.wrapper.SDFDatabase;


public class MetFusionBatchMode {

	private final static String ARGUMENT_INDICATOR = "-";
	// batchfile, sdf-file
	public static enum ARGUMENTS {mf, sdf, out, format, proxy, record, server, cache, unique, 
		fp, fragOffline, db, chnops, compress, verbose, SDFonly, searchppm, mzabs, mzppm, spectralSDF, spectralSDFName, skip};
	private final static int NUM_ARGS = ARGUMENTS.values().length;
	private boolean checkMF, checkSDF, checkOUT, checkFORMAT, checkPROXY, checkRECORD, checkSERVER, checkCACHE, 
		checkUNIQUE, checkFP, checkFRAGOFFLINE, checkDB, checkCHNOPS, checkCOMPRESS, checkVERBOSE, checkSDFONLY, 
		checkSEARCHPPM, checkMZABS, checkMZPPM, checkSPECTRALSDF, checkSKIP;
	private Map<ARGUMENTS, String> settings;
	private final static String DEFAULT_SERVER = "http://www.massbank.jp/";
	
	private final String os = System.getProperty("os.name");
	private final String fileSeparator = System.getProperty("file.separator");
	private final String userHome = System.getProperty("user.home");
	private final String currentDir = System.getProperty("user.dir");
	private final String tempDir = System.getProperty("java.io.tmpdir");
	private final String lineSeparator = System.getProperty("line.separator");
	private final String fileEncoding = System.getProperty("file.encoding");
	
	private boolean doneCheck = false;
	private boolean doneSetup = false;
	
	private MetFusionBatchFileHandler batchFileHandler;
	private ColorcodedMatrix colorMatrix;
	private ColorcodedMatrix colorMatrixAfter;
	private List<ResultExt> secondOrder;
	private List<ResultExt> clusterResults;
	private List<ResultExtGroupBean> tanimotoClusters;
	
	private final static String prefixSeparator = "_";
	
	
	public MetFusionBatchMode(String[] args) {
		this.doneSetup = setup();
		this.doneCheck = checkArguments(args);
	}
	
	/**
	 * Setup the current path environment to write result files.
	 * 
	 * @return <p><b>true</b> if the current directory where this class is being run is a directory
	 * and it is read-/writable.
	 * <p><b>false</b> if the current directory or path is not useable. Fallback to tmp dir.
	 */
	private boolean setup() {
		this.batchFileHandler = new MetFusionBatchFileHandler(null);	// create new dummy instance for proper SDF handling, even witout mf file
		boolean success = false;
		File dir = new File(currentDir);
		if(dir.isDirectory() && dir.canRead() && dir.canWrite())
			success = true;
		return success;
	}
	
	private boolean checkArguments(String[] args) {
		boolean success = false;
		OutputFormats[] of = OutputFormats.values();
		Fingerprints[] fps = Fingerprints.values();
		Databases[] dbs = Databases.values();
		
		if(args.length < 4) {	// at least -mf and -sdf OR -out needs to be specified
			System.out.println("Please provide the following arguments:");
			System.out.println("-mf /path/to/mf-file");
			System.out.println("Alternatively: -record /path/to/MassBank-record");
			System.out.println("\noptionally: -sdf /path/to/sdf-file");
			System.out.println("-searchppm allowed ppm deviation for mass search");
			System.out.println("-mzabs absolute deviation for fragment matching (Da)");
			System.out.println("-mzppm relative deviation for fragment matching (ppm)");
			System.out.println("-out /output/path");
			System.out.print("-format ");
			for (OutputFormats format : of) {
				System.out.print("[" + format + "] ");
			}
			System.out.println("\noptionally: -proxy\t\t(use proxy if provided)");
			System.out.println("optionally: -chnops\t\t(only retrieve (biological) compounds based on C,H,N,O,P,S)");
			System.out.println("optionally: -server http://www.your-massbank.server/");
			System.out.println("optionally: -cache /path/to/cache");
			System.out.println("optionally: -unique\t\t(filter out duplicates)");
			System.out.println("optionally: -fragOffline\t\t(generate fragments in files rather than in memory - recommended for large datasets)");
			System.out.println("optionally: -compress\t\t(compress resulting SDF or XLS file)");
			System.out.println("optionally: -verbose\t\t(create additional output files for intermediate results)");
			System.out.println("optionally: -SDFonly\t\t(use provided SDF filenames in mf file as respective database replacements)");
			System.out.println("optionally: -spectralSDF\t\t(use specified SDF for spectral database part and perform online query of compound database)");
			System.out.println("optionally: -skip\t\t(skip computation if output files already exists)");
			System.out.print("optionally: -fp ");
			for (Fingerprints fp : fps) {
				System.out.print("[" + fp + "] ");
			}
			System.out.println("\noptionally: -db compound datase");
			for (Databases db : dbs) {
				System.out.print("[" + db + "] ");
			}
			
			System.out.println("\n\nExample call: java -jar JARFILE -mf settings.mf #this uses the current directory for output!");
			System.out.println("Example call: java -jar JARFILE -mf settings.mf -out /tmp");
			System.out.println("Example call: java -jar JARFILE -mf settings.mf -sdf compounds.sdf -out /tmp");
			System.out.println("Example call: java -jar JARFILE -record XX000001.txt -out /tmp -format SDF");
			System.out.println("Example call: java -jar JARFILE -mf settings.mf -sdf compounds.sdf -out /tmp -format SDF");
			System.out.println("Example call: java -jar JARFILE -mf settings.mf -sdf compounds.sdf -out /tmp -format SDF -proxy");
			System.out.println("Example call: java -jar JARFILE -mf settings.mf -sdf compounds.sdf -out /tmp -format SDF -proxy -unique");
			System.out.println("Example call: java -jar JARFILE -mf settings.mf -sdf compounds.sdf -out /tmp -format SDF -fp ECFP");
			System.out.println("Example call: java -jar JARFILE -mf settings.mf -sdf compounds.sdf -out /tmp -format SDF -fragOffline");
			System.out.println("Example call: java -jar JARFILE -mf settings.mf -sdf compounds.sdf -out /tmp -format SDF -chnops");
			System.out.println("Example call: java -jar JARFILE -mf settings.mf -SDFonly \t\t(both SDF filenames are stored in settings.mf)");
			System.out.println("Example call: java -jar JARFILE -mf settings.mf  -out /tmp -format SDF_XLS -skip");
			
			return success;
		}
		
		this.settings = new HashMap<ARGUMENTS, String>();
		for (int i = 0; i < args.length; i++) {
			if(args[i].startsWith(ARGUMENT_INDICATOR)) {
				String temp = args[i].substring(1);	// skip indicator char
				if(temp.equals(ARGUMENTS.mf.toString()))
					this.checkMF = Boolean.TRUE;
				if(temp.equals(ARGUMENTS.sdf.toString())) {
					this.checkSDF = Boolean.TRUE;
					System.out.println("SDF provided!");
				}
				if(temp.equals(ARGUMENTS.out.toString()))
					this.checkOUT = Boolean.TRUE;
				if(temp.equals(ARGUMENTS.format.toString()))
					this.checkFORMAT = Boolean.TRUE;
				if(temp.equals(ARGUMENTS.proxy.toString())) {
					this.checkPROXY = Boolean.TRUE;		// proxy does not have an additional property, mark as set/unset and continue
					continue;
				}
				if(temp.equals(ARGUMENTS.record.toString()))
					this.checkRECORD = Boolean.TRUE;
				if(temp.equals(ARGUMENTS.server.toString()))
					this.checkSERVER = Boolean.TRUE;
				if(temp.equals(ARGUMENTS.cache.toString()))
					this.checkCACHE = Boolean.TRUE;
				if(temp.equals(ARGUMENTS.unique.toString())) {
					this.checkUNIQUE = Boolean.TRUE;	// unique does not have an additional property, mark as set/unset and continue
					continue;
				}
				if(temp.equals(ARGUMENTS.fp.toString()))
					this.checkFP = Boolean.TRUE;
				if(temp.equals(ARGUMENTS.fragOffline.toString())) {
					this.checkFRAGOFFLINE = Boolean.TRUE;	// fragOffline does not have an additional property, mark as set/unset and continue
					continue;
				}
				if(temp.equals(ARGUMENTS.db.toString()))
					this.checkDB = Boolean.TRUE;
				if(temp.equals(ARGUMENTS.chnops.toString())) {
					this.checkCHNOPS = Boolean.TRUE;	// chnops does not have an additional property, mark as set/unset and continue
					continue;
				}
				if(temp.equals(ARGUMENTS.compress.toString())) {
					this.checkCOMPRESS = Boolean.TRUE;	// compress does not have an additional property, mark as set/unset and continue
					continue;
				}
				if(temp.equals(ARGUMENTS.verbose.toString())) {
					this.checkVERBOSE = Boolean.TRUE;	// verbose does not have an additional property, mark as set/unset and continue
					continue;
				}
				if(temp.equals(ARGUMENTS.SDFonly.toString())) {
					this.checkSDFONLY = Boolean.TRUE;	// SDFonly does not have an additional property, mark as set/unset and continue
					continue;
				}
				if(temp.equals(ARGUMENTS.searchppm.toString()))
					this.checkSEARCHPPM = Boolean.TRUE;
				if(temp.equals(ARGUMENTS.mzabs.toString()))
					this.checkMZABS = Boolean.TRUE;
				if(temp.equals(ARGUMENTS.mzppm.toString()))
					this.checkMZPPM = Boolean.TRUE;
				if(temp.equals(ARGUMENTS.spectralSDF.toString())) {
					this.checkSPECTRALSDF = Boolean.TRUE;	// spectralSDF does not have an additional property, mark as set/unset and continue
					continue;
				}
				if(temp.equals(ARGUMENTS.skip.toString())) {
					this.checkSKIP = Boolean.TRUE;			// skip does not have an additional property, mark as set/unset and continue
					continue;
				}
				
				settings.put(ARGUMENTS.valueOf(temp), args[i+1]);	// put value into map
				i++;	// skip value, iterate over new argument
			}
		}
		if(!checkOUT) {
			settings.put(ARGUMENTS.out, currentDir);
			checkOUT = Boolean.TRUE;
		}
		if(!checkMF & checkSDFONLY)		// SDFonly can only work with SDF files specified in mf file
			return Boolean.FALSE;
		
		if(checkMF & checkSDF | checkMF & checkOUT | checkRECORD & checkOUT)
			success = true;
		
		return success;
	}
	
	/**
	 * Provide arguments in the following way:
	 * 
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		MetFusionBatchMode mfbm = new MetFusionBatchMode(args);
		
		if(mfbm.doneSetup)
			System.out.println("Setup valid!");
		else {
			System.err.println("Setup failed!");
			System.exit(-1);
		}
		
		if(mfbm.doneCheck)
			System.out.println("Check arguments done - valid!");
		else {
			System.err.println("Error checking arguments - invalid!");
			System.exit(-1);
		}
		
		// decide whether or not to use SDF -> use SDF if -sdf was set
		// if not, use defined database setting in -mf file
//		String mfFile = mfbm.settings.get(ARGUMENTS.mf);
//		File mfFileHandler = new File(mfFile);
//		String prefix = mfFileHandler.getName().substring(0, mfFileHandler.getName().lastIndexOf("."));	// name of batch file - use as prefix for output files!
		
		String prefix = "";
		if(mfbm.checkMF) {
			String mfFile = mfbm.settings.get(ARGUMENTS.mf);
			File mfFileHandler = new File(mfFile);
			prefix = mfFileHandler.getName().substring(0, mfFileHandler.getName().lastIndexOf("."));	// name of batch file - use as prefix for output files!
			mfbm.batchFileHandler = new MetFusionBatchFileHandler(mfFileHandler);
			try {
				mfbm.batchFileHandler.readFile();
			} catch (IOException e) {
				System.err.println("Error while reading settings file [" + mfFile + "]");
				System.exit(-1);
			}
			mfbm.batchFileHandler.printSettings();
		}
		
		// set parameters for fragmenter and database threads
		String outPath = mfbm.settings.get(ARGUMENTS.out);
		if(!outPath.endsWith(mfbm.fileSeparator))
			outPath += mfbm.fileSeparator;
		
		MetFusionBatchSettings settings = null;
		if(mfbm.checkMF)
			settings = mfbm.batchFileHandler.getBatchSettings();
		else if(mfbm.checkRECORD)
			settings = new MetFusionBatchSettings();		// use default settings
		
		Ionizations ion = settings.getMbIonization();		// retrieve ionization for MassBank
		
		MetFragBatchMode metfragbm = new MetFragBatchMode(outPath);
		MassBankBatchMode mbbm = null;
		if(mfbm.checkSERVER)	// create new MassBankBatchMode with provided server
			mbbm = new MassBankBatchMode(outPath, ion, mfbm.settings.get(ARGUMENTS.server));
		else mbbm = new MassBankBatchMode(outPath, ion, DEFAULT_SERVER);	// create new MassBankBatchMode with default server
		
		if(mfbm.checkCACHE)				// overwrite default cache location if argument was given
			mbbm.setCacheMassBank(mfbm.settings.get(ARGUMENTS.cache));
		
		mbbm.setInputSpectrum(settings.getPeaks());
		mbbm.setSelectedInstruments(settings.getMbInstruments());
		mbbm.setLimit(settings.getMbLimit());
		
		metfragbm.setInputSpectrum(settings.getPeaks());
		
		// set compound database for MetFrag
		String selectedDB = settings.getMfDatabase().toString();
		if(mfbm.checkDB) {	// take db argument from command line first
			String switchDB = mfbm.settings.get(ARGUMENTS.db);
			System.out.println("Selected compound DB overwritten by switch -> [" + switchDB + "] is used!");
			metfragbm.setSelectedDB(switchDB);
		}
		else if(selectedDB != null && !selectedDB.isEmpty())	// take db argument from settings second
			metfragbm.setSelectedDB(settings.getMfDatabase().toString());
		else if(!mfbm.checkDB)
				metfragbm.setSelectedDB(Databases.pubchem.toString());	// default to PubChem database finally
		
		metfragbm.setMolecularFormula(settings.getMfFormula());
		metfragbm.setSelectedAdduct(settings.getMfAdduct().getDifference());
		metfragbm.setParentIon(settings.getMfParentIon());
		metfragbm.setExactMass(settings.getMfExactMass());
		metfragbm.setMzabs(settings.getMfMZabs());
		metfragbm.setMzppm(settings.getMfMZppm());
		metfragbm.setSearchppm(settings.getMfSearchPPM());
		metfragbm.setLimit(settings.getMfLimit());
		metfragbm.setDatabaseID(settings.getMfDatabaseIDs());
		metfragbm.setOnlyCHNOPS(settings.isOnlyCHNOPS());
		
		if(mfbm.checkRECORD) {			// overwrite default settings with record specific ones
			File f = new File(mfbm.settings.get(ARGUMENTS.record));
			prefix = f.getName().substring(0, f.getName().lastIndexOf("."));	// name of batch file - use as prefix for output files!
			MassBankUtilities mbu = new MassBankUtilities();
			String[] result = mbu.getPeaklistFromFile(f);		// read in record
			
			mbbm.setInputSpectrum(result[0]);		// set peaks for MassBank
			metfragbm.setInputSpectrum(result[0]);	// set peaks for MetFrag
			
			// TODO: set mzabs to 0 when using Hill CO-spectra
			if(result[2].startsWith("CO")) 
				metfragbm.setMzabs(0d);
			
			// TODO: EAWAG spectra, orbitrap settings
			if(result[2].startsWith("EA")) {
				metfragbm.setMzabs(0.001d);
				metfragbm.setMzppm(5);
				metfragbm.setSearchppm(5);
				metfragbm.setOnlyCHNOPS(false);
				metfragbm.setMolecularFormula(result[4]);
				
				mbbm.setSelectedInstruments(settings.getMbInstruments() + 
						",APCI-ITFT,LC-APCI-QTOF,LC-APCI-ITFT,LC-APCI-Q,LC-APPI-QQ,LC-APCI-QTOF,ESI-FTICR,HPLC-ESI-TOF,UPLC-ESI-QTOF");	// add orbitrap instrument
			}
						
			// TODO: use formula for query/exact mass
//			if(!result[4].isEmpty())
//				metfragbm.setMolecularFormula(result[4]);
			
			metfragbm.setExactMass(Double.valueOf(result[1]));	// set exact mass
			metfragbm.setParentIon(Double.valueOf(result[1]));	// set parent ion same as exact mass
			metfragbm.setSelectedAdduct(Adducts.Neutral.getDifference());	// default not neutral adduct
			
			ion = Ionizations.valueOf(result[3]);				// set correct ionization from record
			mbbm.setSelectedIon(String.valueOf(ion.getValue()));
			
			// TODO: CHEBI run with increased search ppm for more results
			//metfragbm.setSearchppm(30d);
		}
		
		// set ionization for MetFrag
		metfragbm.setMode(ion.getValue());
		
		if(mfbm.checkPROXY)		// if proxy switch was set, use proxy in MetFrag
			metfragbm.setProxy(Boolean.TRUE);
		
		if(mfbm.checkUNIQUE) {	// filter out duplicate
			metfragbm.setUniqueInchi(Boolean.TRUE);
			mbbm.setUniqueInchi(Boolean.TRUE);
		}
		else if(mfbm.checkRECORD && !mfbm.checkUNIQUE) {	// record provided but unique flag not set, fall back to all results
			metfragbm.setUniqueInchi(Boolean.FALSE);
			mbbm.setUniqueInchi(Boolean.FALSE);
		}
			
		
		if(mfbm.checkFRAGOFFLINE) {
			metfragbm.setGenerateFragmentsInMemory(Boolean.FALSE);
		}
		
		if(mfbm.checkCHNOPS)	// only biological compounds containing C,H,N,O,P,S ?
			metfragbm.setOnlyCHNOPS(true);
		
		String settingsSDF = settings.getSdfFile().trim();
		
		// set provided ppm settings
		if(mfbm.checkSEARCHPPM)
			metfragbm.setSearchppm(Double.valueOf(mfbm.settings.get(ARGUMENTS.searchppm)));
		if(mfbm.checkMZABS)
			metfragbm.setMzabs(Double.valueOf(mfbm.settings.get(ARGUMENTS.mzabs)));
		if(mfbm.checkMZPPM)
			metfragbm.setMzppm(Double.valueOf(mfbm.settings.get(ARGUMENTS.mzppm)));
		
		// sdf path
		if(mfbm.isCheckSDF()) {	// mfbm.checkSDF
			String sdfFile = mfbm.settings.get(ARGUMENTS.sdf);
			File sdf = new File(sdfFile);
			List<IAtomContainer> compounds = mfbm.batchFileHandler.consumeSDF(sdf.getAbsolutePath());
			System.out.println("#compounds from sdf file -> " + compounds.size());
			metfragbm.setSelectedDB(metfragbm.getDbSDF());
			metfragbm.setSelectedSDF(sdf.getAbsolutePath());
		}
		else if(!settingsSDF.isEmpty() && mfbm.checkMF) {	// SDF file provided via settings file
			File sdf = new File(settingsSDF);
			List<IAtomContainer> compounds = mfbm.batchFileHandler.consumeSDF(sdf.getAbsolutePath());
			System.out.println("#compounds from sdf file -> " + compounds.size());
			metfragbm.setSelectedDB(metfragbm.getDbSDF());
			metfragbm.setSelectedSDF(sdf.getAbsolutePath());
		}
		
		OutputFormats of = OutputFormats.SDF;	// DEFAULT output format
		if(mfbm.checkFORMAT)
			of = OutputFormats.valueOf(mfbm.settings.get(ARGUMENTS.format));
		
		Fingerprints fp = Fingerprints.CDK;		// DEFAULT fingerprinter
		if(mfbm.checkFP) {		// use alternative Fingerprinter
			fp = Fingerprints.valueOf(mfbm.settings.get(ARGUMENTS.fp));
			
			mbbm.setFingerprinter(fp);
			metfragbm.setFingerprinter(fp);
		}
		
		// skip run if output files are already present
		if(mfbm.checkSKIP) {
			String filePresent = "";
			boolean checkdone = false;
			if(of.equals(OutputFormats.SDF_XLS)) {
				String check1 = addPrefixToFile(prefix, ".sdf");
				String check2 = addPrefixToFile(prefix, ".xls");
				
				File f1 = new File(outPath, check1);
				File f2 = new File(outPath, check2);
				
				if(f1.exists() && f2.exists()) {
					System.out.println("Result files [" + f1.getAbsolutePath() + ", " +  f2.getAbsolutePath() + "] already exists for [" + prefix + "]. Skipping run!");
					return;
				}
				checkdone = true;
			}
			else if(of.equals(OutputFormats.SDF))
				filePresent = addPrefixToFile(prefix, ".sdf");
			else if(of.equals(OutputFormats.XLS))
				filePresent = addPrefixToFile(prefix, ".xls");
			else if(of.equals(OutputFormats.CSV))
				filePresent = addPrefixToFile(prefix, ".csv");
			else if(of.equals(OutputFormats.ODF))
				filePresent = addPrefixToFile(prefix, ".odf");
			
			File check = new File(outPath, filePresent);
			if(!checkdone && check.exists() && !check.isDirectory()) {
				System.out.println("Result file [" + check.getAbsolutePath() + "] already exists for [" + prefix + "]. Skipping run!");
				return;
			}
		}
		
		MetFusionThreadBatchMode metfusionBatch = new MetFusionThreadBatchMode(mfbm, mbbm, metfragbm, outPath, prefix, of);
		if(fp.equals(Fingerprints.ECFP) | fp.equals(Fingerprints.FCFP)) {	// use ChemAxon fingerprints if desired
			metfusionBatch.setUseECFP(Boolean.TRUE);
		}
		if(mfbm.checkCOMPRESS)		// enable compression if specified
			metfusionBatch.setCompress(Boolean.TRUE);
		if(mfbm.checkVERBOSE)		// enable verbosity if specified
			metfusionBatch.setVerbose(Boolean.TRUE);
		
		if(mfbm.checkSDFONLY) {	// start alternative threading based on two SDF files provided that act as fragmenter/database result replacement
			MetFusionThreadSDFOnly sdfOnly = new MetFusionThreadSDFOnly(settings, outPath, prefix, of);
			sdfOnly.run();
			
			return;		// exit SDF only run
		}
		
		if(mfbm.checkSPECTRALSDF) {	// start alternative threading based on a single SDF file that acts as spectral database result replacement
			SDFDatabase spectralSDF = new SDFDatabase("spectralSDF", settings.getSpectralSDF());
			MetFusionThreadSpectralSDFBatchMode spectralSDFOnly = 
					new MetFusionThreadSpectralSDFBatchMode(mfbm, spectralSDF, metfragbm, outPath, prefix, of);
			spectralSDFOnly.run();
			
			return;		// exit spectral SDF run
		}
		
		metfusionBatch.run();
	}

	public static String addPrefixToFile(String prefix, String filename) {
		if(filename.startsWith("."))
			return prefix + filename;
		else return prefix + prefixSeparator + filename;
	}
	
	public void setColorMatrix(ColorcodedMatrix colorMatrix) {
		this.colorMatrix = colorMatrix;
	}

	public ColorcodedMatrix getColorMatrix() {
		return colorMatrix;
	}

	public void setColorMatrixAfter(ColorcodedMatrix colorMatrixAfter) {
		this.colorMatrixAfter = colorMatrixAfter;
	}

	public ColorcodedMatrix getColorMatrixAfter() {
		return colorMatrixAfter;
	}

	public void setSecondOrder(List<ResultExt> secondOrder) {
		this.secondOrder = secondOrder;
	}

	public List<ResultExt> getSecondOrder() {
		return secondOrder;
	}

	public void setTanimotoClusters(List<ResultExtGroupBean> tanimotoClusters) {
		this.tanimotoClusters = tanimotoClusters;
	}

	public List<ResultExtGroupBean> getTanimotoClusters() {
		return tanimotoClusters;
	}

	public boolean isCheckMF() {
		return checkMF;
	}

	public void setCheckMF(boolean checkMF) {
		this.checkMF = checkMF;
	}

	public boolean isCheckSDF() {
		return checkSDF;
	}

	public void setCheckSDF(boolean checkSDF) {
		this.checkSDF = checkSDF;
	}

	public boolean isCheckOUT() {
		return checkOUT;
	}

	public void setCheckOUT(boolean checkOUT) {
		this.checkOUT = checkOUT;
	}

	public List<ResultExt> getClusterResults() {
		return clusterResults;
	}

	public void setClusterResults(List<ResultExt> clusterResults) {
		this.clusterResults = clusterResults;
	}

}
