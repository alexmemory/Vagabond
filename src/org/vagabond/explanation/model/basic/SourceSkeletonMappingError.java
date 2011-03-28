package org.vagabond.explanation.model.basic;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.util.LoggerUtil;
import org.vagabond.xmlmodel.MappingType;

public class SourceSkeletonMappingError extends AbstractBasicExplanation 
		implements IBasicExplanation {

	static Logger log = Logger.getLogger(SourceSkeletonMappingError.class);
	
	private Set<MappingType> maps;
	
	public SourceSkeletonMappingError () {
		super();
		maps = new HashSet<MappingType> ();
	}
	
	public SourceSkeletonMappingError (IAttributeValueMarker marker) {
		super(marker);
		maps = new HashSet<MappingType> ();
	}
	
	public SourceSkeletonMappingError (IAttributeValueMarker marker, 
			Set<MappingType> maps) {
		super(marker);
		this.maps = maps;
	}

	@Override
	public ExplanationType getType() {
		return ExplanationType.SourceSkeletonMappingError;
	}

	@Override
	public Object getExplanation() {
		return maps;
	}

	@Override
	public Set<MappingType> getMappingSideEffects() {
		return maps;
	}

	public void setMap(Set<MappingType> maps) {
		this.maps = maps;
	}

	public void addMap(MappingType map) {
		maps.add(map);
	}
	
}
