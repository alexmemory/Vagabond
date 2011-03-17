package org.vagabond.explanation.model.basic;

import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ISingleMarker;

public interface IBasicExplanation {

	enum ExplanationType {
		CopySourceError,
		InfluenceSourceError,
		CorrespondenceError,
		SuperflousMappingError,
		SourceSkeletonMappingError,
		TargetSkeletonMappingError
	}
	
	public int getSideEffectSize ();
	public IMarkerSet getSideEffects ();
	public ExplanationType getType ();
	public ISingleMarker explains();
	public Object getExplanation();
}
