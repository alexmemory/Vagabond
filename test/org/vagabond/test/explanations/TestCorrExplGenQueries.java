package org.vagabond.test.explanations;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.junit.Test;
import org.vagabond.explanation.generation.CopySourceExplanationGenerator;
import org.vagabond.explanation.generation.CorrespondencExplanationGenerator;
import org.vagabond.explanation.generation.QueryHolder;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.mapping.model.ModelLoader;
import org.vagabond.mapping.model.ValidationException;
import org.vagabond.mapping.scenarioToDB.DatabaseScenarioLoader;
import org.vagabond.test.AbstractVagabondDBTest;
import org.vagabond.test.AbstractVagabondTest;

public class TestCorrExplGenQueries extends AbstractVagabondDBTest {

	static Logger log = Logger.getLogger(TestCopyExplGenQueries.class);
	
	private CorrespondencExplanationGenerator gen;
	
	public TestCorrExplGenQueries (String name) throws Exception {
		super(name);
		
		File mapFile = new File("resource/test/simpleTest.xml");		
		MapScenarioHolder map = ModelLoader.getInstance().load(mapFile);
		
		DatabaseScenarioLoader.getInstance().loadScenario(con, map);
		
		gen = new CorrespondencExplanationGenerator();
		
		AbstractVagabondTest.setSchemas("resource/test/simpleTest.xml");
	}
	
	@Test
	public void testGetMapProvQuery () throws Exception {
		String query = QueryHolder.getQuery("Correspondence.GetMapProv")
				.parameterize("target.employee","2|2");
		String result =  "\ntrans_prov\n" + 
				"-----\n" +
				 "M2";

		testSingleQuery(query, result);
	}
	
	@Test
	public void testSideEffectsQuery () throws Exception {
		String query = QueryHolder.getQuery("Correspondence.GetSideEffects")
				.parameterize("target.employee", "('M1')");
		String result = "\n tid\n" +
				"-----\n" +
				"3$MID$"; 
			
		testSingleQuery(query, result);
		
		query = QueryHolder.getQuery("Correspondence.GetSideEffects")
				.parameterize("target.employee", "('M2')");
		result = "\n tid\n" +
		"-----\n" +
		"1$MID$1\n"+
		"2$MID$2\n"+
		"4$MID$2";

		testSingleQuery(query, result);
	}
}
