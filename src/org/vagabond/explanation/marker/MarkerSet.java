package org.vagabond.explanation.marker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger; import org.vagabond.util.LogProviderHolder;

public class MarkerSet implements IMarkerSet {

	static Logger log = LogProviderHolder.getInstance().getLogger(MarkerSet.class);
	
	private Set<ISingleMarker> markers;
	
	public MarkerSet () {
		markers = new HashSet<ISingleMarker> ();
	}
	
	@Override
	public boolean equals (Object other) {
		if (other == null)
			return false;
		
		if (other == this)
			return true;
		
		if (other instanceof IMarkerSet) {
			IMarkerSet oMarker = (IMarkerSet) other;
			
			return markers.equals(oMarker.getElems());
		}
		
		return false;
	}
	
	@Override
	public int getSize() {
		int size = 0;
		
		for (ISingleMarker marker: markers)
			size += marker.getSize();
		
		return size;
	}

	@Override
	public int getNumElem() {
		return markers.size();
	}

	@Override
	public Set<ISingleMarker> getElems() {
		return markers;
	}
	
	@Override
	public List<ISingleMarker> getElemList() {
		return new ArrayList<ISingleMarker> (markers);
	}

	@Override
	public IMarkerSet union(IMarkerSet other) {
		if (other.getElems() != null)
			this.markers.addAll(other.getElems());
		return this;
	}

	@Override
	public boolean add(ISingleMarker marker) {
		return markers.add(marker);
	}

	@Override
	public String toString () {
		StringBuffer result = new StringBuffer();
		result.append("MarkerSet: {");
		
		for (ISingleMarker marker: markers) {
			result.append(marker.toString() + ",");
		}
		result.deleteCharAt(result.length() - 1);
		
		result.append("}");
		
		return result.toString();
	}

	@Override
	public boolean addAll(Collection<? extends ISingleMarker> arg0) {
		return markers.addAll(arg0);
	}

	@Override
	public void clear() {
		markers.clear();
	}

	@Override
	public boolean contains(Object arg0) {
		return markers.contains(arg0);
	}

	@Override
	public boolean containsAll(Collection<?> arg0) {
		return markers.containsAll(arg0);
	}

	@Override
	public boolean isEmpty() {
		return markers.isEmpty();
	}

	@Override
	public Iterator<ISingleMarker> iterator() {
		return markers.iterator();
	}

	@Override
	public boolean remove(Object arg0) {
		return markers.remove(arg0);
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		return markers.removeAll(arg0);
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		return markers.retainAll(arg0);
	}

	@Override
	public int size() {
		return markers.size();
	}

	@Override
	public Object[] toArray() {
		return markers.toArray();
	}

	@Override
	public <T> T[] toArray(T[] arg0) {
		return markers.toArray(arg0);
	}

	@Override
	public boolean contains(String relName, String tid) throws Exception {
		return this.contains(MarkerFactory.newTupleMarker(relName, tid));
	}
	
}
