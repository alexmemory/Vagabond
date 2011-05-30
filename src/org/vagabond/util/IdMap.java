package org.vagabond.util;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class IdMap<Type> {

	private SortedMap<Integer, Type> idToObj;
	private Map<Type, Integer> ObjToId;
	private int maxId = -1;
	private int hash = -1;
	
	public IdMap () {
		idToObj = new TreeMap<Integer, Type> ();
		ObjToId = new HashMap<Type, Integer> ();
	}
	
	public void put (int id, Type value) {
		idToObj.put(id, value);
		ObjToId.put(value, id);
		maxId = Math.max(id, maxId);
	}
	
	public void put (Type value) {
		maxId++;
		put(maxId, value);
	}
	
	public boolean containsKey (int id) {
		return idToObj.containsKey(id);
	}
	
	public boolean containsValue (Type value) {
		return ObjToId.get(value) != null;
	}
	
	public Type get (int id) {
		return idToObj.get(id);
	}
	
	public int get (Type value) {
		return ObjToId.get(value);
	}
	
	public int getSize () {
		return idToObj.size();
	}
	
	public int getMaxId () {
		return maxId;
	}
	
	@Override
	public boolean equals (Object other) {
		if (other == null)
			return false;
		
		if (this == other)
			return false;
		
		if (! (other instanceof IdMap<?>))
			return false;
		
		IdMap<?> oMap = (IdMap<?>) other;
		
		if (this.hashCode() != oMap.hashCode())
			return false;
		
		if (this.maxId != oMap.maxId)
			return false;
		
		if (! this.idToObj.equals(oMap.idToObj))
			return false;
		
		if (! this.ObjToId.equals(oMap.ObjToId))
			return false;
		
		return true;
	}
	
	@Override
	public int hashCode() {
		if (hash == -1) {
			hash = maxId;
			hash = hash * 13 + idToObj.hashCode();
		}
		
		return hash;
	}
}
