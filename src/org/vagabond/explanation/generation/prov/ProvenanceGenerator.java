package org.vagabond.explanation.generation.prov;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.vagabond.explanation.generation.QueryHolder;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.ITupleMarker;
import org.vagabond.explanation.model.prov.MapAndWLProvRepresentation;
import org.vagabond.explanation.model.prov.ProvWLRepresentation;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.util.CollectionUtils;
import org.vagabond.util.ConnectionManager;
import org.vagabond.util.Pair;
import org.vagabond.util.ResultSetUtil;
import org.vagabond.xmlmodel.MappingType;

public class ProvenanceGenerator {

	static Logger log = Logger.getLogger(ProvenanceGenerator.class);
	
	private static ProvenanceGenerator instance;
	
	static {
		instance = new ProvenanceGenerator();
	}
	
	private Map<String,Vector<Pair<String,Set<MappingType>>>> targetToRelMapMap;
	
	private ProvenanceGenerator () {
		targetToRelMapMap = new HashMap<String,Vector<Pair<String,Set<MappingType>>>> ();
	}
	
	public static ProvenanceGenerator getInstance() {
		return instance;
	}
	
	public void reset () {
		targetToRelMapMap = new HashMap<String,Vector<Pair<String,Set<MappingType>>>> ();
	}
	
	public Vector<String> computeMapProvAsStrings (IAttributeValueMarker error) 
			throws Exception {
		String query;
		ResultSet rs;
		Vector<String> maps;
		
		maps = new Vector<String>();
		
		query = QueryHolder.getQuery("MapAndTransProv.GetMapProv")
				.parameterize("target." + error.getRel(), error.getTid());
		log.debug("Compute MapProv for <" + error + "> with query:\n" + query);
		
		rs = ConnectionManager.getInstance().execQuery(query);
		
		while(rs.next())
			maps.add(rs.getString(1));
		
		ConnectionManager.getInstance().closeRs(rs);
		
		log.debug("Get map strings provenance for <" + error 
				+ "> returned <" + maps + ">");
		
		return maps;
	}
	
	public Set<MappingType> computeMapProv (IAttributeValueMarker error) 
			throws Exception {
		Set<MappingType> maps;
		Vector<String> mapStrings;
		
		mapStrings = computeMapProvAsStrings(error);
		maps = new HashSet<MappingType> ();
		
		for (String map: mapStrings)
			maps.add(MapScenarioHolder.getInstance().getMapping(map));
		
		log.debug("Get mapping provenance for <" + error 
				+ "> returned <" + maps + ">");
		
		return maps;
	}
	
	public ProvWLRepresentation computeCopyProvenance (IAttributeValueMarker error) 
			throws Exception {
		ResultSet rs;
		SourceProvParser parser;
		String query;
		
		query = getCopyCSQuery(error);
		log.debug("Parameterized copy source explanation query for <" 
				+ error + ">:\n" + query);
		
		rs = ConnectionManager.getInstance().execQuery(query);
		parser = new SourceProvParser(rs);
		ConnectionManager.getInstance().closeRs(rs);
		
		return parser.getAllProv();
	}
		
		
	private String getCopyCSQuery (IAttributeValueMarker error) {
		String table = error.getRel();
		String tid = error.getTid();
		String attr = error.getAttrName();
		
		return QueryHolder.getQuery("CopyCS.GetProv").
				parameterize("target." + table, tid, attr);
	}
	
	public ProvWLRepresentation computePIProv (IAttributeValueMarker error)
			throws Exception {
		String query;
		ResultSet rs;
		SourceProvParser parser;
		ProvWLRepresentation prov;
		
		query = QueryHolder.getQuery("InfluenceCS.GetProv")
				.parameterize("target." + error.getRel(), error.getTid(), 
						error.getAttrName());
		
		rs = ConnectionManager.getInstance().execQuery(query);
		
		parser = new SourceProvParser(rs);
		prov = parser.getAllProv();
		
		log.debug("compute prov for <" + error + ">:\n" + prov);
		
		ConnectionManager.getInstance().closeRs(rs);
		
		return prov;
	}
	
	public Vector<Set<MappingType>> getWlPosToMapping (String targetRel) 
			throws Exception {
		return Pair.pairVecToValueVec(getBaseRelAccessToMapping(targetRel));
	}
	
	public Vector<String> getWlPosToBaseRelName (String targetRel) 
			throws Exception {
		return Pair.pairVecToKeyVec(getBaseRelAccessToMapping(targetRel));
	}
	
	public Vector<Pair<String,Set<MappingType>>> getBaseRelAccessToMapping 
			(String targetRel) throws Exception {
		Vector<Pair<String,Set<MappingType>>> result;
		String query;
		String parse;
		ResultSet rs;
		
		if (targetToRelMapMap.containsKey(targetRel))
			return targetToRelMapMap.get(targetRel); 
		
		result = new Vector<Pair<String,Set<MappingType>>>();
		
		query = QueryHolder.getQuery("MetaQ.GetMapsForBaseRelAccess")
				.parameterize("target." + targetRel);
		
		rs = ConnectionManager.getInstance().execQuery(query);
		
		if (!rs.next())
			throw new Exception("query returned zero tuples");
		
		parse = rs.getString(1);
		
		// parse the rel1:M1,M2|rel2:M3| ... format
		for (String entry: parse.split("\\|")) {
			String rel;
			String maps;
			Set<MappingType> value;
			
			rel = entry.substring(entry.indexOf('.') + 1, entry.indexOf(':'));
			maps = entry.substring(entry.indexOf(':') + 1);
			value = new HashSet<MappingType> ();
			result.add(new Pair<String,Set<MappingType>>(rel, value));
			
			for (String map: maps.split(",")) {
				value.add(MapScenarioHolder.getInstance().getMapping(map));
			}
		}
		
		log.debug("compute base rel access to mapping map for <" + targetRel + ">:\n"
				+ result); 
		
		ConnectionManager.getInstance().closeRs(rs);
		
		targetToRelMapMap.put(targetRel, result);
		return result;
	}
	
	public String[] getProvSchemaForTarget (String targetRel) throws Exception {
		String query;
		String resultStr;
		ResultSet rs;
		
		
		query = QueryHolder.getQuery("MetaQ.GetProvQueryResultAttrs")
				.parameterize("target." + targetRel);
		
		log.debug("compute provenance schema for <" + targetRel 
				+ "> with query: <" + query + ">");
		
		rs = ConnectionManager.getInstance().execQuery(query);
		
		rs.next();
		resultStr = rs.getString(1).trim();
		resultStr = resultStr.substring(1, resultStr.length() - 1);
		
		ConnectionManager.getInstance().closeRs(rs);

		return resultStr.split(",");
	}
	
	public MapAndWLProvRepresentation computePIAndMapProv 
			(IAttributeValueMarker error) throws Exception {
		MapAndWLProvRepresentation result;
		Vector<Set<MappingType>> relMapMap;
		Map<MappingType, Vector<Integer>> mapPos;
		Set<MappingType> allMaps;
		
		result = new MapAndWLProvRepresentation(computePIProv(error));
		relMapMap = getWlPosToMapping(error.getRel());
		allMaps = CollectionUtils.<MappingType>unionSets(relMapMap);
		
		for(Vector<ITupleMarker> wl: result.getWitnessLists())
			result.addMapProv(computMapProvFromWL(wl, relMapMap, allMaps));
			
		mapPos = new HashMap<MappingType, Vector<Integer>> ();
		for(int i = 0; i < relMapMap.size(); i++) {
			for(MappingType map: relMapMap.get(i)) {
				if (!mapPos.containsKey(map))
					mapPos.put(map, new Vector<Integer>());
				mapPos.get(map).add(i);
			}
		}
		result.setMapToWlPos(mapPos);
		
		return result;
	}
	
	public MappingType computMapProvFromWL (Vector<ITupleMarker> wl, 
			Vector<Set<MappingType>> relMapMap,
			Set<MappingType> allMaps) {
		ITupleMarker tid;
		Set<MappingType> mapset = new HashSet<MappingType> (allMaps);
		
		for(int i =0; i < wl.size(); i++) {
			tid = wl.get(i);
			
			if(tid != null)
				mapset.retainAll(relMapMap.get(i));
			else
				mapset.removeAll(relMapMap.get(i));
		}
		
		assert(mapset.size() == 1);
		
		return mapset.iterator().next();
	}
}
