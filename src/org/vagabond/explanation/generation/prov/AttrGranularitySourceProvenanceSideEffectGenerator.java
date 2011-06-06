package org.vagabond.explanation.generation.prov;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.vagabond.explanation.generation.QueryHolder;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.marker.ITupleMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.model.prov.MapAndWLProvRepresentation;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.mapping.model.MappingGraph;
import org.vagabond.util.ConnectionManager;
import org.vagabond.util.Pair;
import org.vagabond.util.ResultSetUtil;
import org.vagabond.xmlmodel.MappingType;

public class AttrGranularitySourceProvenanceSideEffectGenerator extends
		SourceProvenanceSideEffectGenerator {

	static Logger log = Logger.getLogger(
			AttrGranularitySourceProvenanceSideEffectGenerator.class);
	
	private Map<Pair<String,String>, int[][][]> mapSourceToTarget;
	private Map<String,Map<String,Vector<Integer>>> sourceSEtidToAttrs;
	
	public AttrGranularitySourceProvenanceSideEffectGenerator () {
		super();
		
		reset();
	}
	
	public void reset () {
		mapSourceToTarget = new HashMap<Pair<String,String>, int[][][]>();
		sourceSEtidToAttrs = new HashMap<String,Map<String,Vector<Integer>>>();
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
		createSourceSEAttrMap(partionedSE);
		relsForAffTarget = getRelAffectedByRels(partionedSE.keySet());
		
		log.debug("partioned source SE: " + partionedSE);
		log.debug("rels affected by source SE rels are: " + relsForAffTarget);
		log.debug("create sourceSE attr map: " + sourceSEtidToAttrs);
		
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
		
		result.remove(error);
		
		return result;
	}
	
	private void createSourceSEAttrMap (Map<String, IMarkerSet> partionedSE) {	
		Map<String, Vector<Integer>> tidToAttrs;
		IMarkerSet markers;
		IAttributeValueMarker marker;
		String tid;
		
		for(String rel: partionedSE.keySet()) {
			markers = partionedSE.get(rel);
			tidToAttrs = new HashMap<String, Vector<Integer>> ();
		
			for(Iterator<ISingleMarker> i = markers.iterator(); i.hasNext();) {
				marker = (IAttributeValueMarker) i.next();
				tid = marker.getTid();
				
				if (!tidToAttrs.containsKey(marker.getTid())) {
					tidToAttrs.put(tid, new Vector<Integer> ());
				}
				tidToAttrs.get(tid).add(marker.getAttrId());
			}
			
			sourceSEtidToAttrs.put(rel, tidToAttrs);
		}
	}
	
	protected void parseTargetSE(String rel, ResultSet rs, IMarkerSet sideEff)
			throws Exception {
		SourceAndMapProvParser parser;
		Vector<Pair<String,MapAndWLProvRepresentation>> parse;
		MapAndWLProvRepresentation curProv;
		Vector<ITupleMarker> wl;
		MappingType m;
		String tid;
		String wRelName;
		
		parser = new SourceAndMapProvParser(rs, rel);
		parse = parser.getAllProv();
		
		// for each affected target tid
		for(int i = 0; i < parse.size(); i++) {
			curProv = parse.get(i).getValue();
			tid = parse.get(i).getKey();
			log.debug("--------- for TID: " + tid);
			
			// iterate through all witness lists wl for this target tuple
			for(int j = 0; j < curProv.getWitnessLists().size(); j++) {
				wl = curProv.getWitnessList(j);
				m = curProv.getMapProv().get(j);
				
				log.debug("---- wl: " + wl + " and map " + m.getId());
				
				// for each tid in wl get sourceSE to determine source attrs
				// and use mapping attribute-map to know for which target attributes
				// markers should be created
				for(int k = 0; k < wl.size() ; k++) {
					ITupleMarker wlElem = wl.get(k);					
					
					if (wlElem != null) {
						log.debug("-- tup: " + wlElem);
						
						wRelName = curProv.getRelNames().get(k);
						int[][][] attrMap = getAttrMapping(rel, m.getId());
						Vector<Integer> sourceAttrPos = getSourceAttrPos
								(wRelName, wlElem.getTid()); 
						int atomPos = curProv.getMapToWlPos().get(m).indexOf(k);
						
						if (atomPos != -1 && sourceAttrPos != null) {
							for(Integer sAttrPos: sourceAttrPos) {
								int[] targetPos = attrMap[atomPos][sAttrPos];
								
								for(int tAttrPos: targetPos) {
									ISingleMarker newMark = MarkerFactory
											.newAttrMarker(rel, tid, tAttrPos);
									log.debug("add side effect: " + newMark);
									sideEff.add(newMark);
								}
							}
						}
					}
				}
			}
		}
		
	}
	
	private Vector<Integer> getSourceAttrPos (String relName, String tid) {
		if (sourceSEtidToAttrs.containsKey(relName))
			return sourceSEtidToAttrs.get(relName).get(tid);
			
		return null;
	}
	
	
	
	private int[][][] getAttrMapping (String relation, String mapping) throws Exception {
		Pair<String,String> key;
		MappingGraph g;
		
		key = new Pair<String,String> (relation,mapping);
		if (!mapSourceToTarget.containsKey(key)) {
			g = MapScenarioHolder.getInstance().getGraphForMapping(mapping);
			mapSourceToTarget.put(key, g.getAtomPosToTargetPosMap(relation));
		}
		
		return mapSourceToTarget.get(key);
	}
	
	@Override
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
						: sourceSE.get(unnumSource).getElems()) {
					conditions.append(
							getSideEffectEqualityCond(source, 
									 sourceErr)
							+ " OR ");
				}
			}
		}
		conditions.delete(conditions.length() - 4, conditions.length() - 1);
		conditions.append(")");
		
		query = QueryHolder.getQuery("ProvSE.GetSideEffectUsingAggPlusCompleteProv")
				.parameterize("target." + relName, conditions.toString(), 
						getProvTidAttrs(relName));
		
		log.debug("Compute side effect query for\nrelname <" + relName + 
				">\nconditions <" + conditions + ">\nwith query:\n" + query);
		
		return query;
	}

	@Override
	protected String getSideEffectEqualityCond (String source, ISingleMarker sourceErr) {
		return "prov_source_" + source + 
				"_tid = " + sourceErr.getTid();
	}

	private String getProvTidAttrs (String targetRel) throws Exception {
		StringBuffer result;
		Vector<String> attrs;
		
		result = new StringBuffer ();
		attrs = ResultSetUtil.getProvTidAttrsForProvSchema(ProvenanceGenerator
				.getInstance().getProvSchemaForTarget(targetRel));
		
		for(String attr: attrs) {
			if (result.length() != 0)
				result.append(',');
			result.append(attr);
		}
		
		return result.toString();
	}
	
}
