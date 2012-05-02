package org.vagabond.explanation.model.basic;

import static org.vagabond.util.HashFNV.fnv;

import org.apache.log4j.Logger;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.util.LogProviderHolder;

public class CopySourceError extends AbstractBasicExplanation implements IBasicExplanation {

	static Logger log = LogProviderHolder.getInstance().getLogger(CopySourceError.class);
	
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
		updateHash();
	}
		
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
