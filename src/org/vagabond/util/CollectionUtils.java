package org.vagabond.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import org.apache.log4j.Logger;

public class CollectionUtils {

	static Logger log = LogProviderHolder.getInstance().getLogger(CollectionUtils.class);
	
	public static <T> Set<T> unionSets (Collection<Set<T>> sets) {
		Set<T> result = new HashSet<T>();
		
		for(Set<T> set: sets)
			result.addAll(set);
		
		return result;
	}
	
	public static <T> Set<T> unionSets (Set<T> ... elems) {
		Set<T> result = new HashSet<T>();
		
		for(Set<T> set: elems)
			result.addAll(set);
		
		return result;
	}
	
	public static <T> Set<T> makeSet (T ... elems) {
		Set<T> result;
		
		result = new HashSet<T> (elems.length);
		
		for(T elem: elems)
			result.add(elem);
		
		return result;
	}
	
	public static <T> Vector<T> makeVec (T ... elems) {
		return new Vector<T> (Arrays.asList(elems)); 
	}
	
	public static <T> Vector<T> makeVecFromArray (T[] array) {
		return new Vector<T> (Arrays.asList(array));
	}
	
	public static <T> List<T> makeList (T ... elems) {
		return Arrays.asList(elems);
	}
	
	public static <T> int linearSearch (T[] array, T elem) {
		for(int i = 0; i < array.length; i++) {
			if (array[i].equals(elem))
				return i;
		}
		
		return -1;
	}
	
	public static <T> Stack<T> makeStack (Collection<T> col) {
		Stack<T> result;
		
		result = new Stack<T> ();
		for(T elem: col)
			result.push(elem);
		
		return result;
	}
	
	public static <T> boolean search (T[] array, T element) {
		for(T test: array) {
			if (test.equals(element))
				return true;
		}
		
		return false;
	}
	

	
	public static <T> List<T> filter (List<T> in, int[] positions) {
		List<T> result;
		
		result = new ArrayList<T> ();
		
		for(int i: positions)
			result.add(in.get(i));
		
		return result;
	}
	
	public static <T> List<T> filter (IdMap<T> in, int[] positions) {
		List<T> result;
		
		result = new ArrayList<T> ();
		
		for(int i: positions)
			result.add(in.get(i));
		
		return result;
	}
	
	public static <T> List<T> filter (IdMap<T> in, Iterator<Integer> pos) {
		List<T> result;
		
		result = new ArrayList<T> ();
		
		while(pos.hasNext())
			result.add(in.get(pos.next()));
		
		return result;
	}

	public static <T> void addToList (List<T> list, T newElem, int pos) {
		while(list.size() <= pos) {
			list.add(null);
		}
		list.set(pos, newElem);
	}
	
}
