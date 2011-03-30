package org.vagabond.explanation.generation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.model.ExplanationFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.SuperflousMappingError;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.util.ConnectionManager;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.RelAtomType;

public class SuperfluousMappingExplanationGenerator 
		implements ISingleExplanationGenerator {

	static Logger log = Logger.getLogger(SuperfluousMappingExplanationGenerator.class);
	
	private IAttributeValueMarker error;
	private SuperflousMappingError expl;
	private Set<MappingType> maps;
	
	@Override
	public IExplanationSet findExplanations(ISingleMarker errorMarker)
			throws Exception {
		IExplanationSet result;
		
		result = ExplanationFactory.newExplanationSet();
		this.error = (IAttributeValueMarker) errorMarker;

		getMapProv();
		generateExplanation (result);
		
		return result;
	}

	private void generateExplanation (IExplanationSet result) throws Exception {
		Map<String, Set<String>> affRels;
		Set<String> mapSet;
		String relName;
		
		expl = new SuperflousMappingError(error);
		affRels = new HashMap<String, Set<String>> ();
		
		for(MappingType map: maps) {
			expl.addMapSE(map);
			
			for(RelAtomType atom: map.getExists().getAtomArray()) {
				relName = atom.getTableref();
				if (!affRels.containsKey(relName)) {
					affRels.put(relName, new HashSet<String> ());
				}
				mapSet = affRels.get(relName);
				mapSet.add(map.getId());
			}
		}
		
		expl.setTransSE(MapScenarioHolder.getInstance().getTransForRels(
				affRels.keySet()));
		
		for (String affRel: affRels.keySet()) {
			computeSideEffects(affRel, affRels.get(affRel));
		}
		expl.getTargetSideEffects().remove(MarkerFactory.newTupleMarker(error));

		result.addExplanation(expl);
	}

	private IMarkerSet computeSideEffects(String rel, Set<String> maps) throws Exception {
		IMarkerSet result;
		String query;
		ResultSet rs;
		IMarkerSet sideEff = expl.getTargetSideEffects();
		StringBuffer mapList;
		
		mapList = new StringBuffer();
		
		for(String mapName: maps) {
			mapList.append("('" + mapName + "'),");
		}
		mapList.deleteCharAt(mapList.length() - 1);

		result = MarkerFactory.newMarkerSet();
		
		query = QueryHolder.getQuery("SuperMap.GetSideEffects")
				.parameterize("target." + rel, mapList.toString());
		log.debug("Run side effect query for <" + rel + "> with query <\n" 
				+ query + ">");
		
		rs = ConnectionManager.getInstance().execQuery(query);
		
		while(rs.next())
			sideEff.add(MarkerFactory.newTupleMarker(
					rel, rs.getString(1)));
		
		ConnectionManager.getInstance().closeRs(rs);
		
		return result;
	}

	private void getMapProv() throws Exception {
		String query;
		ResultSet rs;
		
		maps = new HashSet<MappingType> ();
		
		query = QueryHolder.getQuery("SuperMap.GetMapProv")
				.parameterize("target." + error.getRelName(), error.getTid());
		log.debug("Compute MapProv for <" + error + "> with query:\n" + query);
		
		rs = ConnectionManager.getInstance().execQuery(query);
		
		while(rs.next()) {
			maps.add(MapScenarioHolder.getInstance()
					.getMapping(rs.getString(1)));
		}
		
		ConnectionManager.getInstance().closeRs(rs);
		
		log.debug("Get mapping provenance for <" + error + "> returned <" + maps + ">");
	}

}
