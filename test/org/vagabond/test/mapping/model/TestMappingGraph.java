package org.vagabond.test.mapping.model;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.junit.Before;
import org.junit.Test;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.ITupleMarker;
import org.vagabond.explanation.marker.MarkerParser;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.mapping.model.MappingGraph;
import org.vagabond.mapping.model.MappingGraph.MappingGraphRel;
import org.vagabond.mapping.model.ValidationException;
import org.vagabond.test.AbstractVagabondTest;
import org.vagabond.util.CollectionUtils;
import org.vagabond.util.Pair;

public class TestMappingGraph extends AbstractVagabondTest {

	static Logger log = Logger.getLogger(TestMappingGraph.class);
	
	@Before
	public void setUp () throws Exception {
		loadToDB("resource/test/simpleTest.xml");
	}
	
	@Test
	public void testSimpleGraph () throws Exception {
		MappingGraph g; 
		MappingGraphRel node;
		
		g = MapScenarioHolder.getInstance().getGraphForMapping("M2");
		log.debug(g);
		
		node = g.getForeachAtomsForVar("a").iterator().next();
		assertEquals(0, node.getPos());
		assertEquals("person", node.getRelName());
		assertEquals(CollectionUtils.makeVec("a","b"), node.getVars());
		
		assertEquals(2, g.getForeachAtomsForVar("b").size());
		assertEquals(1, g.getExistsAtomsForVar("c").size());
		
		g.getJoinVarsAndAtoms("c");
	}
	
	@Test
	public void testGetJoinAttrs () throws Exception {
		Set<Pair<Integer,String>> result;
		MappingGraph g; 

		g = MapScenarioHolder.getInstance().getGraphForMapping("M2");
		result = g.getJoinVarsAndAtoms("c");
		
		log.debug(result);
		
		assertEquals(CollectionUtils.makeSet(new Pair<Integer,String>(0, "b")),
				result);
		
		result = g.getJoinVarsAndAtoms("b");
		assertEquals(CollectionUtils.makeSet(), result);
	}
	
	@Test
	public void testAttrMapping () throws Exception {
		MappingGraph g; 
		int[][][] result, expect = {
				{{0},{1}},
				{{0},{1}}
				};
		
		
		g = MapScenarioHolder.getInstance().getGraphForMapping("M2");
		result = g.getAtomPosToTargetPosMap(0);
		
		logArray(result);
		
		compare3DArray (expect, result);
	}
	
	@Test
	public void testAttrMappingHomeless () throws Exception {
		MappingGraph g; 
		int[][][] result, expect = {
				{{0},{},{},{1}},
				{{0,1},{},{0,1}},
				{{0},{1},{}}
				};
		
		
		loadToDB("resource/exampleScenarios/homelessDebugged.xml");
		g = MapScenarioHolder.getInstance().getGraphForMapping("M1");
		result = g.getAtomPosToTargetPosMap(0);
		
		logArray(result);
		
		compare3DArray (expect, result);
	}
	
	@Test
	public void testGetAtomPosForTargetVar () throws Exception {
		MappingGraph g;
		int[][] result, expect = {
				{},
				{1}
		};

		g = MapScenarioHolder.getInstance().getGraphForMapping("M2");
		result = g.getAtomPosForTargetPos("employee", 1);

		compare2DArray (expect, result);
	}
	
	private void logArray (int[][][] array) {
		StringBuffer result;
		
		result = new StringBuffer ();
		result.append('\n');
		for(int i = 0; i < array.length; i++) {
			result.append("Atom " + i);
			for(int j = 0; j < array[i].length; j++) {
				result.append("\t Var " + j + ": [");
				if (array[i][j] != null) {
					for(int k = 0 ; k < array[i][j].length; k++) {
						if (k != 0)
							result.append(',');
						result.append(array[i][j][k] + "");
					}
				}
				result.append("]\n");
			}
		}
		
		log.debug(result.toString());
	}
	
	private void compare2DArray (int[][] expect, int[][] result) {
		assertEquals(expect.length, result.length);
		for(int i = 0; i < result.length; i++) {
			assertEquals(expect[i].length, result[i].length);
			for(int j = 0; j < result[i].length; j++) {
				assertEquals(expect[i][j], result[i][j]);
			}
		}
	}
	
	private void compare3DArray (int[][][] expect, int[][][] result) {
		assertEquals(expect.length, result.length);
		for(int i = 0; i < result.length; i++) {
			assertEquals(expect[i].length, result[i].length);
			for(int j = 0; j < result[i].length; j++) {
				if (expect[i][j] != null) {
					assertEquals(expect[i][j].length, result[i][j].length);
					for(int k = 0 ; k < result[i][j].length; k++) {
						assertEquals(expect[i][j][k], result[i][j][k]);
					}
				}
			}
		}
	}
}
