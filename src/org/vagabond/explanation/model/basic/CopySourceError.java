package org.vagabond.explanation.model.basic;

import org.apache.log4j.Logger;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ISingleMarker;

public class CopySourceError implements IBasicExplanation {

	static Logger log = Logger.getLogger(CopySourceError.class);
	
	private IMarkerSet sourceSE;
	private IMarkerSet targetSE;
	private ISingleMarker explains;
	
	public CopySourceError () {
		
	}
	
	public CopySourceError (IMarkerSet sourceSE, IMarkerSet targetSE, 
			ISingleMarker explains) {
		this.sourceSE = sourceSE;
		this.targetSE = targetSE;
		this.explains = explains;
	}
	
	@Override
	public int getSideEffectSize() {
		return targetSE.getSize();
	}

	@Override
	public IMarkerSet getSideEffects() {
		return targetSE;
	}

	@Override
	public ExplanationType getType() {
		return ExplanationType.CopySourceError;
	}

	@Override
	public ISingleMarker explains() {
		return explains;
	}
	
	public void setExplains (ISingleMarker explains) {
		this.explains = explains;
	}

	public void setSourceSE(IMarkerSet sourceSE) {
		this.sourceSE = sourceSE;
	}

	public void setTargetSE(IMarkerSet targetSE) {
		this.targetSE = targetSE;
	}
	
	@Override
	public Object getExplanation() {
		return sourceSE;
	}
	
	public String toString () {
		return "CopySourceError <" + explains.toString() + ">\n\n" +
				"with source side-effect:\n\n" + sourceSE.toString() +
				"and target side-effect:\n\n" + targetSE.toString();
	}


}
