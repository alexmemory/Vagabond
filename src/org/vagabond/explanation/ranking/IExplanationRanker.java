package org.vagabond.explanation.ranking;

import java.util.Iterator;

import org.vagabond.explanation.model.ExplanationCollection;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.IBasicExplanation;



public interface IExplanationRanker extends Iterator<IExplanationSet> {

//	public void setExplanationCollection (ExplanationCollection coll);
	public void initialize (ExplanationCollection coll);
	public boolean ready ();
	// user confirmed 
	public void confirmExplanation (IBasicExplanation correctExpl);
	
	public int getIterPos();
	public IExplanationSet previous();
	public boolean hasPrevious();
	public int getNumberOfExplSets ();
	public int getNumberPrefetched ();
	public void resetIter();
}
