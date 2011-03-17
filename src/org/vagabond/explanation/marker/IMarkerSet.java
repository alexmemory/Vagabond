package org.vagabond.explanation.marker;

import java.util.List;
import java.util.Set;

public interface IMarkerSet {

	public int getSize();
	public int getNumElem();
	public Set<ISingleMarker> getElems();
	public List<ISingleMarker> getElemList();
	public IMarkerSet union (IMarkerSet other);
	public void add (ISingleMarker marker);
}
