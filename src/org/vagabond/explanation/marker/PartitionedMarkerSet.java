package org.vagabond.explanation.marker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PartitionedMarkerSet {

	ArrayList<IMarkerSet> mParts;
	ArrayList<MarkerSummary> attrParts;
	Map<ISchemaMarker, Integer> attrToMSet;
	
	public PartitionedMarkerSet () {
		mParts = new ArrayList<IMarkerSet> ();
		attrParts = new ArrayList<MarkerSummary> ();
		attrToMSet = new HashMap<ISchemaMarker, Integer> ();
	}
	
	public IMarkerSet getMarkerPartition (ISchemaMarker attr) {
		return mParts.get(attrToMSet.get(attr));
	}
	
	public MarkerSummary getAttrPartition (ISchemaMarker attr) {
		return attrParts.get(attrToMSet.get(attr));
	}
	
	public void addPartition (IMarkerSet markers, MarkerSummary attrs) {
		int numPart;
		
		mParts.add(markers);
		attrParts.add(attrs);
		
		numPart = attrParts.size();
		for (ISchemaMarker m : attrs)
			attrToMSet.put(m, numPart - 1);
	}
	
	
}
