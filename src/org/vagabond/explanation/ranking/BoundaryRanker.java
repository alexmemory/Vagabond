package org.vagabond.explanation.ranking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
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

	public IScoringFunction funcnames;
	Boundary mybound;
	
	private int donePos;
	private int curPos;
	private int explCollSize;
	int[][] explSetArray;
	int[] optimal;
	int[] thrown;
	ExplanationCollection explcoll;
	ArrayList<IExplanationSet> RankedExplSets = new ArrayList<IExplanationSet>();
	
	private boolean doneRanking;
    
	
	public BoundaryRanker(ExplanationCollection coll, IScoringFunction f)
	{
		this.funcnames = f;
		this.mybound = new Boundary(coll, f);
		this.explcoll = coll;
		explCollSize = coll.getDimensions().capacity();
		
		explSetArray = new int[explCollSize][explCollSize];
		
		//initialize first explset
		for (int i = 0; i< explCollSize; i++)
		{
		    Arrays.fill(explSetArray[i], 0);
		    explSetArray[i][i] = 1;
		}
		
		//initialize flag array
		optimal = new int[explCollSize];
		thrown  = new int[explCollSize];
		Arrays.fill(optimal, 0);
		Arrays.fill(thrown, 0);		
		
    }
	
	@Override
	public void initialize(ExplanationCollection coll) {
		doneRanking = false;
		
	}

	@Override
	public boolean hasNext() {
		// if ranking is not done
		// finish ranking first
		if (!doneRanking)
		{
			generateUpTo(curPos + 1);
		}
		
	    return curPos < donePos;
	}

	private void generateUpTo(int upTo) {
		while (RankedExplSets.size() < upTo)
		{
			//1. extend combination of explanation set by one
			//   only extend sets not in thrown?
			//   add optimal if not in the current?
			for (int row = 0; row< explCollSize; row++)
			{
				//build combination of explanation set
				IExplanationSet currComb = buildExplSet(explSetArray[row], explcoll);
				int currUpBound = mybound.getUpBound(currComb, explSetArray[row], explcoll);
				int currLowBound = mybound.getLowBound(currComb, explSetArray[row], explcoll);
				IExplanationSet expandedComb = expandExplSet(explSetArray[row], explcoll, optimal, thrown);
				if (expandedComb.getScore() <= currLowBound)
				{
					
				}
				
				
			}
			
			//2. for each combination: update low/up boundary
			//                         compute score and compare
			//                         -- throw the combination and mark flag
			//                         -- return optimal
			//                         -- update score
			
			//3. insert ranked_expl_set array list
		}
		
	}

	private IExplanationSet expandExplSet(int[] is, ExplanationCollection explcoll2,
			int[] optimal2, int[] thrown2) {
		// TODO Auto-generated method stub
		return null;
	}

	private IExplanationSet buildExplSet(int[] is,
			ExplanationCollection explcoll2) {
		// TODO Auto-generated method stub
		return null;
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
		return iterPos > 0;
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
