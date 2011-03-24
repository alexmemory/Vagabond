package org.vagabond.explanation.generation.prov;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.vagabond.explanation.generation.QueryHolder;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.marker.ITupleMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.util.ConnectionManager;
import org.vagabond.util.ResultSetUtil;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.RelAtomType;

public class SourceProvenanceSideEffectGenerator {

	public static Logger log = Logger.getLogger(SourceProvenanceSideEffectGenerator.class);
	
	protected IAttributeValueMarker error;
	private IMarkerSet sourceSE;
	
	public IMarkerSet computeTargetSideEffects(IMarkerSet sourceSE) throws Exception {
		String query;
		ResultSet rs;
		IMarkerSet result = MarkerFactory.newMarkerSet();
		Map<String, Set<String>> relsForAffTarget;
		Map<String, IMarkerSet> partionedSE;
		
		this.sourceSE = sourceSE;
		partionedSE = partitionSourceSE();
		relsForAffTarget = getRelAffectedByRels(partionedSE.keySet());
		
		for(String targetRel: relsForAffTarget.keySet()) {
			query = getSideEffectQuery(targetRel, 
					relsForAffTarget.get(targetRel), 
					partionedSE);
			
			log.debug("Compute side effects for target relation <"
					+ targetRel + "> using query:\n" + query);
			
			rs = ConnectionManager.getInstance().execQuery(query);
			parseTargetSE(targetRel, rs, result);
			ConnectionManager.getInstance().closeRs(rs);
		}
		
		result.remove(MarkerFactory.newTupleMarker(
				(IAttributeValueMarker) error));
		
		return result;
	}
	
	private Set<String> getNumRelForTrans (String targetRel) throws SQLException, ClassNotFoundException {
		List<String> rels;
		String query;
		ResultSet rs;
		String result;
		
		query = QueryHolder.getQuery("ProvSE.GetProvQueryResultAttrs")
				.parameterize("target." + targetRel);
		
		rs = ConnectionManager.getInstance().execQuery(query);
		
		rs.next();
		result = rs.getString(1).trim();
		result = result.substring(1, result.length() - 1);
		rels  = ResultSetUtil.getBaseRelsForProvSchema(result.split(","));
		
		ConnectionManager.getInstance().closeRs(rs);
		
		return new HashSet<String> (rels);
	}

	private void parseTargetSE(String rel, ResultSet rs, IMarkerSet sideEff)
			throws Exception {
		while(rs.next()) {
			sideEff.add(MarkerFactory.newTupleMarker(rel, 
					rs.getString(1)));
		}
	}

	private Map<String, Set<String>> getRelAffectedByRels 
			(Collection<String> inputRels) throws SQLException, ClassNotFoundException {
		String targetName;
		Set<String> sources;
		Map<String, Set<String>> result;
		
		result = new HashMap<String, Set<String>> ();

		for(MappingType map: MapScenarioHolder.getInstance().getScenario().
				getMappings().getMappingArray()) {
			for(RelAtomType atom: map.getForeach().getAtomArray()) {
				if (inputRels.contains(atom.getTableref())) {
					for(RelAtomType affRel: map.getExists().getAtomArray()) {
						targetName = affRel.getTableref();
						if (!result.containsKey(targetName)) {
							sources = getNumRelForTrans(targetName);
							result.put(targetName, sources);
						}
					}
				}
			}
		}
		
		log.debug("Relations affected by <" + inputRels + "> are:\n" + result);
		
		return result;
	}
	
	private Map<String, IMarkerSet> partitionSourceSE () {
		Map<String, IMarkerSet> parts;
		IMarkerSet mSet;
		String relName;
		
		parts = new HashMap<String, IMarkerSet> ();
		for(ISingleMarker marker: sourceSE) {
			relName = ((ITupleMarker) marker).getRel();
			mSet = parts.get(relName);
			if (mSet == null) {
				mSet = MarkerFactory.newMarkerSet();
				parts.put(relName, mSet);
			}
			mSet.add(marker);
		}
		
		log.debug("paritioned source side effects into:\n" + parts);
		
		return parts;
	}

	public String getSideEffectQuery (String relName, Set<String> sourceRels, 
			Map<String, IMarkerSet> sourceSE) {
		StringBuffer conditions;
		String query;
		String unnumSource;
		
		conditions = new StringBuffer();
		
		conditions.append("(");
		for(String source: sourceRels) {
			unnumSource = getUnNumRelName(source);
			if (sourceSE.get(unnumSource) != null) {
				for(ISingleMarker sourceErr: sourceSE.get(source).getElems()) {
					conditions.append(
							getSideEffectEqualityCond(source, 
									(ITupleMarker) sourceErr)
							+ " AND ");
				}
			}
		}
		conditions.delete(conditions.length() - 5, conditions.length() - 1);
		conditions.append(")");
		
		query = QueryHolder.getQuery("ProvSE.GetSideEffect")
				.parameterize("target." + relName, conditions.toString());
		
		log.debug("Compute side effect query for\nrelname <" + relName + 
				">\nconditions <" + conditions + ">\nwith query:\n" + query);
		
		return query;
	}
	
	private String getUnNumRelName(String source) {
		if (!source.contains("_"))
			return source;
		return source.substring(0, source.lastIndexOf('_'));
	}

	private String getSideEffectEqualityCond (String source, ITupleMarker sourceErr) {
		return "prov_source_" + source + 
				"_tid IS DISTINCT FROM " + sourceErr.getTid();
	}

	
}
