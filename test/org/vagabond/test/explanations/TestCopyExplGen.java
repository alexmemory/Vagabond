package org.vagabond.test.explanations;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.junit.BeforeClass;
import org.junit.Test;


import org.vagabond.explanation.generation.CopyCSParser;
import org.vagabond.explanation.generation.CopySourceExplanationGenerator;
import org.vagabond.explanation.generation.QueryHolder;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ITupleMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.CopySourceError;
import org.vagabond.explanation.model.prov.CopyProvExpl;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.mapping.model.ModelLoader;
import org.vagabond.mapping.model.ValidationException;
import org.vagabond.mapping.scenarioToDB.DatabaseScenarioLoader;
import org.vagabond.test.AbstractVagabondTest;
import org.vagabond.util.ConnectionManager;
import org.vagabond.util.PropertyWrapper;

import static org.junit.Assert.*;


public class TestCopyExplGen extends AbstractVagabondTest {

	static Logger log = Logger.getLogger(TestCopyExplGen.class);
	
	private static MapScenarioHolder map;
	private static CopySourceExplanationGenerator gen;
	private static PropertyWrapper queries;
	
	@BeforeClass
	public static void setUp () throws SQLException, ClassNotFoundException, XmlException, IOException, ValidationException {
		Connection con = ConnectionManager.getInstance().getConnection("localhost", 
				"tramptest", "postgres", "");
		
		File mapFile = new File("resource/test/simpleTest.xml");		
		map = ModelLoader.getInstance().load(mapFile);
		
		DatabaseScenarioLoader.getInstance().loadScenario(con, map);
		
		gen = new CopySourceExplanationGenerator();
		
		setSchemas("resource/test/simpleTest.xml");
		
		queries = new PropertyWrapper("resource/queries/CopyCS.xml");
		queries.setProperty("copy1", 
				"SELECT PROVENANCE ON CONTRIBUTION (COPY PARTIAL TRANSITIVE) " +
				"p.name, a.city " +
				"FROM source.person p, source.address a " +
				"WHERE p.address = a.id AND name = 'Peter';");
		
		QueryHolder.getInstance().loadFromDir(new File("resource/queries"));
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
	public void testExplGenSideEffect () throws Exception {
		IAttributeValueMarker a1 = MarkerFactory.
				newAttrMarker("employee", "2|2", "city");
		IMarkerSet m1 = MarkerFactory.newMarkerSet(
				MarkerFactory.newTupleMarker("address", "2"));
		IMarkerSet sideE = MarkerFactory.newMarkerSet(
				MarkerFactory.newTupleMarker("employee", "2|2"),
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
