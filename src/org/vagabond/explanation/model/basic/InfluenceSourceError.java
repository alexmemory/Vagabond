package org.vagabond.explanation.model.basic;

import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.marker.MarkerFactory;

public class InfluenceSourceError implements IBasicExplanation {

	private IMarkerSet sourceSE;
	private IMarkerSet targetSE;
	private ISingleMarker explains;
	
	private int hash = -1;

	public InfluenceSourceError () {
		sourceSE = MarkerFactory.newMarkerSet();
		targetSE = MarkerFactory.newMarkerSet();
	}
	
	public InfluenceSourceError (ISingleMarker explains) {
		this.explains = explains;
		sourceSE = MarkerFactory.newMarkerSet();
		targetSE = MarkerFactory.newMarkerSet();
	}
	
	public InfluenceSourceError (IMarkerSet sourceSE, IMarkerSet targetSE, 
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
		return ExplanationType.InfluenceSourceError;
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

	public IMarkerSet getSourceSE () {
		return sourceSE;
	}
	
	public void setTargetSE(IMarkerSet targetSE) {
		this.targetSE = targetSE;
	}


	@Override
	public Object getExplanation() {
		return sourceSE;
	}

	@Override
	public String toString () {
		return "InfluenceSourceError <" + explains.toString() + ">\n\n" +
				"with source side-effect:\n<" + sourceSE.toString() +
				">\n\nand target side-effect:\n<" + targetSE.toString() + ">";
	}

	@Override
	public boolean equals (Object other) {
		CopySourceError err;
		
		if (other == null)
			return false;
		
		if (this == other)
			return true;
		
		if (!(other instanceof CopySourceError))
			return false;
		
		err = (CopySourceError) other;
		
		if (!this.explains.equals(err.explains()))
			return false;
		
		if (!this.sourceSE.equals(err.getSourceSE()))
			return false;
		
		if (!this.targetSE.equals(err.getSideEffects()))
			return false;
		
		return true;
	}
	
	@Override
	public int hashCode () {
		if (hash  == -1)
		{
			hash = explains.hashCode();
			hash = hash * 13 + sourceSE.hashCode();
			hash = hash * 13 + targetSE.hashCode();
		}
		return hash;
	}
	
}
