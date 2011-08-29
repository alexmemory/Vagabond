package org.vagabond.explanation.ranking;

import java.util.Comparator;

import org.vagabond.explanation.model.IExplanationSet;

public class ExplanationSetComparator implements Comparator<IExplanationSet> {
	@Override
	public int compare(IExplanationSet o1, IExplanationSet o2) {
		return o1.getSideEffectSize() - 
				o2.getSideEffectSize();
	}

}
