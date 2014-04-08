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

	public static int Explanation_Size_Score = 1;
	public static int SideEffect_Size_Score = 2;
	public static int Weighted_Combined_WMScoring = 3;
	public static int Entropy_Score = 4;
	public static int Avg_ErrTypeWeight_Score = 5;

	public int getScore(IBasicExplanation expl);
	public int getScore(IExplanationSet set);
	public int getScore(Collection<IBasicExplanation> expls);
	public Monotonicity getMonotonicityProperty ();
	public int getFTypeCode();
	
	public enum ScoreFuncTypeCode {
		Explanation_Size_Score,
		SideEffect_Size_Score,
		Weighted_Combined_WMScoring,
		Entropy_Score,
		Avg_ErrTypeWeight_Score
	}


	
}
