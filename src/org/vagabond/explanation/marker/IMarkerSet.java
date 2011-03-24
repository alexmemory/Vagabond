package org.vagabond.explanation.marker;

import java.util.List;
import java.util.Set;

public interface IMarkerSet extends Set<ISingleMarker> {

	public int getSize();
	public int getNumElem();
	public Set<ISingleMarker> getElems();
	public List<ISingleMarker> getElemList();
	public IMarkerSet union (IMarkerSet other);
	public boolean add (ISingleMarker marker);
	public boolean contains (String relName, String tid) throws Exception;
}
