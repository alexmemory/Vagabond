package org.vagabond.explanation.generation;

import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.vagabond.explanation.generation.prov.ProvenanceGenerator;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.marker.ScenarioDictionary;
import org.vagabond.explanation.model.ExplanationFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.SourceSkeletonMappingError;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.util.ConnectionManager;
import org.vagabond.util.LogProviderHolder;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.RelAtomType;

public class SourceSkeletonMappingExplanationGenerator implements
		ISingleExplanationGenerator {

	static Logger log = LogProviderHolder.getInstance().getLogger(SourceSkeletonMappingExplanationGenerator.class);
	
	private IAttributeValueMarker error;
	private SourceSkeletonMappingError expl;
	private Set<MappingType> maps;

	@Override
	public IExplanationSet findExplanations(ISingleMarker errorMarker)
			throws Exception {
		IExplanationSet result;
		
		result = ExplanationFactory.newExplanationSet();
		this.error = (IAttributeValueMarker) errorMarker;

		maps = ProvenanceGenerator.getInstance().computeMapProv(error);
		generateExplanation (result);
		
		return result;
	}
	
	private void generateExplanation (IExplanationSet result) throws Exception {
		class RelAttrMapSet {
			Set<String> attrSet;
			Set<String> mapSet;
			
			RelAttrMapSet() {
				attrSet = new HashSet<String>();
				mapSet = new HashSet<String>();
			}
		}
		
		Map<String, RelAttrMapSet> affRels = new HashMap<String, RelAttrMapSet>();
		RelAttrMapSet relAttrMapSet;
		
		expl = new SourceSkeletonMappingError(error);
		
		String relName = error.getRel();
		String attrName = error.getAttrName();
		int errpos = ScenarioDictionary.getInstance().getAttrId(relName, attrName);
		String varName = null;
		
		for(MappingType map: maps) {
			// mappings with a single source atom cannot join wrong.
			if (map.getForeach().getAtomArray().length == 1)
				continue;
			
			expl.addMap(map);
			
			// 1st step: find the var name for the error in the target
			RelAtomType[] targetRels = map.getExists().getAtomArray();
			for (RelAtomType targetRel: targetRels) {
				if (targetRel.getTableref().equals(relName)) {
					varName = targetRel.getVarArray(errpos);
					break;
				}
			}
			
			// 2nd step: find all the relations that have the same var name in the source
			// and store the attributes of these relations in a set.
			HashSet<String> sideEffectAttrs = new HashSet<String>();
			
			for (RelAtomType sourceRel: map.getForeach().getAtomArray()) {
				String[] varNames = sourceRel.getVarArray();
				for (String v: varNames) {
					if (v.equals(varName)) {
						sideEffectAttrs.addAll(Arrays.asList(varNames));
						break;
					}
				}
			}
			
			//TODO check that we actually have more than one source atom, otherwise there is no such explanation
			
			// 3rd step: find the attributes in the target that appear in the list
			// found in the 2nd step.
			for (RelAtomType targetRel: targetRels) {
				String targetRelName = targetRel.getTableref();
				String[] varNames = targetRel.getVarArray();
				int vpos = 0;
				for (String v: varNames) {
					if (sideEffectAttrs.contains(v)) {
						if (!affRels.containsKey(targetRelName)) {
							affRels.put(targetRelName, new RelAttrMapSet());
						}
						relAttrMapSet = affRels.get(targetRelName);
						relAttrMapSet.mapSet.add(map.getId());
						String targetAttrName = ScenarioDictionary.getInstance()
												.getAttrName(targetRelName, vpos);
						relAttrMapSet.attrSet.add(targetAttrName);
					}
					vpos++;
				}
			}
		}
		
		expl.setTransSE(MapScenarioHolder.getInstance().getTransForRels(
				affRels.keySet()));
		
		for (String affRel: affRels.keySet()) {
			computeSideEffects(affRel, affRels.get(affRel).mapSet, 
					affRels.get(affRel).attrSet);
		}
		expl.getTargetSideEffects().remove(error);

		// we found at least one mapping that may have joined incorrectly
		if (expl.getMappingSideEffectSize() != 0)
			result.addExplanation(expl);
	}

	private IMarkerSet computeSideEffects(String rel, Set<String> maps, Set<String> attrs) throws Exception {
		IMarkerSet result;
		String query;
		ResultSet rs;
		IMarkerSet sideEff = expl.getTargetSideEffects();
		StringBuffer mapList = new StringBuffer();
		StringBuffer attrList = new StringBuffer();
		
		for(String mapName: maps) {
			mapList.append("('" + mapName + "'),");
		}
		mapList.deleteCharAt(mapList.length() - 1);
		
		//TODO why add attrs here
		for(String attrName: attrs) {
			attrList.append(attrName + ",");
		}
		attrList.deleteCharAt(attrList.length() - 1);

		result = MarkerFactory.newMarkerSet();
		
		query = QueryHolder.getQuery("SuperMap.GetSideEffectsAlternative")
				.parameterize(attrList.toString(), "target." + rel, mapList.toString());
		log.debug("Run side effect query for <" + rel + "> with query <\n" 
				+ query + ">");
		
		rs = ConnectionManager.getInstance().execQuery(query);
		
		while(rs.next()) {
			String tid = rs.getString(1);
			
			for (String attr: attrs) 
				sideEff.add(MarkerFactory.newAttrMarker(
						rel, tid, attr));
		}
		
		ConnectionManager.getInstance().closeRs(rs);
		
		return result;
	}

}
