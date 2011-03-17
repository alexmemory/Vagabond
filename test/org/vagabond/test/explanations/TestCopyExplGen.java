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
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ITupleMarker;
import org.vagabond.explanation.marker.MarkerFactory;
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
		
		queries = new PropertyWrapper();
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
	public void testExplGen () throws Exception {
		ITupleMarker e1 = MarkerFactory.newTupleMarker("employee", "1|1");
		gen.findExplanations(e1);
	}
	
}
