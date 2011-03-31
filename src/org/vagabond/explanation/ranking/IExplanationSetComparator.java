package org.vagabond.explanation.ranking;

import java.util.Comparator;
import org.vagabond.explanation.model.IExplanationSet;

public class IExplanationSetComparator implements Comparator<IExplanationSet> {
	@Override
	public int compare(IExplanationSet o1, IExplanationSet o2) {
		return ((IExplanationSet)o1).getSideEffectSize() - 
				((IExplanationSet)o2).getSideEffectSize();
	}

}
