package org.vagabond.explanation.ranking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.marker.MarkerComparators;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.model.ExplanationCollection;
import org.vagabond.explanation.model.ExplanationFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.ExplanationComparators;
import org.vagabond.explanation.model.basic.IBasicExplanation;
import org.vagabond.explanation.model.basic.IBasicExplanation.ExplanationType;
import org.vagabond.explanation.ranking.AStarExplanationRanker.OneErrorExplSet;
import org.vagabond.explanation.ranking.AStarExplanationRanker.RankedListElement;
import org.vagabond.explanation.ranking.scoring.IScoringFunction;
import org.vagabond.util.BitMatrix;
import org.vagabond.util.IdMap;
import org.vagabond.util.LogProviderHolder;
import org.vagabond.util.LoggerUtil;
import org.vagabond.util.ewah.IntIterator;

public class BoundaryRanker implements IExplanationRanker {
	
	static Logger log = LogProviderHolder.getInstance().getLogger(
			BoundaryRanker.class);
	
	ExplanationCollection explColl;
	IScoringFunction scoreFunc;
	RankingElement ele;
	RankedElements rankedElements;
	int collSize;

	public BoundaryRanker(ExplanationCollection coll, IScoringFunction f)
	{
	    this.explColl = coll;
	    this.scoreFunc = f;
		
    }
	
	public class RankingElement
	{
		int explMatrix[];
		double upBound;
		double lowBound;
		double rankingScore;
		boolean FullCover;
		int rankedSize;
		
	}
	
	public class RankedElements 
	{
		PriorityQueue<RankingElement> rankedQueue;
		int explSetSize[];
	}


	@Override
	public void initialize(ExplanationCollection coll) {
		//load expl coll size (collsize <-- # of explset)
		collSize = coll.getDimensions().capacity();
		
		int explsetSize = coll.getExplSets().size();
		rankedElements.rankedQueue = new PriorityQueue<RankingElement>(explsetSize,
				new Comparator<RankingElement>()
				{
					public int compare(RankingElement explset1, RankingElement explset2)
					{
						//if only one rank element is fully covered
						if (explset1.FullCover ^ explset2.FullCover)
						{
							if (explset2.FullCover)
							     return 1;
							if (explset1.FullCover)
							     return 0;
						}
						//none or both are fully covered
						// if explset2 covers more error marker then we prefer explset2
						else if (explset2.rankedSize > explset1.rankedSize )
						{
							return explset2.rankedSize - explset1.rankedSize ;
						}
						//else compare score / boundary?
						else
						{
							return explset2.lowBound - explset1.upBound;
							
						}
					}
				}
		);
		
		// insert all candidate from explset1 to priority queue.
		//rankedElements.rankedQueue.add(
		

		
		
	}


	@Override
	public boolean hasNext() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IExplanationSet next() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean ready() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void confirmExplanation(IBasicExplanation correctExpl) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IExplanationSet getRankedExpl(int rank) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getScore(int rank) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getIterPos() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public IExplanationSet previous() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasPrevious() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getNumberOfExplSets() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getNumberPrefetched() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void resetIter() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean hasAtLeast(int numElem) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isFullyRanked() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void rankFull() {
		// TODO Auto-generated method stub
		
	}
	
}
