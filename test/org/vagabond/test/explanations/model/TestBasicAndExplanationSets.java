package org.vagabond.test.explanations.model;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Comparator;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.MarkerParser;
import org.vagabond.explanation.marker.ScenarioDictionary;
import org.vagabond.explanation.model.ExplanationFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.CopySourceError;
import org.vagabond.explanation.model.basic.CorrespondenceError;
import org.vagabond.explanation.model.basic.ExplanationComparators;
import org.vagabond.explanation.model.basic.IBasicExplanation;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.mapping.model.ModelLoader;
import org.vagabond.test.AbstractVagabondTest;
import org.vagabond.xmlmodel.CorrespondenceType;
import org.vagabond.xmlmodel.MappingType;

public class TestBasicAndExplanationSets extends AbstractVagabondTest {

	static Logger log = Logger.getLogger(TestBasicAndExplanationSets.class);
	
	@Before
	public void setUp () throws Exception {
		loadToDB("resource/exampleScenarios/homelessDebugged.xml");
	}
	
	@Test
	public void testBasicExplanations () throws Exception {
		CopySourceError c1, c2;
		CorrespondenceError r1,r2;
		HashSet<CorrespondenceType> corrs;
		HashSet<MappingType> maps;
		
		// copy error
		c1 = new CopySourceError();
		c1.setExplains(MarkerParser.getInstance().parseMarker("A(person,2,name)"));
		c1.setSourceSE(MarkerParser.getInstance().parseSet("{T(socialworker,1)}"));
		c1.setTargetSE(MarkerParser.getInstance().parseSet("{}"));
				
		c2 = new CopySourceError();
		c2.setExplains(MarkerParser.getInstance().parseMarker("A(person,2,name)"));
		c2.setSourceSE(MarkerParser.getInstance().parseSet("{T(socialworker,1)}"));
		c2.setTargetSE(MarkerParser.getInstance().parseSet("{}"));
				
		assertEquals(c1,c2);
		
		c1.setTargetSE(MarkerParser.getInstance().parseSet("{T(person,2|1|1)}"));
		
		assertFalse(c1.equals(c2));
		assertFalse(c2.equals(c1));
		
		c1.setTargetSE(MarkerParser.getInstance().parseSet("{}"));
		c1.setSourceSE(MarkerParser.getInstance().parseSet("{T(socialworker,2)}"));
		
		assertFalse(c1.equals(c2));
		assertFalse(c2.equals(c1));
		
		c1.setSourceSE(MarkerParser.getInstance().parseSet("{T(socialworker,1)}"));
		c1.setExplains(MarkerParser.getInstance().parseMarker("A(person,1,name)"));
		
		assertFalse(c1.equals(c2));
		assertFalse(c2.equals(c1));
		
		// correspondence error
		corrs = new HashSet<CorrespondenceType> ();
		corrs.add(MapScenarioHolder.getInstance().getCorr("c2"));
		maps = new HashSet<MappingType> ();
		maps.add(MapScenarioHolder.getInstance().getMapping("M2"));
		
		r1 = new CorrespondenceError();
		r1.setExplains(MarkerParser.getInstance().parseMarker("A(person,1,name)"));
		r1.setCorrSE(corrs);
		r1.setMapSE(maps);
		r1.setTargetSE(MarkerParser.getInstance().parseSet("{}"));
		
		r2 = new CorrespondenceError();
		r2.setExplains(MarkerParser.getInstance().parseMarker("A(person,1,name)"));
		r2.setCorrSE(corrs);
		r2.setMapSE(maps);
		r2.setTargetSE(MarkerParser.getInstance().parseSet("{}"));
		
		assertEquals(r1,r2);
	}
	
	@Test
	public void testHashingAndEqualsForBasic () throws Exception {
		CopySourceError c1, c2, c3, c4;
		HashSet<IBasicExplanation> set = new HashSet<IBasicExplanation> ();
		
		// copy error
		c1 = new CopySourceError();
		c1.setExplains(MarkerParser.getInstance().parseMarker("A(person,2,name)"));
		c1.setSourceSE(MarkerParser.getInstance().parseSet("{T(socialworker,1)}"));
		c1.setTargetSE(MarkerParser.getInstance().parseSet("{}"));
				
		c2 = new CopySourceError();
		c2.setExplains(MarkerParser.getInstance().parseMarker("A(person,2,name)"));
		c2.setSourceSE(MarkerParser.getInstance().parseSet("{T(socialworker,1)}"));
		c2.setTargetSE(MarkerParser.getInstance().parseSet("{}"));
		
		int h1 = c1.hashCode();
		int h2 = c2.hashCode();
		
		set.add(c1);
		set.add(c2);
		IMarkerSet s = c1.getTargetSideEffects();
		s.add(MarkerParser.getInstance().parseMarker("A(person,3,name)"));
		c1.setTargetSE(s);
		
		int h1a = c1.hashCode();
		assertTrue(h1 + " " + h1a, h1 == h1a);
		assertTrue(set.contains(c1));
		
		IMarkerSet s2 = c1.getRealTargetSideEffects();
		s2.add(MarkerParser.getInstance().parseMarker("A(person,3,name)"));
		c1.setRealTargetSideEffects(s2);
		
		// equals
		IMarkerSet errors = MarkerParser.getInstance().parseSet("{A(person,1,name),A(person,2,name)}");
		
		c3 = new CopySourceError();
		c3.setExplains(MarkerParser.getInstance().parseMarker("A(person,1,name)"));
		c3.setSourceSE(MarkerParser.getInstance().parseSet("{T(socialworker,1)}"));
		c3.setTargetSE(MarkerParser.getInstance().parseSet("{A(person,2,name)}"));
				
		c4 = new CopySourceError();
		c4.setExplains(MarkerParser.getInstance().parseMarker("A(person,2,name)"));
		c4.setSourceSE(MarkerParser.getInstance().parseSet("{T(socialworker,1)}"));
		c4.setTargetSE(MarkerParser.getInstance().parseSet("{A(person,1,name)}"));
		
		c3.computeRealTargetSEAndExplains(errors);
		c4.computeRealTargetSEAndExplains(errors);
		
		assertEquals(c3,c4);
		assertEquals(0, c3.getRealTargetSideEffectSize());
		assertEquals(0, c4.getRealTargetSideEffectSize());
	}
	
	@Test
	public void testExplanationSet () throws Exception {
		CopySourceError c1, c2;
		IExplanationSet set1, set2, set3;

		c1 = new CopySourceError();
		c1.setExplains(MarkerParser.getInstance().parseMarker("A(person,2,name)"));
		c1.setSourceSE(MarkerParser.getInstance().parseSet("{T(socialworker,1)}"));
		c1.setTargetSE(MarkerParser.getInstance().parseSet("{}"));
				
		c2 = new CopySourceError();
		c2.setExplains(MarkerParser.getInstance().parseMarker("A(person,3,name)"));
		c2.setSourceSE(MarkerParser.getInstance().parseSet("{T(socialworker,2)}"));
		c2.setTargetSE(MarkerParser.getInstance().parseSet("{}"));
		
		set1 = ExplanationFactory.newExplanationSet(c1,c2);
		set2 = ExplanationFactory.newExplanationSet(c2,c1);
		set3 = ExplanationFactory.newExplanationSet(c1,c1);
		
		assertTrue(set1.equals(set2));
		assertTrue(set2.equals(set1));
		assertTrue(set1.hashCode() == set2.hashCode());
		
		assertTrue(set1.contains(c1));
		assertTrue(set1.contains(c2));
		
		assertTrue(set2.contains(c1));
		assertTrue(set2.contains(c2));
		
		assertTrue(set3.contains(c1));
		assertFalse(set3.contains(c2));
	}
	
	@Test
	public void testComparators () throws Exception {
		Comparator<IBasicExplanation> C1 = ExplanationComparators.fullSideEffWithTie;
		Comparator<IExplanationSet> SC1 = ExplanationComparators.setIndElementComp;
		
		CopySourceError c1, c2, c3;
		IExplanationSet set1, set2, set3, set4;

		c1 = new CopySourceError();
		c1.setExplains(MarkerParser.getInstance().parseMarker("A(person,2,name)"));
		c1.setSourceSE(MarkerParser.getInstance().parseSet("{T(socialworker,1)}"));
		c1.setTargetSE(MarkerParser.getInstance().parseSet("{}"));
				
		c2 = new CopySourceError();
		c2.setExplains(MarkerParser.getInstance().parseMarker("A(person,3,name)"));
		c2.setSourceSE(MarkerParser.getInstance().parseSet("{T(socialworker,2)}"));
		c2.setTargetSE(MarkerParser.getInstance().parseSet("{}"));

		c3 = new CopySourceError();
		c3.setExplains(MarkerParser.getInstance().parseMarker("A(person,3,name)"));
		c3.setSourceSE(MarkerParser.getInstance().parseSet("{T(socialworker,2)}"));
		c3.setTargetSE(MarkerParser.getInstance().parseSet("{}"));
		
		set1 = ExplanationFactory.newExplanationSet(c1,c2);
		set2 = ExplanationFactory.newExplanationSet(c2,c1);
		set3 = ExplanationFactory.newExplanationSet(c1,c1);
		set4 = ExplanationFactory.newExplanationSet(c1,c2,c3);
		
		// compare basic explanations
		assertEquals(-1, C1.compare(c1, c2));
		assertEquals(1, C1.compare(c2, c1));
		assertEquals(0, C1.compare(c2, c3));
		assertEquals(0, C1.compare(c3, c2));
		
		// compare explanation sets
		assertEquals(0, SC1.compare(set1, set2));
		assertEquals(0, SC1.compare(set2, set1));
		assertEquals(0, SC1.compare(set1, set4));
		assertEquals(0, SC1.compare(set4, set1));
		assertEquals(0, SC1.compare(set4, set2));
		assertEquals(0, SC1.compare(set2, set4));
		
		assertEquals(1, SC1.compare(set1, set3));
		assertEquals(-1, SC1.compare(set3, set1));
		
		assertEquals(-1, SC1.compare(set3, set4));
		assertEquals(1, SC1.compare(set4, set3));
	}
}
