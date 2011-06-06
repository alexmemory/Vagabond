package org.vagabond.explanation.generation.prov;

import org.apache.log4j.Logger;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.IMarkerSet;

public class SideEffectGenerator {

	static Logger log = Logger.getLogger(SideEffectGenerator.class);
	
	private static SideEffectGenerator instance;
	private ISideEffectGenerator genImpl;
	
	static {
		instance = new SideEffectGenerator();
		// set default implementation
		instance.genImpl = new AttrGranularitySourceProvenanceSideEffectGenerator();
	}
	
	private SideEffectGenerator () {
		
	}
	
	public static SideEffectGenerator getInstance () {
		return instance;
	}
	
	public void setSideEffectImpl (ISideEffectGenerator impl) {
		this.genImpl = impl;
	}
	
	public IMarkerSet computeTargetSideEffects (IMarkerSet sourceSE, 
			IAttributeValueMarker error) 
			throws Exception {
		return genImpl.computeTargetSideEffects(sourceSE, error);
	}
	
	public void reset () {
		genImpl.reset();
	}
	
	
	
}
