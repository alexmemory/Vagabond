package org.vagabond.explanation.generation;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.model.ExplanationFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.CopySourceError;
import org.vagabond.explanation.model.prov.CopyProvExpl;
import org.vagabond.util.ConnectionManager;

public class CopySourceExplanationGenerator implements
		ISingleExplanationGenerator {

	private CopyProvExpl prov;
	private IAttributeValueMarker error;
	
	@Override
	public IExplanationSet findExplanations(ISingleMarker errorMarker) throws Exception {
		this.error = (IAttributeValueMarker) errorMarker;
		return getExplanationSets();
	}
	
	private IExplanationSet getExplanationSets() throws Exception {
		IExplanationSet result = ExplanationFactory.newExplanationSet();
		CopySourceError expl;
		
		retrieveCopyProvenance();
		
		expl = new CopySourceError();
		expl.setExplains(error);
		expl.setSourceSE(prov.getTuplesInProv());
		result.addExplanation(expl);
		
		return result;
	}
	
	private void retrieveCopyProvenance () 
			throws Exception {
		ResultSet rs;
		CopyCSParser parser;
		
		rs = ConnectionManager.getInstance().execQuery(getQuery());
		parser = new CopyCSParser(rs);
		this.prov = parser.getAllProv();
	}

	private String getQuery () {
		String table = error.getRelName();
		String tid = error.getTid();
		String attr = error.getAttrName();
		
		return QueryHolder.getQuery("CopyCS.GetProv").
				parameterize(table, tid, attr);
	}
	
	
}
