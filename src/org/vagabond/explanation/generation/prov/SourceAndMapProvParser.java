package org.vagabond.explanation.generation.prov;

import java.sql.ResultSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.vagabond.explanation.marker.ITupleMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.model.prov.MapAndWLProvRepresentation;
import org.vagabond.util.CollectionUtils;
import org.vagabond.util.Pair;
import org.vagabond.util.ResultSetUtil;
import org.vagabond.xmlmodel.MappingType;

public class SourceAndMapProvParser {
	
	static Logger log = Logger.getLogger(SourceAndMapProvParser.class);
	
	private ResultSet dbResult;
	private Vector<Pair<String,MapAndWLProvRepresentation>> allProv;
	private Vector<Pair<String, Set<MappingType>>> relMapMap;
	private List<String> baseRels;
	private Vector<String> unnumRels;
	
	public SourceAndMapProvParser (ResultSet result, String targetRel) throws Exception {
		this.dbResult = result;
		allProv = new Vector<Pair<String, MapAndWLProvRepresentation>>();
		relMapMap = ProvenanceGenerator.getInstance()
				.getBaseRelAccessToMapping(targetRel);
		baseRels = Pair.pairVecToKeyVec(relMapMap);
		
		unnumRels = new Vector<String> ();
		for(int i = 0; i < baseRels.size(); i++)
			unnumRels.add(i,ResultSetUtil.getUnnumRelFromRel(baseRels.get(i)));
		
		createTupleAndWLSet();
		createMapProv (targetRel);
	}
	
	private void createTupleAndWLSet () throws Exception {
		Vector<ITupleMarker> witList;
		ITupleMarker tup;
		String curTid = null;
		String pTid;
		MapAndWLProvRepresentation curProv = null;
		
		while(dbResult.next()) {
			witList = new Vector<ITupleMarker> ();
			// new target tuple, create new ProvRep
			if (!dbResult.getString(1).equals(curTid)) {
				curTid = dbResult.getString(1);
				curProv = new MapAndWLProvRepresentation();
				curProv.setRelNames(baseRels);
				allProv.add(new Pair<String, MapAndWLProvRepresentation>
						(curTid, curProv));
			}
			
			// add to current prov rep
			for (int i = 2; i <= dbResult.getMetaData().getColumnCount(); i++) {
				pTid = dbResult.getString(i);
				log.debug("parsed tid <" + pTid + ">");
				
				if (pTid != null) {
					tup = MarkerFactory.newTupleMarker(unnumRels.get(i - 2), pTid);
					log.debug("add tuple marker " + tup);
					witList.add(tup);	
					curProv.addTupleInProv(tup);
				}
				else
					witList.add(null);
			}
			
			log.debug("created witness list " + witList);
			curProv.addWitnessList(witList);
		}
	}

	private void createMapProv (String targetRel) throws Exception {
		Vector<Pair<String, Set<MappingType>>> relMapMap;
		MapAndWLProvRepresentation prov;
		Collection<Set<MappingType>> maps;
		Set<MappingType> allMaps;
		Map<MappingType, Vector<Integer>> mapPos;
		
		relMapMap = ProvenanceGenerator.getInstance()
				.getBaseRelAccessToMapping(targetRel);
		maps = Pair.<String,Set<MappingType>>pairColToValueCol(relMapMap);
		allMaps = CollectionUtils.<MappingType>unionSets(maps);	
		
		mapPos = new HashMap<MappingType, Vector<Integer>> ();
		for(int i = 0; i < relMapMap.size(); i++) {
			for(MappingType map: relMapMap.get(i).getValue()) {
				if (!mapPos.containsKey(map))
					mapPos.put(map, new Vector<Integer>());
				mapPos.get(map).add(i);
			}
		}
		
		for(int i = 0; i < allProv.size(); i++) {
			prov = allProv.get(i).getValue();
			prov.setMapToWlPos(mapPos);
			
			for(Vector<ITupleMarker> wl: prov.getWitnessLists()) {
				MappingType map = ProvenanceGenerator.getInstance()
						.computMapProvFromWL(wl, relMapMap, allMaps);
				prov.addMapProv(map);
			}
		}
	}
	
	
	
	public Vector<Pair<String,MapAndWLProvRepresentation>> getAllProv() {
		return allProv;
	}

	
}
