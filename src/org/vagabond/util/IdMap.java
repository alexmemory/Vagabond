package org.vagabond.util;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class IdMap<Type> {

	private SortedMap<Integer, Type> idToObj;
	private Map<Type, Integer> ObjToId;
	private int maxId = -1;
	
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
}
