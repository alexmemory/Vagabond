package org.vagabond.explanation.model.basic;

import static org.vagabond.util.HashFNV.fnv;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.util.LogProviderHolder;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.TransformationType;

public class SourceSkeletonMappingError extends AbstractBasicExplanation 
		implements IBasicExplanation {

	static Logger log = LogProviderHolder.getInstance().getLogger(SourceSkeletonMappingError.class);
	
	private Set<MappingType> mapSE;
	private Set<TransformationType> transSE;
	
	public SourceSkeletonMappingError () {
		super();
		setUp();
	}
	
	public SourceSkeletonMappingError (IAttributeValueMarker marker) {
		super(marker);
		setUp();
	}
	
	public SourceSkeletonMappingError (IAttributeValueMarker marker, 
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
		return ExplanationType.SourceSkeletonMappingError;
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

	@Override
	public void setMapSE(Set<MappingType> maps) {
		this.mapSE = maps;
		updateHash();
	}

	public void addMap(MappingType map) {
		mapSE.add(map);
		updateHash();
	}

	@Override
	public Collection<TransformationType> getTransformationSideEffects () {
		return transSE;
	}
	
	@Override
	public int getTransformationSideEffectSize () {
		return transSE.size();
	}
	
	@Override
	public void setTransSE (Collection<TransformationType> transSE) {
		this.transSE = new HashSet<TransformationType> (transSE);
		updateHash();
	}
	
	@Override
	protected void computeHash () {
		super.computeHash();
		hash = fnv(mapSE, hash);
		hash = fnv(transSE, hash);
	}
}
