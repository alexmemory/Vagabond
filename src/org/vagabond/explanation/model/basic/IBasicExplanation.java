package org.vagabond.explanation.model.basic;

import java.util.Collection;

import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.xmlmodel.CorrespondenceType;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.TransformationType;

public interface IBasicExplanation extends Comparable<IBasicExplanation> {

	enum ExplanationType {
		CopySourceError,
		InfluenceSourceError,
		CorrespondenceError,
		SuperflousMappingError,
		SourceSkeletonMappingError,
		TargetSkeletonMappingError
	}
	
	public ExplanationType getType ();
	
	public ISingleMarker explains();
	public void setExplains (ISingleMarker explains);
	public Object getExplanation();
	
	public int getTargetSideEffectSize();
	public IMarkerSet getTargetSideEffects ();
	
	public int getRealTargetSideEffectSize();
	public IMarkerSet getRealTargetSideEffects();
	public void setRealTargetSideEffects(IMarkerSet set);
	
	public IMarkerSet getRealExplains();
	public void setRealExplains(IMarkerSet set);
	
	public void computeRealTargetSEAndExplains (IMarkerSet errors);
	
	public int getSourceSideEffectSize ();
	public IMarkerSet getSourceSideEffects ();
	
	public int getMappingSideEffectSize ();
	public Collection<MappingType> getMappingSideEffects ();
	
	public int getCorrSideEffectSize ();
	public Collection<CorrespondenceType> getCorrespondenceSideEffects ();
	
	public int getTransformationSideEffectSize ();
	public Collection<TransformationType> getTransformationSideEffects ();
	
	public void recomputeHash();
}
