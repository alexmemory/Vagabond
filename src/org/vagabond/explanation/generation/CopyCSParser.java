package org.vagabond.explanation.generation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.vagabond.explanation.marker.ITupleMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.model.prov.CopyProvExpl;
import org.vagabond.util.ResultSetUtil;

import static org.vagabond.util.LoggerUtil.*;

public class CopyCSParser {

	static Logger log = Logger.getLogger(CopyCSParser.class);
	
	private ResultSet dbResult;
	private CopyProvExpl allProv;
	private List<Integer> tidAttrPos;
	private List<String> relNames;
	
	public CopyCSParser (ResultSet result) throws Exception {
		this.dbResult = result;
		allProv = new CopyProvExpl();
		tidAttrPos = new ArrayList<Integer> ();
		relNames = new ArrayList<String> ();
		
		parse();
	}
	
	private void parse() throws Exception {
		parseSchema();
		createTupleAndWLSet();
	}
	
	private void parseSchema () throws SQLException {
		String[] colNames;
		String colName;
		String relName;

		log.debug("parse schema of result set");
		
		colNames = ResultSetUtil.getResultColumns(dbResult);
		
		for(int i = 0; i < colNames.length; i++) {
			colName = colNames[i];
			if (isTidProvAttr(colName)) {
				relName = ResultSetUtil.getRelFromProvName(colName);
				relNames.add(relName);
				tidAttrPos.add(i + 1);
			}
		}
		
		logArray(log, colNames, "ColNames");
		logArray(log, relNames.toArray(), "RelNames");
		
		allProv.setRelNames(relNames);
	}
	
	private void createTupleAndWLSet () throws Exception {
		Vector<ITupleMarker> witList;
		ITupleMarker tup;
		String tid;
		
		while(dbResult.next()) {
			witList = new Vector<ITupleMarker> ();
			for (int i = 0; i < relNames.size(); i++) {
				tid = dbResult.getString(tidAttrPos.get(i));
				tup = MarkerFactory.newTupleMarker(relNames.get(i), tid);
				witList.add(tup);
				allProv.addTupleInProv(tup);
			}
			allProv.addWitnessList(witList);
		}
	}
	
	private boolean isTidProvAttr (String name) {
		return ResultSetUtil.getAttrFromProvName(name).equals("tid") 
				&& ResultSetUtil.isProvAttr(name);
	}

	public CopyProvExpl getAllProv() {
		return allProv;
	}
}
