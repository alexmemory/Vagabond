package org.vagabond.explanation.marker;

import java.io.Console;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.vagabond.mapping.scenarioToDB.MaterializedViewsBroker;
import org.vagabond.util.ConnectionManager;
import org.vagabond.util.Enums.Marker_Type;
import org.vagabond.util.LogProviderHolder;
import org.vagabond.util.LoggerUtil;
import org.vagabond.explanation.marker.IMarkerSet;;;

public class DBMarkerSet extends MarkerSet {

	static Logger log = LogProviderHolder.getInstance().getLogger(DBMarkerSet.class);
	
	private MarkerSummary sum;
	private String query;
	private String relName;  // NULL if not materialized
	boolean materialised;
	private Set<ISingleMarker> javaObj;
	
	private int size = -1;
	private int numElem = -1;
	
	
	//Indicates the physical representation currently being used. this may be one of the
	//     enum types Marker_Type
	boolean[] current_types=new boolean[3];
	
	public void GenerateRep(Marker_Type new_type)
	{
	  if(current_types[new_type.ordinal()])
	  {
		  //the destination type is already set
		  return;
	  }
	  
	  //Case A : TABLE type destination
	  if(new_type == Marker_Type.TABLE_REP)
	  {
		  //Create a new table and drop the old one if any
		  
		  
		  //Case 1 : convert query to table
		  if(current_types[Marker_Type.QUERY_REP.ordinal()])
		  {
			  materialize(true);
			  //Insert into the table the query results
			  
		  }
		  //Case 2 : convert java objects to table
		  else if(current_types[Marker_Type.JAVA_REP.ordinal()])
		  {
			  //Delete the existing table and create a new table
			  materialize(false);
			  
			  //use the objects in MarkerSet field of this class to insert data into the table
			  for(ISingleMarker marker_temp : getJavaObj())
			  {
				  insertSingleMarker(marker_temp);
			  }
			  current_types[Marker_Type.TABLE_REP.ordinal()] = Boolean.TRUE;
		  }
		  
	  }
	  
	  //Case B : QUERY type destination
	  else if(new_type == Marker_Type.QUERY_REP)
	  {
		  //Case 1 : convert table to query
		  
		  //Case 2 : convert java objects to query
	  }
	  
	  //Case C : JAVAOBJ type destination
	  else if(new_type == Marker_Type.JAVA_REP)
	  {
		  //Case 1 : convert table to java objects
		  
		  //Case 2 : convert  query to java objects
	  }
	}
	
	public DBMarkerSet (String q) {
		query = q;
		relName = null;
	}
	
	public DBMarkerSet (String q, boolean materialize) {
		query = q;
		relName = null;
		current_types[Marker_Type.QUERY_REP.ordinal()] = Boolean.TRUE;
		
		if (materialize) {
			materialize();
		}
	}
	
	public DBMarkerSet (Set<ISingleMarker> obj, boolean materialize) {
		javaObj = obj;
		relName = null;
		current_types[Marker_Type.JAVA_REP.ordinal()] = Boolean.TRUE;
		
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
		
		compose(instance.getViewHandler(this),true);
	}
	
	public void materialize(boolean populate) {
		MaterializedViewsBroker instance = MaterializedViewsBroker.getInstance();
		
		compose(instance.getViewHandler(this),populate);
	}
	
	public void compose(int viewId,boolean populate) {
		relName = "errmarkers" + viewId;
		String qDrop = "DROP TABLE IF EXISTS " + relName;
		String qCreate ;
		
		if(populate)
			qCreate	= "CREATE TABLE " + relName + " AS " + query;
		else
			qCreate = "CREATE TABLE " + relName + " (relation text, tid text, attribute text)";
		
		try {
			ConnectionManager.getInstance().execUpdate(qDrop);
			ConnectionManager.getInstance().execUpdate(qCreate);
		} catch (Exception e) 
		{
		System.out.println(e.toString())	;
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
		
		if (other instanceof DBMarkerSet) {
			DBMarkerSet ov =(DBMarkerSet)other;
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

		if (log.isDebugEnabled()) {log.debug("Check if markers for query:\n" + query1 + "\n and query: \n" + query2 +"\n are the same.");};

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
		if (isMaterialized()) {
			String q = "SELECT * FROM " + relName;

			if (log.isDebugEnabled()) {log.debug("Get the size of markers for query:\n" + q);};

			size = querySize(q);
		} else {
			size = getSize();
		}
		
		return size;
		
	}
	
	@Override
	public int getSize() {
		if (isMaterialized())
			return size;
		
		if (log.isDebugEnabled()) {log.debug("Get the size of markers for query:\n" + query);};

		size = getQueryResultCount(query);
		
		return size;
	}
	
	private int querySize(String q) {
		ResultSet rs;
		int s = 0;
		try {
			rs = ConnectionManager.getInstance().execQuery(q);

			while(rs.next()) {
				String attBits = rs.getString(3);

				for (int i=0; i < attBits.length(); i++) {
					if (attBits.charAt(i) == '1') {
						s ++;
					}
				}

			}

			ConnectionManager.getInstance().closeRs(rs);
		} catch (Exception e) {
			;
		}
		return s;
	}

	@Override
	public int getNumElem() {
		if (isMaterialized()) return numElem;
		
		return getQueryResultCount(query);
	}
	
	public int getQueryResultCount (String q) {
		ResultSet rs;
		String sizeQuery = "SELECT COUNT(*) AS num " + 
							"FROM (" + q + ") AS A";
		int numElem = -1;
		
		try {
			rs = ConnectionManager.getInstance().execQuery(sizeQuery);
			while(rs.next()) {
				numElem = rs.getInt("num");
			}
			ConnectionManager.getInstance().closeRs(rs);
		} catch (Exception e) {
			LoggerUtil.logException(e, log);
		}
		
		return numElem;
	}

	@Override
	public Set<ISingleMarker> getElems() {
		Set<ISingleMarker> markers = new HashSet<ISingleMarker> ();
		ResultSet rs;
		
		if (log.isDebugEnabled()) {log.debug("Compute markers for query:\n" + query);};

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
			 System.out.println(e.toString());
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
		if (other instanceof DBMarkerSet) {
			DBMarkerSet ov = (DBMarkerSet)other;
			return new DBMarkerSet(query + " UNION " + ov.query);
		}
		
		String newQuery = query + " UNION VALUES ";
		for (ISingleMarker marker : other) {
			newQuery +=  addSingleMarkerQueryString(marker)+",";
		}
		newQuery = removeLastComma(newQuery); // remove the last comma
		return new DBMarkerSet(newQuery);
	}
	
	private String addSingleMarkerQueryString(ISingleMarker marker) {
		String unionStr = "";
		unionStr += singleMarkerQueryString(marker);
		
		return unionStr;
	}

	private String singleMarkerQueryString(ISingleMarker marker) {
		String singleMarkerStr = "";
		if (marker instanceof AttrValueMarker) {
			AttrValueMarker m0 = (AttrValueMarker)marker;
			singleMarkerStr = " ('";
			singleMarkerStr += m0.getRel() + "','";
			singleMarkerStr += m0.getTid() + "',";
			try {
				singleMarkerStr += getPostBitConst(m0);
			} catch (Exception e) {
				LoggerUtil.logException(e, log);
			}
			singleMarkerStr += ")";
		} else if (marker instanceof TupleMarker) {
			ArrayList<String> attrValueMarkerSet = getAttrValueMarkerSet((TupleMarker)marker);
			for (String s : attrValueMarkerSet)
				singleMarkerStr += s + ",";
			singleMarkerStr = removeLastComma(singleMarkerStr); // remove the last comma
		} else {
			throw new IllegalArgumentException("Not supported marker type");
		}
		return singleMarkerStr;
	}
	
	private String removeLastComma(String s) {
		return s.substring(0, s.length()-1);
	}

	private ArrayList<String> getAttrValueMarkerSet(TupleMarker marker) {
		ArrayList<String> attrValueMarkerSet = new ArrayList<String>();
		String relName = marker.getRel();
		String tid = marker.getTid();
		String singleMarkerStr;
		try {
			for (String attrName : ScenarioDictionary.getInstance().getAttrNameList(relName)) {
				singleMarkerStr = " ('" + relName + "','";
				singleMarkerStr += tid + "',";
				try {
					singleMarkerStr += getPostBitConst((IAttributeValueMarker) marker);
				} catch (Exception e) {
					LoggerUtil.logException(e, log);
				}
				singleMarkerStr += ")";
				attrValueMarkerSet.add(singleMarkerStr);
			}
		} catch (Exception e) {
			LoggerUtil.logException(e, log);
		}
		return attrValueMarkerSet;
	}
	
	public String addSingleMarker(ISingleMarker marker) {
		if (this.contains(marker)) {
			return query;
		}
		
		query += " UNION VALUES " + addSingleMarkerQueryString(marker);
		if (isMaterialized()) insertSingleMarker(marker);
		
		return query;
		
	}
	
	private void insertSingleMarker(ISingleMarker marker) {
		String s = singleMarkerQueryString(marker);
		String q = "INSERT INTO " + relName + " VALUES " + s;
		try {
			ConnectionManager.getInstance().execUpdate(q);
		} catch (Exception e) {
			;
		}

		// Update size and numElem
		size += marker.getSize();
		numElem ++;
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
//		Set<ISingleMarker> markers = getElems(); //TODO: on db side.
//		return markers.contains(arg0);
		String q;
		
		if (!(arg0 instanceof ISingleMarker))
			return false;

		ISingleMarker o = (ISingleMarker)arg0;
		if (o instanceof ITupleMarker) {
			q = "SELECT * FROM (" + query + ") AS q WHERE rel = '" 
			+ o.getRel() + "' AND tid = '" + o.getTid() + "'";
		} 
		else {
			try {
				IAttributeValueMarker a = (IAttributeValueMarker) o;
				q = "SELECT * FROM (" + query + ") AS q WHERE rel = '" + a.getRel() + "' AND tid = '" 
						+ a.getTid() + "' AND att = " + getPostBitConst(a);
			} catch (Exception e) {
				LoggerUtil.logException(e, log);
				return false;
			}
		}
		
		return getQueryResultCount(q) == o.getSize();		
	}
	
	private String getVarbitConst (int size, boolean value) {
		String r = "";
		for(int i = 0; i < size; i++)
			r += (value) ? "1" : "0";
		
		return r;
	}

	@Override
	public boolean containsAll(Collection<?> arg0) {
//		Set<ISingleMarker> markers = getElems(); // TODO: 2 cases: markerset and view
//		return markers.containsAll(arg0);
		if (arg0 instanceof DBMarkerSet) {
			String q = ((DBMarkerSet)arg0).query + " EXCEPT ( " + this.query + " ) ";
			return querySize(q) == 0;
		}
		
		if (arg0 instanceof IMarkerSet) {
			IMarkerSet ms = (IMarkerSet)arg0;
			String q1 = "";
			for (ISingleMarker marker : ms) {
				q1 += singleMarkerQueryString(marker) + ",";
			}
			String q = this.query + " EXCEPT VALUES " + q1;
			q = removeLastComma(q);
			return getSize()-getQueryResultCount(q) == ms.getSize();
		}
		
		return false;
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
	
	private String getPostBitConst (IAttributeValueMarker m) throws Exception {
		return "B'" + getMarkerAtt(m.getRel(), m.getAttrId()) + "'::bit varying";
	}
	
	private String getMarkerAtt(String relName, int attId) throws Exception {
		int attLength = ScenarioDictionary.getInstance().getAttrNameList(relName).size();
		int attPos = (int)Math.pow(2, attLength-attId-1);
		return String.format("%"+attLength+"s", Integer.toBinaryString(attPos)).replace(' ', '0');
	}

	@Override
	public boolean remove(Object arg0) {
		if (!this.contains((ISingleMarker)arg0)) { //TODO check semantics (half contained tuple marker)
			return false;
		}
		sum = null;
		String exceptStr = " EXCEPT VALUES ";
		exceptStr += singleMarkerQueryString((ISingleMarker)arg0);
		query += exceptStr;
		
		if (isMaterialized()) deleteSingleMarker((ISingleMarker)arg0); // TODO: run delete on the db side.
		
		return true;
	}
	
	private void deleteSingleMarker(ISingleMarker marker) {
		// Only to be called if the view has been materialized
		String s = singleMarkerQueryString(marker);
		// Update size and numElem
//		if (marker instanceof AttrValueMarker) {
//			size --;
//			numElem --;
//		}
		
//		if (marker instanceof TupleMarker) {
//			String sizeq = "SELECT * FROM " + relName + " WHERE (rel, tid, att) IN ( VALUES " + s + " )";
//			int changedSize = querySize(sizeq);
//			size -= changedSize;
//			numElem -= changedSize;
//		}
		
		String q = "DELETE FROM " + relName + " WHERE (rel, tid, att) IN ( VALUES " + s + " )";
		try {
			int changedSize = ConnectionManager.getInstance().execUpdate(q);
			size -= changedSize;
			numElem -= changedSize;
		} catch (Exception e) {
			LoggerUtil.logException(e, log);
		}
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
//		boolean changed = false;
//		sum = null;
//		for (Object marker : arg0) {
//			changed |= remove(marker);
//		}
//		
//		return changed; // TODO: maybe on a batch delete
		boolean changed = false;
		sum = null;

		if (arg0 instanceof DBMarkerSet) {
			DBMarkerSet msv = (DBMarkerSet)arg0;
			query += " EXCEPT ( " + msv.query + " )";
			if (isMaterialized()) changed = deleteFromQuery(msv.query);
		} else if (arg0 instanceof IMarkerSet) {
			IMarkerSet ms = (IMarkerSet)arg0;
			String exceptStr = " EXCEPT VALUES ";
			String values = "";
			for (ISingleMarker marker : ms) {
				values += singleMarkerQueryString(marker) + ",";
			}
			values = removeLastComma(values);
			query += exceptStr + values;
			try {
				if (isMaterialized()) changed = deleteMultipleMarkers(values);
			} catch (Exception e) {
				;
			}
		}
		
		return changed;
	}
	
	private boolean deleteMultipleMarkers(String values) throws Exception {
		// Only to be called if the view has been materialized
//		String sizeq = "SELECT * FROM " + relName + " WHERE (rel, tid, att) IN ( VALUES " + values + " )";
//		int changedNum = querySize(sizeq);
		String q = "DELETE FROM " + relName + " WHERE (rel, tid, att) IN ( VALUES " + values + " )";
		try {
			int changedNum  = ConnectionManager.getInstance().execUpdate(q);
			size -= changedNum;
			numElem -= changedNum;
			return changedNum>0;
		} catch (Exception e) {
			LoggerUtil.logException(e, log);
		}
		return false;
	}
	
	private boolean deleteFromQuery(String q) {
		String sizeq = "SELECT * FROM " + relName + " WHERE (rel, tid, att) IN ( SELECT * FROM ( " + q + " ) AS A)";
		int changedNum = querySize(sizeq);
		size -= changedNum;
		numElem -= changedNum;
		
		String q1 = "DELETE FROM " + relName + " WHERE (rel, tid, att) IN (SELECT * FROM ( " + q + " ) AS A)";
		try {
			ConnectionManager.getInstance().execUpdate(q1);
		} catch (Exception e) {
			;
		}
		
		return changedNum>0;
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		int tempSize = getSize();
		boolean changed = false;
		if (arg0 instanceof DBMarkerSet) {
			DBMarkerSet msv = (DBMarkerSet)arg0;
			query = "( " + query + " ) INTERSECT ( " + msv.query + " )";
			changed = (tempSize != getSize());
			if (isMaterialized()) changed = keepQuery(msv.query);
		} else if (arg0 instanceof IMarkerSet) { // INTERSECT VALUES()
			IMarkerSet ms = (IMarkerSet)arg0;
			String values = "";
			for (ISingleMarker marker : ms) {
				values += singleMarkerQueryString(marker) + ",";
			}
			values = removeLastComma(values);
			query = "( " + query + " ) INTERSECT ( VALUES " + values + " )";
			if (isMaterialized()) changed = keepMultipleMarkers(values);
		}

		return changed;
	}
	
	private boolean keepQuery(String q) {
//		String sizeq = "SELECT * FROM " + relName + " WHERE (rel, tid, att) NOT IN ( SELECT * FROM ( " + q + " ) AS A)";
//		int changedNum = querySize(sizeq);
		
		String q1 = "DELETE FROM " + relName + " WHERE (rel, tid, att) NOT IN (SELECT * FROM ( " + q + " ) AS A)";
		try {
			int changedNum = ConnectionManager.getInstance().execUpdate(q1);
			size -= changedNum;
			numElem -= changedNum;
			return changedNum>0;
		} catch (Exception e) {
			LoggerUtil.logException(e, log);
		}
		
		return false;
	}
	
	private boolean keepMultipleMarkers(String values) {
		// Only to be called if the view has been materialized
//		String sizeq = "SELECT * FROM " + relName + " WHERE (rel, tid, att) NOT IN ( VALUES " + values + " )";		
		String q = "DELETE FROM " + relName + " WHERE (rel, tid, att) NOT IN ( VALUES " + values + " )";
		try {
			int changedNum = ConnectionManager.getInstance().execUpdate(q);
			size -= changedNum;
			numElem -= changedNum;
			return changedNum>0;	
		} catch (Exception e) {
			LoggerUtil.logException(e, log);
		}
		
		return false;
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
		DBMarkerSet clone = new DBMarkerSet(query);
		
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
		Set<ISingleMarker> markers = getElems(); // project only the attributes in sum.
		IMarkerSet cloneSet; //TODO later use projection query instead of materializing
		
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
//			this.sum = MarkerFactory.newMarkerSummary(this); //TODO calls getElems()
			this.sum = getSummaryFromView();
		return sum; //TODO SELECT DISTINCT rel, attr FROM (q). Insert to a schemamarker and return it.
	}
	
	private MarkerSummary getSummaryFromView() {
		String q = "SELECT DISTINCT rel, att FROM (" + query + ") AS A";
		if (isMaterialized()) q = "SELECT DISTINCT rel, att FROM " + relName;
		ResultSet rs;
		String relName;
		String attrId;
		MarkerSummary result = new MarkerSummary();
		
		try {
			rs = ConnectionManager.getInstance().execQuery(q);
			while(rs.next()) {
				relName = rs.getString(1);
				attrId = rs.getString(2);
				result.addAll(newSchemaMarker(relName, attrId));
			}
			ConnectionManager.getInstance().closeRs(rs);
		} catch (Exception e) {
			LoggerUtil.logException(e, log);
		}
		
		return result;
	}
	
	private Collection<ISchemaMarker> newSchemaMarker(String relName, String attrId) throws Exception {
		Vector<ISchemaMarker> result;
		int relId = ScenarioDictionary.getInstance().getRelId(relName);
		int numAttr = ScenarioDictionary.getInstance().getTupleSize(relId);
		result = new Vector<ISchemaMarker> ();
		for (int i = 0; i < numAttr; i++)
			if (attrId.charAt(numAttr - i - 1) == '1')
				result.add(MarkerFactory.newSchemaMarker(relId, i));
		
		return result;
	}

	private Set<ISingleMarker> getJavaObj() {
		return javaObj;
	}

	private void setJavaObj(Set<ISingleMarker> javaObj) {
		this.javaObj = javaObj;
		current_types[Marker_Type.JAVA_REP.ordinal()] = true;
	}
}
