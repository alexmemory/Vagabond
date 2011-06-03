package org.vagabond.explanation.model.prov;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.vagabond.explanation.marker.ITupleMarker;
import org.vagabond.util.Pair;
import org.vagabond.xmlmodel.MappingType;
import static org.vagabond.util.LoggerUtil.ObjectColToStringWithMethod;
import static org.vagabond.util.LoggerUtil.logException;

public class MapAndWLProvRepresentation extends ProvWLRepresentation {

	static Logger log = Logger.getLogger(MapAndWLProvRepresentation.class);
	
	private Vector<MappingType> mapProv;
	private Map<MappingType, Vector<Integer>> mapToWlPos;
	private Set<MappingType> allMaps;
	private int hash = -1;
	
	public MapAndWLProvRepresentation () {
		super();
		
		init();
	}
	
	public MapAndWLProvRepresentation (ProvWLRepresentation prov) {
		this.relNames = prov.relNames;
		this.tuplesInProv = prov.tuplesInProv;
		this.witnessLists = prov.witnessLists;
		
		init();
	}
	
	private void init () {
		mapProv = new Vector<MappingType> ();
		mapToWlPos = new HashMap<MappingType, Vector<Integer>> ();
		allMaps = new HashSet<MappingType> ();
	}
	

	public Vector<MappingType> getMapProv() {
		return mapProv;
	}

	public void setMapProv(Vector<MappingType> mapProv) {
		this.mapProv = mapProv;
		allMaps = new HashSet<MappingType> (mapProv);
	}

	public void addMapProv(MappingType map) {
		mapProv.add(map);
		allMaps.add(map);
	}
	
	public Pair<Vector<ITupleMarker>, MappingType> getProvAndMap (int i) {
		return new Pair<Vector<ITupleMarker>, MappingType>
				(witnessLists.get(i), mapProv.get(i));
	}
	
	public Vector<Pair<Vector<ITupleMarker>, MappingType>> getAllProvAndMap () {
		Vector<Pair<Vector<ITupleMarker>, MappingType>> result;
		
		result = new Vector<Pair<Vector<ITupleMarker>, MappingType>> ();
		
		for(int i = 0; i < mapProv.size(); i++)
			result.add(new Pair<Vector<ITupleMarker>, MappingType>
					(witnessLists.get(i), mapProv.get(i)));
		
		return result;
	}
	
	public Map<MappingType, Vector<Integer>> getMapToWlPos() {
		return mapToWlPos;
	}

	public void setMapToWlPos(Map<MappingType, Vector<Integer>> mapToWlPos) {
		this.mapToWlPos = mapToWlPos;
	}
	
	public void addMapToWlPos(MappingType map, Vector<Integer> pos) {
		mapToWlPos.put(map, pos);
	}
	
	public Vector<Integer> getMapToWlPosPositions (MappingType map) {
		return mapToWlPos.get(map);
	}

	public Set<MappingType> getAllMaps() {
		return allMaps;
	}

	public void setAllMaps(Set<MappingType> allMaps) {
		this.allMaps = allMaps;
	}

	@Override
	public String toString () {
		String result;
		
		result = super.toString();
		try {
			result += "\n\tmap prov <" + 
					ObjectColToStringWithMethod(mapProv, MappingType.class,"getId") 
					+ ">" ;
		} catch (Exception e) {
			logException(e, log);
		}
		
		return result;
	}

	@Override
	public int hashCode () {
		if (hash == -1) { 
			hash = super.hashCode();
			hash = 13 * hash + mapProv.hashCode();
		}
		
		return hash;
	}

	@Override
	public boolean equals (Object other) {
		if (other == null)
			return false;
		
		if (other == this)
			return true;
		
		if (! (other instanceof MapAndWLProvRepresentation))
			return false;
		
		MapAndWLProvRepresentation oMap = (MapAndWLProvRepresentation) other;
		
		if (! super.equals(oMap))
			return false;
		
		return this.mapProv.equals(oMap.mapProv);
	}
	
}
