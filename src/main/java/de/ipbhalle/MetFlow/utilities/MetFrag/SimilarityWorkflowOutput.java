/**
 * created by Michael Gerlich on Dec 4, 2009 - 11:14:26 AM
 * email: mgerlich@ipb-halle.de
 */
package de.ipbhalle.MetFlow.utilities.MetFrag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.fingerprint.Fingerprinter;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.openscience.cdk.similarity.Tanimoto;

import de.ipbhalle.MetFlow.wrapper.WorkflowOutputAlignment;

public class SimilarityWorkflowOutput {

	private Float[][] matrix = null;
	private Map<WorkflowOutputAlignment, IAtomContainer> candidateToStructure = null;
	private Map<WorkflowOutputAlignment, Integer> candidateToPosition = null;
	private StringBuilder allSimilarityValues = new StringBuilder();
	private float similarityThreshold;
	
	public SimilarityWorkflowOutput(Map<WorkflowOutputAlignment, IAtomContainer> candidateToStructure, float similarityThreshold) throws CDKException
	{
		matrix = new Float[candidateToStructure.size()][candidateToStructure.size()];
		this.candidateToStructure = candidateToStructure;
		initializePositions();
		this.similarityThreshold = similarityThreshold;
		calculateSimilarity();
	}
	
	//initialize the position of the candidates in the matrix
	private void initializePositions()
	{
		int i = 0;
		candidateToPosition = new HashMap<WorkflowOutputAlignment, Integer>();
		for (WorkflowOutputAlignment candidate : candidateToStructure.keySet()) {
			candidateToPosition.put(candidate, i);
			i++;
		}
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
		Map<WorkflowOutputAlignment, BitSet> candidateToFingerprint = new HashMap<WorkflowOutputAlignment, BitSet>();
		for (WorkflowOutputAlignment woa : candidateToStructure.keySet()) {
			Fingerprinter f = new Fingerprinter();
			BitSet fp = f.getFingerprint(candidateToStructure.get(woa));
			candidateToFingerprint.put(woa, fp);
		}

		int countJ = 0;
		int countI = 0;
		for (WorkflowOutputAlignment candidate1 : candidateToStructure.keySet()) {
			for (WorkflowOutputAlignment candidate2 : candidateToStructure.keySet()) {
				if(countJ < countI)	// || candidate1.equals(candidate2)
				{
					countJ++;
					continue;
				}		
				Float similarity = compareFingerprints(candidateToFingerprint.get(candidate1), candidateToFingerprint.get(candidate2));
				matrix[countI][countJ] = similarity;
				allSimilarityValues.append(similarity + "\n");
				
				countJ++;
			}
			countJ = 0;
			countI++;
		}
		
		System.out.println("Tanimoto Matrix");
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				System.out.print("["+matrix[i][j]+"]  ");
			}
			System.out.println();
		}
		
		return matrix;
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
	 * Gets the tanimoto distance between two candidate structures.
	 * 
	 * @param candidate1 the candidate1
	 * @param candidate2 the candidate2
	 * 
	 * @return the tanimoto distance
	 */
	public float getTanimotoDistance(WorkflowOutputAlignment candidate1, WorkflowOutputAlignment candidate2)
	{
		int pos1 = 0;
		int pos2 = 0;
		try
		{
			pos1 = candidateToPosition.get(candidate1);
			pos2 = candidateToPosition.get(candidate2);
		}
		catch(NullPointerException e)
		{
			return 0;
		}
		if(matrix[pos1][pos2] != null)
			return matrix[pos1][pos2];
		else
			return matrix[pos2][pos1];
	}
	
	/**
	 * Gets the maximum tanimoto distance for a specific candidate, traverses a complete matrix row
	 * 
	 * @param candidate the candidate for which to find the maximum tanimoto distance for all other candidates except itself
	 * @return the maximum tanimoto distance
	 */
	public float getMaxTanimotoDistance(WorkflowOutputAlignment candidate) {
		int pos1 = 0;
		float tanimoto = 0.0f;
		
		try
		{
			pos1 = candidateToPosition.get(candidate);
			for (int i = 0; i < matrix[pos1].length; i++) {
				// skip empty cells
				if(matrix[pos1][i] == null)
					continue;
				
				// found new max value which is not for the same candidate (tanimoto = 1)
				if(matrix[pos1][i] > tanimoto && pos1 != i)
					tanimoto = matrix[pos1][i];
			}
		}
		catch(NullPointerException e)
		{
			return 0;
		}
		
		return tanimoto;
	}
	
	/**
	 * Cleans list: remove transitive relations
	 * Only the largest sets are used
	 * 
	 * @param groupedCandidates the grouped candidates
	 * 
	 * @return the list< similarity group>
	 */
	private List<SimilarityGroupWorkflowOutput> cleanList(List<SimilarityGroupWorkflowOutput> groupedCandidates)
	{
		List<SimilarityGroupWorkflowOutput> cleanedList = new ArrayList<SimilarityGroupWorkflowOutput>();		
		List<SimilarityGroupWorkflowOutput> alreadyRemoved = new ArrayList<SimilarityGroupWorkflowOutput>();
		
		Map<WorkflowOutputAlignment, List<WorkflowOutputAlignment>> candidatesToCompareMapBase = new HashMap<WorkflowOutputAlignment, List<WorkflowOutputAlignment>>();
		Map<WorkflowOutputAlignment, SimilarityGroupWorkflowOutput> candidatesToCompareMap = new HashMap<WorkflowOutputAlignment, SimilarityGroupWorkflowOutput>();
		
		for (SimilarityGroupWorkflowOutput simGroup : groupedCandidates) {
			candidatesToCompareMap.put(simGroup.getCandidateTocompare(), simGroup);
			candidatesToCompareMapBase.put(simGroup.getCandidateTocompare(), simGroup.getSimilarCandidatesWithBase());
		}
		
		if(groupedCandidates.size() == 1)
			cleanedList = groupedCandidates;
		else
		{	
			//initialize with one value
			cleanedList.add(groupedCandidates.get(0));
			for (WorkflowOutputAlignment key1 : candidatesToCompareMap.keySet()) {
				for (WorkflowOutputAlignment key2 : candidatesToCompareMap.keySet()) {
					if(key1.equals(key2))
						continue;
					
					List<WorkflowOutputAlignment> similar1 = candidatesToCompareMapBase.get(key1);
					List<WorkflowOutputAlignment> similar2 = candidatesToCompareMapBase.get(key2);
					
					if(isIdentical(similar1, similar2))
					{
						continue;
					}
					
					if(similar1.size() > similar2.size() && similar1.containsAll(similar2))
					{
						cleanedList.remove(candidatesToCompareMap.get(key2));
						alreadyRemoved.add(candidatesToCompareMap.get(key2));
					}
					else if(similar2.containsAll(similar1))
					{
						cleanedList.remove(candidatesToCompareMap.get(key1));
						alreadyRemoved.add(candidatesToCompareMap.get(key1));
					}
					else
					{
						if(!cleanedList.contains(candidatesToCompareMap.get(key2)) && !alreadyRemoved.contains(candidatesToCompareMap.get(key2)))
							cleanedList.add(candidatesToCompareMap.get(key2));
						if(!cleanedList.contains(candidatesToCompareMap.get(key1)) && !alreadyRemoved.contains(candidatesToCompareMap.get(key1)))
							cleanedList.add(candidatesToCompareMap.get(key1));
					}
						
				}
			}
			cleanedList = removeDuplicates(cleanedList);
		}
		return cleanedList;
	}
	
	
	/**
	 * Checks if the given structures are isomorph.
	 * 
	 * @param candidate1 the candidate1
	 * @param candidate2 the candidate2
	 * 
	 * @return true, if is isomorph
	 * 
	 * @throws CDKException the CDK exception
	 */
	public boolean isIsomorph(WorkflowOutputAlignment candidate1, WorkflowOutputAlignment candidate2) throws CDKException
	{
		IAtomContainer cand1 = this.candidateToStructure.get(candidate1);
		IAtomContainer cand2 = this.candidateToStructure.get(candidate2);
		return UniversalIsomorphismTester.isIsomorph(cand1, cand2);
	}
	
	/**
	 * Checks if is identical.
	 * 
	 * @param similar1 the similar1
	 * @param similar2 the similar2
	 * 
	 * @return true, if is identical
	 */
//	private boolean isIdentical(List<String> similar1, List<String> similar2)
//	{		
//		String[] candidatesToCompare = new String[similar1.size()];
//		candidatesToCompare = similar1.toArray(candidatesToCompare);
//		Arrays.sort(candidatesToCompare);
//		
//		boolean isAlreadyContained = false;
//		
//		String[] candidateListTemp = new String[similar2.size()];
//		candidateListTemp = similar2.toArray(candidateListTemp);
//		Arrays.sort(candidateListTemp);
//		
//		if(Arrays.equals(candidatesToCompare, candidateListTemp))
//		{
//			isAlreadyContained = true;
//		}
//
//		return isAlreadyContained;
//	}
	
	/**
	 * Checks if is identical.
	 * 
	 * @param similar1 the similar1
	 * @param similar2 the similar2
	 * 
	 * @return true, if is identical
	 */
	private boolean isIdentical(List<WorkflowOutputAlignment> similar1, List<WorkflowOutputAlignment> similar2)
	{		
		WorkflowOutputAlignment[] candidatesToCompare = new WorkflowOutputAlignment[similar1.size()];
		candidatesToCompare = similar1.toArray(candidatesToCompare);
		Arrays.sort(candidatesToCompare);
		
		boolean isAlreadyContained = false;
		
		WorkflowOutputAlignment[] candidateListTemp = new WorkflowOutputAlignment[similar2.size()];
		candidateListTemp = similar2.toArray(candidateListTemp);
		Arrays.sort(candidateListTemp);
		
		if(Arrays.equals(candidatesToCompare, candidateListTemp))
		{
			isAlreadyContained = true;
		}

		return isAlreadyContained;
	}
	
	/**
	 * Removes the duplicates.
	 * 
	 * @param groupedCandidates the grouped candidates
	 * 
	 * @return the list< similarity group>
	 */
	private List<SimilarityGroupWorkflowOutput> removeDuplicates(List<SimilarityGroupWorkflowOutput> groupedCandidates)
	{		
		List<SimilarityGroupWorkflowOutput> toRemove = new ArrayList<SimilarityGroupWorkflowOutput>();
		
		
		if(groupedCandidates.size() == 1)
			return groupedCandidates;
		else
		{
			for (SimilarityGroupWorkflowOutput simGroup1 : groupedCandidates) {
				
				if(toRemove.contains(simGroup1))
					continue;
				
				WorkflowOutputAlignment[] candidatesToCompare = new WorkflowOutputAlignment[simGroup1.getSimilarCandidatesWithBase().size()];
				candidatesToCompare = simGroup1.getSimilarCandidatesWithBase().toArray(candidatesToCompare);
				Arrays.sort(candidatesToCompare);
				
				
				for (SimilarityGroupWorkflowOutput simGroup2 : groupedCandidates) {
										
					if(simGroup1.equals(simGroup2) || toRemove.contains(simGroup2))
						continue;
					
					List<WorkflowOutputAlignment> temp = simGroup2.getSimilarCandidatesWithBase();
					WorkflowOutputAlignment[] candidateListTemp = new WorkflowOutputAlignment[temp.size()];
					candidateListTemp = simGroup2.getSimilarCandidatesWithBase().toArray(candidateListTemp);
					Arrays.sort(candidateListTemp);
					if(Arrays.equals(candidatesToCompare, candidateListTemp))
						toRemove.add(simGroup2);			
				}
			}
			
			for (SimilarityGroupWorkflowOutput simGroup : toRemove) {
				groupedCandidates.remove(simGroup);
			}
			return groupedCandidates;
		}
	}
	
	/**
	 * Gets the all similarity values.
	 * 
	 * @return the all similarity values
	 */
	public StringBuilder getAllSimilarityValues()
	{
		return allSimilarityValues;
	}
	
	/**
	 * Gets the tanimoto distance from a list of candidates and groups them!.
	 * 
	 * @param candidateGroup the candidate group
	 * @param threshold the threshold
	 * 
	 * @return the tanimoto distance list
	 */
	public List<SimilarityGroupWorkflowOutput> getTanimotoDistanceList(List<WorkflowOutputAlignment> candidateGroup)
	{
		List<SimilarityGroupWorkflowOutput> groupedCandidates = new ArrayList<SimilarityGroupWorkflowOutput>();
		for (WorkflowOutputAlignment cand1 : candidateGroup) {
			SimilarityGroupWorkflowOutput simGroup = new SimilarityGroupWorkflowOutput(cand1);
			for (WorkflowOutputAlignment cand2 : candidateGroup) {
				System.out.println("Comparing cand1: " + cand1.getId() + "/" + cand1.getCompound() + "  with cand2: " + cand2.getId() + "/" + cand2.getCompound());
				if(cand1.equals(cand2))
					continue;
				else if(cand1 == null || cand2 == null)
					continue;
				else
				{
					// suche groesste tanimoto wert fuer cand1 und fuege den cand2 hinzu
					Float tanimoto = getTanimotoDistance(cand1, cand2);
					System.out.println("tanimoto = " + tanimoto + "\n");
					if(tanimoto > similarityThreshold)
						simGroup.addSimilarCompound(cand2, tanimoto);
				}
			}
			
//			System.out.println("\nsimgroup for -> " + cand1.getCompound() + "  " + cand1.getId() + "  " + cand1.getRecord());
//			for (WorkflowOutputAlignment woa : simGroup.getSimilarCompounds()) {
//				System.out.println("similar compound -> " + woa.getCompound() + "  " + woa.getId() + "  " + woa.getRecord());
//			}
			
			//now add similar compound to the group list
			//if(!isContainedInPreviousResults(groupedCandidates, simGroup))
			groupedCandidates.add(simGroup);
		}
		groupedCandidates = cleanList(groupedCandidates);
		return groupedCandidates;
	}
	
	/**
	 * Gets the tanimoto alignment.
	 * 
	 * @param allOutputs all outputs from all ports (w/ and w/o moldata) 
	 * @param testMap the test map containing the outputs from the primary alignment column (only w/ moldata)
	 * @param cands the candidate list from the non-primary output ports (only w/ moldata)
	 * @param primary the index of the primary alignment column taken from allOutputs
	 * 
	 * @return the tanimoto alignment, including all outputs, sorted by score, tanimoto distance and outputs w/o moldata
	 */
	public List<List<WorkflowOutputAlignment>> getTanimotoAlignment(List<List<WorkflowOutputAlignment>> allOutputs,
			Map<WorkflowOutputAlignment, IAtomContainer> testMap, List<WorkflowOutputAlignment> cands, int primary) {
		// new list of lists for alignment
		List<List<WorkflowOutputAlignment>> newAlign = new ArrayList<List<WorkflowOutputAlignment>>();
		List<SimilarityGroupWorkflowOutput> similars = getTanimotoDistanceList(cands);
		
		// all rows from the outputs
		for (int i = 0; i < allOutputs.size(); i++) {
			// all (2) columns, one for MetFrag, one for MassBank
			for (int j = 0; j < allOutputs.get(i).size(); j++) {
				// fuer jeden primary testen, welches sein naechster nachbar in der tanimoto float[][] matrix ist und diesen alignen
				// duplikat zuweisungen erlauben -> grouping der ergebnisse in primary column
				if(j == primary) {	// primary output gefunden
					// fuer jeden aehnlichen compound, finde den einen aehnlichsten
					float tanimotoMax = -1.0f;
					WorkflowOutputAlignment neighbor = null;
					for (SimilarityGroupWorkflowOutput sgw : similars) {
						List<Float> tanimotos = sgw.getSimilarCompoundsTanimoto();
						for(int l = 0; l < tanimotos.size(); l++) {
							// set new nearest neighbor
							if(tanimotos.get(l) > tanimotoMax) {
								tanimotoMax = tanimotos.get(l);
								// obere Dreiecksmatrix ohne Diagonale -> selber compound liefert auf sich selbst 0
								neighbor = sgw.getSimilarCompounds().get(l);
							}
						}
					}
					
					List<WorkflowOutputAlignment> group = new ArrayList<WorkflowOutputAlignment>();
					group.add(allOutputs.get(i).get(primary));
					group.add(neighbor);
					newAlign.add(group);
				}
				
				// korrekte zuweisung in die liste per score fehlt noch
			}
		}
		
		return newAlign;
	}
}
