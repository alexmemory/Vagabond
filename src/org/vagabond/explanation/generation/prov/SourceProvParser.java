package org.vagabond.explanation.generation.prov;

import static org.vagabond.util.LoggerUtil.logArray;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.vagabond.explanation.marker.ITupleMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.model.prov.ProvWLRepresentation;
import org.vagabond.util.LogProviderHolder;
import org.vagabond.util.ResultSetUtil;

public class SourceProvParser {

	static Logger log = LogProviderHolder.getInstance().getLogger(SourceProvParser.class);
	
	private ResultSet dbResult;
	private ProvWLRepresentation allProv;
	private List<Integer> tidAttrPos;
	private List<String> relNames;
	
	public SourceProvParser (ResultSet result) throws Exception {
		this.dbResult = result;
		allProv = new ProvWLRepresentation();
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
		
		log.debug("Tid attribute positions" +  tidAttrPos.toString());
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
				log.debug("parsed tid <" + tid + ">");
				
				if (tid != null) {
					tup = MarkerFactory.newTupleMarker(relNames.get(i), tid);
					log.debug("add tuple marker " + tup);
					witList.add(tup);	
					allProv.addTupleInProv(tup);
				}
				else
					witList.add(null);
			}
			
			log.debug("created witness list " + witList);
			allProv.addWitnessList(witList);
		}
	}
	
	private boolean isTidProvAttr (String name) {
		return ResultSetUtil.getAttrFromProvName(name).equals("tid") 
				&& ResultSetUtil.isProvAttr(name);
	}

	public ProvWLRepresentation getAllProv() {
		return allProv;
	}
}
