package org.vagabond.test.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;

import org.junit.Before;
import org.junit.Test;
import org.vagabond.test.AbstractVagabondTest;
import org.vagabond.test.TestOptions;
import org.vagabond.util.ConnectionManager;
import org.vagabond.util.ResultSetUtil;

public class TestResultSetUtil extends AbstractVagabondTest {

	@Before
	public void setUp () throws Exception {
		loadToDB("resource/test/simpleTest.xml");
	}
	
	
	
	@Test
	public void testProvAttrSplitters () {
		String[] names = {"prov_public_r_a", 
						"prov_public_r_1_b", 
						"prov_target_rel__with__underscore_name"};
		assertEquals(ResultSetUtil.getAttrFromProvName(names[0]),"a");
		assertEquals(ResultSetUtil.getAttrFromProvName(names[1]),"b");
		assertEquals(ResultSetUtil.getAttrFromProvName(names[2]),"name");
		
		assertEquals(ResultSetUtil.getRelFromProvName(names[0]),"r");
		assertEquals(ResultSetUtil.getRelFromProvName(names[1]),"r");
		assertEquals(ResultSetUtil.getRelFromProvName(names[2]),"rel_with_underscore");
		
		assertTrue(ResultSetUtil.isProvAttr(names[0]));
		assertTrue(ResultSetUtil.isProvAttr(names[1]));
		assertTrue(ResultSetUtil.isProvAttr(names[2]));
	}
	
	@Test 
	public void testRelExtraction () {
		String[] cols = {"tid","name","city","prov_source_person_tid",
				"prov_source_person_name","prov_source_person_address",
				"prov_source_address_tid","prov_source_address_id",
				"prov_source_address_city"};
		
		assertArrayEquals(ResultSetUtil.getBaseRelsForProvSchema(cols).toArray(), 
				new String[] {"person","address"});
		
		cols = ("city,prov_public_address_id,prov_public_address_city," +
				"prov_public_address_street,prov_public_address_number," +
				"prov_public_address_1_id,prov_public_address_1_city," +
				"prov_public_address_1_street,prov_public_address_1_number").split(",");
		
		assertArrayEquals(ResultSetUtil.getBaseRelsForProvSchema(cols).toArray(), 
				new String[] {"address","address_1"});
	}
	
	@Test
	public void test () throws Exception {
		String[] cols;
		ResultSet rs;
		Connection con = TestOptions.getInstance().getConnection();
		
		rs = ConnectionManager.getInstance().execQuery(con,
				"SELECT * FROM source.person");
		cols = ResultSetUtil.getResultColumns(rs);
		ConnectionManager.getInstance().closeRs(rs);
		assertArrayEquals(cols, new String[] {"tid","name","address"});
		
		rs = ConnectionManager.getInstance().execQuery(con,
				"SELECT PROVENANCE tid FROM source.person");
		cols = ResultSetUtil.getResultColumns(rs);
		ConnectionManager.getInstance().closeRs(rs);
		assertArrayEquals(cols, new String[] {"tid","prov_source_person_tid",
				"prov_source_person_name","prov_source_person_address"});
	}
	
}
