package org.vagabond.explanation.ranking.scoring;

import java.util.Collection;

import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.IBasicExplanation;

public class ExplanationSizeScore implements IScoringFunction {

	public static final ExplanationSizeScore inst = new ExplanationSizeScore();
	
	@Override
	public int getFTypeCode()
	{
		return Explanation_Size_Score;
	}
	
	@Override
	public int getScore(IBasicExplanation expl) {
		return 1;
	}

	@Override
	public int getScore(IExplanationSet set) {
		return set.getSize();
	}

	@Override
	public Monotonicity getMonotonicityProperty() {
		return Monotonicity.unionMonotone;
	}

	@Override
	public int getScore(Collection<IBasicExplanation> expls) {
		return expls.size();
	}

	

}
