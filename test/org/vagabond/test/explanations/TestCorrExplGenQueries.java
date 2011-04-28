package org.vagabond.test.explanations;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.vagabond.explanation.generation.CorrespondencExplanationGenerator;
import org.vagabond.explanation.generation.QueryHolder;
import org.vagabond.test.AbstractVagabondDBTest;

public class TestCorrExplGenQueries extends AbstractVagabondDBTest {

	static Logger log = Logger.getLogger(TestCopyExplGenQueries.class);
	
	private CorrespondencExplanationGenerator gen;
	
	public TestCorrExplGenQueries (String name) throws Exception {
		super(name);
		
		loadToDB("resource/test/simpleTest.xml");
		
		gen = new CorrespondencExplanationGenerator();
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
	
	@Test
	public void testClose () throws Exception {
		AbstractVagabondDBTest.closeDown();
	}
}
