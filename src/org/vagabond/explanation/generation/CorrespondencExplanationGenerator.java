package org.vagabond.explanation.generation;

import java.sql.ResultSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.vagabond.explanation.generation.prov.ProvenanceGenerator;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.model.ExplanationFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.CorrespondenceError;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.util.ConnectionManager;
import org.vagabond.util.LogProviderHolder;
import org.vagabond.xmlmodel.CorrespondenceType;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.RelAtomType;

public class CorrespondencExplanationGenerator implements
		ISingleExplanationGenerator {

	static Logger log = LogProviderHolder.getInstance().getLogger(
			CorrespondencExplanationGenerator.class);
	
	private CorrespondenceError expl;
	private IAttributeValueMarker error;
	private Map<Set<CorrespondenceType>,CorrespondenceError> explForCorr;
	
	public CorrespondencExplanationGenerator () {
		explForCorr = new HashMap<Set<CorrespondenceType>,CorrespondenceError> ();
	}
	
	@Override
	public IExplanationSet findExplanations(ISingleMarker errorMarker)
			throws Exception {
		IExplanationSet result;
		Set<CorrespondenceType> corrs;
		
		this.error = (IAttributeValueMarker) errorMarker;
		result = ExplanationFactory.newExplanationSet();
		
		corrs = findCorrespondences();
		
		if (explForCorr.containsKey(corrs)) {
			expl = explForCorr.get(corrs);
		}
		else {	
			
			expl = new CorrespondenceError(errorMarker);
			expl.setCorrSE(corrs);
			// did not find correspondence?
			if (expl.getCorrSideEffectSize() == 0)
				return result;
			
			computeSideEffects();
			explForCorr.put(corrs, expl);
		}
		
		result.addExplanation(expl);
		
		return result;
	}

	private void computeSideEffects() throws Exception {
		Set<MappingType> affMaps;
		Map<String, Set<String>> mapsPerTarget;
		
		affMaps = new HashSet<MappingType> ();
		
		for(CorrespondenceType corr: expl.getCorrespondenceSideEffects()) {
			affMaps.addAll(MapScenarioHolder.getInstance()
					.getMapsForCorr(corr));
		}
		
		expl.setMapSE(affMaps);
		
		mapsPerTarget = partitionMapsToTarget(affMaps, 
				expl.getCorrespondenceSideEffects());
		
		expl.setTransSE(MapScenarioHolder.getInstance().getTransForRels(
				mapsPerTarget.keySet()));
		
		for(String target: mapsPerTarget.keySet()) {
			runSideEffectQuery (target, mapsPerTarget.get(target));
		}
		
		expl.getTargetSideEffects().remove(expl.explains());
	}
	
	private void runSideEffectQuery (String rel, Set<String> maps) throws Exception {
		StringBuffer mapList;
		String query;
		IMarkerSet sideEff;
		ResultSet rs;
		String attrName;
		
		attrName = ((IAttributeValueMarker) expl.explains()).getAttrName();
		sideEff = expl.getTargetSideEffects();
		mapList = new StringBuffer();
		
		for(String mapName: maps) {
			mapList.append("('" + mapName + "'),");
		}
		mapList.deleteCharAt(mapList.length() - 1);
		
		query = QueryHolder.getQuery("Correspondence.GetSideEffects")
				.parameterize("target." + rel, mapList.toString());
		
		if (log.isDebugEnabled()) {log.debug("Run side effect query for <" + rel + "> with query <\n" 
				+ query + ">");};
		
		rs = ConnectionManager.getInstance().execQuery(query);

		while(rs.next())
			sideEff.add(MarkerFactory.newAttrMarker(
					rel, rs.getString(1), attrName));
		
		ConnectionManager.getInstance().closeRs(rs);
	}
	
	

	private Map<String, Set<String>> partitionMapsToTarget
			(Set<MappingType> affMaps, Collection<CorrespondenceType> corrs) {
		Map<String,Set<String>> mapsPerTarget;
		String targetName;
		String mapName;
		mapsPerTarget = new HashMap<String,Set<String>> ();
		Set<String> targetAffByCorr;
		
		
		// find target relations affected by correspondences
		targetAffByCorr = new HashSet<String>();
		for (CorrespondenceType corr: corrs) {
			targetAffByCorr.add(corr.getTo().getTableref());
		}
		
		// create target rel to mappings Map
		for (MappingType map: affMaps) {
			mapName = map.getId();
			for(RelAtomType target: map.getExists().getAtomArray()) {
				targetName = target.getTableref();
				if (targetAffByCorr.contains(targetName)) {
					if (!mapsPerTarget.containsKey(targetName))
						mapsPerTarget.put(targetName, new HashSet<String> ());
					mapsPerTarget.get(targetName).add(mapName);
				}
			}
		}
		
		return mapsPerTarget;
	}

	private Set<CorrespondenceType> findCorrespondences () throws Exception {
		Vector<String> mappings;
		Set<CorrespondenceType> corrCandi;
		MappingType map;
		
		corrCandi = new HashSet<CorrespondenceType> ();
		mappings = ProvenanceGenerator.getInstance().computeMapProvAsStrings(error);
		
		// get candidate correspondences
		for (String mapName: mappings) {
			map = MapScenarioHolder.getInstance().getMapping(mapName);
			for(CorrespondenceType corr: MapScenarioHolder.getInstance()
					.getCorrespondences(map)) {
				if (corrMapsOnError(corr))
					corrCandi.add(corr);
			}
		}
		
		if (log.isDebugEnabled()) {log.debug("Correpsondence candidates are " + corrCandi.toString());};
		return corrCandi;
	}
	
	private boolean corrMapsOnError (CorrespondenceType corr) {
		if (!corr.getTo().getTableref().equals(error.getRel()))
			return false;
		for (String attrName: corr.getTo().getAttrArray()) {
			if (attrName.equals(error.getAttrName()))
				return true;
		}
		
		return false;
	}	

}
