package org.vagabond.explanation.ranking.scoring;

import java.util.Comparator;

import org.vagabond.explanation.model.basic.IBasicExplanation;

public class ScoreBasicComparator implements Comparator<IBasicExplanation> {

	private IScoringFunction f;
	
	public ScoreBasicComparator (IScoringFunction f) {
		this.f = f;
	}
	
	@Override
	public int compare(IBasicExplanation l, IBasicExplanation r) {
		int scoreL, scoreR;
		
		scoreL = f.getScore(l);
		scoreR = f.getScore(r);
		if (scoreL < scoreR)
			return -1;
		if (scoreL > scoreR)
			return 1;
		
		return 0;
	}

}
