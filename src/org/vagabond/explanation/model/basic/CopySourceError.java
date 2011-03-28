package org.vagabond.explanation.model.basic;

import org.apache.log4j.Logger;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.marker.MarkerFactory;

public class CopySourceError extends AbstractBasicExplanation implements IBasicExplanation {

	static Logger log = Logger.getLogger(CopySourceError.class);
	
	private IMarkerSet sourceSE;
	
	public CopySourceError () {
		super();
		sourceSE = MarkerFactory.newMarkerSet();
	}
	
	public CopySourceError (ISingleMarker explains) {
		super(explains);
		sourceSE = MarkerFactory.newMarkerSet();
	}
	
	public CopySourceError (IMarkerSet sourceSE, IMarkerSet targetSE, 
			ISingleMarker explains) {
		super(explains);
		this.sourceSE = sourceSE;
		this.targetSE = targetSE;
	}
	
	@Override
	public ExplanationType getType() {
		return ExplanationType.CopySourceError;
	}
	
	@Override
	public int getSourceSideEffectSize() {
		return sourceSE.getSize();
	}

	@Override
	public IMarkerSet getSourceSideEffects () {
		return sourceSE;
	}

	public void setSourceSE(IMarkerSet sourceSE) {
		this.sourceSE = sourceSE;
	}
		
	@Override
	public Object getExplanation() {
		return sourceSE;
	}
	
//	@Override
//	public String toString () {
//		return "CopySourceError <" + explains.toString() + ">\n\n" +
//				"with source side-effect:\n<" + sourceSE.toString() +
//				">\n\nand target side-effect:\n<" + targetSE.toString() + ">";
//	}

//	@Override
//	public boolean equals (Object other) {
//		CopySourceError err;
//		
//		if (other == null)
//			return false;
//		
//		if (this == other)
//			return true;
//		
//		if (!(other instanceof CopySourceError))
//			return false;
//		
//		err = (CopySourceError) other;
//		
//		if (!this.explains.equals(err.explains()))
//			return false;
//		
//		if (!this.sourceSE.equals(err.getSourceSE()))
//			return false;
//		
//		if (!this.targetSE.equals(err.getTargetSideEffects()))
//			return false;
//		
//		return true;
//	}
//	
//	@Override
//	public int hashCode () {
//		if (hash == -1)
//		{
//			hash = explains.hashCode();
//			hash = hash * 13 + sourceSE.hashCode();
//			hash = hash * 13 + targetSE.hashCode();
//		}
//		return hash;
//	}

}
