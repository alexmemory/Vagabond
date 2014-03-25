package org.vagabond.explanation.ranking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
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
	
	/*
	 * \item input: explanation set collection, score function
\item step1: given the score function and the size/other property of the expl set collection, 
             initialize up/low boundary
\item step2: create ID map for error - expl set, error - expl
\item step3: starting from error0, for every expl in the expl set that explains error0, compute score, update boundary, insert to priority-queue
\item step4: for each element in the queue, expand the element until every error is explained.
\item step5: if all errors are explained, move to CES set with scores.
\item step6: continue step 4 for all candidates in the queue, then the CES set will be complete.


boundary class (input: score func, expl coll; output: up boundary, low boundary)
ID map (input: expl coll, err set; output: mapping err - expl sets, supports indexing)
rank element (up bound, low bound, score, map vector)
priority queue of elements (ranked by low bound first, then up bound)
ces queue (expl set, score, index)
a method to create expl set from id map and explset vector

	 */
	static Logger log = LogProviderHolder.getInstance().getLogger(
			BoundaryRanker.class);

	public IScoringFunction myfunc;
	Boundary mybound;
	
	private int donePos;
	private int curPos;
	private int explCollSize;
	int[][] explSetArray;
	int[] optimal;
	int[] thrown;
	ExplanationCollection explcoll;
	int currentstep;
	int maxStep;
	IMarkerSet allErrSets;
	ISingleMarker[] allErrs;
	
	//priority queue of elements
    PriorityQueue<FullCoverExplSet> RankedFullExplSets = new PriorityQueue<FullCoverExplSet>();
    PriorityQueue<RankElement> PartialExplSets = new PriorityQueue<RankElement>();
    
    //ID-Map required
    Map erridMap = new IdentityHashMap();
    
	//priority queue of elements
	public class RankElement
	{
		int[] ExplSetVec;
		IMarkerSet coveredErrs;
		IExplanationSet explSet;
		int upBound;
		int lowBound;
		int score;
		IScoringFunction func;
	}
	
	public IExplanationSet generateExplSet(int[] ExplVector, Map IDMap)
	{
		// for each index in ExplVector, retrieve the expl from the non-zero indexed explset in explcoll
		
		IExplanationSet retExplSet = ExplanationFactory.newExplanationSet();
		
		for (int i = 0; i < ExplVector.length; i ++)
		{
		  
			//erridMap.put(singleErr, currExplSet);
			ISingleMarker e = (ISingleMarker) IDMap.keySet().toArray()[i];
			IExplanationSet explSet = (IExplanationSet) IDMap.get(e);
			int explIdx = ExplVector[i];
			IBasicExplanation expl = (IBasicExplanation) explSet.toArray()[explIdx];
			retExplSet.add(expl);
		
		}
		return null;
		
	}
	
	class boundarycomp implements Comparator<RankElement> {
		  public int compare(RankElement ele1, RankElement ele2) {			  
		    if (ele1.lowBound > ele2.upBound) 
		    {
		      //low1 > up2 ==> 1
		      return 1;
		    } 
		    
		    
		    else if (ele1.upBound < ele1.upBound) 
		    {
		      //low1 <= up2 && up1 < up2 ==> -1
		      return -1;
		    } 

		    else if (ele1.upBound > ele1.upBound) 
		    {
		      //low1 <= up2 && up1 > up2 ==> -1
		      return 1;
		    } 
		    
		    else 
		    {
		      //low1 <= up2 && up1 = up2 ==> -1
		      return 0;
		    }
		  }
		}
	
	//Priority queue of CES
	public class FullCoverExplSet
	{
		IExplanationSet fullexplset;
		int rank;
		int score;
	}
	
	class comp implements Comparator<FullCoverExplSet> {
		  public int compare(FullCoverExplSet explset1, FullCoverExplSet explset2) {
		    if (explset1.score > explset2.score) {
		      return 1;
		    } else if (explset1.score < explset2.score) {
		      return -1;
		    } else {
		      return 0;
		    }
		  }
		}	
	
	private boolean doneRanking;
    
	
	public BoundaryRanker(ExplanationCollection coll, IScoringFunction f)
	{
		this.myfunc = f;
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
		
		//initialize ID Map
		Collection<IExplanationSet> explsets = coll.getExplSets();
		for (IExplanationSet ExplSet:explsets)
		{
			IMarkerSet a = ExplSet.getExplains();
			List<IBasicExplanation> expls = ExplSet.getExplanations();

			for (ISingleMarker singleErr:a)
			{
				IExplanationSet currExplSet = (IExplanationSet) erridMap.get(singleErr);
				currExplSet.addAll(expls);
			    erridMap.put(singleErr, currExplSet);
			    allErrSets.add(singleErr);
			}
		}
		
		//initialize error array
		allErrs = (ISingleMarker[]) erridMap.keySet().toArray();
		
		maxStep = allErrs.length;
		
		
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
		// Issues: 
		// 1) exponential amount of combination candidates
		// 2) duplicate/common combinations
		// 3) multiple optimal combination candidates at each size level
		if (upTo == -1)
		{
		    //rankFull
			//iterate by error index, incrementally set explanations
			for (int errIdx = 0; errIdx< maxStep; errIdx++)
			{
				//1. if any element in queue, pop out and extend
				RankElement topEle = PartialExplSets.poll();
				IExplanationSet currStepExplSet = (IExplanationSet) erridMap.get(allErrs[errIdx]);
				int branches = currStepExplSet.size();
				if (topEle != null)
				{					
					for (int i = 0; i<branches; i++)
					{
						RankElement cpEle = topEle;
						//extend explanation set
						cpEle.explSet.add(currStepExplSet.getExplanations().get(i));
						//update low and up boundary
						cpEle.upBound = Math.max(cpEle.upBound, this.myfunc.getScore(cpEle.explSet));
						cpEle.lowBound = Math.min(cpEle.lowBound, this.myfunc.getScore(cpEle.explSet));
						
						//compute score
						cpEle.score = this.myfunc.getScore(cpEle.explSet);
						
						//if full covered, move to full explanation set queue?
						if (cpEle.coveredErrs.diff(this.allErrSets) == null)
						{
							FullCoverExplSet newFullCS = new FullCoverExplSet();
							newFullCS.fullexplset = cpEle.explSet;
							newFullCS.score = cpEle.score;
							RankedFullExplSets.add(newFullCS);
						}
						
						//insert back to queue
						else 
						{
							PartialExplSets.add(cpEle);
						}
					}
					
				}					
				//2. create new element, compute score and then insert
				else
				{
					
					for (int i = 0; i<branches; i++)
					{
						RankElement newEle = new RankElement();
						newEle.explSet.add(currStepExplSet.getExplanations().get(i));
						//update low and up boundary
						newEle.upBound = Math.max(newEle.upBound, this.myfunc.getScore(newEle.explSet));
						newEle.lowBound = Math.min(newEle.lowBound, this.myfunc.getScore(newEle.explSet));
						
						//compute score
						newEle.score = this.myfunc.getScore(newEle.explSet);
						
						//if full covered, move to full explanation set queue?
						if (newEle.coveredErrs.diff(this.allErrSets) == null)
						{
							FullCoverExplSet newFullCS = new FullCoverExplSet();
							newFullCS.fullexplset = newEle.explSet;
							newFullCS.score = newEle.score;
							RankedFullExplSets.add(newFullCS);
						}
						
						//insert back to queue
						else 
						{
							PartialExplSets.add(newEle);
						}
					
					}
				}
			}
		}
		
		//limited generate
		else while (RankedFullExplSets.size() < upTo && !doneRanking)
		{
			
			/*
			 * \
			 * item step3: starting from error0, for every expl in the expl set that explains error0, compute score, update boundary, insert to priority-queue
		\item step4: for each element in the queue, expand the element until every error is explained.
		\item step5: if all errors are explained, move to CES set with scores.
			 */
			
			
			
			
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
				
				if (myfunc.getScore(expandedComb)<= currLowBound)
				{
					// throw branches with score lower or equal to low bound
				}
				else if (myfunc.getScore(expandedComb) >= currUpBound)
				{
					// add into optimal if reaches up bound
				}
				else
				{
					// for values between boundaries, cache?
					
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
		if (doneRanking)
		{
			if (rank > RankedFullExplSets.size())
			    return null;
			else
			{
				FullCoverExplSet[] explsets = (FullCoverExplSet[]) RankedFullExplSets.toArray();
				return explsets[rank].fullexplset;
			}
				
		}
		else
		{
			generateUpTo(rank);
			return getRankedExpl(rank);
		}
	}

	@Override
	public int getScore(int rank) {
		if (doneRanking)
		{
			if (rank > RankedFullExplSets.size())
			    return -1;
			else
			{
				FullCoverExplSet[] explsets = (FullCoverExplSet[]) RankedFullExplSets.toArray();
				return explsets[rank].score;
			}
				
		}
		else
		{
			generateUpTo(rank);
			return getScore(rank);
		}
		
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
	public void resetIter() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean hasAtLeast(int numElem) {
		return RankedFullExplSets.size()>=numElem;
	}

	@Override
	public boolean isFullyRanked() {
		return doneRanking;
	}

	@Override
	public void rankFull() {
		generateUpTo(-1);
		
	}
}
