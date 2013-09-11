package org.vagabond.explanation.ranking.scoring;

import java.util.Collection;

import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.IBasicExplanation;


public class SideEffectSizeScoreWithWeightedTypeError extends SideEffectSizeScore implements IScoringFunction{

	public static final IScoringFunction inst = new SideEffectSizeScoreWithWeightedTypeError();
	
	enum ExplanationType{
		CopySourceError,
		InfluenceSourceError,
		CorrespondenceError,
		SuperflousMappingError,
		SourceSkeletonMappingError,
		TargetSkeletonMappingError
	}
	
	int GetErrorTypeWeight(org.vagabond.explanation.model.basic.IBasicExplanation.ExplanationType explanationType)
	{
		int retV = 0;
		switch (explanationType)
		{
		case CopySourceError:
			retV = 1;
			break;
		case InfluenceSourceError:
			retV = 1;
			break;
		case CorrespondenceError:
			retV = 1;
			break;
		case SuperflousMappingError:
			retV = 1;
			break;
		case SourceSkeletonMappingError:
			retV = 1;
			break;
		case TargetSkeletonMappingError:
			retV = 1;
			break;
		}
		return retV;
	}
	
	@Override
	public int getScore(IBasicExplanation expl) {
		return GetErrorTypeWeight(expl.getType()) * expl.getRealTargetSideEffectSize();
		//return expl.getRealTargetSideEffectSize();
	}

	@Override
	public int getScore(IExplanationSet set) {
		return set.getSideEffectSize();
	}

	@Override
	public Monotonicity getMonotonicityProperty() {
		return Monotonicity.unionMonotone;
	}

	@Override
	public int getScore(Collection<IBasicExplanation> expls) {
		int collectionScore = 0;
		
		IMarkerSet sideEff = MarkerFactory.newMarkerSet();
		
		for(IBasicExplanation expl: expls)
		{	
			sideEff.union(expl.getRealTargetSideEffects());
			collectionScore = collectionScore + GetErrorTypeWeight(expl.getType()) * expl.getRealTargetSideEffectSize();
		    }
		
		return collectionScore;
		//return sideEff.getSize();
	}


	
}
