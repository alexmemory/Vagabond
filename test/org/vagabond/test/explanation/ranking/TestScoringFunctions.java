package org.vagabond.test.explanation.ranking;

import static org.junit.Assert.assertEquals;

import java.util.Comparator;

import org.junit.Test;
import org.vagabond.explanation.generation.ExplanationSetGenerator;
import org.vagabond.explanation.marker.MarkerParser;
import org.vagabond.explanation.model.ExplanationFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.CopySourceError;
import org.vagabond.explanation.ranking.scoring.ExplanationSizeScore;
import org.vagabond.explanation.ranking.scoring.IScoringFunction;
import org.vagabond.explanation.ranking.scoring.ScoreExplSetComparator;
import org.vagabond.explanation.ranking.scoring.SideEffectSizeScore;
import org.vagabond.test.AbstractVagabondTest;

public class TestScoringFunctions extends AbstractVagabondTest {
	
	private ExplanationSetGenerator explSetGen = new ExplanationSetGenerator();

	
	private void setUp (String filename) throws Exception {
		loadToDB(filename);
	}
	
	@Test
	public void testSideEffScoring () throws Exception {
		IScoringFunction f = SideEffectSizeScore.inst;
		Comparator<IExplanationSet> comp = new ScoreExplSetComparator(f);
		
		setUp ("resource/exampleScenarios/homelessDebugged.xml");
		
		// copy error
		CopySourceError c1 = new CopySourceError();
		c1.setExplains(MarkerParser.getInstance().parseMarker("A(person,2,name)"));
		c1.setSourceSE(MarkerParser.getInstance().parseSet("{A(socialworker,1,name)}"));
		c1.setTargetSE(MarkerParser.getInstance().parseSet("{}"));
				
		CopySourceError c2 = new CopySourceError();
		c2.setExplains(MarkerParser.getInstance().parseMarker("A(person,1,name)"));
		c2.setSourceSE(MarkerParser.getInstance().parseSet("{A(socialworker,1,name)}"));
		c2.setTargetSE(MarkerParser.getInstance().parseSet("{A(person,3,name)}"));

		CopySourceError c3 = new CopySourceError();
		c3.setExplains(MarkerParser.getInstance().parseMarker("A(person,2|1|1,name)"));
		c3.setSourceSE(MarkerParser.getInstance().parseSet("{A(socialworker,1,name)}"));
		c3.setTargetSE(MarkerParser.getInstance().parseSet("{A(person,3,name),A(person,1|3|2,name)}"));

		IExplanationSet e1 = ExplanationFactory.newExplanationSet(c1,c2);
		IExplanationSet e2 = ExplanationFactory.newExplanationSet(c3);
		
		assertEquals(1, f.getScore(e1));
		assertEquals(2, f.getScore(e2));
		assertEquals(-1, comp.compare(e1, e2));
		assertEquals(1, comp.compare(e2, e1));
		assertEquals(0, comp.compare(e1, e1));
		assertEquals(0, comp.compare(e2, e2));
	}
	
	@Test
	public void testExplSizeScoring () throws Exception {
		IScoringFunction f = ExplanationSizeScore.inst;
		Comparator<IExplanationSet> comp = new ScoreExplSetComparator(f);
		
		setUp ("resource/exampleScenarios/homelessDebugged.xml");
		
		// copy error
		CopySourceError c1 = new CopySourceError();
		c1.setExplains(MarkerParser.getInstance().parseMarker("A(person,2,name)"));
		c1.setSourceSE(MarkerParser.getInstance().parseSet("{A(socialworker,1,name)}"));
		c1.setTargetSE(MarkerParser.getInstance().parseSet("{}"));
				
		CopySourceError c2 = new CopySourceError();
		c2.setExplains(MarkerParser.getInstance().parseMarker("A(person,1,name)"));
		c2.setSourceSE(MarkerParser.getInstance().parseSet("{A(socialworker,1,name)}"));
		c2.setTargetSE(MarkerParser.getInstance().parseSet("{A(person,3,name)}"));

		CopySourceError c3 = new CopySourceError();
		c3.setExplains(MarkerParser.getInstance().parseMarker("A(person,2|1|1,name)"));
		c3.setSourceSE(MarkerParser.getInstance().parseSet("{A(socialworker,1,name)}"));
		c3.setTargetSE(MarkerParser.getInstance().parseSet("{A(person,3,name),A(person,1|3|2,name)}"));

		IExplanationSet e1 = ExplanationFactory.newExplanationSet(c1,c2);
		IExplanationSet e2 = ExplanationFactory.newExplanationSet(c3);
		
		assertEquals(2, f.getScore(e1));
		assertEquals(1, f.getScore(e2));
		assertEquals(1, comp.compare(e1, e2));
		assertEquals(-1, comp.compare(e2, e1));
		assertEquals(0, comp.compare(e1, e1));
		assertEquals(0, comp.compare(e2, e2));
		
	}
	

}
