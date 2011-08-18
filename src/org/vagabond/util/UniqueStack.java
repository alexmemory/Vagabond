package org.vagabond.util;

import java.util.Collection;
import java.util.EmptyStackException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;


public class UniqueStack<T> extends java.util.Stack<T> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	Set<T> elemSet;
	Vector<T> stack;
	
	public UniqueStack () {
		init();
	}
	
	public UniqueStack (Collection<? extends T> newEl) {
		init();
		this.addAll(newEl);
	}
	
	private void init () {
		elemSet = new HashSet<T> ();
		stack = new Vector<T> ();
	}
	
	public T push (T elem) {
		if (!elemSet.contains(elem))
		{
			elemSet.add(elem);
			stack.add(elem);
		}
		return elem;
	}
	
	public boolean empty () {
		return stack.size() == 0;
	}
	
	public T pop () {
		T result;
		
		if(empty())
			throw new EmptyStackException();
		
		
		result = stack.lastElement();
		stack.removeElementAt(stack.size() - 1);
		elemSet.remove(result);
		
		return result;
	}
	
	public T peek () {
		if(empty())
			throw new EmptyStackException();
		
		return stack.lastElement();
	}
	
	public int search (Object elem) {
		return stack.indexOf(elem);
	}
	
	public boolean addAll (Collection<? extends T> newElems) {
		Iterator<? extends T> iter = newElems.iterator();
		
		while(iter.hasNext())
			push(iter.next());
		
		return newElems.size() != 0;
	}
}
