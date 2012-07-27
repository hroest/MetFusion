/**
 * created by Michael Gerlich on May 26, 2010
 * last modified May 26, 2010 - 2:55:49 PM
 * email: mgerlich@ipb-halle.de
 */
package de.ipbhalle.metfusion.integration.Tanimoto;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Formatter;
import java.util.List;

import org.apache.commons.math.linear.BlockRealMatrix;
import org.apache.commons.math.linear.RealMatrix;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.fingerprint.Fingerprinter;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.similarity.Tanimoto;

import de.ipbhalle.metfusion.integration.Similarity.ISimilarity;
import de.ipbhalle.metfusion.wrapper.Result;
import de.ipbhalle.metfusion.wrapper.ResultExt;

public class TanimotoSimilarity implements ISimilarity, Runnable {

	/**
	 * enumeration stating possible ways of mathematical comparison,
	 *  e.g. greater, greater equal, less, less equal
	 */
	public static enum Ways {GREATER, LESS};
	
	/**
	 * enumeration stating possible ways of ordering,
	 *  e.g. ascending or descending
	 */
	public static enum Order {ASCENDING, DESCENDING};
	
	private List<Result> primaries;
	private List<Result> candidates;
	private double[][] data;
	private double[] rowScores;
	private double[] colScores;
	
	private float threshold = 0.5f;
	private int numHits = 3;
	
	private RealMatrix matrix;
	
	private File matrixFile;
	
	public static void main(String[] args) {
		TanimotoSimilarity sim = null;
		double[] rowScores, colScores;
		try {
			sim = new TanimotoSimilarity(new File("./src/test/resources/mat.txt"));
			rowScores = sim.readVector(new File("./src/test/resources/rowScores.txt"));
			colScores = sim.readVector(new File("./src/test/resources/colScores.txt"));
			sim.setColScores(colScores);
			sim.setRowScores(rowScores);
			sim.setThreshold(0.5f);
			sim.setNumHits(3);
			
			List<Result> candidates = new ArrayList<Result>();
			for (int i = 0; i < rowScores.length; i++) {
				candidates.add(new Result("MetFrag", String.valueOf(i), String.valueOf(i), rowScores[i], null));
			}
			
			List<Result> primaries = new ArrayList<Result>();
			for (int i = 0; i < colScores.length; i++) {
				primaries.add(new Result("MassBank", String.valueOf(i), String.valueOf(i), colScores[i], null));
			}
			
			sim.setCandidates(candidates);
			sim.setPrimaries(primaries);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		TanimotoIntegration integration = new TanimotoIntegration(sim);
		List<ResultExt> newOrder = integration.computeNewOrdering();
		for (ResultExt resultExt : newOrder) {
			System.out.println(resultExt.getName() + "\tscore -> " + resultExt.getResultScore());
		}
	}
	
	public TanimotoSimilarity(File matrix) throws NumberFormatException, IOException {
		this.matrixFile = matrix;
		this.matrix = readMatrix(matrix);
		this.threshold = 0.5f;
	}
	
	public TanimotoSimilarity(File matrix, boolean runnable) throws NumberFormatException, IOException {
		this.matrixFile = matrix;
		if(!runnable) {		// else wait for run() call
			this.matrix = readMatrix(matrix);
			this.threshold = 0.5f;
		}
	}
	
	@Override
	public void run() {
		if(matrixFile != null && matrixFile.isFile() && matrixFile.canRead()) {
			try {
				this.matrix = readMatrix(matrixFile);
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.threshold = 0.5f;
		}
	}
	
	/**
	 * Default constructor for CDK fingerprint based similarity computation.
	 * 
	 * @param primaries - the result list of primaries from a spectral DB 
	 * @param candidates - the result list of candidates from a fragmenter
	 */
	public TanimotoSimilarity(List<Result> primaries, List<Result> candidates) {
		this.setData(new double[candidates.size()][primaries.size()]);
		
		this.primaries = primaries;
		this.candidates = candidates;
		calculateSimilarity();
		matrix =  new BlockRealMatrix(data);
		
		rowScores = generateListScores(candidates);
		colScores = generateListScores(primaries);
		
		//writeVector(colScores, new File("/tmp/colScores.txt"));
		//writeVector(rowScores, new File("/tmp/rowScores.txt"));
	}
	
	/**
	 * Default constructor for ChemAxon ECFP-based similarity computation.
	 * 
	 * @param primaries - the result list of primaries from a spectral DB 
	 * @param candidates - the result list of candidates from a fragmenter
	 */
	public TanimotoSimilarity(List<Result> primaries, List<Result> candidates, boolean useECFP) {
		this.setData(new double[candidates.size()][primaries.size()]);
		
		this.primaries = primaries;
		this.candidates = candidates;
		if(!useECFP) {		// default CDK
			calculateSimilarity();
		}
		else {				// use ECFP
			calculateSimilarityECFP();
		}
		matrix =  new BlockRealMatrix(data);
		
		rowScores = generateListScores(candidates);
		colScores = generateListScores(primaries);
	}
	
	/**
	 * Constructor for a new Tanimoto Similarity comparison.
	 * 
	 * @param primaries the result list of primaries, usually results from a reference spectra DB (e.g. MassBank) 
	 * @param candidates the result list of possible candidates, usually results from a fragmenter (e.g. MetFrag)
	 * @param numHits the number of hits to be used for threshold based reranking
	 * @param threshold the threshold to be used for the threshold based reranking - if this value is not between [0 - 1.0],
	 * it defaults to 0.5
	 */
	public TanimotoSimilarity(List<Result> primaries, List<Result> candidates, int numHits, float threshold) {
		this(primaries, candidates);
		this.numHits = (numHits > 0 && numHits < primaries.size() ? numHits : 1);
		this.threshold = (threshold >= 0f && threshold <= 1.0f ? threshold : 0.5f);
	}
	
	private BitSet generateBitSet(IAtomContainer container) {
		Fingerprinter fp = new Fingerprinter();
		try {
			return fp.getFingerprint(container);
		} catch (CDKException e) {
			//e.printStackTrace();
			return null;
		}
	}

	@Override
	public double[] generateListScores(List<Result> list) {
		double[] scores = new double[list.size()];
		for (int i = 0; i < scores.length; i++) {
			scores[i] = list.get(i).getScore();
		}
		
		return scores;
	}

	private void calculateSimilarity() {
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				BitSet BitSet1 = primaries.get(j).getBitset();
				BitSet BitSet2 = candidates.get(i).getBitset();
				if(BitSet1 == null || BitSet2 == null) {
					BitSet1 = generateBitSet(primaries.get(j).getMol());
					BitSet2 = generateBitSet(candidates.get(i).getMol());
				}
				try {
					float sim = Tanimoto.calculate(BitSet1, BitSet2);
					data[i][j] = Double.valueOf(sim);
				} catch (CDKException e) {
					System.err.println("Error computing Tanimoto similarity at index [" + i + "," + j + "] -> using 0.0 instead.");
					data[i][j] = 0d;
				}
			}
		}
	}
	
	private void calculateSimilarityECFP() {
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				float sim = primaries.get(j).getEcfp().getTanimoto(candidates.get(i).getEcfp());	// ECFP Tanimoto computes dissimilarity!
				sim = 1f - sim;	// similarity = 1 - dissimilarity
				data[i][j] = Double.valueOf(sim);
			}
		}
	}
	
	@Override
	public RealMatrix createComparisonMatrix(Double[][] data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean createImageFromMatrix(RealMatrix matrix) {
		// TODO Auto-generated method stub
		return false;
	}

	public double[] readVector(File file) throws NumberFormatException, IOException {
		double[] arr = null;
		
		BufferedReader bufRdr  = new BufferedReader(new FileReader(file));
		String line = null;
		
		// read each line of text file
		while ((line = bufRdr.readLine()) != null) {
			String[] split = line.split("\t");
			int num = split.length;
			arr = new double[num];
			for (int i = 0; i < split.length; i++) {
				arr[i] = Double.parseDouble(split[i]);
			}
		}
		// close the file
		bufRdr.close();
		
		System.out.println("arr.length -> " + arr.length);
		
		return arr;
	}
	
	public boolean writeVector(double[] arr, File file) {
		Formatter formatter = null;
		try {
			formatter = new Formatter(file);
		} catch (FileNotFoundException e) {
			//e.printStackTrace();
			// File not found exception thrown since this is a new
		    // file name. However, Formatter will create the new file.
			System.err.println("Error creating Formatter for vector writing!");
			return false;
		}
		
		//formatter.format("MetFrag|MassBank\t");
		for (int i = 0; i < arr.length; i++) {
			formatter.format("%1.3f\t", arr[i]);	// write score followed by a tab
		}
		
		formatter.flush();
		formatter.close();
		return true;
	}
	
	public RealMatrix readMatrix(File file) throws NumberFormatException, IOException {
		RealMatrix rm = null;
		
		BufferedReader bufRdr  = new BufferedReader(new FileReader(file));
		String line = null;
		int row = 0;
		int col = 0;

		List<double[]> list = new ArrayList<double[]>();
		
		// ignore first row and first column
		boolean firstRow = false;
		
		double[] rowScores = null;
		double[] colScores = null;
		List<Double> rows = new ArrayList<Double>();
		
		// read each line of text file
		while ((line = bufRdr.readLine()) != null) {
			if(line.contains("[") && !firstRow) {
				firstRow = true;
				
				// store colScores from header row
				String[] split = line.split("\t");
				colScores = new double[split.length];
				for (int i = 0; i < split.length; i++) {
					if(split[i].contains("[")) {
						String temp = split[i].substring(split[i].indexOf("[")+1, split[i].length()-1);
						colScores[i] = Double.parseDouble(temp);
					}
				}
				setColScores(colScores);		// set column scores read from matrix header row
				
				continue;
			}
			String[] split = line.split("\t");
			int num = split.length;
			double[] values = new double[num-1];	// use one column less to skip rowName header from R
//			if(split[i].contains("[")) {
//				values = new double[num - (i+1)];	// remove first column as it contains rowNames
//			}
			
			// start in second column as the first will contain the row names
			for (int i = 0; i < split.length; i++) {
				if(split[i].contains("[")) {
					String temp = split[i].substring(split[i].indexOf("[")+1, split[i].length()-1);
					rows.add(Double.parseDouble(temp));
				}
				//if(!split[i].contains("[")) {
				else {
					values[col] = Double.parseDouble(split[i]);
					col++;
				}
			}
			col = 0;
			list.add(values);
			row++;
		}
		// close the file
		bufRdr.close();
		
		rowScores = new double[rows.size()];
		for (int i = 0; i < rowScores.length; i++) {
			rowScores[i] = rows.get(i).doubleValue();
		}
		setRowScores(rowScores);	// set row scores read from matrix
		
		double[][] data = new double[row][list.get(0).length];
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				data[i][j] = list.get(i)[j];
			}
		}
		
		rm = new BlockRealMatrix(data);
		
		return rm;
	}
	
	@Override
	public boolean writeMatrix(RealMatrix matrix, File file) {
		System.out.println("started writing Tanimoto matrix -> " + file.getAbsolutePath());
		Formatter formatter = null;
		try {
			formatter = new Formatter(file);
		} catch (FileNotFoundException e) {
			//e.printStackTrace();
			// File not found exception thrown since this is a new
		    // file name. However, Formatter will create the new file.
			System.err.println("Error creating Formatter for matrix writing!");
			return false;
		}
		
		//formatter.format("MetFrag|MassBank\t");
		for (int i = 0; i < primaries.size(); i++) {
			// write column header
			if(i < (primaries.size() - 1)) {
				formatter.format(primaries.get(i).getId());
				formatter.format("[%1.3f]\t", primaries.get(i).getScore());	// write score followed by a tab
			}
			else {
				formatter.format(primaries.get(i).getId());
				formatter.format("[%1.3f]%n", primaries.get(i).getScore());	// write last score followed by newline
			}
		}
		
		for (int i = 0; i < data.length; i++) {			// rows
			for (int j = 0; j < data[i].length; j++) {	// columns
				if(j == 0 ) {			// write row header
					formatter.format(candidates.get(i).getId());
					formatter.format("[%1.3f]\t", candidates.get(i).getScore());
				}
				
				if(j < (data[i].length - 1)) {
					formatter.format("%1.3f\t", data[i][j]);
				}
				else {
					formatter.format("%1.3f%n", data[i][j]);
				}
			}
		}
		formatter.flush();
		formatter.close();
		System.out.println("finished writing Tanimoto matrix");
		return true;
	}

	@Override
	public void printMatrix() {
		if(data != null) {
			StringBuffer sb = new StringBuffer();
			System.out.println("Matrix has " + data.length + " rows and " + data[0].length + " cols.");
			for (int i = 0; i < data.length; i++) {
				for (int j = 0; j < data[i].length; j++) {
					sb.append(String.format("%1.3f ", data[i][j]));
				}
				sb.append("\n");
			}
			System.out.println(sb.toString());
		}
		else System.err.println("Error printing Matrix - is null!");
	}
	
	@Override
	public void setData(double[][] data) {
		this.data = data;
	}

	@Override
	public double[][] getData() {
		return data;
	}

	@Override
	public void setPrimaries(List<Result> primaries) {
		this.primaries = primaries;
	}

	@Override
	public List<Result> getPrimaries() {
		return primaries;
	}

	@Override
	public void setCandidates(List<Result> candidates) {
		this.candidates = candidates;
	}

	@Override
	public List<Result> getCandidates() {
		return candidates;
	}

	@Override
	public void setMatrix(RealMatrix matrix) {
		this.matrix = matrix;
	}

	@Override
	public RealMatrix getMatrix() {
		return matrix;
	}

	@Override
	public double[] getColScores() {
		return colScores;
	}

	@Override
	public double[] getRowScores() {
		return rowScores;
	}

	@Override
	public void setColScores(double[] scores) {
		this.colScores = scores;		
	}

	@Override
	public void setRowScores(double[] scores) {
		this.rowScores = scores;
	}

	@Override
	public float getThreshold() {
		return threshold;
	}

	@Override
	public void setThreshold(float threshold) {
		this.threshold = threshold;
	}

	@Override
	public int getNumHits() {
		return this.numHits;
	}

	@Override
	public void setNumHits(int numHits) {
		this.numHits = numHits;
	}

}
