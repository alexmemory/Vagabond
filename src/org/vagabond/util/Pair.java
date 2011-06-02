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
	
	public static <T,K> Collection<T> pairColToValueCol 
			(Collection<Pair<K,T>> pairCol) {
		Collection<T> result;
		
		result = new Vector<T>();
		
		for(Pair<?,T> pair: pairCol)
			result.add(pair.getValue());
		
		return result;
	}
}
