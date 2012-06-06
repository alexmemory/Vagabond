package org.vagabond.explanation.model.basic;

import static org.vagabond.util.HashFNV.fnv;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.util.LogProviderHolder;
import org.vagabond.xmlmodel.CorrespondenceType;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.TransformationType;

public class CorrespondenceError extends AbstractBasicExplanation 
		implements IBasicExplanation {

	static Logger log = LogProviderHolder.getInstance().getLogger(CorrespondenceError.class);
	
	private Set<CorrespondenceType> correspondences;
	private Set<MappingType> mapSE;
	private Set<TransformationType> transSE;

	public CorrespondenceError () {
		super();
		setUp();
	}

	public CorrespondenceError (ISingleMarker marker) {
		super(marker);
		setUp();
		error = (IAttributeValueMarker) marker;
	}
	
	private void setUp() {
		mapSE = new HashSet<MappingType> ();
		transSE = new HashSet<TransformationType> ();
		correspondences = new HashSet<CorrespondenceType> ();
	}
	
	@Override
	public ExplanationType getType() {
		return ExplanationType.CorrespondenceError;
	}

	@Override
	public Object getExplanation() {
		return correspondences;
	}

	@Override
	public Collection<CorrespondenceType> getCorrespondenceSideEffects() {
		return correspondences;
	}

	@Override
	public int getCorrSideEffectSize() {
		return correspondences.size();
	}
	
	@Override
	public void setCorrSE (Set<CorrespondenceType> correspondence) {
		this.correspondences = correspondence;
		updateHash();
	}
	
	public void addCorrespondence (CorrespondenceType corr) {
		this.correspondences.add(corr);
		updateHash();
	}

	@Override
	public int getMappingSideEffectSize () {
		return mapSE.size();
	}
	
	@Override
	public Collection<MappingType> getMappingSideEffects () {
		return mapSE;
	}

	@Override
	public void setMapSE(Set<MappingType> mapSE) {
		this.mapSE = mapSE;
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
		hash = fnv(correspondences, hash);
		hash = fnv(mapSE, hash);
		hash = fnv(transSE, hash);
	}
}