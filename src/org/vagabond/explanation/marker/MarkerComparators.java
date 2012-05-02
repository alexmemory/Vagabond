package org.vagabond.explanation.marker;

import java.util.Comparator;

import org.vagabond.explanation.model.basic.IBasicExplanation;

public interface MarkerComparators {

	public static final Comparator<ISingleMarker> singleMarkerComp = new Comparator<ISingleMarker>() {

		@Override
		public int compare(ISingleMarker o1, ISingleMarker o2) {
			int comp;
			
			comp = o1.getRelId() - o2.getRelId(); 
			if (comp != 0)
				return comp;
			
			comp = o1.getTid().compareTo(o2.getTid());
			if (comp != 0)
				return comp;
			
			if (o1 instanceof IAttributeValueMarker) {
				if (o2 instanceof IAttributeValueMarker) {
					IAttributeValueMarker a1, a2;
					a1 = (IAttributeValueMarker) o1;
					a2 = (IAttributeValueMarker) o2;
					
					return a1.getAttrId() - a2.getAttrId();
				}
				return -1;
			}
			if (o2 instanceof IAttributeValueMarker) {
				return 1;
			}
			
			return 0;
		}
		
	};
	
}
