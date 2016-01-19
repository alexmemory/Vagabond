package org.vagabond.explanation.ranking;

import java.util.Iterator;

import org.vagabond.explanation.model.ExplanationCollection;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.IBasicExplanation;
import org.vagabond.explanation.ranking.scoring.IScoringFunction;

public interface IExplanationRanker extends Iterator<IExplanationSet> {

	public void initializeCollection(ExplanationCollection coll);
	public boolean ready();
	public IExplanationSet getRankedExpl(int rank);
	public int getScore(int rank);
	public int getIteratorPosition();
	public IExplanationSet previous();
	public boolean hasPrevious();
	public int getNumberOfExplSets();
	public int getNumberPrefetched();
	public void resetIter();
	boolean isFullyRanked();
	public void rankFull();
	public IScoringFunction getScoringFunction();
	public void confirmExplanation(IBasicExplanation correctExpl); // user confirmed
	public boolean hasAtLeast(int numElem); // check that this ranker can produce 
											// at least these many explanation sets
}