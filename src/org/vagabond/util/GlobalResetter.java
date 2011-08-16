package org.vagabond.util;

import org.apache.log4j.Logger; import org.vagabond.util.LogProviderHolder;
import org.vagabond.explanation.generation.prov.ProvenanceGenerator;
import org.vagabond.explanation.generation.prov.SideEffectGenerator;
import org.vagabond.explanation.generation.prov.SourceProvenanceSideEffectGenerator;
import org.vagabond.mapping.model.MapScenarioHolder;

/**
 * Global access to reset various cached data structures. Should be used before setting
 * a new mapping scenario.
 * 
 * @author lord_pretzel
 *
 */

public class GlobalResetter {

	static Logger log = LogProviderHolder.getInstance().getLogger(GlobalResetter.class);
	
	private static GlobalResetter instance;
	
	static {
		instance = new GlobalResetter();
	}
	
	private GlobalResetter () {
		
	}
	
	public static GlobalResetter getInstance () {
		return instance;
	}
	
	public void reset () {
		MapScenarioHolder.getInstance().reset();
		ProvenanceGenerator.getInstance().reset();
		SideEffectGenerator.getInstance().reset();
	}
}
