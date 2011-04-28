package org.vagabond.explanation.generation.prov;

import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.IMarkerSet;

public interface ISideEffectGenerator {

	public IMarkerSet computeTargetSideEffects (IMarkerSet sourceSE, 
			IAttributeValueMarker error) throws Exception;
	
}
