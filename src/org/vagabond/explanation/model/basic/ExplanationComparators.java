package org.vagabond.explanation.model.basic;

import java.util.Comparator;

import org.vagabond.explanation.model.IExplanationSet;

public interface ExplanationComparators {

	public class BasicExplanationSameElementComparator 
			implements Comparator<IBasicExplanation> {
		@Override
		public int compare(IBasicExplanation o1, IBasicExplanation o2) {
			if (o1.getExplanation().equals(o2.getExplanation()))
				return 0;
			return -1;
		}
	}
	
	public class BasicExplanationFullSideEffectComparator implements
			Comparator<IBasicExplanation> {
		@Override
		public int compare(IBasicExplanation o1, IBasicExplanation o2) {
			if (o1.getRealTargetSideEffectSize() != o2.getRealTargetSideEffectSize())
				return o1.getRealTargetSideEffectSize() - o2.getRealTargetSideEffectSize();
			if (o1.getSourceSideEffectSize() != o2.getSourceSideEffectSize())
				return o1.getSourceSideEffectSize() - o2.getSourceSideEffectSize();
			if (o1.getMappingSideEffectSize() != o2.getMappingSideEffectSize())
				return o1.getMappingSideEffectSize() - o2.getMappingSideEffectSize();
			if (o1.getCorrSideEffectSize() != o2.getCorrSideEffectSize())
				return o1.getCorrSideEffectSize() - o2.getCorrSideEffectSize();
			if (o1.getTransformationSideEffectSize() != o2.getTransformationSideEffectSize())
				return o1.getTransformationSideEffectSize() - o2.getTransformationSideEffectSize();
			return o2.getRealExplains().size() - o1.getRealExplains().size();
		}
	}
	
	public class BasicExplanationSideEffectComparator implements
			Comparator<IBasicExplanation> {
		@Override
		public int compare(IBasicExplanation o1, IBasicExplanation o2) {
			return o1.getTargetSideEffectSize() - o2.getTargetSideEffectSize();
		}
	}

	public class BasicExplanationRealSideEffectComparator implements
			Comparator<IBasicExplanation> {
		@Override
		public int compare(IBasicExplanation o1, IBasicExplanation o2) {
			return o1.getRealTargetSideEffectSize() - o2.getRealTargetSideEffectSize();
		}
	}
	
	public class ExplSetSameElementComp implements
			Comparator<IExplanationSet> {

		@Override
		public int compare(IExplanationSet o1, IExplanationSet o2) {
			if (o1.size() != o2.size())
				return -1;
			
			for(IBasicExplanation o1Expl: o1.getExplanations()) {
				boolean found = false;
				for(IBasicExplanation o2Expl: o2.getExplanations()) {
					if (sameElemComp.compare(o1Expl, o2Expl) == 0) {
						found = true;
						break;
					}
				}
				
				// sets not the same
				if (!found)
					return -1;
			}
			return 0;
		}
	}

	public static final BasicExplanationFullSideEffectComparator fullSideEffComp 
			= new BasicExplanationFullSideEffectComparator();
	public static final BasicExplanationSideEffectComparator sideEffComp 
			= new BasicExplanationSideEffectComparator();
	public static final BasicExplanationRealSideEffectComparator realEffComp
			= new BasicExplanationRealSideEffectComparator();
	public static final BasicExplanationSameElementComparator sameElemComp
			= new BasicExplanationSameElementComparator ();
	
	public static final ExplSetSameElementComp setSameElemComp
			= new ExplSetSameElementComp();
}
