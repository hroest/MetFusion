/**
 * created by Michael Gerlich on Dec 4, 2009 - 1:05:23 PM
 * email: mgerlich@ipb-halle.de
 */
package de.ipbhalle.MetFlow.utilities.MetFrag;

import java.util.ArrayList;
import java.util.List;

import de.ipbhalle.MetFlow.wrapper.WorkflowOutputAlignment;

public class SimilarityGroupWorkflowOutput {

	private List<WorkflowOutputAlignment> similarCandidates = new ArrayList<WorkflowOutputAlignment>();
	private List<WorkflowOutputAlignment> similarCandidatesWithBase = new ArrayList<WorkflowOutputAlignment>();
	private List<Float> tanimotoCandidates = new ArrayList<Float>();
	private WorkflowOutputAlignment candidateTocompare;
	
//	public SimilarityGroupWorkflowOutput(String candidateToCompare)
//	{
//		super(candidateToCompare);
//		this.candidateTocompare = candidateToCompare;
//		this.similarCandidatesWithBase.add(candidateToCompare);
//	}
	
	public SimilarityGroupWorkflowOutput(WorkflowOutputAlignment candidateToCompare)
	{
		this.candidateTocompare = candidateToCompare;
		this.similarCandidatesWithBase.add(candidateToCompare);
	}
	
	/**
	 * Adds the similar compound.
	 * 
	 * @param candidate the candidate
	 */
	public void addSimilarCompound(WorkflowOutputAlignment candidate, float tanimotoDist)
	{
		similarCandidates.add(candidate);
		similarCandidatesWithBase.add(candidate);
		tanimotoCandidates.add(tanimotoDist);
	}
	
	/**
	 * Gets the similar compounds.
	 * 
	 * @return the similar compounds
	 */
	public List<WorkflowOutputAlignment> getSimilarCompounds()
	{
		return similarCandidates;
	}
	
	
	
	/**
	 * Gets the similar compounds' tanimoto distances
	 * 
	 * @return the similar compounds
	 */
	public List<Float> getSimilarCompoundsTanimoto()
	{
		return tanimotoCandidates;
	}

	public WorkflowOutputAlignment getCandidateTocompare() {
		return candidateTocompare;
	}


	/**
	 * Gets the similar candidates with base.
	 * 
	 * @return the similar candidates with base
	 */
	public List<WorkflowOutputAlignment> getSimilarCandidatesWithBase() {
		return similarCandidatesWithBase;
	}

	public void addSimilarCompound(WorkflowOutputAlignment candidate, Float tanimotoDist) {
		similarCandidates.add(candidate);
		similarCandidatesWithBase.add(candidate);
		tanimotoCandidates.add(tanimotoDist);		
	}
}
