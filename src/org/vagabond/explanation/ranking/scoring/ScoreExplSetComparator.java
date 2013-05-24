package org.vagabond.explanation.ranking.scoring;

import java.util.Comparator;

import org.vagabond.explanation.model.IExplanationSet;

public class ScoreExplSetComparator implements Comparator<IExplanationSet> {

	private IScoringFunction f;
	
	public ScoreExplSetComparator (IScoringFunction f) {
		this.f = f;
	}
	
	@Override
	public int compare(IExplanationSet l, IExplanationSet r) {
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
