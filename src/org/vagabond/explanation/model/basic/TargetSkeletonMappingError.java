package org.vagabond.explanation.model.basic;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger; import org.vagabond.util.LogProviderHolder;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.util.LoggerUtil;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.TransformationType;

public class TargetSkeletonMappingError extends AbstractBasicExplanation 
		implements IBasicExplanation {

	static Logger log = LogProviderHolder.getInstance().getLogger(TargetSkeletonMappingError.class);
	
	private Set<MappingType> mapSE;
	private Set<TransformationType> transSE;
	
	public TargetSkeletonMappingError () {
		super();
		setUp();
	}
	
	public TargetSkeletonMappingError (IAttributeValueMarker marker) {
		super(marker);
		setUp();
	}
	
	public TargetSkeletonMappingError (IAttributeValueMarker marker, 
			Set<MappingType> maps) {
		super(marker);
		setUp();
		this.mapSE = maps;
	}
	
	private void setUp () {
		mapSE = new HashSet<MappingType> ();
		transSE = new HashSet<TransformationType> ();
	}
	

	@Override
	public ExplanationType getType() {
		return ExplanationType.TargetSkeletonMappingError;
	}

	@Override
	public Object getExplanation() {
		return mapSE;
	}

	@Override
	public int getMappingSideEffectSize() {
		return mapSE.size();
	}
	
	@Override
	public Set<MappingType> getMappingSideEffects() {
		return mapSE;
	}

	public void setMap(Set<MappingType> maps) {
		this.mapSE = maps;
	}

	public void addMap(MappingType map) {
		mapSE.add(map);
	}

	@Override
	public Collection<TransformationType> getTransformationSideEffects () {
		return transSE;
	}
	
	@Override
	public int getTransformationSideEffectSize () {
		return transSE.size();
	}
	
	public void setTransSE (Collection<TransformationType> transSE) {
		this.transSE = new HashSet<TransformationType> (transSE);
	}
	
}
