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
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.IBasicExplanation;
import org.vagabond.explanation.model.basic.IBasicExplanation.ExplanationType;
import org.vagabond.explanation.ranking.DummyRanker;
import org.vagabond.explanation.ranking.scoring.IScoringFunction.Monotonicity;

/**
 * @author lord_pretzel
 *
 */
public class ErrorTypeScore implements IScoringFunction {	
	public static final IScoringFunction inst = new ErrorTypeScore();
	

	public double getErrorWeight(ExplanationType errortype){
		double retV = 0;
		switch (errortype){
		case CopySourceError:
			 retV = 0.1;
		     break;
		case InfluenceSourceError:
			 retV = 0.1;
	         break;
		case CorrespondenceError:
			 retV = 0.1;
	         break;
		case SuperflousMappingError:
			 retV = 0.1;
		     break;
		case SourceSkeletonMappingError:
			 retV = 0.1;
	         break;
		case TargetSkeletonMappingError:
			 retV = 0.1;
		     break;
		default: 
			retV = 0;   
		}
		return retV;
	}
	
	@Override
	public int getScore(IBasicExplanation expl) {
		ExplanationType errType = expl.getType();
		double typeWeight = this.getErrorWeight(errType);
		int sizeScore  = expl.getSourceSideEffectSize();
		return (int) (typeWeight * sizeScore);
	}

	@Override
	public int getScore(IExplanationSet set) {
		int retScore = 0;
		List<IBasicExplanation> listExpl = set.getExplanations();
		Iterator<IBasicExplanation> iter = listExpl.iterator();
		
		while (iter.hasNext()){
			retScore += iter.next().getSourceSideEffectSize()  * this.getErrorWeight(iter.next().getType());
		}
		return retScore;
	}

	@Override
	public Monotonicity getMonotonicityProperty() {
		return Monotonicity.unionMonotone;
	}

	@Override
	public int getScore(Collection<IBasicExplanation> expls) {
		
		int retScore = 0;
		
		for(IBasicExplanation expl: expls)
			retScore += expl.getSourceSideEffectSize()  * this.getErrorWeight(expl.getType());
		
		return retScore;
	}





}