package org.vagabond.explanation.marker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.vagabond.util.CollectionUtils;
import org.vagabond.util.LogProviderHolder;
import org.vagabond.util.LoggerUtil;
import org.vagabond.util.Pair;

public class MarkerFactory {

	static Logger log = LogProviderHolder.getInstance().getLogger(MarkerFactory.class);
	
	private static MarkerFactory instance = new MarkerFactory();
	
	private static ArrayList<ArrayList<ISchemaMarker>> schemaMConsts;
	
	static {
		schemaMConsts = new ArrayList<ArrayList<ISchemaMarker>> ();
	}
	
	private MarkerFactory () {
		
	}
	
	public static IMarkerSet newMarkerSet () {
		return new MarkerSet();
	}
	
	
	public static IMarkerSet newBitMarkerSet(){
		return new BitMarkerSet();
	}
	
	public static IMarkerSet newMarkerSet (ISingleMarker ... markers) {
		 MarkerSet result;
		 
		 result = new MarkerSet();
		 
		 for (ISingleMarker marker: markers) {
			 result.add(marker);
		 }
		 
		 return result;
	}
	
	public static IMarkerSet newBitMarkerSet(ISingleMarker ... markers){
		BitMarkerSet result = new BitMarkerSet();
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
	
	public static IMarkerSet newBitMarkerSet (Collection<ISingleMarker> markers) {
		BitMarkerSet result = new BitMarkerSet();
		
		for (ISingleMarker marker: markers)
			result.add(marker);
		
		return result;
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
			(ITupleMarker tup, int attr) throws Exception {
		return new AttrValueMarker(tup.getRelId(), tup.getTid(), attr);
	}
	
	public static IAttributeValueMarker newAttrMarker 
		(ITupleMarker tup, String attr) throws Exception {
		return new AttrValueMarker(tup.getRel(), tup.getTid(), attr);
	}
	
	public static ISingleMarker newAttrMarker(int relId, int tid, int attrId) {
		return new AttrValueMarker(relId, tid, attrId);
	}
	
	public static ITupleMarker newTupleMarker (String relName, String tid) 
			throws Exception {
		return new TupleMarker(relName, tid);
	}
	
	public static ITupleMarker newTupleMarker (int relId, int tidId) {
		return new TupleMarker(relId, tidId);
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

	public static ISchemaMarker newSchemaMarker (int relId, int attrId) {
		ArrayList<ISchemaMarker> attrs;
		ISchemaMarker result;
		
		assert(ScenarioDictionary.getInstance().validateAttrId(relId, attrId));
		
		attrs = relId < schemaMConsts.size() ? schemaMConsts.get(relId) : null;
		if (attrs == null) {
			attrs = new ArrayList<ISchemaMarker> ();
			CollectionUtils.addToList(schemaMConsts, attrs, relId);
		}
		
		result = attrId < attrs.size() ? attrs.get(attrId) : null;
		if (result == null) {
			try {
				result = new AttrMarker(relId, attrId);
			} catch (Exception e) {
				LoggerUtil.logException(e, log);
				result = null;
			}
			CollectionUtils.addToList(attrs, result, attrId);
		}
		
		return result; 
	}
	
	public static ISchemaMarker newSchemaMarker (String relName, int attrId) throws Exception {
		return newSchemaMarker(ScenarioDictionary.getInstance().getRelId(relName), attrId);
	}
	
	public static ISchemaMarker newSchemaMarker (String relName, String attrName) throws Exception {
		int relId = ScenarioDictionary.getInstance().getRelId(relName);
		int attrId = ScenarioDictionary.getInstance().getAttrId(relId, attrName);
		
		return newSchemaMarker(relId, attrId);
	}
	
	public static ISchemaMarker newSchemaMarker (IAttributeValueMarker attr) {
		return newSchemaMarker(attr.getRelId(), attr.getAttrId());
	}
	
	public static MarkerSummary newMarkerSummary () {
		return new MarkerSummary();
	}
	
	public static MarkerSummary newMarkerSummary (ISchemaMarker ... m) {
		MarkerSummary result = new MarkerSummary();
		
		for(ISchemaMarker mark: m)
			result.add(mark);
		
		return result;
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
			return CollectionUtils.makeSet(newSchemaMarker((IAttributeValueMarker) marker));
		}
		
		numAttr = ScenarioDictionary.getInstance().getTupleSize(relId);
		result = new Vector<ISchemaMarker> ();
		for (int i = 0; i < numAttr; i++)
			result.add(newSchemaMarker(relId, i));
		
		return result;
	}

	
}
