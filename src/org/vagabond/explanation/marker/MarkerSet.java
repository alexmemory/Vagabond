package org.vagabond.explanation.marker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

public class MarkerSet implements IMarkerSet {

	static Logger log = Logger.getLogger(MarkerSet.class);
	
	private Set<ISingleMarker> markers;
	
	public MarkerSet () {
		markers = new HashSet<ISingleMarker> ();
	}
	
	@Override
	public boolean equals (Object other) {
		if (other == null)
			return false;
		
		if (other instanceof IMarkerSet) {
			IMarkerSet oMarker = (IMarkerSet) other;
			Iterator<ISingleMarker> iter = this.markers.iterator();

			if (this.getNumElem() != oMarker.getNumElem())
				return false;
			while(iter.hasNext()) {
				ISingleMarker elem = iter.next();
				log.debug("check " + elem);
				if (!oMarker.getElems().contains(elem))
					return false;
			}

			return true;
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
	public void add(ISingleMarker marker) {
		markers.add(marker);
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
	
}
