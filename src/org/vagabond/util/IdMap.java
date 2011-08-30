package org.vagabond.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public class IdMap<Type> implements Map<Integer,Type>, Iterator<Type> {

	protected SortedMap<Integer, Type> idToObj;
	protected Map<Type, Integer> ObjToId;
	private int maxId = -1;
	private int hash = -1;
	private int iter = -1;
	
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
	
	public boolean containsVal (Type value) {
		return ObjToId.get(value) != null;
	}
	
	public int size() {
		return idToObj.keySet().size();
	}
	
	public Type get (int id) {
		return idToObj.get(id);
	}
	
	public int getId (Type value) {
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

	@Override
	public boolean hasNext() {
		return iter < maxId;
	}

	@Override
	public Type next() {
		return idToObj.get(++iter);
	}
	
	public void resetIter () {
		iter = -1;
	}

	@Override
	public void remove() {
		//TODO nothing
	}

	@Override
	public void clear() {
		idToObj.clear();
		ObjToId.clear();
		maxId = -1;
	}

	@Override
	public boolean containsKey(Object key) {
		return idToObj.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return ObjToId.containsKey(value);
	}

	@Override
	public Set<java.util.Map.Entry<Integer, Type>> entrySet() {
		return idToObj.entrySet();
	}

	@Override
	public Type get(Object key) {
		return idToObj.get(key);
	}

	@Override
	public boolean isEmpty() {
		return idToObj.isEmpty();
	}

	@Override
	public Set<Integer> keySet() {
		return idToObj.keySet();
	}

	@Override
	public Type put(Integer key, Type value) {
		ObjToId.put(value, key);
		return idToObj.put(key, value);
	}

	@Override
	public void putAll(Map<? extends Integer, ? extends Type> m) {
		for(Integer i: m.keySet()) {
			Type val = m.get(i);
			idToObj.put(i, val);
			ObjToId.put(val, i);
			maxId = (maxId > i) ? maxId: i;
		}
	}

	@Override
	public Type remove(Object key) {
		Type val;
		
		val = idToObj.get(key);
		if (val != null) {
			ObjToId.remove(val);
			idToObj.remove(key);
		}
	
		return val;
	}

	@Override
	public Collection<Type> values() {
		return ObjToId.keySet();
	}
}