package org.vagabond.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
	
	public static int product (int[] in) {
		int result = 1;
		
		for(int i = 0; i < in.length; i++)
			result *= in[i];
		
		return result;
	}
	
	public static int product (Collection<Integer> in) {
		int result = 1;
		
		for(int i: in)
			result *= i;
		
		return result;
	}
	
	public static int sum (int[] in) {
		int result = 0;
		
		for(int i = 0; i < in.length; i++)
			result += in[i];
		
		return result;
	}
	
	public static <T> int compareSet (Set<T> s1, Set<T> s2, Comparator<? super T> elemComp) {
		return compareCollection(s1,s2,elemComp);
	}
	
	public static <T> int compareCollection (Collection<T> s1, Collection<T> s2, Comparator<? super T> elemComp) {
		List<T> sort1, sort2;
		
		int comp = s1.size() - s2.size();
		if (comp != 0)
			return comp;
		
		sort1 = new ArrayList<T> (s1);
		sort2 = new ArrayList<T> (s2);
		
		Collections.sort(sort1, elemComp);
		Collections.sort(sort2, elemComp);
		
		for(int i = 0; i < sort1.size(); i++) {
			T l,r;
			l = sort1.get(i);
			r = sort2.get(i);
			comp = elemComp.compare(l,r);
			if(comp != 0)
				return comp;
		}
		
		return 0;
	}
}
