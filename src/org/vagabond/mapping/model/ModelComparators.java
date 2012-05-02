package org.vagabond.mapping.model;

import java.util.Comparator;

import org.vagabond.xmlmodel.CorrespondenceType;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.TransformationType;

public interface ModelComparators {

	public static final Comparator<MappingType> mappingIdComp = new Comparator<MappingType> () {

		@Override
		public int compare(MappingType o1, MappingType o2) {
			return o1.getId().compareTo(o2.getId());
		}
		
	};
	
	public static final Comparator<CorrespondenceType> corrIdComp = new Comparator<CorrespondenceType> () {

		@Override
		public int compare(CorrespondenceType o1, CorrespondenceType o2) {
			return o1.getId().compareTo(o2.getId());
		}
		
	};

	public static final Comparator<TransformationType> transIdComp = new Comparator<TransformationType> () {

		@Override
		public int compare(TransformationType o1, TransformationType o2) {
			return o1.getId().compareTo(o2.getId());
		}
		
	};

	
}
