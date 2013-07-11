package org.vagabond.explanation.marker;

import java.util.List;
import java.util.Set;

public interface IMarkerSet extends Set<ISingleMarker> {

	public int getSize();
	public int getNumElem();
	public Set<ISingleMarker> getElems();
	public List<ISingleMarker> getElemList();
	public IMarkerSet union (IMarkerSet other);
	public IMarkerSet intersect (IMarkerSet other);
	public IMarkerSet diff (IMarkerSet other);
	public boolean add (ISingleMarker marker);
	public boolean add (int relId, int attrId, int tidId);
	public boolean add (String relName, String attrName, int tidId) throws Exception;
	public boolean contains (String relName, String tid) throws Exception;
	public String toUserString();
	public IMarkerSet cloneSet ();
	public MarkerSummary getSummary ();
	public IMarkerSet subset (MarkerSummary sum);
}
