package org.vagabond.explanation.model.basic;



import java.util.Comparator;

public class BasicExplanationSideEffectComparator implements
		Comparator<IBasicExplanation> {

	public static BasicExplanationSideEffectComparator comp 
			= new BasicExplanationSideEffectComparator();
	
	@Override
	public int compare(IBasicExplanation o1, IBasicExplanation o2) {
		return o1.getTargetSideEffectSize() - o2.getTargetSideEffectSize();
	}

}
