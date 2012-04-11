package org.vagabond.explanation.marker;

import java.util.Collection;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.vagabond.util.CollectionUtils;
import org.vagabond.util.LogProviderHolder;
import org.vagabond.util.Pair;

public class MarkerFactory {

	static Logger log = LogProviderHolder.getInstance().getLogger(MarkerFactory.class);
	
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
	
	public static IMarkerSet newMarkerSet (Collection<ISingleMarker> markers) {
		MarkerSet result;
		
		result = new MarkerSet();
		
		for (ISingleMarker marker: markers)
			result.add(marker);
		
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
			(String relName, String tid, int attrId) throws Exception {
		return new AttrValueMarker(relName, tid, attrId);
	}
	
	public static IAttributeValueMarker newAttrMarker 
			(int rel, String tid, int attr) throws Exception {
		return new AttrValueMarker(rel, tid, attr);
	}
	
	public static IAttributeValueMarker newAttrMarker 
			(ITupleMarker tup, int attr) {
		return new AttrValueMarker(tup.getRelId(), tup.getTid(), attr);
	}
	
	public static IAttributeValueMarker newAttrMarker 
		(ITupleMarker tup, String attr) throws Exception {
		return new AttrValueMarker(tup.getRel(), tup.getTid(), attr);
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
	
	public static ITupleMarker newTupleMarker (IAttributeValueMarker attr) throws Exception {
		return new TupleMarker(attr.getRel(), attr.getTid());
	}
	
	public static ITupleMarker newTupleMarker (Pair<String,String> values) throws Exception {
		return new TupleMarker(values.getKey(), values.getValue());
	}

	public static MarkerSummary newMarkerSummary (IMarkerSet set) {
		MarkerSummary result;
		
		result = new MarkerSummary();
		
		for (ISingleMarker marker: set) {
			result.addAll(newSchemaMarker(marker));
		}
		
		return result;
	}
	
	public static Collection<ISchemaMarker> newSchemaMarker (ISingleMarker marker) {
		Vector<ISchemaMarker> result;
		int numAttr;
		int relId = marker.getRelId();
		
		
		if (marker instanceof IAttributeValueMarker) {
			int attrId = ((IAttributeValueMarker) marker).getAttrId();
			return CollectionUtils.makeSet((ISchemaMarker) new AttrMarker(relId, attrId));
		}
		
		numAttr = ScenarioDictionary.getInstance().getTupleSize(relId);
		result = new Vector<ISchemaMarker> ();
		for (int i = 0; i < numAttr; i++)
			result.add(new AttrMarker(relId, i));
		
		return result;
	}
}
