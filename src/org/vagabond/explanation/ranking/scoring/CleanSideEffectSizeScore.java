package org.vagabond.explanation.ranking.scoring;

import java.util.Collection;
import java.util.Map;

import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.IBasicExplanation;

public class CleanSideEffectSizeScore implements IScoringFunction {
	
	
	public static final IScoringFunction inst = new CleanSideEffectSizeScore();
	
	@Override
	public int getScore(IBasicExplanation expl) {
		return expl.getRealTargetSideEffectSize();
	}

	@Override
	public int getScore(IExplanationSet set) {
		return set.getSideEffectSize();
	}

	@Override
	public Monotonicity getMonotonicityProperty() {
		return Monotonicity.unionMonotone;
	}

	@Override
	public int getScore(Collection<IBasicExplanation> expls) {
		IMarkerSet sideEff = MarkerFactory.newMarkerSet();
		
		for(IBasicExplanation expl: expls)
			sideEff.union(expl.getRealTargetSideEffects());
		
		return sideEff.getSize();
	}



}
