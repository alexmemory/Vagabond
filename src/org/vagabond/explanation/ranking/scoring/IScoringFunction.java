package org.vagabond.explanation.ranking.scoring;

import java.util.Collection;

import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.IBasicExplanation;

public interface IScoringFunction {

	public enum Monotonicity {
		strictMonotone,
		unionMonotone,
		notMonotone
	}
	
	public int getScore(IBasicExplanation expl);
	public int getScore(IExplanationSet set);
	public int getScore(Collection<IBasicExplanation> expls);
	public Monotonicity getMonotonicityProperty ();
	
}
