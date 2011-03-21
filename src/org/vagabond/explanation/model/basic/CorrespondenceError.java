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

public class CorrespondenceError implements IBasicExplanation {

	static Logger log = Logger.getLogger(CorrespondenceError.class);
	
	private IMarkerSet sideEffects;
	private Set<CorrespondenceType> correspondences;
	private Collection<String> mapSE;
	private IAttributeValueMarker explains;

	public CorrespondenceError () {
		setUp();
	}

	public CorrespondenceError (ISingleMarker marker) {
		setUp();
		explains = (IAttributeValueMarker) marker;
	}
	
	private void setUp() {
		sideEffects = MarkerFactory.newMarkerSet();
		mapSE = new ArrayList<String> ();
		correspondences = new HashSet<CorrespondenceType> ();
	}
	
	@Override
	public int getSideEffectSize() {
		return sideEffects.getSize();
	}

	@Override
	public IMarkerSet getSideEffects() {
		return sideEffects;
	}

	@Override
	public ExplanationType getType() {
		return ExplanationType.CorrespondenceError;
	}

	@Override
	public ISingleMarker explains() {
		return explains;
	}

	@Override
	public Object getExplanation() {
		return correspondences;
	}

	public Set<CorrespondenceType> getCorrespondences() {
		return correspondences;
	}

	public void setCorrespondences (Set<CorrespondenceType> correspondence) {
		this.correspondences = correspondence;
	}
	
	public void addCorrespondence (CorrespondenceType corr) {
		this.correspondences.add(corr);
	}

	public Collection<String> getMapSE () {
		return mapSE;
	}

	public void setMapSE(Collection<String> mapSE) {
		this.mapSE = mapSE;
	}

	public void setSideEffects(IMarkerSet sideEffects) {
		this.sideEffects = sideEffects;
	}

	public void setExplains(IAttributeValueMarker explains) {
		this.explains = explains;
	}

}
