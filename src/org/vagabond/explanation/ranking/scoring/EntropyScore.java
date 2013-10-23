/**
 * 
 */
package org.vagabond.explanation.ranking.scoring;

import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.model.ExplanationFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.IBasicExplanation;
import org.vagabond.explanation.model.basic.IBasicExplanation.ExplanationType;
import org.vagabond.explanation.ranking.DummyRanker;
import org.vagabond.explanation.ranking.scoring.IScoringFunction.Monotonicity;

/**
 * @author Zhen
 *
 */
public class EntropyScore implements IScoringFunction {
	public static final EntropyScore inst = new EntropyScore();

	@Override
	public int getScore(IBasicExplanation expl) {
		/*for single error, entropy is always 0.
		 */
		IMarkerSet a = expl.getRealExplains();
		int explsize = a.getSize();
		
		return 0;

	}

	@Override
	public int getScore(IExplanationSet set) {
		
		/*initialize variables*/
		double retScore = 0.0;
		
		/*
		 * for each explanation (Lambda) in explanation set ({Lambda}),
		 *  compute entropy score
		 *  
		 *  for the complete error set,
		 *  the score is the sum of weighted average of entropy score for all errors in the set
		 *  
		 *  Score({Lambda}) = |Lambda| / Sum(|Lambda|) * EntropyScore(Lambda)
		 *  
		 *  computation of entropy score for Explanation
		 *  EntropyScore(Lambda) = Sum(Entropy(ISingleMarker))
		 *  
		 *  Entropy(ISingleMarker) = 
		 *  */

		Map<IBasicExplanation, Double> mEntropyScores;
		mEntropyScores = new HashMap<IBasicExplanation, Double> ();
		
		
		Map<IBasicExplanation, Integer> mExplTupleCount;
		mExplTupleCount = new HashMap<IBasicExplanation, Integer> ();
		
		for ( IBasicExplanation tmpBasicExpl: set.getExplanationsSet())
		{
			int explTupleCount = 0;
			int explWeight = 0;
			
			int tmpTid;
			
			Map<IBasicExplanation, Set<Integer>> mTupleMap;
			mTupleMap = new HashMap<IBasicExplanation, Set<Integer>> ();
			
			Map<Integer, Integer> mTupleCountMap;
			mTupleCountMap = new HashMap<Integer, Integer> ();
			
			for (ISingleMarker tmpSingleMarker : tmpBasicExpl.getRealExplains())
			{
				// for each single error, retrieve tuple id
				tmpTid = tmpSingleMarker.getTidId();
				
				// update Explanation:TupleId map relation
				if (mTupleMap.containsKey(tmpBasicExpl))
				{
					mTupleMap.get(tmpBasicExpl).add(tmpTid);
				}
				else
				{
					Set<Integer> mNewTidSet = new HashSet<Integer>();
					mNewTidSet.add(tmpTid);
					mTupleMap.put(tmpBasicExpl, mNewTidSet);
				}
				
				// update TupleId:TupleIdCount map relation				
				// increase counter if contains the Tuple id.
				if (mTupleCountMap.containsKey(tmpTid))
				{
					mTupleCountMap.put(tmpTid, mTupleCountMap.get(tmpTid) + 1);
				}
				else
				{
					// first time table id, initial the map value as 1.
					mTupleCountMap.put(tmpTid, 1);
				}		
				
			}
			
			// compute entropy for each explanation
			double explentropy = 0.0;
			
			// compute total number of tuples counts

			Iterator<Integer> iterTupleId = mTupleCountMap.keySet().iterator();
			while (iterTupleId.hasNext())
			{
				int tempTupleId = iterTupleId.next();
				explTupleCount = explTupleCount + mTupleCountMap.get(tempTupleId);
			}
			
			
			// compute total number of tuples counts
			double tmpEntropy = 0;
			iterTupleId = null;
			iterTupleId = mTupleCountMap.keySet().iterator();
			while (iterTupleId.hasNext())
			{
				int tempTupleId = iterTupleId.next();
				double tmpWeight = mTupleCountMap.get(tempTupleId) / explTupleCount;
				tmpEntropy = tmpEntropy -  tmpWeight * Math.log(tmpWeight) ;
			}			
			
			mEntropyScores.put(tmpBasicExpl, tmpEntropy);
			mExplTupleCount.put(tmpBasicExpl, explTupleCount);
			
			
		}
		
		int TotalCount = 0;
		for (IBasicExplanation tmpExpl : mExplTupleCount.keySet() )
		{
			TotalCount += mExplTupleCount.get(tmpExpl);
		}

		return (int) (retScore * 10000);
	}

	@Override
	public Monotonicity getMonotonicityProperty() {
		return Monotonicity.unionMonotone;
	}

	@Override
	public int getScore(Collection<IBasicExplanation> expls) {
		
		/*initialize variables*/
		double retScore = 0.0;
		
		Map<ISingleMarker, IExplanationSet> mSingleErrorMap;
		mSingleErrorMap = new HashMap<ISingleMarker, IExplanationSet> ();

		/*initialize map*/
		for (IBasicExplanation expl:expls){
			for (ISingleMarker singleErr : expl.getRealExplains()){
				if (mSingleErrorMap.containsKey(singleErr))
				{
					mSingleErrorMap.get(singleErr).addExplanation(expl);
				}
				else
				{
					IExplanationSet mNewExplanationSet = ExplanationFactory.newExplanationSet(expl);
				    mSingleErrorMap.put(singleErr, mNewExplanationSet);
				}
			}
			
		}

		/*loop through map to compute average weight*/
		double mAvgWeight;
		Iterator<ISingleMarker> iterSingleErr = mSingleErrorMap.keySet().iterator();
		while (iterSingleErr.hasNext()){
			mAvgWeight = 0;
			ISingleMarker m = iterSingleErr.next();
		    for (IBasicExplanation expl:mSingleErrorMap.get(m)){
		    	/*loop through set of explanations, sum up weight of error type*/
		    	mAvgWeight += this.getErrorWeight(expl.getType());
		    }
		    /*divide sum of error weight by size of the set of explanations
		     * the result is the average weight for this single error
		    */
		    mAvgWeight /= mSingleErrorMap.get(m).getSize();
		    /*add single error weight into total score*/
			retScore += mAvgWeight;
		}
		
		
		return (int) (retScore * 10000);
	}

}