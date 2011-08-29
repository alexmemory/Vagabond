package org.vagabond.explanation.generation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.vagabond.explanation.generation.prov.ProvenanceGenerator;
import org.vagabond.explanation.generation.prov.SideEffectGenerator;
import org.vagabond.explanation.generation.prov.SourceProvenanceSideEffectGenerator;
import org.vagabond.explanation.marker.AttrValueMarker;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.marker.ITupleMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.model.ExplanationFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.InfluenceSourceError;
import org.vagabond.explanation.model.prov.MapAndWLProvRepresentation;
import org.vagabond.mapping.model.MappingGraph;
import org.vagabond.util.CollectionUtils;
import org.vagabond.util.Pair;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.RelAtomType;

public class InfluenceSourceExplanationGenerator 
		extends SourceProvenanceSideEffectGenerator 
		implements ISingleExplanationGenerator {

	private InfluenceSourceError expl;
	private IExplanationSet result;
	
	@Override
	public IExplanationSet findExplanations(ISingleMarker errorMarker)
			throws Exception {
		MapAndWLProvRepresentation prov;
		result = ExplanationFactory.newExplanationSet();
		
		this.error = (IAttributeValueMarker) errorMarker;
		prov = ProvenanceGenerator.getInstance().computePIAndMapProv(error);
		genExplsForProv(prov);
		
		return result;
	}

	private void genExplsForProv(MapAndWLProvRepresentation prov) throws Exception {
		Set<MappingType> allMaps;
		Map<String, Set<Pair<Integer,String>>> joinAttrMap;
		Map<String, Vector<Integer>> atomWLPos;
		
		allMaps = new HashSet<MappingType>(prov.getMapProv());
		joinAttrMap = findJoinAttrsCandiForMaps (allMaps);
		atomWLPos = getAtomsToWlPos();
		
		// for each witness list get map prov and mark source join attributes
		for(Pair<Vector<ITupleMarker>, MappingType> wlPlusMap
				: prov.getAllProvAndMap()) {
			String mapId = wlPlusMap.getValue().getId();
			Set<Pair<Integer,String>> attrs = joinAttrMap.get(mapId);
			
			// for each potentially failed join attr add an explanation
			for(Pair<Integer,String> attr: attrs) {
				IMarkerSet sourceSE;
				IMarkerSet targetSE;
				int wlPos = atomWLPos.get(mapId).get(attr.getKey());
				int attrPos;
				ITupleMarker wlElem = wlPlusMap.getKey().get(wlPos);
				
				attrPos = CollectionUtils.linearSearch(
						wlPlusMap.getValue().getForeach()
								.getAtomArray(attr.getKey()).getVarArray(), 
						attr.getValue());
				expl = new InfluenceSourceError(error);
				sourceSE = MarkerFactory.newMarkerSet(
						MarkerFactory.newAttrMarker(
								wlElem,
								attrPos
								));
				targetSE = SideEffectGenerator.getInstance()
						.computeTargetSideEffects(sourceSE, error);
				expl.setSourceSE(sourceSE);
				expl.setTargetSE(targetSE);
				
				result.addExplanation(expl);
			}
		}
	}
	
	private Map<String, Vector<Integer>> getAtomsToWlPos () throws Exception {
		Map<String,Vector<Integer>> result;
		int i;
		result = new HashMap<String,Vector<Integer>> ();
		
		i = 0;
		for(Pair<String,Set<MappingType>> baseToMaps
				: ProvenanceGenerator.getInstance()
						.getBaseRelAccessToMapping(error.getRel()))
		{
			for(MappingType map: baseToMaps.getValue()) {
				String mapId = map.getId();
				Vector<Integer> wlPos;
				
				if (!result.containsKey(mapId))
					result.put(mapId, new Vector<Integer> ());
				wlPos = result.get(mapId);
				wlPos.add(i);
			}
			i++;
		}
		return result;
	}
	
	private Map<String, Set<Pair<Integer,String>>> findJoinAttrsCandiForMaps 
			(Set<MappingType> maps) {
		int errorAttrId = ((AttrValueMarker) error).getAttrId();
		Map<String, Set<Pair<Integer,String>>> mapsToJoinAttrs;
		
		mapsToJoinAttrs = new HashMap<String, Set<Pair<Integer,String>>> ();
		
		for(MappingType map: maps) {
			String varName = null;
			Set<Pair<Integer,String>> joinAttrs;
			MappingGraph mGraph;
			
			mGraph = new MappingGraph(map);
			
			// find the var name for the attribute of the error marker in the target
			for (RelAtomType targetRel: map.getExists().getAtomArray()) {
				if (targetRel.getTableref().equals(error.getRel())) {
					varName = targetRel.getVarArray(errorAttrId);
					break;
				}
			}
			
			joinAttrs = mGraph.getJoinVarsAndAtoms(varName);
			mapsToJoinAttrs.put(map.getId(), joinAttrs);
		}
		
		return mapsToJoinAttrs;
	}
	
	

}
