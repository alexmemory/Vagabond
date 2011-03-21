package org.vagabond.explanation.model.basic;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.marker.MarkerFactory;

public class CorrespondenceError implements IBasicExplanation {

	static Logger log = Logger.getLogger(CorrespondenceError.class);
	
	private IMarkerSet sideEffects;
	private String correspondence;
	private Collection<String> mapSE;
	private IAttributeValueMarker explains;

	public CorrespondenceError () {
		sideEffects = MarkerFactory.newMarkerSet();
		mapSE = new ArrayList<String> ();
	}
	
	public CorrespondenceError (ISingleMarker marker) {
		sideEffects = MarkerFactory.newMarkerSet();
		mapSE = new ArrayList<String> ();
		explains = (IAttributeValueMarker) marker;
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
		return correspondence;
	}

	public String getCorrespondence() {
		return correspondence;
	}

	public void setCorrespondence(String correspondence) {
		this.correspondence = correspondence;
	}

	public Collection<String> getMapSE() {
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
