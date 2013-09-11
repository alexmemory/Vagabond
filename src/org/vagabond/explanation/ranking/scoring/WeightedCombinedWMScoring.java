/**
 * 
 */
package org.vagabond.explanation.ranking.scoring;

import java.util.Collection;
import java.util.Iterator;

import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.IBasicExplanation;

/**
 * @author lord_pretzel
 *
 */
public class WeightedCombinedWMScoring implements IScoringFunction {
	public static IScoringFunction[] funcnames;
	public static double[] funcweights;
	public WeightedCombinedWMScoring (IScoringFunction[] f, double[] weights) {
	this.funcnames = funcnames;
	this.funcweights = funcweights;
}
	/* (non-Javadoc)
	* @see org.vagabond.explanation.ranking.scoring.IScoringFunction#getScore(org.vagabond.explanation.model.basic.IBasicExplanation)
	*/
	@Override
	public int getScore(IBasicExplanation expl) {
	double score = 0.0;
	for (int i = 0; i < funcnames.length; i++)
	score += funcweights[i] * funcnames[i].getScore(expl);
	return (int) score;
	}

	/* (non-Javadoc)
	* @see org.vagabond.explanation.ranking.scoring.IScoringFunction#getScore(org.vagabond.explanation.model.IExplanationSet)
	*/
	@Override
	public int getScore(IExplanationSet set) {
		double score = 0.0;
		Iterator<IBasicExplanation> iter  = set.iterator();
		while (iter.hasNext()){
			IBasicExplanation expl = iter.next();
			for (int i = 0; i < funcnames.length; i++)
			score += funcweights[i] * funcnames[i].getScore(expl);
		}
		return (int) score;
	}

	/* (non-Javadoc)
	* @see org.vagabond.explanation.ranking.scoring.IScoringFunction#getScore(java.util.Collection)
	*/
	@Override
	public int getScore(Collection<IBasicExplanation> expls) {
		double score = 0.0;
		Iterator<IBasicExplanation> iter  = expls.iterator();
		while (iter.hasNext()){
			IBasicExplanation expl = iter.next();
			for (int i = 0; i < funcnames.length; i++)
			score += funcweights[i] * funcnames[i].getScore(expl);
		}
		return (int) score;
	
	}

	/* (non-Javadoc)
	* @see org.vagabond.explanation.ranking.scoring.IScoringFunction#getMonotonicityProperty()
	*/
	@Override
	public Monotonicity getMonotonicityProperty() {
	    return Monotonicity.unionMonotone;
	}

}