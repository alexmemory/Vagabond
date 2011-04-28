package org.vagabond.explanation.generation;

import java.sql.ResultSet;
import java.util.List;
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
import org.vagabond.explanation.marker.SchemaResolver;
import org.vagabond.explanation.model.ExplanationFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.TargetSkeletonMappingError;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.util.ConnectionManager;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.RelAtomType;
import org.vagabond.xmlmodel.ForeignKeyType;

public class TargetSkeletonMappingExplanationGenerator implements
		ISingleExplanationGenerator {

	static Logger log = Logger.getLogger(TargetSkeletonMappingExplanationGenerator.class);
	
	private IAttributeValueMarker error;
	private TargetSkeletonMappingError expl;
	private Set<MappingType> maps;

	@Override
	public IExplanationSet findExplanations(ISingleMarker errorMarker)
			throws Exception {
		IExplanationSet result;
		
		result = ExplanationFactory.newExplanationSet();
		this.error = (IAttributeValueMarker) errorMarker;

		maps = ProvenanceGenerator.getInstance().getMapProv(error);
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
		
		expl = new TargetSkeletonMappingError(error);
		
		String relName = error.getRelName();
		String attrName = error.getAttrName();
		int errpos = SchemaResolver.getInstance().getAttrId(relName, attrName);
		String varName = null;
		
		for(MappingType map: maps) {
			// Verify that attrName is in the "From" attribute of a foreign key.
			boolean isIn = false;
			ForeignKeyType[] fksInTarget = 
				SchemaResolver.getInstance().getTargetSchema().getForeignKeyArray();
			Set<String> attrArr = new HashSet<String>();
			for (ForeignKeyType fk: fksInTarget) {
				List<String> allAttrList = Arrays.asList(fk.getFrom().getAttrArray());
				if (allAttrList.contains(attrName)) {
					// Now verify that attrName is the "From" var in the mapping.
					String fromRelName = relName;
					String toRelName = fk.getTo().getTableref();
					String fromAttrName = attrName;
					// Find the position of fromAttrName in the foreign key.
					int fromAttrPos = 0;
					for (String attr: fk.getFrom().getAttrArray()) {
						if (attr.equals(fromAttrName)) {
							break;
						}
						fromAttrPos ++;
					}
					// Find the name of the "To" attribute in the foreign key on the same position.
					String toAttrName = fk.getTo().getAttrArray(fromAttrPos);
					// Now check if this combination of "From" and "To" attribute names
					// appear in this mapping.
					int fromPos = errpos;
					int toPos = SchemaResolver.getInstance().getAttrId(toRelName, toAttrName);
					RelAtomType[] targetRels = map.getExists().getAtomArray();
					for (RelAtomType targetRel: targetRels) {
						if (targetRel.getTableref().equals(fromRelName)) {
							varName = targetRel.getVarArray(fromPos);
							for (RelAtomType tRel: targetRels) {
								if (varName.equals(tRel.getVarArray(toPos))) {
									isIn = true;
									attrArr.addAll(allAttrList);
									break;
								}
							}
							break;
						}
					}

				}
			}
			
			if (isIn) {
				if (!affRels.containsKey(relName)) {
					affRels.put(relName, new RelAttrMapSet());
				}
				relAttrMapSet = affRels.get(relName);
				relAttrMapSet.mapSet.add(map.getId());
				relAttrMapSet.attrSet.addAll(attrArr);
				expl.addMap(map);
			}
		}
		
		expl.setTransSE(MapScenarioHolder.getInstance().getTransForRels(
				affRels.keySet()));
		
		for (String affRel: affRels.keySet()) {
			computeSideEffects(affRel, affRels.get(affRel).mapSet, 
					affRels.get(affRel).attrSet);
		}
		expl.getTargetSideEffects().remove(error);

		if (expl.getMappingSideEffectSize() > 0)
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
		
		for(String attrName: attrs) {
			attrList.append(attrName + ",");
		}
		attrList.deleteCharAt(attrList.length() - 1);

		result = MarkerFactory.newMarkerSet();
		
		query = QueryHolder.getQuery("SuperMap.GetSideEffects")
				.parameterize("target." + rel, mapList.toString());
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
