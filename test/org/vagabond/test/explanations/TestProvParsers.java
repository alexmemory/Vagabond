package org.vagabond.test.explanations;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.sql.ResultSet;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vagabond.explanation.generation.QueryHolder;
import org.vagabond.explanation.generation.prov.SourceAndMapProvParser;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.MarkerParser;
import org.vagabond.explanation.model.prov.MapAndWLProvRepresentation;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.test.AbstractVagabondTest;
import org.vagabond.util.CollectionUtils;
import org.vagabond.util.ConnectionManager;
import org.vagabond.util.Pair;
import org.vagabond.xmlmodel.MappingType;

public class TestProvParsers extends AbstractVagabondTest {

	static Logger log = Logger.getLogger(TestProvParsers.class);
	
	private SourceAndMapProvParser parser;
	
	@BeforeClass
	public static void setUp () throws Exception {	
		loadToDB("resource/test/simpleTest.xml");
		QueryHolder.getInstance().loadFromDir(new File ("resource/queries"));
	}

	@Test
	public void provAndMapParser () throws Exception {
		String query;
		ResultSet rs;
		Vector<Pair<String,MapAndWLProvRepresentation>> rep;
		IMarkerSet set1, set2;
		MappingType m;
		Vector<MappingType> maps;
		
		set1 = MarkerParser.getInstance().parseSet(
				"{T(address,2),T(person,2)}");
		set2 = MarkerParser.getInstance().parseSet(
				"{T(address,2),T(person,4)}");
		m = MapScenarioHolder.getInstance().getMapping("M2");
		maps = CollectionUtils.makeVec(m);
		
		query = QueryHolder.getQuery
				("ProvSE.GetSideEffectUsingAggPlusCompleteProv")
				.parameterize("target.employee",
						"(prov_source_address_tid = 2 " +
								"OR prov_source_address_tid = 3 )",
						"prov_source_person_tid, prov_source_address_tid");
		
		if (log.isDebugEnabled()) {log.debug("exec query:\n" + query);};
		rs = ConnectionManager.getInstance().execQuery(query);
		
		parser = new SourceAndMapProvParser(rs, "employee");
		
		rep = parser.getAllProv();
		if (log.isDebugEnabled()) {log.debug("result is:\n" + rep);};
		
		ConnectionManager.getInstance().closeRs(rs);
		
		assertEquals(2, rep.size());
		assertEquals(set1, rep.get(0).getValue().getTuplesInProv());
		assertEquals(set2, rep.get(1).getValue().getTuplesInProv());
		assertEquals(maps, rep.get(0).getValue().getMapProv());
		assertEquals(maps, rep.get(1).getValue().getMapProv());
	}
	
}
