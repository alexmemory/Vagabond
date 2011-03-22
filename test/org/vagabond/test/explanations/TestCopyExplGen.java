package org.vagabond.test.explanations;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vagabond.explanation.generation.CopyCSParser;
import org.vagabond.explanation.generation.CopySourceExplanationGenerator;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.CopySourceError;
import org.vagabond.explanation.model.prov.CopyProvExpl;
import org.vagabond.test.AbstractVagabondTest;
import org.vagabond.util.ConnectionManager;
import org.vagabond.util.PropertyWrapper;


public class TestCopyExplGen extends AbstractVagabondTest {

	static Logger log = Logger.getLogger(TestCopyExplGen.class);
	
	private static CopySourceExplanationGenerator gen;
	private static PropertyWrapper queries;
	
	@BeforeClass
	public static void setUp () throws Exception {		
		loadToDB("resource/test/simpleTest.xml");
		
		gen = new CopySourceExplanationGenerator();
				
		queries = new PropertyWrapper("resource/queries/CopyCS.xml");
		queries.setProperty("copy1", 
				"SELECT PROVENANCE ON CONTRIBUTION (COPY PARTIAL TRANSITIVE) " +
				"p.name, a.city " +
				"FROM source.person p, source.address a " +
				"WHERE p.address = a.id AND name = 'Peter';");
	}
	
	@Test
	public void testCopyProvParser () throws Exception {
		ResultSet rs = ConnectionManager.getInstance().execQuery(
			queries.getProperty("copy1"));
		CopyCSParser parser = new CopyCSParser(rs);
		CopyProvExpl expl = parser.getAllProv();
		IMarkerSet mSet = MarkerFactory.newMarkerSet();
		mSet.add(MarkerFactory.newTupleMarker("address", "1"));
		mSet.add(MarkerFactory.newTupleMarker("person", "1"));
		
		assertArrayEquals(expl.getRelNames().toArray(), new String[] {
				"person","address"});
		
		log.debug(expl.getTuplesInProv().toString());
		assertEquals(expl.getTuplesInProv(), mSet);
	}
	
	@Test
	public void testExplGenNoSide () throws Exception {
		IAttributeValueMarker a1 = MarkerFactory.
				newAttrMarker("employee", "1|1", "city");
		IMarkerSet m1 = MarkerFactory.newMarkerSet(
				MarkerFactory.newTupleMarker("address", "1"));
		IExplanationSet eSet;
		CopySourceError e1;
		
		eSet = gen.findExplanations(a1);
		e1 = (CopySourceError) eSet.getExplanations().get(0);
		
		assertEquals(e1.getSourceSE(), m1);
		assertEquals(e1.explains(), a1);
		assertEquals(e1.getSideEffects().getSize(), 0);
	}
	
	@Test
	public void testSideEffectQueryGen () throws Exception {
		Set<String> sourceRels;
		Map<String, IMarkerSet> sourceErr;
		IMarkerSet errSet, errSet2;
		String resultQuery;
		String query;
		//TODO handle multiple accesses to same base rel
		query = "SELECT prov.tid\n" +
				"FROM\n" +
				"(SELECT *\n" +
				"FROM target.employee) AS prov\n" +
				"WHERE NOT EXISTS (SELECT subprov.tid\n" +
				"FROM (SELECT PROVENANCE * FROM target.employee) AS subprov\n" + 
				"WHERE prov.tid = subprov.tid " +
				"AND (prov_source_address_tid IS DISTINCT FROM 2 " +
				"AND prov_source_address_tid IS DISTINCT FROM 3 ))";
		errSet = MarkerFactory.newMarkerSet(
				MarkerFactory.newTupleMarker("employee", "1")
				);
		errSet2 = MarkerFactory.newMarkerSet(
				MarkerFactory.newTupleMarker("address", "2"),
				MarkerFactory.newTupleMarker("address", "3")
				);
		sourceErr = new HashMap<String, IMarkerSet> ();
		sourceErr.put("employee", errSet);
		sourceErr.put("address", errSet2);
		
		sourceRels = new HashSet<String> ();
		sourceRels.add("address");
		sourceRels.add("person");
		
		resultQuery = gen.getSideEffectQuery("employee", sourceRels, sourceErr).trim();
		log.debug(resultQuery);
		
		assertEquals(query, resultQuery);
	}
	
	@Test
	public void testExplGenSideEffect () throws Exception {
		IAttributeValueMarker a1 = MarkerFactory.
				newAttrMarker("employee", "2|2", "city");
		IMarkerSet m1 = MarkerFactory.newMarkerSet(
				MarkerFactory.newTupleMarker("address", "2"));
		IMarkerSet sideE = MarkerFactory.newMarkerSet(
				MarkerFactory.newTupleMarker("employee", "4|2"));
		IExplanationSet eSet;
		CopySourceError e1;
		
		eSet = gen.findExplanations(a1);
		e1 = (CopySourceError) eSet.getExplanations().get(0);
		
		assertEquals(e1.getSourceSE(), m1);
		assertEquals(e1.explains(), a1);
		assertEquals(e1.getSideEffects(), sideE);
	}
	
}
