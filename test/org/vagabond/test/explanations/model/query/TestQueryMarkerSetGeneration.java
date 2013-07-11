package org.vagabond.test.explanations.model.query;

import static org.junit.Assert.*;
import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.MarkerParser;
import org.vagabond.explanation.marker.MarkerSet;
import org.vagabond.explanation.marker.query.QueryMarkerSetGenerator;
import org.vagabond.test.AbstractVagabondTest;
import org.vagabond.test.explanations.model.TestBasicAndExplanationSets;

/**
 * 
 * @author lord_pretzel
 *
 */
public class TestQueryMarkerSetGeneration extends AbstractVagabondTest {
	
	static Logger log = Logger.getLogger(AbstractVagabondTest.class);
	
	/**
	 * @throws Exception
	 */
	@Before
	public void setUp () throws Exception {
		loadToDB("resource/exampleScenarios/homelessDebugged.xml");
		loadToDB("resource/test/simpleTest.xml");
	}
		
	/**
	 * @throws Exception
	 
	@Test
	public void testUsingSimpleQuery () throws Exception {
		IMarkerSet expected = MarkerParser.getInstance().parseSet("{(0,1,0)}");
		String query = "SELECT 'person' AS rel,'name' AS attr, tid" +
						"FROM target.person WHERE livesIn = 'Toronto'";
		IMarkerSet actual = QueryMarkerSetGenerator.getInstance().genMSetFromQuery (query);
		
		assertEquals(expected, actual);
	}
	*/
	@Test
	public void testMarkerSetGeneration() throws Exception
	{
		QueryMarkerSetGenerator  a = new QueryMarkerSetGenerator();
		IMarkerSet actual = a.genMSetFromQuery("SELECT 'person' AS rel, 'address' AS attr, tid FROM source.person ;");
		MarkerSet test = new MarkerSet();
		test.add("person", "address", 1);
		test.add("person", "address", 2);
		test.add("person", "address", 3);
		test.add("person", "address", 4);
		IMarkerSet expected = test;
		assertEquals(expected, actual);
		
	}
	
	
}
