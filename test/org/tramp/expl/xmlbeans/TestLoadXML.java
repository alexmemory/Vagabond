package org.tramp.expl.xmlbeans;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.PropertyConfigurator;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tramp.expl.model.MapScenarioHolder;
import org.tramp.expl.scenarioToDB.DatabaseScenarioLoader;
import org.tramp.expl.scenarioToDB.SchemaCodeGenerator;
import org.tramp.xmlmodel.MappingScenarioDocument;
import org.tramp.xmlmodel.MappingScenarioDocument.MappingScenario;

import static org.junit.Assert.*;
import static org.tramp.xmlmodel.MappingScenarioDocument.MappingScenario.Factory.*;


public class TestLoadXML {

	static Connection con;
	
	@BeforeClass public static void setUp () throws ClassNotFoundException, SQLException {
		PropertyConfigurator.configure("resource/test/testLog4jproperties.txt");
		Class.forName("org.postgresql.Driver");
		con = DriverManager.getConnection(
				"jdbc:postgresql://127.0.0.1:5432/tramptest", "postgres", "");
	}
	
	@AfterClass public static void tearDown () throws SQLException {
		con.close();
	}
	
	@Test public void loadXml () throws XmlException, IOException {
		MappingScenario mapScen = loadAnXml("resource/test/testScenario.xml");
		assertTrue(validate(mapScen));
		assertEquals(mapScen.getConnectionInfo().getHost(),"localhost");
		assertEquals(mapScen.getMappings().getMappingArray(0).getId(), "M1");
		
		mapScen = loadAnXml("resource/test/testWithCopy.xml");
		assertTrue(validate(mapScen));
		assertEquals(mapScen.getConnectionInfo().getHost(),"localhost");
		assertEquals(mapScen.getMappings().getMappingArray(0).getId(), "M1");
	}
	
	@Test public void genSchemaDDL () throws XmlException, IOException {
		String ddl;
		
		MappingScenario mapScen = loadAnXml("resource/test/testScenario.xml");
		ddl = SchemaCodeGenerator.getInstance().getSchemasCode(
				mapScen);
		assertEquals(ddl, loadString("resource/test/testDDL.sql"));
	}
	
	@Test public void genLoadScript () throws XmlException, IOException {
		String script;
		
		MappingScenario mapScen = loadAnXml("resource/test/testScenario.xml");
		script = SchemaCodeGenerator.getInstance().getSchemaPlusInstanceCode(mapScen);
		assertEquals(script, loadString("resource/test/testDDLWithData.sql"));
		
		mapScen = loadAnXml("resource/test/testWithCopy.xml");
		script = SchemaCodeGenerator.getInstance().getSchemaPlusInstanceCode(mapScen);
		assertEquals(script, loadString("resource/test/testDDLWithCopy.sql"));
	}
	
	@Test public void testExecuteLoad () throws SQLException, XmlException, IOException {
		MappingScenarioDocument mapDoc = MappingScenarioDocument.Factory.
				parse(new File("resource/test/testScenario.xml"));
		DatabaseScenarioLoader.getInstance().loadScenario(con, new MapScenarioHolder(mapDoc));
		
		mapDoc = MappingScenarioDocument.Factory.
			parse(new File("resource/test/testWithCopy.xml"));
		DatabaseScenarioLoader.getInstance().loadScenario(con, new MapScenarioHolder(mapDoc));
	}
	
	private MappingScenario loadAnXml (String file) throws XmlException, IOException {
		MappingScenarioDocument doc = 
			MappingScenarioDocument.Factory.parse(new File(file));
		return doc.getMappingScenario();
	}
	
	private boolean validate (MappingScenario map) {
		List<?> errors = new ArrayList();
		XmlOptions options = new XmlOptions ();
		boolean result;
		
		options.setErrorListener(errors);
		
		result = map.validate(options);
		for(Object error: errors) {
			System.out.println(error);
		}
		
		return result;
	}
	
	private String loadString (String fileName) throws IOException {
		File file = new File(fileName);
		BufferedReader in = new BufferedReader(new FileReader(file));
		StringBuffer inStr = new StringBuffer();
		
		while(in.ready()) {
			inStr.append(in.readLine() + "\n");
		}
		in.close();
		
		return inStr.toString();
	}
 }
