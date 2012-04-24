package org.vagabond.explanation.marker;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.vagabond.explanation.generation.QueryHolder;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.mapping.scenarioToDB.MaterializedViewsBroker;
import org.vagabond.mapping.scenarioToDB.SchemaCodeGenerator;
import org.vagabond.util.ConnectionManager;
import org.vagabond.util.LogProviderHolder;
import org.vagabond.xmlmodel.DataType;
import org.vagabond.xmlmodel.RelInstanceFileType;
import org.vagabond.xmlmodel.RelInstanceType;
import org.vagabond.xmlmodel.RelInstanceType.Row;

public class MarkerSetView extends MarkerSet {

	static Logger log = LogProviderHolder.getInstance().getLogger(MarkerSetView.class);
	
	private MarkerSummary sum;
	private String query;
	private String relName;  // NULL if not materialized
	private int size = -1;
	private int numElem = -1;
	
	public MarkerSetView (String q) {
		query = q;
		relName = null;
	}
	
	public MarkerSetView (String q, boolean materialize) {
		query = q;
		relName = null;
		if (materialize) {
			materialize();
		}
	}
	
	public String getQuery() {
		return query;
	}
	
	public boolean isMaterialized() {
		return relName != null;
	}
	
	public String getRelName() {
		return relName;
	}
	
	public void materialize() {
		MaterializedViewsBroker instance = MaterializedViewsBroker.getInstance();
		compose(instance.getViewHandler(this));
	}
	
	public void compose(int viewId) {
		relName = "errmarkers" + viewId;
		String qDrop = "DROP TABLE IF EXISTS " + relName;
		String qCreate = "CREATE TABLE " + relName + " AS " + query;
		
		try {
			ConnectionManager.getInstance().execUpdate(qDrop);
			ConnectionManager.getInstance().execUpdate(qCreate);
		} catch (Exception e) {
			;
		}

		// Update size and numElem
		size = getMaterializedSize();
		numElem = getMaterializedSize();
	}
	
	public void decompose() {
		query = null;
		size = -1;
		numElem = -1;
		String qDrop = "DROP TABLE IF EXISTS " + relName;
		try {
			ConnectionManager.getInstance().execUpdate(qDrop);
		} catch (Exception e) {
			;
		}
		relName = null;
	}
	
	@Override
	public boolean equals (Object other) {
		if (other == null)
			return false;
		
		if (other == this)
			return true;
		
		if (! (other instanceof IMarkerSet))
			return false;
		
		if (other instanceof MarkerSetView) {
			MarkerSetView ov =(MarkerSetView)other;
			if (query.toUpperCase().equals(ov.getQuery().toUpperCase()))
				return true;
				
			if (this.getSize() != ov.getSize())
				return false;
			
			if (!isMaterialized()) materialize();

			if (!ov.isMaterialized()) ov.materialize();

			return markerSetsEqualOnDBSide(relName, ov.getRelName());
			
		}
		
		IMarkerSet o = (IMarkerSet) other;
		if (this.isMaterialized() && this.getSize() != o.getSize())
			return false;
		
		Set<ISingleMarker> markers = getElems();
		return markers.equals(o);
	}
	
	private boolean markerSetsEqualOnDBSide(String relName1, String relName2) {
		String query1 = "SELECT COUNT(*) FROM ( "+
						" SELECT * FROM " + relName1 +
						" EXCEPT " +
						" SELECT * FROM " + relName2 +
						" ) AS A";
		String query2 = "SELECT COUNT(*) FROM ( "+
						" SELECT * FROM " + relName2 +
						" EXCEPT " +
						" SELECT * FROM " + relName1 +
						" ) AS A";

		int size1 = -1;
		int size2 = -1;;
		ResultSet rs;

		log.debug("Check if markers for query:\n" + query1 + "\n and query: \n" + query2 +"\n are the same.");

		try {
			rs = ConnectionManager.getInstance().execQuery(query1);
			while(rs.next()) {
				size1 = rs.getInt(1);
			}
			
			if (size1 != 0)
				return false;

			rs = ConnectionManager.getInstance().execQuery(query2);
			while(rs.next()) {
				size2 = rs.getInt(1);
			}

			ConnectionManager.getInstance().closeRs(rs);
		} catch (Exception e) {
			;
		}
		
		return size2==0;
	}
	
	public int getMaterializedSize() {
		ResultSet rs;
		if (isMaterialized()) {
			String q = "SELECT COUNT(*) AS num FROM " + relName;

			log.debug("Get the size of markers for query:\n" + q);

			try {
				rs = ConnectionManager.getInstance().execQuery(q);

				while(rs.next()) {
					size = rs.getInt("num");
				}

				ConnectionManager.getInstance().closeRs(rs);
			} catch (Exception e) {
				;
			}
		} else {
			size = getSize();
		}
		
		return size;
		
	}
	
	@Override
	public int getSize() {
		if (isMaterialized())
			return size;
		
		ResultSet rs;

		log.debug("Get the size of markers for query:\n" + query);

		size = 0;
		try {
			rs = ConnectionManager.getInstance().execQuery(query);

			while(rs.next()) {
				String attBits = rs.getString(3);

				for (int i=0; i < attBits.length(); i++) {
					if (attBits.charAt(i) == '1') {
						size ++;
					}
				}

			}

			ConnectionManager.getInstance().closeRs(rs);
		} catch (Exception e) {
			;
		}
		
		return size;
	}

	@Override
	public int getNumElem() {
		if (isMaterialized()) return numElem;
		
		ResultSet rs;
		String sizeQuery = "SELECT COUNT(*) AS num " + 
							"FROM (" + query + ") AS A";
		
		try {
			rs = ConnectionManager.getInstance().execQuery(sizeQuery);
			while(rs.next()) {
				numElem = rs.getInt("num");
			}
			ConnectionManager.getInstance().closeRs(rs);
		} catch (Exception e) {
			;
		}
		
		return numElem;
	}

	@Override
	public Set<ISingleMarker> getElems() {
		Set<ISingleMarker> markers = new HashSet<ISingleMarker> ();
		ResultSet rs;
		
		log.debug("Compute markers for query:\n" + query);

		try {
			String q = query;
			if (isMaterialized()) {
				q = "SELECT * FROM " + relName;
			}
			rs = ConnectionManager.getInstance().execQuery(q);

			while(rs.next()) {
				String rel = rs.getString(1);
				String tid = rs.getString(2);
				String attBits = rs.getString(3);

				for (int i=0; i < attBits.length(); i++) {
					if (attBits.charAt(i) == '1') {
						String attName = ScenarioDictionary.getInstance().getAttrName(rel, i);
						ISingleMarker m = new AttrValueMarker(rel, tid, attName);
						markers.add(m);
					}
				}

			}

			ConnectionManager.getInstance().closeRs(rs);
		} catch (Exception e) {
			;
		}
		
		return markers;
	}
	
	@Override
	public List<ISingleMarker> getElemList() {
		Set<ISingleMarker> markers = getElems();
		return new ArrayList<ISingleMarker> (markers);
	}

	@Override
	public IMarkerSet union(IMarkerSet other) {
		if (other instanceof MarkerSetView) {
			MarkerSetView ov = (MarkerSetView)other;
			return new MarkerSetView(query + " UNION " + ov.query);
		}
		
		String newQuery = "";
		for (ISingleMarker marker : other) {
			newQuery = query + addSingleMarkerQueryString(marker);
		}
		return new MarkerSetView(newQuery);
	}
	
	private String addSingleMarkerQueryString(ISingleMarker marker) {
		String unionStr = " UNION SELECT '";
		if (marker instanceof AttrValueMarker) {
			AttrValueMarker m0 = (AttrValueMarker)marker;
			unionStr += m0.getRel() + "','";
			unionStr += m0.getTid() + "',B'";
			String attId = "";
			try {
				attId = getMarkerAtt(m0.getRel(), m0.getAttrId());
			} catch (Exception e) {
				e.printStackTrace();
			}
			unionStr += attId;
			unionStr += "'::bit varying";
			unionStr += unionStr;
		} else if (marker instanceof TupleMarker) {
			String relName = marker.getRel();
			String tid = marker.getTid();
			try {
				for (String attrName : ScenarioDictionary.getInstance().getAttrNameList(relName)) {
					unionStr += relName + "','";
					unionStr += tid + "',B'";
					String attId = "";
					try {
						attId = getMarkerAtt(attrName, ScenarioDictionary.getInstance().getAttrId(relName, attrName));
					} catch (Exception e) {
						e.printStackTrace();
					}
					unionStr += attId;
					unionStr += "'::bit varying";
					unionStr += unionStr;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			;
		}
		
		return unionStr;
	}
	
	public String addSingleMarker(ISingleMarker marker) {
		if (this.contains(marker)) {
			return query;
		}
		
		query += addSingleMarkerQueryString(marker);
		if (isMaterialized()) materialize();
		
		return query;
		
	}

	@Override
	public boolean add(ISingleMarker marker) {
		String tempQ = query;
		addSingleMarker(marker);
		
		return !tempQ.equals(query);
	}
	
	@Override
	public String toString () {
		StringBuffer result = new StringBuffer();
		result.append("MarkerSet: {");
		Set<ISingleMarker> markers = getElems();
		
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
		boolean changed = false;
		sum = null;
		for (ISingleMarker marker : arg0) {
			changed |= add(marker);
		}
		
		return changed;
	}

	@Override
	public void clear() {
		query = null;
		relName = null;
		size = -1;
		numElem = -1;
		super.clear();
		sum = null;
		if (isMaterialized()) decompose();
	}

	@Override
	public boolean contains(Object arg0) {
		Set<ISingleMarker> markers = getElems();

		return markers.contains(arg0);
	}

	@Override
	public boolean containsAll(Collection<?> arg0) {
		Set<ISingleMarker> markers = getElems();

		return markers.containsAll(arg0);
	}

	@Override
	public boolean isEmpty() {
		return getSize()>0;
	}

	@Override
	public Iterator<ISingleMarker> iterator() {
		Set<ISingleMarker> markers = getElems();

		return markers.iterator();
	}
	
	private String getMarkerAtt(String relName, int attId) throws Exception {
		int attLength = ScenarioDictionary.getInstance().getAttrNameList(relName).size();
		int attPos = (int)Math.pow(2, attLength-attId-1);
		return String.format("%"+attLength+"s", Integer.toBinaryString(attPos)).replace(' ', '0');
	}

	@Override
	public boolean remove(Object arg0) {
		if (!this.contains((ISingleMarker)arg0)) {
			return false;
		}
		sum = null;
		String exceptStr = " EXCEPT SELECT '";
		if (arg0 instanceof AttrValueMarker) {
			AttrValueMarker marker = (AttrValueMarker)arg0;
			exceptStr += marker.getRel() + "','";
			exceptStr += marker.getTid() + "',B'";
			String attId = "";
			try {
				attId = getMarkerAtt(marker.getRel(), marker.getAttrId());
			} catch (Exception e) {
				e.printStackTrace();
			}
			exceptStr += attId;
			exceptStr += "'::bit varying";
		}
		query += exceptStr;
		
		if (isMaterialized()) materialize();
		
		return true;
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		boolean changed = false;
		sum = null;
		for (Object marker : arg0) {
			changed |= remove(marker);
		}
		
		return changed;
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		String tempQuery = query;
		int tempSize = getSize();
		boolean changed = false;
		if (arg0 instanceof MarkerSetView) {
			MarkerSetView mv1 = (MarkerSetView)arg0;
			query = query + " INTERSECT ( " + mv1.query + " )";
			changed = (tempSize != getSize());
		} else if (arg0 instanceof IMarkerSet) {
			changed = removeAll(arg0);
			query = tempQuery + " EXCEPT ( " + query  + " )";
		}

		if (isMaterialized()) materialize();
		return changed;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public Object[] toArray() {
		Set<ISingleMarker> markers = getElems();

		return markers.toArray();
	}

	@Override
	public <T> T[] toArray(T[] arg0) {
		Set<ISingleMarker> markers = getElems();

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
		MarkerSetView clone = new MarkerSetView(query);
		
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
		Set<ISingleMarker> markers = getElems();
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
	
}
