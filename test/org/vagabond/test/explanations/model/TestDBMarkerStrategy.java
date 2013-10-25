package org.vagabond.test.explanations.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.vagabond.explanation.marker.DBMarkerStrategy;
import org.vagabond.test.AbstractVagabondTest;
import org.vagabond.util.Enums.Marker_Type;

import java.util.HashSet;
import java.util.Set;

public class TestDBMarkerStrategy 
{
	Set<Marker_Type> left ;
	Set<Marker_Type> right ;
	Set<Marker_Type> out;
	DBMarkerStrategy strat ;
	
	public void initialize () throws Exception {
	
	}
	@Before
	public void setUp () 
	{
		left = new HashSet<Marker_Type>();
		right = new HashSet<Marker_Type>();

		strat = new DBMarkerStrategy();
	}
	
	
	@Test
	public  void testQueryUnion () throws Exception
	{
		Set<Marker_Type> left = new HashSet<Marker_Type>();
		Set<Marker_Type> right = new HashSet<Marker_Type>();
		Set<Marker_Type> out;
		DBMarkerStrategy strat = new DBMarkerStrategy();
		
		left.add(Marker_Type.QUERY_REP);
		right.add(Marker_Type.QUERY_REP);
		
		out = strat.getBinaryOperationOutput(left, right);
		
		assertTrue(out.contains(Marker_Type.QUERY_REP));
		assertTrue(out.size()==1);
		
		
		
	}
	
	@Test
	public  void testJavaUnion() throws Exception
	{
		Set<Marker_Type> left = new HashSet<Marker_Type>();
		Set<Marker_Type> right = new HashSet<Marker_Type>();
		Set<Marker_Type> out;
		DBMarkerStrategy strat = new DBMarkerStrategy();
		
		left.add(Marker_Type.JAVA_REP);
		right.add(Marker_Type.JAVA_REP);
		
		out = strat.getBinaryOperationOutput(left, right);
		
		assertTrue(out.contains(Marker_Type.JAVA_REP));
		assertTrue(out.size()==1);
		
		
		
	}
	
	@Test
	public  void testTableUnion() throws Exception
	{
		Set<Marker_Type> left = new HashSet<Marker_Type>();
		Set<Marker_Type> right = new HashSet<Marker_Type>();
		Set<Marker_Type> out;
		DBMarkerStrategy strat = new DBMarkerStrategy();
		
		left.add(Marker_Type.TABLE_REP);
		right.add(Marker_Type.TABLE_REP);
		
		out = strat.getBinaryOperationOutput(left, right);
		
		assertTrue(out.contains(Marker_Type.TABLE_REP));
		assertTrue(out.size()==1);
		
		
		
	}
	
	@Test
	public  void testQueryTableUnion() throws Exception
	{
		Set<Marker_Type> left = new HashSet<Marker_Type>();
		Set<Marker_Type> right = new HashSet<Marker_Type>();
		Set<Marker_Type> out;
		DBMarkerStrategy strat = new DBMarkerStrategy();
		
		left.add(Marker_Type.QUERY_REP);
		right.add(Marker_Type.TABLE_REP);
		
		out = strat.getBinaryOperationOutput(left, right);
		
		assertTrue(out.contains(Marker_Type.TABLE_REP));
		assertTrue(out.contains(Marker_Type.QUERY_REP));
		assertTrue(out.size()==2);
		
		
		
	}
	
	@Test
	public  void testQueryJavaUnion()
	{
		Set<Marker_Type> left = new HashSet<Marker_Type>();
		Set<Marker_Type> right = new HashSet<Marker_Type>();
		Set<Marker_Type> out;
		DBMarkerStrategy strat = new DBMarkerStrategy();
		
		left.add(Marker_Type.QUERY_REP);
		right.add(Marker_Type.JAVA_REP);
		
		out = strat.getBinaryOperationOutput(left, right);
		
		assertTrue(out.contains(Marker_Type.JAVA_REP));
		assertTrue(out.contains(Marker_Type.QUERY_REP));
		assertTrue(out.size()==2);
		
		
		
	}
	
	@Test
	public  void testTableJavaUnion()
	{
		Set<Marker_Type> left = new HashSet<Marker_Type>();
		Set<Marker_Type> right = new HashSet<Marker_Type>();
		Set<Marker_Type> out;
		DBMarkerStrategy strat = new DBMarkerStrategy();
		
		left.add(Marker_Type.TABLE_REP);
		right.add(Marker_Type.JAVA_REP);
		
		out = strat.getBinaryOperationOutput(left, right);
		
		
		assertTrue(out.contains(Marker_Type.TABLE_REP));
		assertTrue(out.size()==1);
		
		
		
	}
	
	@Test
	public  void testTableQuery_JavaUnion()
	{
		Set<Marker_Type> left = new HashSet<Marker_Type>();
		Set<Marker_Type> right = new HashSet<Marker_Type>();
		Set<Marker_Type> out;
		DBMarkerStrategy strat = new DBMarkerStrategy();
		
		left.add(Marker_Type.TABLE_REP);
		left.add(Marker_Type.QUERY_REP);
		right.add(Marker_Type.JAVA_REP);
		
		out = strat.getBinaryOperationOutput(left, right);
		
		assertTrue(out.contains(Marker_Type.QUERY_REP));
		assertTrue(out.contains(Marker_Type.TABLE_REP));
		assertTrue(out.size()==2);
		
		
		
	}
	
	public  void testTableJava_JavaUnion()
	{
		Set<Marker_Type> left = new HashSet<Marker_Type>();
		Set<Marker_Type> right = new HashSet<Marker_Type>();
		Set<Marker_Type> out;
		DBMarkerStrategy strat = new DBMarkerStrategy();
		
		left.add(Marker_Type.TABLE_REP);
		left.add(Marker_Type.JAVA_REP);
		right.add(Marker_Type.JAVA_REP);
		
		out = strat.getBinaryOperationOutput(left, right);
		
		assertTrue(out.contains(Marker_Type.JAVA_REP));
		assertTrue(out.contains(Marker_Type.TABLE_REP));
		assertTrue(out.size()==2);
		
	}
	
	public  void testTableJavaQuery_QueryTableJavaUnion()
	{
		Set<Marker_Type> left = new HashSet<Marker_Type>();
		Set<Marker_Type> right = new HashSet<Marker_Type>();
		Set<Marker_Type> out;
		DBMarkerStrategy strat = new DBMarkerStrategy();
		
		left.add(Marker_Type.TABLE_REP);
		left.add(Marker_Type.JAVA_REP);
		left.add(Marker_Type.QUERY_REP);
		right.add(Marker_Type.JAVA_REP);
		right.add(Marker_Type.TABLE_REP);
		right.add(Marker_Type.QUERY_REP);
		
		out = strat.getBinaryOperationOutput(left, right);
		
		assertTrue(out.contains(Marker_Type.JAVA_REP));
		assertTrue(out.contains(Marker_Type.TABLE_REP));
		assertTrue(out.contains(Marker_Type.QUERY_REP));
		assertTrue(out.size()==3);
		
	}


}
