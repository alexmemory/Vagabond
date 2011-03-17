package org.vagabond.explanation.marker;

import org.apache.log4j.Logger;

public class MarkerFactory {

	static Logger log = Logger.getLogger(MarkerFactory.class);
	
	private static MarkerFactory instance = new MarkerFactory();
	
	private MarkerFactory () {
		
	}
	
	public static IMarkerSet newMarkerSet () {
		return new MarkerSet();
	}
	
	public static IMarkerSet newMarkerSet (ISingleMarker ... markers) {
		 MarkerSet result;
		 
		 result = new MarkerSet();
		 
		 for (ISingleMarker marker: markers) {
			 result.add(marker);
		 }
		 
		 return result;
	}
	
	public static IAttributeValueMarker newAttrMarker () {
		return new AttrValueMarker();
	}
	
	public static IAttributeValueMarker newAttrMarker 
			(String relName, String tid, String attrName) throws Exception {
		return new AttrValueMarker(relName, tid, attrName);
	}
	
	public static IAttributeValueMarker newAttrMarker 
			(int rel, String tid, int attr) throws Exception {
		return new AttrValueMarker(rel, tid, attr);
	}
	
	public static ITupleMarker newTupleMarker () {
		return new TupleMarker();
	}
	
	public static ITupleMarker newTupleMarker (String relName, String tid) 
			throws Exception {
		return new TupleMarker(relName, tid);
	}
	
	public static ITupleMarker newTupleMarker (int relId, String tid) 
			throws Exception {
		return new TupleMarker(relId, tid);
	}
	

}
