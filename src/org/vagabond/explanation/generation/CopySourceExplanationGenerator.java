package org.vagabond.explanation.generation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.marker.ITupleMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.model.ExplanationFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.CopySourceError;
import org.vagabond.explanation.model.prov.CopyProvExpl;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.util.ConnectionManager;
import org.vagabond.util.ResultSetUtil;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.RelAtomType;
import org.vagabond.xmlmodel.RelationType;

public class CopySourceExplanationGenerator implements
		ISingleExplanationGenerator {

	static Logger log = Logger.getLogger(CopySourceExplanationGenerator.class);
	
	private CopyProvExpl prov;
	private IAttributeValueMarker error;
	private CopySourceError expl;
	
	@Override
	public IExplanationSet findExplanations(ISingleMarker errorMarker) throws Exception {
		this.error = (IAttributeValueMarker) errorMarker;
		
		return getExplanationSets();
	}
	
	private IExplanationSet getExplanationSets() throws Exception {
		IExplanationSet result = ExplanationFactory.newExplanationSet();
		
		retrieveCopyProvenance();
		
		expl = new CopySourceError(error);
		expl.setSourceSE(prov.getTuplesInProv());
		expl.setTargetSE(computeTargetSideEffects());
		
		log.debug("Generated Explanation:\n" + expl.toString());
		
		result.addExplanation(expl);
		
		log.debug("Generated Explanation:\n" + expl.toString());
		
		return result;
	}
	
	private void retrieveCopyProvenance () 
			throws Exception {
		ResultSet rs;
		CopyCSParser parser;
		String query;
		
		query = getCopyCSQuery();
		log.debug("Parameterized copy source explanation query for <" 
				+ error + ">:\n" + query);
		
		rs = ConnectionManager.getInstance().execQuery(query);
		parser = new CopyCSParser(rs);
		ConnectionManager.getInstance().closeRs(rs);
		
		this.prov = parser.getAllProv();
	}

	private IMarkerSet computeTargetSideEffects() throws Exception {
		String query;
		ResultSet rs;
		IMarkerSet result = MarkerFactory.newMarkerSet();
		Map<String, Set<String>> relsForAffTarget;
		Map<String, IMarkerSet> partionedSE;
		
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
				(IAttributeValueMarker) expl.explains()));
		
		return result;
	}
	
	private Set<String> getNumRelForTrans (String targetRel) throws SQLException, ClassNotFoundException {
		List<String> rels;
		String query;
		ResultSet rs;
		String result;
		
		query = QueryHolder.getQuery("CopyCS.GetProvQueryResultAttrs")
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
//							for(RelAtomType oneSource: map.getForeach().getAtomArray()) {
//								sources.add(oneSource.getTableref());
//							}
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
		for(ISingleMarker marker: expl.getSourceSE()) {
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
//		StringBuffer provTidAttrs;
		
		conditions = new StringBuffer();
//		provTidAttrs = new StringBuffer();
		
		conditions.append("(");
		for(String source: sourceRels) {
			unnumSource = getUnNumRelName(source);
//			provTidAttrs.append("prov_source_" + source + "_tid,");
			if (sourceSE.get(unnumSource) != null) {
				for(ISingleMarker sourceErr: sourceSE.get(source).getElems()) {
					conditions.append(
							getSideEffectEqualityCond(source, 
									(ITupleMarker) sourceErr)
							+ " AND ");
				}
			}
		}
//		provTidAttrs.deleteCharAt(provTidAttrs.length() - 1);
		conditions.delete(conditions.length() - 5, conditions.length() - 1);
		conditions.append(")");
		
		query = QueryHolder.getQuery("CopyCS.GetSideEffect")
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

	private String getCopyCSQuery () {
		String table = error.getRelName();
		String tid = error.getTid();
		String attr = error.getAttrName();
		
		return QueryHolder.getQuery("CopyCS.GetProv").
				parameterize("target." + table, tid, attr);
	}
	
	
}
