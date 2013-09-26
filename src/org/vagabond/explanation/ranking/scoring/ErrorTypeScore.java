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
	public double[] errweights;
	public ErrorTypeScore (double[] weights) {
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
		ExplanationType errType = expl.getType();
		double typeWeight = this.getErrorWeight(errType);
		return (int) (typeWeight * 10000);
	}

	@Override
	public int getScore(IExplanationSet set) {
		double retScore = 0;
		List<IBasicExplanation> listExpl = set.getExplanations();
		Iterator<IBasicExplanation> iter = listExpl.iterator();
		
		while (iter.hasNext()){
			retScore += this.getErrorWeight(iter.next().getType());
		}
		retScore /= set.getSize();
		
		return (int) (retScore  * 10000 ) ;
	}

	@Override
	public Monotonicity getMonotonicityProperty() {
		return Monotonicity.unionMonotone;
	}

	@Override
	public int getScore(Collection<IBasicExplanation> expls) {
		// TODO:
		// remove duplicate elements in collection
		// copy input then modify, compute
		double retScore = 0;
		
		for(IBasicExplanation expl: expls)
			retScore += this.getErrorWeight(expl.getType());
		
		retScore /= expls.size();
		return (int) (retScore  * 10000);
	}

}