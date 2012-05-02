package org.vagabond.explanation.model.basic;

import static org.vagabond.util.HashFNV.fnv;

import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.marker.MarkerFactory;

public class InfluenceSourceError extends AbstractBasicExplanation 
		implements IBasicExplanation {

	private IMarkerSet sourceSE;

	public InfluenceSourceError () {
		sourceSE = MarkerFactory.newMarkerSet();
	}
	
	public InfluenceSourceError (ISingleMarker explains) {
		super(explains);
		sourceSE = MarkerFactory.newMarkerSet();
	}
	
	public InfluenceSourceError (IMarkerSet sourceSE, IMarkerSet targetSE, 
			ISingleMarker explains) {
		super(explains);
		this.sourceSE = sourceSE;
		this.targetSE = targetSE;
	}


	@Override
	public ExplanationType getType() {
		return ExplanationType.InfluenceSourceError;
	}

	public void setSourceSE(IMarkerSet sourceSE) {
		this.sourceSE = sourceSE;
		updateHash();
	}

	@Override
	public IMarkerSet getSourceSideEffects () {
		return sourceSE;
	}
	
	@Override
	public int getSourceSideEffectSize() {
		return sourceSE.getSize();
	}
	
//	public void setTargetSE(IMarkerSet targetSE) {
//		this.targetSE = targetSE;
//	}


	@Override
	public Object getExplanation() {
		return sourceSE;
	}
	
	@Override
	protected void computeHash () {
		super.computeHash();
		hash = fnv(sourceSE, hash);
	}
	
}
