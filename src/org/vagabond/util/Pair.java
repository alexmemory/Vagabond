package org.vagabond.util;

import java.util.Collection;
import java.util.Vector;

public class Pair<K,V> {

	private K key;
	private V value;
	private int hash = -1;
	
	public Pair () {
		
	}
	
	public Pair (K key, V value) {
		this.key = key;
		this.value = value;
	}
	
	public void setKey (K key) {
		this.key = key;
	}
	
	public void setValue (V value) {
		this.value = value;
	}

	public K getKey() {
		return key;
	}

	public V getValue() {
		return value;
	}
	
	@Override
	public int hashCode () {
		if (hash == -1) {
			hash = key.hashCode();
			hash = hash * 13 + value.hashCode();
		}
		
		return hash;
	}
	
	@Override
	public boolean equals (Object other) {
		if (other == null)
			return false;
		
		if (this == other)
			return true;
		
		if (!(other instanceof Pair<?,?>))
			return false;
		
		Pair<?,?> oPair = (Pair<?,?>) other;
		
		if (!oPair.key.equals(this.key))
			return false;
		
		return oPair.value.equals(this.value);
	}
	
	@Override
	public String toString () {
		return "[" + key.toString() + "," + value.toString() + "]";
	}
	
	public static <K,V> Collection<V> pairColToValueCol 
			(Collection<Pair<K,V>> pairCol) {
		Collection<V> result;
		
		result = new Vector<V>();
		
		for(Pair<K,V> pair: pairCol)
			result.add(pair.getValue());
		
		return result;
	}
	
	public static <K,V> Vector<V> pairVecToValueVec (Vector<Pair<K,V>> pairVec) {
		Vector<V> result;
		
		result = new Vector<V> ();
		
		for(Pair<K,V> pair: pairVec) {
			result.add(pair.getValue());
		}
		
		return result;
	}
	
	public static <K,V> Vector<K> pairVecToKeyVec (Vector<Pair<K,V>> pairVec) {
		Vector<K> result;
		
		result = new Vector<K> ();
		
		for(Pair<K,V> pair: pairVec) {
			result.add(pair.getKey());
		}
		
		return result;
	}
}
