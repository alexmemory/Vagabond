package org.vagabond.explanation.marker;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class MarkerSummary implements Set<ISchemaMarker> {

	public Set<ISchemaMarker> schemaMarkers;
	
	public MarkerSummary () {
		schemaMarkers = new HashSet<ISchemaMarker> ();
	}
	
	public boolean add (ISchemaMarker newMarker) {
		return schemaMarkers.add(newMarker);
	}

	@Override
	public boolean addAll(Collection<? extends ISchemaMarker> c) {
		return schemaMarkers.addAll(c);
	}

	@Override
	public void clear() {
		schemaMarkers.clear();
	}

	@Override
	public boolean contains(Object o) {
		return schemaMarkers.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return schemaMarkers.containsAll(c);
	}

	@Override
	public boolean isEmpty() {
		return schemaMarkers.isEmpty();
	}

	@Override
	public Iterator<ISchemaMarker> iterator() {
		return schemaMarkers.iterator();
	}

	@Override
	public boolean remove(Object o) {
		return schemaMarkers.remove(o);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return schemaMarkers.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return retainAll(c);
	}

	@Override
	public int size() {
		return schemaMarkers.size();
	}

	@Override
	public Object[] toArray() {
		return schemaMarkers.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return schemaMarkers.toArray(a);
	}
	
	@Override
	public String toString () {
		return schemaMarkers.toString();
	}
}
