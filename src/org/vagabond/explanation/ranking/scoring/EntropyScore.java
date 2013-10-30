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
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.TupleMarker;
import org.vagabond.explanation.model.ExplanationFactory;
import static org.vagabond.explanation.model.ExplanationFactory.*;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.IBasicExplanation;
import org.vagabond.explanation.ranking.DummyRanker;
import org.vagabond.explanation.ranking.scoring.IScoringFunction.Monotonicity;
import org.vagabond.explanation.model.basic.IBasicExplanation.ExplanationType;

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
		 *  
		 *  Map<TupleMarker, ExplanationSet>
		 *  Map<TupleMarker, Double>
		 *  Map<TupleMarker Integer>
		 *  */
		
		Map<TupleMarker, IExplanationSet> mTupleExplSetMap;
		mTupleExplSetMap = new HashMap<TupleMarker, IExplanationSet> ();
		
		Map<TupleMarker, Double>mTupleEntropyMap;
		mTupleEntropyMap = new HashMap<TupleMarker, Double> ();
				
		Map<TupleMarker, IMarkerSet> mTupleMarkerSetMap;
		mTupleMarkerSetMap = new HashMap<TupleMarker, IMarkerSet> ();
		
		for ( IBasicExplanation tmpBasicExpl: set.getExplanationsSet())
		{
			
			for (ISingleMarker tmpSingleMarker : tmpBasicExpl.getRealExplains())
			{
				int tmpRId = tmpSingleMarker.getRelId();
				int tmpTId = tmpSingleMarker.getTidId();
				TupleMarker tmpTupleMarker = new TupleMarker(tmpRId, tmpTId);
				
				if (mTupleExplSetMap.containsKey(tmpTupleMarker))
					mTupleExplSetMap.get(tmpTupleMarker).add(tmpBasicExpl);
				else
				{
					IExplanationSet tmpSet = newExplanationSet();
					tmpSet.add(tmpBasicExpl);
					mTupleExplSetMap.put(tmpTupleMarker, tmpSet);
				}
				
				if (mTupleMarkerSetMap.containsKey(tmpTupleMarker))
					mTupleMarkerSetMap.get(tmpTupleMarker).add(tmpSingleMarker);
				else
					mTupleMarkerSetMap.put(tmpTupleMarker, MarkerFactory.newMarkerSet(tmpSingleMarker));
			}
		}
		
		//initialize entropy map
		for (TupleMarker tmpTupleMarker : mTupleMarkerSetMap.keySet())
		{
			mTupleEntropyMap.put(tmpTupleMarker, 0.0);
		}
		
		//loop through Map<TupleMarker, ExplanationSet>
		Map<ExplanationType, Double>mErrTypeCounterMap;
		mErrTypeCounterMap = new HashMap<ExplanationType, Double> ();
		mErrTypeCounterMap.put(ExplanationType.CopySourceError, 0.0);
		mErrTypeCounterMap.put(ExplanationType.InfluenceSourceError, 0.0);
		mErrTypeCounterMap.put(ExplanationType.CorrespondenceError, 0.0);
		mErrTypeCounterMap.put(ExplanationType.SuperflousMappingError, 0.0);
		mErrTypeCounterMap.put(ExplanationType.SourceSkeletonMappingError, 0.0);
		mErrTypeCounterMap.put(ExplanationType.TargetSkeletonMappingError, 0.0);
	
		int totalExplSetSize = 0;
//		for (TupleMarker tmpTupleMarker : mTupleExplSetMap.keySet())
//		{
//			int mSizeofExplSet   = mTupleExplSetMap.get(tmpTupleMarker).size();
//			totalExplSetSize = totalExplSetSize + mSizeofExplSet;       
//		}
		totalExplSetSize = set.getSize();

		for (TupleMarker tmpTupleMarker : mTupleExplSetMap.keySet())
		{
			int mCurrentExplSetSize = mTupleExplSetMap.get(tmpTupleMarker).size();

			double currentExplWeight;
			int mCurrentExplSize;
		    for (IBasicExplanation expl : mTupleExplSetMap.get(tmpTupleMarker))
		    {
		    	mErrTypeCounterMap.put(expl.getType(), mErrTypeCounterMap.get(expl.getType())+1);
		    }
		    
		    for (IBasicExplanation expl : mTupleExplSetMap.get(tmpTupleMarker))
		    {
		    	mCurrentExplSize = expl.getRealExplains().getSize();
		    	currentExplWeight = mCurrentExplSize/ mCurrentExplSetSize;
			    double explScore = getPartialEntropy(mErrTypeCounterMap, mCurrentExplSize);	
			    
			    mTupleEntropyMap.put(tmpTupleMarker, 
			    		mTupleEntropyMap.get(tmpTupleMarker) + currentExplWeight * explScore);
			    
		    }
		}
		
		for (TupleMarker tmpTupleMarker : mTupleExplSetMap.keySet())
		{
			double currentExplSetWeight = mTupleExplSetMap.get(tmpTupleMarker).getSize() / totalExplSetSize;
			retScore = retScore + mTupleEntropyMap.get(tmpTupleMarker);
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

		
		Map<TupleMarker, Set<IBasicExplanation>>mTupleExplSetMap;
		mTupleExplSetMap = new HashMap<TupleMarker, Set<IBasicExplanation>> ();
		
		Map<TupleMarker, Double>mTupleEntropyMap;
		mTupleEntropyMap = new HashMap<TupleMarker, Double> ();
				
		Map<TupleMarker, Set<IBasicExplanation>>mTupleMarkerSetMap;
		mTupleMarkerSetMap = new HashMap<TupleMarker, Set<IBasicExplanation>> ();
		
		for ( IBasicExplanation tmpBasicExpl: expls)
		{
			
			for (ISingleMarker tmpSingleMarker : tmpBasicExpl.getRealExplains())
			{
				int tmpRId = tmpSingleMarker.getRelId();
				int tmpTId = tmpSingleMarker.getTidId();
				TupleMarker tmpTupleMarker = new TupleMarker(tmpRId, tmpTId);
				//ISchemaMarker
				if (mTupleExplSetMap.containsKey(tmpTupleMarker))
				{
					mTupleExplSetMap.get(tmpTupleMarker).add(tmpBasicExpl);
				}
				else
				{
					Set<IBasicExplanation> tmpSet = new HashSet<IBasicExplanation> ();
					tmpSet.add(tmpBasicExpl);
					mTupleExplSetMap.put(tmpTupleMarker, tmpSet);
				}
				
				if (mTupleMarkerSetMap.containsKey(tmpTupleMarker))
				{
					mTupleMarkerSetMap.get(tmpTupleMarker).add(tmpBasicExpl);
				}
				else
				{
					Set<IBasicExplanation> tmpSet = new HashSet<IBasicExplanation> ();
					tmpSet.add(tmpBasicExpl);
					mTupleMarkerSetMap.put(tmpTupleMarker, tmpSet);
				}				
			}
		}
		
		//initialize entropy map
		for (TupleMarker tmpTupleMarker : mTupleMarkerSetMap.keySet())
		{
			mTupleEntropyMap.put(tmpTupleMarker, 0.0);
		}
		
		//loop through Map<TupleMarker, ExplanationSet>
		Map<ExplanationType, Double>mErrTypeCounterMap;
		mErrTypeCounterMap = new HashMap<ExplanationType, Double> ();
		mErrTypeCounterMap.put(ExplanationType.CopySourceError, 0.0);
		mErrTypeCounterMap.put(ExplanationType.InfluenceSourceError, 0.0);
		mErrTypeCounterMap.put(ExplanationType.CorrespondenceError, 0.0);
		mErrTypeCounterMap.put(ExplanationType.SuperflousMappingError, 0.0);
		mErrTypeCounterMap.put(ExplanationType.SourceSkeletonMappingError, 0.0);
		mErrTypeCounterMap.put(ExplanationType.TargetSkeletonMappingError, 0.0);
	
		int totalExplSetSize = expls.size();
//		for (TupleMarker tmpTupleMarker : mTupleExplSetMap.keySet())
//		{
//			int mSizeofExplSet   = mTupleExplSetMap.get(tmpTupleMarker).size();
//			totalExplSetSize = totalExplSetSize + mSizeofExplSet;       
//		}

		for (TupleMarker tmpTupleMarker : mTupleExplSetMap.keySet())
		{
			int mCurrentExplSetSize = mTupleExplSetMap.get(tmpTupleMarker).size();
//			double currentExplSetWeight = mCurrentExplSetSize / totalExplSetSize;
//			double currentExplWeight;
//			int mCurrentExplSize;
		    for (IBasicExplanation expl : mTupleMarkerSetMap.get(tmpTupleMarker))
		    {
		    	mErrTypeCounterMap.put(expl.getType(), mErrTypeCounterMap.get(expl.getType())+1);
		    }

//	    	mCurrentExplSize = .getRealExplains().getSize();
//	    	currentExplWeight = mCurrentExplSetSize / mCurrentExplSetSize;
		    double tupleMarkerScore = getPartialEntropy(mErrTypeCounterMap, mCurrentExplSetSize);	
		    
		    mTupleEntropyMap.put(tmpTupleMarker, tupleMarkerScore);
		    
//		    for (IBasicExplanation expl : mTupleMarkerSetMap.get(tmpTupleMarker))
//		    {
//
//			    
//		    }
		    
		}
		
		for (TupleMarker tmpTupleMarker : mTupleExplSetMap.keySet())
		{
			retScore = retScore + (mTupleEntropyMap.get(tmpTupleMarker) * mTupleExplSetMap.get(tmpTupleMarker).size() / totalExplSetSize);
		}
		return (int) (retScore * 10000);
	}
	
	
	private double getPartialEntropy(Map<ExplanationType, Double> ErrTypeCounter, int TotalSize)
	{
		double retScore = 0.0;
		
		for (ExplanationType ErrType : ErrTypeCounter.keySet())
		{
			double probability = ErrTypeCounter.get(ErrType) / TotalSize;
			retScore = retScore - probability * Math.log(probability);
		}

		return retScore;
	}

	private void resetErrorCounter(Map<ExplanationType, Double> ErrTypeCounter)
	{

		for (ExplanationType ErrType : ErrTypeCounter.keySet())
		{
			ErrTypeCounter.put(ErrType, 0.0);
		}

	}
	
}