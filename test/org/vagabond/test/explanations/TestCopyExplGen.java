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
import org.vagabond.explanation.generation.CopySourceExplanationGenerator;
import org.vagabond.explanation.generation.prov.SourceProvParser;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.marker.MarkerParser;
import org.vagabond.explanation.model.ExplanationFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.CopySourceError;
import org.vagabond.explanation.model.prov.ProvWLRepresentation;
import org.vagabond.test.AbstractVagabondTest;
import org.vagabond.util.ConnectionManager;
import org.vagabond.util.PropertyWrapper;


public class TestCopyExplGen extends AbstractVagabondTest {

	static Logger log = Logger.getLogger(TestCopyExplGen.class);
	
	private static CopySourceExplanationGenerator gen;
	private static PropertyWrapper queries;
	
	@BeforeClass
	public static void setUp () throws Exception {		
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
		loadToDB("resource/test/simpleTest.xml");
		
		ResultSet rs = ConnectionManager.getInstance().execQuery(
			queries.getProperty("copy1"));
		SourceProvParser parser = new SourceProvParser(rs);
		ProvWLRepresentation expl = parser.getAllProv();
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
		loadToDB("resource/test/simpleTest.xml");
		
		IAttributeValueMarker a1 = MarkerFactory.
				newAttrMarker("employee", "1|1", "city");
		IMarkerSet m1 = MarkerFactory.newMarkerSet(
				MarkerFactory.newTupleMarker("address", "1"));
		IExplanationSet eSet;
		CopySourceError e1;
		
		eSet = gen.findExplanations(a1);
		e1 = (CopySourceError) eSet.getExplanations().get(0);
		
		assertEquals(e1.getSourceSideEffects(), m1);
		assertEquals(e1.explains(), a1);
		assertEquals(e1.getTargetSideEffects().getSize(), 0);
	}
	

	
	@Test
	public void testExplGenSideEffect () throws Exception {
		loadToDB("resource/test/simpleTest.xml");
		
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
		
		assertEquals(e1.getSourceSideEffects(), m1);
		assertEquals(e1.explains(), a1);
		assertEquals(e1.getTargetSideEffects(), sideE);
	}
	
	@Test
	public void testHomelessExplGen () throws Exception {
		IExplanationSet result, expec;
		CopySourceError c1;
		IAttributeValueMarker error;
		
		loadToDB("resource/exampleScenarios/homelessDebugged.xml");
		
		error = (IAttributeValueMarker) MarkerParser.getInstance()
				.parseMarker("A(person,1,name)");
		
		c1 = new CopySourceError(error);
		c1.setSourceSE(MarkerParser.getInstance()
				.parseSet("{T(socialworker,1)}"));
		c1.setTargetSE(MarkerParser.getInstance()
				.parseSet("{T(person,2|1|1)}"));
		
		expec = ExplanationFactory.newExplanationSet(c1);
		
		result = gen.findExplanations(error);
		
		assertEquals(expec, result);
	}
	
	@Test
	public void testNormalizeExplGen () throws Exception {
		IExplanationSet result, expec;
		CopySourceError c1;
		IAttributeValueMarker error;
		
		loadToDB("resource/test/targetSkeletonError.xml");
		
		error = (IAttributeValueMarker) MarkerParser.getInstance()
				.parseMarker("A(person,1,name)");
		
		c1 = new CopySourceError(error);
		c1.setSourceSE(MarkerParser.getInstance()
				.parseSet("{T(employee,1)}"));
		c1.setTargetSE(MarkerParser.getInstance()
				.parseSet("{T(address,1)}"));
		
		expec = ExplanationFactory.newExplanationSet(c1);
		
		result = gen.findExplanations(error);
		
		assertEquals(expec, result);
	}
	
}
