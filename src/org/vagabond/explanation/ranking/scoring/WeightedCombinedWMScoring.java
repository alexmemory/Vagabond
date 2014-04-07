/**
 * 
 */
package org.vagabond.explanation.ranking.scoring;

import java.util.Collection;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.IBasicExplanation;

/**
 * @author lord_pretzel
 *
 */
public class WeightedCombinedWMScoring implements IScoringFunction {	
	//public static final IScoringFunction inst = new WeightedCombinedWMScoring();
	public IScoringFunction[] funcnames;
	public double[] funcweights;
	public WeightedCombinedWMScoring (IScoringFunction[] f, double[] weights) {
	this.funcnames = f;
	this.funcweights = weights;
	
}
	/* (non-Javadoc)
	* @see org.vagabond.explanation.ranking.scoring.IScoringFunction#getScore(org.vagabond.explanation.model.basic.IBasicExplanation)
	*/
	
	@Override
	public int getFTypeCode()
	{
		return Weighted_Combined_WMScoring;
	}
	
	@Override
	public int getScore(IBasicExplanation expl) {
		double score = 0.0;
		for (int i = 0; i < funcnames.length; i++)
		{	
			score += funcweights[i] * funcnames[i].getScore(expl);
		}
		return (int) (score * 10000);
	}

	/* (non-Javadoc)
	* @see org.vagabond.explanation.ranking.scoring.IScoringFunction#getScore(org.vagabond.explanation.model.IExplanationSet)
	*/
	@Override
	public int getScore(IExplanationSet set) {
		double score = 0.0;
		for (int i = 0; i < funcnames.length; i++)
		{	
			score += funcweights[i] * funcnames[i].getScore(set);
		}
		return (int) (score  * 10000);
	}

	/* (non-Javadoc)
	* @see org.vagabond.explanation.ranking.scoring.IScoringFunction#getScore(java.util.Collection)
	*/
	@Override
	public int getScore(Collection<IBasicExplanation> expls) {
		double score = 0.0;
		for (int i = 0; i < funcnames.length; i++)
		{	
			score += funcweights[i] * funcnames[i].getScore(expls);
		}
		return (int) (score * 10000);
	
	}

	/* (non-Javadoc)
	* @see org.vagabond.explanation.ranking.scoring.IScoringFunction#getMonotonicityProperty()
	*/
	@Override
	public Monotonicity getMonotonicityProperty() {
	    return Monotonicity.unionMonotone;
	}

}