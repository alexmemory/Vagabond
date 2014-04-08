/**
 * 
 */
package org.vagabond.explanation.ranking.scoring;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ISchemaMarker;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.model.ExplanationFactory;

import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.IBasicExplanation;
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
		//IMarkerSet a = expl.getRealExplains();
		//int explsize = a.getSize();
		
		return 0;

	}
	
	@Override
	public int getFTypeCode()
	{
		return Entropy_Score;
	}

	@Override
	public int getScore(IExplanationSet set) {
		
		/*initialize variables*/
		double retScore = 0.0;
		
		// define datatypes for this scoring function
		Map<ISchemaMarker, IExplanationSet> mPartialSchemaExplSetMap;
		mPartialSchemaExplSetMap = new HashMap<ISchemaMarker, IExplanationSet> ();
		
		Map<ISchemaMarker, Double>mPartialSchemaEntropyScoreMap;
		mPartialSchemaEntropyScoreMap = new HashMap<ISchemaMarker, Double> ();
				
		Map<ISchemaMarker, IMarkerSet> mPartialSchemaMarkerSetMap;
		mPartialSchemaMarkerSetMap = new HashMap<ISchemaMarker, IMarkerSet> ();
		
		
		// step1. partitioning explanation set according to target schemaMarker (relationID + attributeID)
		for ( IBasicExplanation tmpBasicExpl: set.getExplanationsSet())
		{
			
			for (ISingleMarker tmpSingleMarker : tmpBasicExpl.getRealExplains())
			{
				Collection<ISchemaMarker> tmpSchemaMarkerColl = MarkerFactory.newSchemaMarker(tmpSingleMarker);
				
				for (ISchemaMarker tmpSchemaMarker : tmpSchemaMarkerColl)
				{
					// update mapping of schemaMarker -> partial explanation set
					if (mPartialSchemaExplSetMap.containsKey(tmpSchemaMarker))
						mPartialSchemaExplSetMap.get(tmpSchemaMarker).add(tmpBasicExpl);
					else
						mPartialSchemaExplSetMap.put(tmpSchemaMarker, 
								ExplanationFactory.newExplanationSet(tmpBasicExpl));
					
					// update mapping of schemaMarker -> partial marker set
					if (mPartialSchemaMarkerSetMap.containsKey(tmpSchemaMarker))
						mPartialSchemaMarkerSetMap.get(tmpSchemaMarker).add(tmpSingleMarker);
					else
						mPartialSchemaMarkerSetMap.put(tmpSchemaMarker, MarkerFactory.newMarkerSet(tmpSingleMarker));
				}
			}
		}
		
		//initialize entropy map
		for (ISchemaMarker tmpSchemaMarker : mPartialSchemaExplSetMap.keySet())
		{
			mPartialSchemaEntropyScoreMap.put(tmpSchemaMarker, 0.0);
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
	
		// get size of complete explanation set
		int totalExplSetSize = 0;
		totalExplSetSize = set.getSize();

		// update counter for error types of each partition of explanation set
		for (ISchemaMarker tmpSchemaMarker : mPartialSchemaExplSetMap.keySet())
		{
			resetErrorCounter(mErrTypeCounterMap); 
			
			int mCurrentExplSetSize = mPartialSchemaExplSetMap.get(tmpSchemaMarker).size();
			
			// update error type counter
		    for (IBasicExplanation expl : mPartialSchemaExplSetMap.get(tmpSchemaMarker))
		    {
		    	mErrTypeCounterMap.put(expl.getType(), mErrTypeCounterMap.get(expl.getType())+1);
		    }
		    // step 2.
		    // compute partial entropy score
			double explScore = getPartialEntropy(mErrTypeCounterMap, mCurrentExplSetSize);
			mPartialSchemaEntropyScoreMap.put(tmpSchemaMarker, explScore);
		}
		
		// step3. sum partial entropy score by weighting partial scores by their explanation set size
		for (ISchemaMarker tmpSchemaMarker : mPartialSchemaEntropyScoreMap.keySet())
		{
			double tmpPartialEntropy = mPartialSchemaEntropyScoreMap.get(tmpSchemaMarker);
			double tmpWeight         = mPartialSchemaExplSetMap.get(tmpSchemaMarker).size() / totalExplSetSize;
			double WeightedEntropy   = tmpPartialEntropy * tmpWeight;
			retScore = retScore + WeightedEntropy;
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
		
		// define datatypes for this scoring function
		Map<ISchemaMarker, IExplanationSet> mPartialSchemaExplSetMap;
		mPartialSchemaExplSetMap = new HashMap<ISchemaMarker, IExplanationSet> ();
		
		Map<ISchemaMarker, Double>mPartialSchemaEntropyScoreMap;
		mPartialSchemaEntropyScoreMap = new HashMap<ISchemaMarker, Double> ();
				
		Map<ISchemaMarker, IMarkerSet> mPartialSchemaMarkerSetMap;
		mPartialSchemaMarkerSetMap = new HashMap<ISchemaMarker, IMarkerSet> ();
		
		
		// step1. partitioning explanation set according to target schemaMarker (relationID + attributeID)
		for ( IBasicExplanation tmpBasicExpl: expls)
		{
			
			for (ISingleMarker tmpSingleMarker : tmpBasicExpl.getRealExplains())
			{
				Collection<ISchemaMarker> tmpSchemaMarkerColl = MarkerFactory.newSchemaMarker(tmpSingleMarker);
				
				for (ISchemaMarker tmpSchemaMarker : tmpSchemaMarkerColl)
				{
					// update mapping of schemaMarker -> partial explanation set
					if (mPartialSchemaExplSetMap.containsKey(tmpSchemaMarker))
						mPartialSchemaExplSetMap.get(tmpSchemaMarker).add(tmpBasicExpl);
					else
						mPartialSchemaExplSetMap.put(tmpSchemaMarker, 
								ExplanationFactory.newExplanationSet(tmpBasicExpl));
					
					// update mapping of schemaMarker -> partial marker set
					if (mPartialSchemaMarkerSetMap.containsKey(tmpSchemaMarker))
						mPartialSchemaMarkerSetMap.get(tmpSchemaMarker).add(tmpSingleMarker);
					else
						mPartialSchemaMarkerSetMap.put(tmpSchemaMarker, MarkerFactory.newMarkerSet(tmpSingleMarker));
				}
			}
		}
		
		//initialize entropy map
		for (ISchemaMarker tmpSchemaMarker : mPartialSchemaExplSetMap.keySet())
		{
			mPartialSchemaEntropyScoreMap.put(tmpSchemaMarker, 0.0);
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
	
		// get size of complete explanation set
		int totalExplSetSize = 0;
		for (IBasicExplanation expl : expls)
		{
			totalExplSetSize = totalExplSetSize + expl.getRealExplains().getSize();
		}

		// update counter for error types of each partition of explanation set
		for (ISchemaMarker tmpSchemaMarker : mPartialSchemaExplSetMap.keySet())
		{
			resetErrorCounter(mErrTypeCounterMap); 
			
			int mCurrentExplSetSize = mPartialSchemaExplSetMap.get(tmpSchemaMarker).size();
			
			// update error type counter
		    for (IBasicExplanation expl : mPartialSchemaExplSetMap.get(tmpSchemaMarker))
		    {
		    	mErrTypeCounterMap.put(expl.getType(), mErrTypeCounterMap.get(expl.getType())+1);
		    }
		    // step 2.
		    // compute partial entropy score
			double explScore = getPartialEntropy(mErrTypeCounterMap, mCurrentExplSetSize);
			mPartialSchemaEntropyScoreMap.put(tmpSchemaMarker, explScore);
		}
		
		// step3. sum partial entropy score by weighting partial scores by their explanation set size
		for (ISchemaMarker tmpSchemaMarker : mPartialSchemaEntropyScoreMap.keySet())
		{
			double tmpPartialEntropy = mPartialSchemaEntropyScoreMap.get(tmpSchemaMarker);
			double tmpWeight         = mPartialSchemaExplSetMap.get(tmpSchemaMarker).size() / totalExplSetSize;
			double WeightedEntropy   = tmpPartialEntropy * tmpWeight;
			retScore = retScore + WeightedEntropy;
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