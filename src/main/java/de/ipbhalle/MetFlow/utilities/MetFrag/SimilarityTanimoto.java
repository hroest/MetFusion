/**
 * created by Michael Gerlich on Jan 4, 2010 - 2:58:21 PM
 * email: mgerlich@ipb-halle.de
 */
package de.ipbhalle.MetFlow.utilities.MetFrag;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.fingerprint.Fingerprinter;
import org.openscience.cdk.similarity.Tanimoto;

import de.ipbhalle.MetFlow.wrapper.WorkflowOutputAlignment;

public class SimilarityTanimoto {

	/**
	 * a two-dimensional n x m matrix
	 */
	private Float[][] tanimotoMatrix = null;
	
	/**
	 * the list of candidates for alignment
	 */
	private List<WorkflowOutputAlignment> candidates;
	
	/**
	 * the list of primaries for alignment
	 */
	private List<WorkflowOutputAlignment> candidateToStructure;
	private StringBuilder allSimilarityValues = new StringBuilder();
	private float similarityThreshold;
	
	/**
	 * the mapping of primaries to their fingerprints
	 */
	private Map<WorkflowOutputAlignment, BitSet> fingerprintsPrimary;
	
	/**
	 * the mapping of candidates to their fingerprints
	 */
	private Map<WorkflowOutputAlignment, BitSet> fingerprintsCandidates;
	
	/**
	 * the mapping of primaries to candidates - keys are primaries (rows), values are candidates (columns)
	 */
	private Map<WorkflowOutputAlignment, Map<WorkflowOutputAlignment, Float>> mapping;
	
	/**
	 * the mapping of candidates to primaries - keys are candidates (columns), values are primaries (rows)
	 */
	private Map<WorkflowOutputAlignment, Map<WorkflowOutputAlignment, Float>> reverseMapping;
	
	/**
	 * the mapping of the primaries to its corresponding indices in the tanimoto matrix 
	 */
	private Map<Integer, WorkflowOutputAlignment> indexPrimaries;
	
	/**
	 * the mapping of the candidates to its corresponding indices in the tanimoto matrix 
	 */
	private Map<Integer, WorkflowOutputAlignment> indexCandidates;
	
	
	public SimilarityTanimoto(List<WorkflowOutputAlignment> primaryData, 
			List<WorkflowOutputAlignment> candidates) throws CDKException
	{
		// create an orthogonal matrix
		tanimotoMatrix = new Float[primaryData.size()][candidates.size()];
		this.candidateToStructure = primaryData;
		this.candidates = candidates;
		
		calculateSimilarity();
	}
	
	public SimilarityTanimoto(List<WorkflowOutputAlignment> primaryData, 
			List<WorkflowOutputAlignment> candidates, float similarityTreshold) throws CDKException
	{
		this(primaryData, candidates);
		this.similarityThreshold = similarityTreshold;
	}
	
	/**
	 * Returns a similarity matrix based on the tanimoto distance
	 * of the fingerprints. (Upper triangular matrix)
	 * 
	 * @param similarityThreshold the similarity threshold
	 * 
	 * @return the float[][]
	 * 
	 * @throws CDKException the CDK exception
	 */
	private Float[][] calculateSimilarity() throws CDKException
	{
		fingerprintsPrimary = new HashMap<WorkflowOutputAlignment, BitSet>();
		for (WorkflowOutputAlignment woa : candidateToStructure) {
			Fingerprinter f = new Fingerprinter();
			BitSet fp = f.getFingerprint(woa.getContainer());
			fingerprintsPrimary.put(woa, fp);
		}
		
		fingerprintsCandidates = new HashMap<WorkflowOutputAlignment, BitSet>();
		for (WorkflowOutputAlignment woa : candidates) {
			Fingerprinter f = new Fingerprinter();
			BitSet fp = f.getFingerprint(candidates.get(candidates.indexOf(woa)).getContainer());
			fingerprintsCandidates.put(woa, fp);
		}

		// map each primary alignment a map/list of all candidates and their score
		mapping = new TreeMap<WorkflowOutputAlignment, Map<WorkflowOutputAlignment,Float>>();
		
		// create new mapping for indices of primaries and candidates
		indexPrimaries = new HashMap<Integer, WorkflowOutputAlignment>();
		indexCandidates = new HashMap<Integer, WorkflowOutputAlignment>();
		
		int countJ = 0;
		int countI = 0;
		for (WorkflowOutputAlignment candidate1 : candidateToStructure) {
			// map primaries to their position
			indexPrimaries.put(countI, candidate1);
			
			//mapping for candidates and their tanimoto similarity distance
			Map<WorkflowOutputAlignment, Float> cands = new HashMap<WorkflowOutputAlignment, Float>();
			
			for (WorkflowOutputAlignment candidate2 : candidates) {	
				Float similarity = compareFingerprints(fingerprintsPrimary.get(candidate1), fingerprintsCandidates.get(candidate2));
				tanimotoMatrix[countI][countJ] = similarity;
				allSimilarityValues.append(similarity + "\n");
				
				// add candidate and its similarity distance to map
				cands.put(candidate2, similarity);
				countJ++;
			}
			countJ = 0;
			countI++;
			
			// add primary alignment object with candidate map
			mapping.put(candidate1, cands);
		}
		
		countJ = 0;
		countI = 0;
		// map each candidate alignment a map/list of all primaries and their score
		reverseMapping = new TreeMap<WorkflowOutputAlignment, Map<WorkflowOutputAlignment,Float>>();
		for (WorkflowOutputAlignment candidate1 : candidates) {
			// map candidates to their position
			indexCandidates.put(countI, candidate1);
			
			//mapping for candidates and their tanimoto similarity distance
			Map<WorkflowOutputAlignment, Float> cands = new HashMap<WorkflowOutputAlignment, Float>();
			
			for (WorkflowOutputAlignment candidate2 : candidateToStructure) {	
				Float similarity = compareFingerprints(fingerprintsCandidates.get(candidate1), fingerprintsPrimary.get(candidate2));
				//tanimotoMatrix[countI][countJ] = similarity;
				//allSimilarityValues.append(similarity + "\n");
				
				// add candidate and its similarity distance to map
				cands.put(candidate2, similarity);
				countJ++;
			}
			countJ = 0;
			countI++;
			
			// add primary alignment object with candidate map
			reverseMapping.put(candidate1, cands);
		}
		
		System.out.println("Tanimoto Matrix");
		for (int i = 0; i < tanimotoMatrix.length; i++) {
			for (int j = 0; j < tanimotoMatrix[i].length; j++) {
				System.out.print("["+tanimotoMatrix[i][j]+"]  ");
			}
			System.out.println();
		}
		
		return tanimotoMatrix;
	}
	
	/**
	 * Compare fingerprints by returning the Tanimoto distance.
	 * 
	 * @param bitSet1 the bit set1
	 * @param bitSet2 the bit set2
	 * 
	 * @return the float
	 * 
	 * @throws CDKException the CDK exception
	 */
	private float compareFingerprints(BitSet bitSet1, BitSet bitSet2) throws CDKException
	{
		return Tanimoto.calculate(bitSet1, bitSet2);
	}
	
	/**
	 * Writes a result log which contains quality measures of the similarity 
	 * comparison.
	 * 
	 * @param file - the file which is used to store the matrix representation
	 */
	public void writeResultLog(File file, Object ... args) {
		Formatter formatter = null;
		try {
			formatter = new Formatter(file);
		} catch (FileNotFoundException e) {
			//e.printStackTrace();
			// File not found exception thrown since this is a new
		    // file name. However, Formatter will create the new file.
			return;
		}
		
		int number = 10;
		if(args.length > 0) {
			if(args[0] instanceof Integer) {
				int newNumber = (Integer) args[0];
				if(newNumber > 0 && newNumber < tanimotoMatrix.length)
					number = newNumber;
			}
		}
		
		int[] ranks = retrieveNearestNeighborsRanksForPrimaries(true, number);
		int sum = calculateSumOfRanks(ranks);
		float mean = calculateMeanOfRanks(ranks);
		
		// write to log file
		formatter.format("Number of ranks:\t");
		formatter.format("%4d %n", number);
		formatter.format("Sum of ranks:\t");
		formatter.format("%4d %n", sum);
		formatter.format("Mean of ranks:\t");
		formatter.format("%4.3f %n", mean);
		
		formatter.format("%nPosition:\tRanks:%n");
		for (int i = 0; i < ranks.length; i++) {
			formatter.format("[%3d]\t", i+1);
			formatter.format("%3d%n", ranks[i]);
		}
		
		formatter.flush();
		formatter.close();
	}
	
	
	/**
	 * Writes a formatted version of the Tanimoto matrix to a specified file.
	 * 
	 * @param file - the file which is used to store the matrix representation
	 */
	public void writeFormattedMatrix(File file) {
		Formatter formatter = null;
		try {
			formatter = new Formatter(file);
		} catch (FileNotFoundException e) {
			//e.printStackTrace();
			// File not found exception thrown since this is a new
		    // file name. However, Formatter will create the new file.
			return;
		}
		
		//formatter.format("MetFrag|MassBank\t");
		for (int i = 0; i < candidates.size(); i++) {
			// write column header
			if(i < (candidates.size() - 1)) {
				formatter.format(candidates.get(i).getId());
				formatter.format("[%1.3f]\t", candidates.get(i).getScore());	// write score followed by a tab
			}
			else {
				formatter.format(candidates.get(i).getId());
				formatter.format("[%1.3f]%n", candidates.get(i).getScore());	// write last score followed by newline
			}
		}
		
		for (int i = 0; i < tanimotoMatrix.length; i++) {			// rows
			for (int j = 0; j < tanimotoMatrix[i].length; j++) {	// columns
				if(j == 0 ) {			// write row header
					formatter.format(candidateToStructure.get(i).getId());
					formatter.format("[%1.3f]\t", candidateToStructure.get(i).getScore());
				}
				
				if(j < (tanimotoMatrix[i].length - 1)) {
					formatter.format("%1.3f\t", tanimotoMatrix[i][j]);
				}
				else {
					formatter.format("%1.3f%n", tanimotoMatrix[i][j]);
				}
			}
		}
		formatter.flush();
		formatter.close();
	}
	
	/**
	 * Returns the nearest maximum neighbor for a given alignment object. If there is no candidate list,
	 * the given alignment object will be returned!
	 * 
	 * @param primary the primary alignment object for which to find the nearest maximum neighbor
	 * @return the nearest maximum neighbor
	 */
	public WorkflowOutputAlignment findMaxNeighborForPrimary(WorkflowOutputAlignment primary) {
		if(mapping.containsKey(primary)) {
			Map<WorkflowOutputAlignment, Float> candidates = mapping.get(primary);
			float current = -1.0f;
			WorkflowOutputAlignment neighbor = null;
			for (WorkflowOutputAlignment woa : candidates.keySet()) {
				if(candidates.get(woa) == 1.0f)
					{
						current = candidates.get(woa);
						System.out.println("neighbor tanimoto -> " + current);
						return woa;
					}
				
				if(candidates.get(woa) > current) {
					current = candidates.get(woa);
					neighbor = woa;
				}
			}
			if(neighbor == null)
				return primary;
			else {
				System.out.println("neighbor tanimoto -> " + current);
				return neighbor;
			}
		}
		else return primary;
	}
	
	/**
	 * Returns the nearest minimum neighbor for a given alignment object.
	 * 
	 * @param primary the primary alignment object for which to find the nearest minimum neighbor
	 * @return the nearest minimum neighbor
	 */
	public WorkflowOutputAlignment findMinNeighborForPrimary(WorkflowOutputAlignment primary) {
		if(mapping.containsKey(primary)) {
			Map<WorkflowOutputAlignment, Float> candidates = mapping.get(primary);
			float current = 1.0f;
			WorkflowOutputAlignment neighbor = null;
			for (WorkflowOutputAlignment woa : candidates.keySet()) {
//				if(candidates.get(woa) == 1.0f)
//					return woa;
				
				if(candidates.get(woa) < current) {
					current = candidates.get(woa);
					neighbor = woa;
				}
			}
			if(neighbor == null)
				return primary;
			else {
				System.out.println("neighbor tanimoto -> " + current);
				return neighbor;
			}
		}
		else return primary;
	}
	
	/**
	 * Returns the n nearest maximum neighbors for a given alignment object.
	 * 
	 * @param primary the primary alignment object for which to find the nearest maximum neighbors
	 * @param number the number of neighbors to retrieve. If number <= 0 or >= size of candidate list, number is set to one (1).
	 * @return a list of the n nearest neighbors
	 */
	public List<WorkflowOutputAlignment> findNMaxNeighborsForPrimary(WorkflowOutputAlignment primary, int number) {
		if(number <= 0 || number >= candidates.size())
			number = 1;
		
		List<WorkflowOutputAlignment> neighbors = new ArrayList<WorkflowOutputAlignment>();
		neighbors.add(primary);
		
		return neighbors;
	}
	
	/**
	 * Returns the n nearest minimum neighbors for a given alignment object.
	 * 
	 * @param primary the primary alignment object for which to find the nearest minimum neighbors
	 * @param number the number of neighbors to retrieve. If number <= 0 or >= size of candidate list, number is set to one (1).
	 * @return a list of the n nearest neighbors
	 */
	public List<WorkflowOutputAlignment> findNMinNeighborsForPrimary(WorkflowOutputAlignment primary, int number) {
		if(number <= 0 || number >= candidates.size())
			number = 1;
		
		List<WorkflowOutputAlignment> neighbors = new ArrayList<WorkflowOutputAlignment>();
		neighbors.add(primary);
		
		return neighbors;
	}
	
	public List<WorkflowOutputAlignment> getNeighborsForTresholdForPrimary(WorkflowOutputAlignment primary, float treshold, boolean upper) {
		if(upper) {
			// tresh = 0.8 -> alle Nachbarn von 1.0 bis 0.8
		}
		else {
			// tresh = 0.8 -> alle Nachbarn von 0.0 bis 0.8
		}
		return null;
		
	}
	
	/**
	 * Retrieves the rank of the nearest neighbor for each of the first <b>number</b>
	 * primaries (<b>primaries</b> set to <i>true</i>) or candidates (<b>primaries</b> set to <i>false</i>).
	 * 
	 * @param primaries - boolean indicating whether to use the primaries for lookup (<b>true</b>)
	 *  or the candidate list (<b>false</b>).
	 * @param number - integer indicating the number of ranks retrieved for the first <i>number</i> objects. 
	 * 
	 * <br>If <i>number</i> is smaller than 0 or greater than number of columns (primaries <b>true</b>) or rows (primaries <b>false</b>),
	 * <i>number</i> is set to 10 or the maximum of the corresponding dimension if this is smaller than 10.
	 * 
	 * @return An int[] containing the rank of the nearest neighbor for primary/candidate <b>i</b> at position <b>[i]</b>.
	 */
	public int[] retrieveNearestNeighborsRanksForPrimaries(boolean primaries, int number) {
		int dim = 0;
		if(primaries) {		// check if number is i
			// check range of rows
			dim = tanimotoMatrix.length;
			if(number > 0 && number < dim) 
				dim = number;
			else if(number > dim)
				number = dim;
			else if(number <= 0 && dim > 10) 
				dim = 10;
			else if(number > 0 && number < 10 && dim < 10)
				dim = number;
			else if(number == dim)
				dim = number;
			else dim = tanimotoMatrix.length;
		}
//		else {
//			// check range of columns
//			dim = tanimotoMatrix.length;
//		}
		
//		Map<WorkflowOutputAlignment, Map<WorkflowOutputAlignment, Float>> mapping = null;
//		// TODO: check reverseMapping
//		if(primaries)
//			mapping = this.mapping;
//		else mapping = this.reverseMapping;
		
		int[] ranks = new int[dim];
		for (int i = 0; i < dim; i++) {
			WorkflowOutputAlignment woa;
			if(primaries) {
				woa = indexPrimaries.get(i);
			}
			else {
				woa = indexCandidates.get(i);
			}
			System.out.println("\ngetRanks for -> " + woa.getCompound() + " " + woa.getRecord() + " " + woa.getScore());
			if(i >= dim)
				break;
			
			// retrieve nearest neighbor and store rank
			WorkflowOutputAlignment nn = null;
			if(primaries) {
				nn = findMaxNeighborForPrimary(woa);
			}
			else {
				// TODO
				//nn = findMaxNeighborForCandidate(woa);
			}
			System.out.println("nearest neighbor getRanks -> " + nn.getCompound() + " " + nn.getRecord() + " " + nn.getScore()); 

			if(primaries) {
				// find index of nearest neighbor in indexCandidates
				Set<Integer> positions = indexCandidates.keySet();
				for (Iterator<Integer> it = positions.iterator(); it.hasNext();) {
					Integer pos = (Integer) it.next();
					if(nn == indexCandidates.get(pos)) {
						// add 1 to rank to switch from 0-based informatic reading to normal reading
						ranks[i] = pos + 1;
						break;
					}				
				}
			}
			else {
				// find index of nearest neighbor in indexPrimaries
				Set<Integer> positions = indexPrimaries.keySet();
				for (Iterator<Integer> it = positions.iterator(); it.hasNext();) {
					Integer pos = (Integer) it.next();
					if(nn == indexPrimaries.get(pos)) {
						// add 1 to rank to switch from 0-based informatic reading to normal reading
						ranks[i] = pos + 1;
						break;
					}				
				}
			}			
		}
		
		for (int j = 0; j < ranks.length; j++) {
			System.out.println("ranks ["+j+"] = " + ranks[j]);
		}
		return ranks;
	}
	
	public int calculateSumOfRanks(int[] ranks) {
		int sum = 0;
		for (int i = 0; i < ranks.length; i++) {
			sum += ranks[i];
		}
		return sum;
	}
	
	public float calculateMeanOfRanks(int[] ranks) {
		float mean = ((float) calculateSumOfRanks(ranks)) / ((float) ranks.length);
		return mean;
	}
}
