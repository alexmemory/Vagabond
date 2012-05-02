package org.vagabond.explanation.ranking.scoring;

import java.util.Comparator;

import org.vagabond.explanation.model.basic.ExplanationComparators;
import org.vagabond.explanation.model.basic.IBasicExplanation;
import org.vagabond.explanation.ranking.RankerFactory;

public class ScoreBasedTotalOrderComparator implements Comparator<IBasicExplanation> {

	private Comparator<IBasicExplanation> comp;
	
	public static Comparator<IBasicExplanation> getComp (IScoringFunction f) {
		return new ScoreBasedTotalOrderComparator(f);
	}
	
	public ScoreBasedTotalOrderComparator (IScoringFunction f) {
		comp = RankerFactory.getScoreBasicComparator(f);
	}
	
	@Override
	public int compare(IBasicExplanation o1, IBasicExplanation o2) {
		int res = comp.compare(o1, o2);
		if (res != 0)
			return res;
		
		return ExplanationComparators.fullSideEffWithTie.compare(o1, o2);
	}

}