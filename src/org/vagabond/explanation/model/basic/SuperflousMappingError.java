package org.vagabond.explanation.model.basic;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.util.LogProviderHolder;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.TransformationType;

public class SuperflousMappingError extends AbstractBasicExplanation 
		implements IBasicExplanation {

	static Logger log = LogProviderHolder.getInstance().getLogger(SuperflousMappingError.class);
	
	private Set<MappingType> mapSE;
	private Set<TransformationType> transSE;
	
	public SuperflousMappingError () {
		super();
		setUp();
	}
	
	public SuperflousMappingError (IAttributeValueMarker marker) {
		super(marker);
		setUp();
	}
	
	public SuperflousMappingError (IAttributeValueMarker marker, 
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
		return ExplanationType.SuperflousMappingError;
	}

	@Override
	public Object getExplanation() {
		return mapSE;
	}

	@Override
	public Set<MappingType> getMappingSideEffects() {
		return mapSE;
	}

	@Override
	public int getMappingSideEffectSize () {
		return mapSE.size();
	}

	public void addMapSE(MappingType map) {
		mapSE.add(map);
	}
	
	public void setMapSE(Set<MappingType> maps) {
		this.mapSE = maps;
	}

	public void setTargetSE(IMarkerSet targetSE) {
		this.targetSE = targetSE;
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
