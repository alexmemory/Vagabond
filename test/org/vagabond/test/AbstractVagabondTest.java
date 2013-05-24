package org.vagabond.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.PropertyConfigurator;
import org.apache.xmlbeans.XmlException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.vagabond.explanation.generation.QueryHolder;
import org.vagabond.explanation.marker.ScenarioDictionary;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.mapping.model.ModelLoader;
import org.vagabond.mapping.model.ValidationException;
import org.vagabond.mapping.scenarioToDB.DatabaseScenarioLoader;
import org.vagabond.util.ConnectionManager;
import org.vagabond.util.IdMap;

public abstract class AbstractVagabondTest {

	@BeforeClass
	public static void setUpLogger () throws FileNotFoundException, IOException, XmlException, ValidationException, SQLException, ClassNotFoundException {
		PropertyConfigurator.configure("resource/test/testLog4jproperties.txt");
		QueryHolder.getInstance().loadFromDir(new File ("resource/queries"));
	}
	
	@AfterClass
	public static void tearDown () throws Exception {
		TestOptions.getInstance().close();
		ConnectionManager.getInstance().closeCon();
	}
	
	@Before
	public void settUp () throws FileNotFoundException, SQLException, IOException, ClassNotFoundException {
		Connection con = TestOptions.getInstance().getConnection();
		ConnectionManager.getInstance().setConnection(con);
	}
	
	@After
	public void cleanUpp() throws FileNotFoundException, SQLException, IOException, ClassNotFoundException {
		TestOptions.getInstance().close();
		ConnectionManager.getInstance().closeCon();
	}
	
	public static void setSchemas (String fileName) throws Exception {
		MapScenarioHolder holder;
		
		holder = ModelLoader.getInstance().load(new File(fileName));
		MapScenarioHolder.getInstance().setDocument(holder.getDocument());
		ScenarioDictionary.getInstance().setSchemas(
				holder.getScenario().getSchemas().getSourceSchema(),
				holder.getScenario().getSchemas().getTargetSchema());
		ScenarioDictionary.getInstance().setMappings(
				holder.getScenario().getMappings());
		ScenarioDictionary.getInstance().initTidMappingGenerating();
		ScenarioDictionary.getInstance().createOffsetsMapping ();
	}
	
	public static void loadToDB (String fileName) throws Exception {
		Connection con = TestOptions.getInstance().getConnection();
		ModelLoader.getInstance().loadToInst(fileName);
		DatabaseScenarioLoader.getInstance().loadScenario(con);
		ScenarioDictionary.getInstance().initFromScenario();
	}
	
	public static void setTids(String rel, String[] tids) throws Exception {
		int relId = ScenarioDictionary.getInstance().getRelId(rel);
		List<IdMap<String>> tidMaps;
		IdMap<String> tidMap;
		Field tidMapField = ScenarioDictionary.class.getDeclaredField("TidMapping");
		
		tidMapField.setAccessible(true);
		tidMaps = (List<IdMap<String>>) tidMapField.get(ScenarioDictionary.getInstance());
		tidMap = tidMaps.get(relId);
		tidMap.clear();
		
		for(int i = 0; i < tids.length; i++)
			tidMap.put(tids[i]);
	}
	
}
