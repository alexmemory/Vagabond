package org.vagabond.explanation.ranking;

import java.util.Iterator;

import org.vagabond.explanation.model.ExplPartition;
import org.vagabond.explanation.model.IExplanationSet;

public interface IPartitionRanker extends Iterator<IExplanationSet> {

	public void initialize (ExplPartition part);
	public void setPerPartitionRanker (String rankScheme);
	public String getPerPartitionRanker();
	
	public long getIterPos();
	public IExplanationSet previous();
	public boolean hasPrevious();
	public long getNumberOfExplSets ();
	public boolean isFullyRanked();
	public long getNumberPrefetched ();
	public void resetIter();
}
