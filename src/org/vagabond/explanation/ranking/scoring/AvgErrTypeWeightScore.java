/**
 * 
 */
package org.vagabond.explanation.ranking.scoring;

import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.IBasicExplanation;
import org.vagabond.explanation.model.basic.IBasicExplanation.ExplanationType;
import org.vagabond.explanation.ranking.DummyRanker;
import org.vagabond.explanation.ranking.scoring.IScoringFunction.Monotonicity;

/**
 * @author Zhen
 *
 */
public class AvgErrTypeWeightScore implements IScoringFunction {
	public double[] errweights;
	public AvgErrTypeWeightScore (double[] weights) {
	this.errweights = weights;
}
	
	public double getErrorWeight(ExplanationType errortype){
		double retV = 0;
		switch (errortype){
		case CopySourceError:
			 retV = this.errweights[0];
		     break;
		case InfluenceSourceError:
			 retV = this.errweights[1];
	         break;
		case CorrespondenceError:
			 retV = this.errweights[2];
	         break;
		case SuperflousMappingError:
			 retV = this.errweights[3];
		     break;
		case SourceSkeletonMappingError:
			 retV = this.errweights[4];
	         break;
		case TargetSkeletonMappingError:
			 retV = this.errweights[5];
		     break;
		default: 
			retV = 1;   
		}
		return retV;
	}
	
	@Override
	public int getScore(IBasicExplanation expl) {
		/*for single explanation, there is only one error type,
		 * simply return the weight of the error type, no extra calculation
		 */
		double retScore = 0.0;
		ExplanationType errType = expl.getType();
		retScore = this.getErrorWeight(errType) * expl.getTargetSideEffectSize();
		return (int) (retScore * 10000);

	}

	@Override
	public int getScore(IExplanationSet set) {
		
		/*initialize variables*/
		double retScore = 0.0;
		
		Map<ISingleMarker, IExplanationSet> mSingleErrorMap;
		mSingleErrorMap = new HashMap<ISingleMarker, IExplanationSet> ();

		List<IBasicExplanation> listExpl = set.getExplanations();
		Iterator<IBasicExplanation> iter = listExpl.iterator();
		
		/*initialize map*/
		while (iter.hasNext()){
			IBasicExplanation mExpl = iter.next();
			for (ISingleMarker singleErr : mExpl.getRealExplains()){
				if (mSingleErrorMap.containsKey(singleErr))
				{
					mSingleErrorMap.get(singleErr).addExplanation(mExpl);
				}
				else
				{
					IExplanationSet mNewExplanationSet = null;
					mNewExplanationSet.add(mExpl);
				    mSingleErrorMap.put(singleErr, mNewExplanationSet);
				}
			}
			
		}

		/*loop through map to compute average weight*/
		double mAvgWeight;
		Iterator<ISingleMarker> iterSingleErr = mSingleErrorMap.keySet().iterator();
		while (iterSingleErr.hasNext()){
			ISingleMarker m = iterSingleErr.next();
			mAvgWeight = 0;
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
					IExplanationSet mNewExplanationSet = null;
					mNewExplanationSet.add(expl);
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