package org.vagabond.explanation.marker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.vagabond.util.LogProviderHolder;

public class MarkerSet implements IMarkerSet {

	static Logger log = LogProviderHolder.getInstance().getLogger(MarkerSet.class);
	
	protected Set<ISingleMarker> markers;
	private MarkerSummary sum;
	private int hash = -1;
	
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
	public int hashCode () {
		if (hash == -1) {
			hash = markers.hashCode();
		}
		return hash;
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
		sum = null;
		return this;
	}

	@Override
	public boolean add(ISingleMarker marker) {
		hash = -1;
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
	
	public String toUserString () {
		StringBuffer result = new StringBuffer();
		
		Map<String,IMarkerSet> markerPerRel = MarkerSetUtil.partitionOnRelation(this);
		
		for(String rel: markerPerRel.keySet()) {
			result.append(" relation " + rel + " (");
			for(ISingleMarker marker: markerPerRel.get(rel)) {
				result.append(marker.toUserStringNoRel());
				result.append(", ");
			}
			result.delete(result.length() - 2, result.length());
			result.append(')');
		}
		
		return result.toString();
	}

	@Override
	public boolean addAll(Collection<? extends ISingleMarker> arg0) {
		sum = null;
		hash = -1;
		return markers.addAll(arg0);
	}

	@Override
	public void clear() {
		markers.clear();
		sum = null;
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
		sum = null;
		hash = -1;
		return markers.remove(arg0); //TODO check semantics
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		sum = null;
		hash = -1;
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

	@Override
	public IMarkerSet intersect(IMarkerSet other) {
		retainAll(other);
		return this;
	}
	
	@Override
	public IMarkerSet cloneSet() {
		MarkerSet clone = new MarkerSet();
		
		for(ISingleMarker m: this.markers)
			clone.add(m);
		
		if (sum != null)
			clone.sum = sum;
		
		return clone;
	}

	@Override
	public IMarkerSet diff(IMarkerSet other) {
		this.removeAll(other);
		return this;
	}
	
	@Override
	public IMarkerSet subset (MarkerSummary sum) {
		IMarkerSet cloneSet;
		
		cloneSet = this.cloneSet();
		
		for(ISingleMarker m: markers) {
			if (!sum.hasAttr(m))
				cloneSet.remove(m);
		}
		
		return cloneSet;
	}

	@Override
	public MarkerSummary getSummary() {
		if (sum == null)
			this.sum = MarkerFactory.newMarkerSummary(this); //TODO check methods 
		return sum;
	}

	@Override
	public boolean add(int relId, int attrId, int tidId) {
		return add(MarkerFactory.newAttrMarker(relId, tidId, attrId));
	}

	@Override
	public boolean add(String relName, String attrName, int tidId) throws Exception 
	{
		int relid = ScenarioDictionary.getInstance().getRelId(relName);
		int attrid = ScenarioDictionary.getInstance().getAttrId(relName, attrName);
		add(relid,attrid,tidId);
		return false;
	}
	
}
