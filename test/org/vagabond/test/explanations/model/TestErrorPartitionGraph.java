package org.vagabond.test.explanations.model;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.vagabond.explanation.generation.partition.ErrorPartitionGraph;
import org.vagabond.explanation.generation.partition.ErrorPartitionGraph.ErrorGraphNodeType;
import org.vagabond.explanation.generation.partition.ErrorPartitionGraph.ErrorNode;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.marker.MarkerParser;
import org.vagabond.explanation.marker.MarkerSummary;
import org.vagabond.test.AbstractVagabondTest;
import org.vagabond.util.BitMatrix;
import org.vagabond.util.CollectionUtils;
import org.vagabond.util.ewah.EWAHCompressedBitmap;
import org.vagabond.util.ewah.BitsetView;

public class TestErrorPartitionGraph extends AbstractVagabondTest {

	static Logger log = Logger.getLogger(TestErrorPartitionGraph.class);
	
	static final String[] edgeRows = new String[] {
		"01110001 0000010",
		"10110000 1100000",
		"11010000 0010000",
		"11100000 0000001",
		"00000110 0001001",
		"00001010 0000100",
		"00001100 0000010",
		"10000000 0000000",
		"01000000 0000000",
		"01000000 0000000",
		"00100000 0000000",
		"00001000 0000000",
		"00000100 0000000",
		"10000010 0000000",
		"00011000 0000000"};
	
	public void testMappingComp (String mapName, String[] vars, ErrorPartitionGraph g) throws Exception {
		for(String var: vars) {
			for(String otherVar: vars) {
				if (! var.equals(otherVar)) {
					assertTrue(mapName + "." + var + " <-> " + mapName + "." + otherVar, 
							g.hasEdge(g.getNode(ErrorGraphNodeType.MappingVar, mapName, var), 
									g.getNode(ErrorGraphNodeType.MappingVar, mapName, otherVar)));
				}
			}
		}
	}
	
	public void checkRelVarConn (ErrorPartitionGraph g) {
		int off = g.getSourceOffset();
		
		// check that source and target nodes are  not connected to each other
		for(int i = off; i < g.getNodes().size(); i++) {
			BitsetView v = (BitsetView) g.getEdges().getReadonlyRow(i);
			v = v.getView(off, v.sizeInBits());
			assertFalse(g.getNodes().get(i).toString(), v.intIterator().hasNext());
		}
	}
	
	public void checkSym (ErrorPartitionGraph g) {
		BitMatrix m = g.getEdges();
		
		for(int i = 0; i < m.getRows(); i++) {
			for(int j = 0; j < m.getCols(); j++)
				if (m.get(i, j))
					assertTrue(m.get(j, i));
				else
					assertFalse(m.get(j, i));
		}
	}
	
	@Test
	public void testSimpleGraph () throws Exception {
		ErrorPartitionGraph g;
		loadToDB("resource/test/simpleTest.xml");
		g = new ErrorPartitionGraph();
		
		log.debug(g.toString());
		
		checkSym(g);
		
		// nodes
		assertEquals("node M1.a", "M:0.0", g.getNode(ErrorGraphNodeType.MappingVar, "M1", "a").toString());
		assertEquals("node M1.b", "M:0.1", g.getNode(ErrorGraphNodeType.MappingVar, "M1", "b").toString());
		assertEquals("node M1.c", "M:0.2", g.getNode(ErrorGraphNodeType.MappingVar, "M1", "c").toString());
		
		assertEquals("node M2.a", "M:1.0", g.getNode(ErrorGraphNodeType.MappingVar, "M2", "a").toString());
		assertEquals("node M2.b", "M:1.1", g.getNode(ErrorGraphNodeType.MappingVar, "M2", "b").toString());
		assertEquals("node M2.c", "M:1.2", g.getNode(ErrorGraphNodeType.MappingVar, "M2", "c").toString());
		
		assertEquals("node person.name", "S:0.0", g.getNode(ErrorGraphNodeType.SourceAttr, "person", "name").toString());
		assertEquals("node address.city", "S:1.1", g.getNode(ErrorGraphNodeType.SourceAttr, "address", "city").toString());
		
		
		// edges between attributes and mapping vars
		assertTrue("M1.a <-> person.name", 
				g.hasEdge(g.getNode(ErrorGraphNodeType.MappingVar, "M1", "a"), 
						g.getNode(ErrorGraphNodeType.SourceAttr, "person", "name")));
		
		testMappingComp("M1", new String[] {"a","b","c"}, g);
		testMappingComp("M2", new String[] {"a","b","c"}, g);
		
		// other
		assertEquals("source offset", 6, g.getSourceOffset());
		checkRelVarConn(g);
	}
	
	@Test
	public void testSeveralCompGraph () throws Exception {
		ErrorPartitionGraph g;
		loadToDB("resource/test/severalComps.xml");
		g = new ErrorPartitionGraph();
		
		log.debug(g.toString());
		
		checkSym(g);
		
		// nodes
		assertEquals("node M1.a", "M:0.0", g.getNode(ErrorGraphNodeType.MappingVar, "M1", "a").toString());
		assertEquals("node M1.b", "M:0.1", g.getNode(ErrorGraphNodeType.MappingVar, "M1", "b").toString());
		assertEquals("node M1.c", "M:0.2", g.getNode(ErrorGraphNodeType.MappingVar, "M1", "c").toString());
		
		assertEquals("node r.r1", "S:0.0", g.getNode(ErrorGraphNodeType.SourceAttr, "r", "r1").toString());
		assertEquals("node v.v1", "T:4.0", g.getNode(ErrorGraphNodeType.TargetAttr, "v", "v1").toString());
		
		// edges between attributes and mapping vars
		assertTrue("M1.a <-> u.u1", 
				g.hasEdge(g.getNode(ErrorGraphNodeType.MappingVar, "M1", "a"), 
						g.getNode(ErrorGraphNodeType.SourceAttr, "u", "u1")));
		
		testMappingComp("M1", new String[] {"a","b","c"}, g);
		testMappingComp("M2", new String[] {"a","b"}, g);
		
		log.debug(g.getEdges().getBitmap().toBitsString());
		
		assertEquals("source offset", 5, g.getSourceOffset());
		checkRelVarConn(g);
	}
	
	@Test
	public void testConnComponentsSimple () throws Exception {
		ErrorPartitionGraph g;
		Set<ErrorNode> nodes, compNodes;
		loadToDB("resource/test/simpleTest.xml");
		g = new ErrorPartitionGraph();
		
		g.getComponents();
		nodes = new HashSet<ErrorNode>();
		nodes.addAll(g.getNodes().values());
		compNodes = new HashSet<ErrorNode>();
		compNodes.addAll(g.getNodesForComponents(g.getNode(ErrorGraphNodeType.MappingVar, 0, 0)));
		
		assertEquals(1, g.getNumComponents());
		assertEquals(nodes, compNodes);
	}
	
	@Test
	public void testConnComponents () throws Exception {
		ErrorPartitionGraph g;
		Set<ErrorNode> nodes, compNodes;
		
		loadToDB("resource/test/severalComps.xml");
		g = new ErrorPartitionGraph();
		
		log.debug(g);
		log.debug(g.getEdges());
		
		g.getComponents();
		nodes = new HashSet<ErrorNode>();
		nodes.addAll(CollectionUtils.filter(g.getNodes(), new EWAHCompressedBitmap("1110011110010").iterator()));
		compNodes = new HashSet<ErrorNode>();
		compNodes.addAll(g.getNodesForComponents(g.getNode(ErrorGraphNodeType.MappingVar, 0, 0)));
		
		log.debug(g.getComponents());
		
		assertEquals(2, g.getNumComponents());
		assertEquals(nodes, compNodes);
	}
	
	@Test
	public void testParitioning () throws Exception {
		ErrorPartitionGraph g;
		
		loadToDB("resource/test/severalComps.xml");
		g = new ErrorPartitionGraph();
		
		MarkerSummary ex1 = MarkerParser.getInstance().parseMarkerSummary("{S(u,u1)}");
		MarkerSummary ex2 = MarkerParser.getInstance().parseMarkerSummary("{S(v,v1)}");
		
		g.getComponents();
		List<MarkerSummary> sum = g.paritionAttrs(MarkerParser.getInstance().parseMarkerSummary("{S(u,u1),S(v,v1)}"));
		
		assertTrue(sum.contains(ex1));
		assertTrue(sum.contains(ex2));
	}
	
}
