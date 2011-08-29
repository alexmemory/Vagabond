package org.vagabond.explanation.generation;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.vagabond.explanation.generation.prov.ProvenanceGenerator;
import org.vagabond.explanation.generation.prov.SideEffectGenerator;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.marker.ITupleMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.model.ExplanationFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.CopySourceError;
import org.vagabond.explanation.model.prov.MapAndWLProvRepresentation;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.util.LogProviderHolder;
import org.vagabond.xmlmodel.MappingType;

public class CopySourceExplanationGenerator  
		implements ISingleExplanationGenerator {

	static Logger log = LogProviderHolder.getInstance().getLogger(CopySourceExplanationGenerator.class);
	
	private IAttributeValueMarker error;
	private MapAndWLProvRepresentation prov;
	protected CopySourceError expl;
	
	@Override
	public IExplanationSet findExplanations(ISingleMarker errorMarker) throws Exception {
		this.error = (IAttributeValueMarker) errorMarker;
		
		return getExplanationSets();
	}
	
	private IExplanationSet getExplanationSets() throws Exception {
		IExplanationSet result = ExplanationFactory.newExplanationSet();
		IMarkerSet sourceSE;
		IMarkerSet targetSE;
		
		prov = ProvenanceGenerator.getInstance().computePIAndMapProv(error);
		sourceSE = getRealCopyFromMappings(); 
		targetSE = SideEffectGenerator.getInstance()
				.computeTargetSideEffects(sourceSE, error);
		
		expl = new CopySourceError(error);
		expl.setSourceSE(sourceSE);
		expl.setTargetSE(targetSE);
		
		log.debug("Generated Explanation:\n" + expl.toString());
		
		result.addExplanation(expl);
		
		log.debug("Generated Explanation:\n" + expl.toString());
		
		return result;
	}
	
	private IMarkerSet getRealCopyFromMappings() throws Exception {
		IMarkerSet result;
		Map<String,int[][]> wlCopyAttrForMap;
		Vector<ITupleMarker> wl;
		MappingType m;
		
		result = MarkerFactory.newMarkerSet();
		wlCopyAttrForMap = getCopyCSAttrsForBaseRels();
		
		/* for each tid in each witness list: Determine from the
		 * mappings from which attributes values are copied to the error
		 * and add IAttributeValueMarkers for each of them.
		 */
		for(int i = 0; i < prov.getWitnessLists().size(); i++) {
			wl = prov.getWitnessList(i);
			m = prov.getMapProv().get(i);
			
			log.debug("WL: <" + wl + "> and map <" + m + ">");
			
			for(int j = 0; j < wl.size(); j++) {
				ITupleMarker wlElem = wl.get(j);
				
				if (wlElem != null) {
					int atomPos = prov.getMapToWlPosPositions(m).indexOf(j);
					int[] sourceAttrs = wlCopyAttrForMap.get(m.getId())[atomPos];
					
					for (int attr: sourceAttrs) {
						IAttributeValueMarker newMarker;
						
						newMarker = MarkerFactory.newAttrMarker(wlElem, attr);
						log.debug("added marker: " + newMarker);
						result.add(newMarker);
					}
				}
			}
		}
		
		return result;
	}
	
	private Map<String,int[][]> getCopyCSAttrsForBaseRels () throws Exception {
		Map<String,int[][]> result;
		result = new HashMap<String,int[][]> ();
		
		for(MappingType map: prov.getAllMaps()) {
			result.put(map.getId(), MapScenarioHolder.getInstance()
					.getGraphForMapping(map)
					.getAtomPosForTargetPos(error.getRel(), error.getAttrId()));
		}
		
		log.debug("created mapping to atom vars affected by target attr mapping.");
		
		return result;
	}
	

	
	
}
