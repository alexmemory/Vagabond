package org.vagabond.explanation.generation.prov;

import java.sql.ResultSet;
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
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.util.ConnectionManager;
import org.vagabond.util.LogProviderHolder;
import org.vagabond.util.ResultSetUtil;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.RelAtomType;

public class SourceProvenanceSideEffectGenerator implements ISideEffectGenerator 
		{

	public static Logger log = LogProviderHolder.getInstance().getLogger(
			SourceProvenanceSideEffectGenerator.class);
	
	protected IAttributeValueMarker error;
	protected IMarkerSet sourceSE;
	
	@Override
	public void reset () {
		
	}
	
	@Override
	public IMarkerSet computeTargetSideEffects(IMarkerSet sourceSE, 
			IAttributeValueMarker error) throws Exception {
		String query;
		ResultSet rs;
		IMarkerSet result = MarkerFactory.newMarkerSet();
		Map<String, Set<String>> relsForAffTarget;
		Map<String, IMarkerSet> partionedSE;
		
		this.sourceSE = sourceSE;
		this.error = error;
		
		partionedSE = partitionSourceSE();
		if (log.isDebugEnabled()) {log.debug("partioned source SE: " + partionedSE);};
		relsForAffTarget = getRelAffectedByRels(partionedSE.keySet());
		if (log.isDebugEnabled()) {log.debug("rels affected by source SE rels are: " + relsForAffTarget);};
		
		for(String targetRel: relsForAffTarget.keySet()) {
			query = getSideEffectQuery(targetRel, 
					relsForAffTarget.get(targetRel), 
					partionedSE);
			
			if (log.isDebugEnabled()) {log.debug("Compute side effects for target relation <"
					+ targetRel + "> using query:\n" + query);};
			
			rs = ConnectionManager.getInstance().execQuery(query);
			parseTargetSE(targetRel, rs, result);
			ConnectionManager.getInstance().closeRs(rs);
		}
		
		result.remove(error);
		
		return result;
	}
	
	private Set<String> getNumRelForTrans (String targetRel) throws Exception {
		List<String> rels;
		
		rels  = ResultSetUtil.getBaseRelsForProvSchema(ProvenanceGenerator
				.getInstance().getProvSchemaForTarget(targetRel));
		
		return new HashSet<String> (rels);
	}

	protected void parseTargetSE(String rel, ResultSet rs, IMarkerSet sideEff)
			throws Exception {
		while(rs.next()) {
			sideEff.add(MarkerFactory.newTupleMarker(rel, 
					rs.getString(1)));
		}
	}

	protected Map<String, Set<String>> getRelAffectedByRels 
			(Collection<String> inputRels) throws Exception {
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
		
		if (log.isDebugEnabled()) {log.debug("Relations affected by <" + inputRels + "> are:\n" + result);};
		
		return result;
	}
	
	protected Map<String, IMarkerSet> partitionSourceSE () {
		Map<String, IMarkerSet> parts;
		IMarkerSet mSet;
		String relName;
		
		parts = new HashMap<String, IMarkerSet> ();
		for(ISingleMarker marker: sourceSE) {
			relName = marker.getRel();
			mSet = parts.get(relName);
			if (mSet == null) {
				mSet = MarkerFactory.newMarkerSet();
				parts.put(relName, mSet);
			}
			mSet.add(marker);
		}
		
		if (log.isDebugEnabled()) {log.debug("paritioned source side effects into:\n" + parts);};
		
		return parts;
	}

	public String getSideEffectQuery (String relName, Set<String> sourceRels, 
			Map<String, IMarkerSet> sourceSE) throws Exception {
		StringBuffer conditions;
		String query;
		String unnumSource;
		
		conditions = new StringBuffer();
		
		conditions.append("(");
		for(String source: sourceRels) {
			unnumSource = getUnNumRelName(source);
			if (sourceSE.get(unnumSource) != null) {
				for(ISingleMarker sourceErr
						: sourceSE.get(unnumSource).getElems()) {//CHECK ok to use unnumSource???
					conditions.append(
							getSideEffectEqualityCond(source, 
									 sourceErr)
							+ " AND ");
				}
			}
		}
		conditions.delete(conditions.length() - 5, conditions.length() - 1);
		conditions.append(")");
		
		query = QueryHolder.getQuery("ProvSE.GetSideEffect")
				.parameterize("target." + relName, conditions.toString());
		
		if (log.isDebugEnabled()) {log.debug("Compute side effect query for\nrelname <" + relName + 
				">\nconditions <" + conditions + ">\nwith query:\n" + query);};
		
		return query;
	}
	
	protected String getUnNumRelName(String source) {
		if (!source.contains("_"))
			return source;
		return source.substring(0, source.lastIndexOf('_'));
	}

	protected String getSideEffectEqualityCond (String source, ISingleMarker sourceErr) {
		return "prov_source_" + source + 
				"_tid IS DISTINCT FROM " + sourceErr.getTid();
	}

	
}
