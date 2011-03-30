package org.vagabond.explanation.model.basic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.xmlmodel.CorrespondenceType;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.TransformationType;

import static org.vagabond.util.LoggerUtil.*;

public class CorrespondenceError extends AbstractBasicExplanation 
		implements IBasicExplanation {

	static Logger log = Logger.getLogger(CorrespondenceError.class);
	
	private Set<CorrespondenceType> correspondences;
	private Collection<MappingType> mapSE;
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
		mapSE = new ArrayList<MappingType> ();
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
	
	public void setCorrespondences (Set<CorrespondenceType> correspondence) {
		this.correspondences = correspondence;
	}
	
	public void addCorrespondence (CorrespondenceType corr) {
		this.correspondences.add(corr);
	}

	@Override
	public int getMappingSideEffectSize () {
		return mapSE.size();
	}
	
	@Override
	public Collection<MappingType> getMappingSideEffects () {
		return mapSE;
	}

	public void setMapSE(Collection<MappingType> mapSE) {
		this.mapSE = mapSE;
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
