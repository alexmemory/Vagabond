package org.vagabond.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.InvalidPropertiesFormatException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.xmlbeans.XmlException;
import org.dbunit.Assertion;
import org.dbunit.DBTestCase;
import org.dbunit.PropertiesBasedJdbcDatabaseTester;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.XmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.vagabond.explanation.generation.QueryHolder;
import org.vagabond.mapping.model.ModelLoader;
import org.vagabond.mapping.model.ValidationException;
import org.vagabond.mapping.scenarioToDB.DatabaseScenarioLoader;
import org.vagabond.test.util.TestOptions;

import static org.vagabond.util.LoggerUtil.*;

public abstract class AbstractVagabondDBTest extends DBTestCase  {

	public static Logger log = Logger.getLogger(AbstractVagabondDBTest.class);
	
	private static final String header = "<!DOCTYPE dataset SYSTEM \"dataset.dtd\">\n" +
			"<dataset>\n" +
			"\t<table name=\"transformed\">\n";
	private static final String footer = "\t</table>\n" +
			"</dataset>";
	
	protected Connection con;
	
	public AbstractVagabondDBTest (String name) {
		super(name);
		String url;
		
		try {
			setUpLogger();
			
			url = TestOptions.getInstance().getUrl();
			
			System.setProperty( 
					PropertiesBasedJdbcDatabaseTester.DBUNIT_DRIVER_CLASS, 
					"org.postgresql.Driver");
	        System.setProperty( 
	        		PropertiesBasedJdbcDatabaseTester.DBUNIT_CONNECTION_URL, 
	        		url);
	        System.setProperty( 
	        		PropertiesBasedJdbcDatabaseTester.DBUNIT_USERNAME, 
	        		TestOptions.getInstance().getUser());
	        System.setProperty( 
	        		PropertiesBasedJdbcDatabaseTester.DBUNIT_PASSWORD, 
	        		TestOptions.getInstance().getPassword());
			System.setProperty( 
					PropertiesBasedJdbcDatabaseTester.DBUNIT_SCHEMA, 
					TestOptions.getInstance().getSchemaName());
			
			con = TestOptions.getInstance().getConnection();
		}
		catch (Exception e) {
			System.err.println(e.toString());
			logException(e, log);
			System.exit(1);
		}
		
	}

	@BeforeClass
	public static void setUpLogger () throws FileNotFoundException, IOException, XmlException, ValidationException {
		PropertyConfigurator.configure("resource/test/testLog4jproperties.txt");
		System.out.println("logger setup");
		QueryHolder.getInstance().loadFromDir(new File ("resource/queries"));
	}
	
	@Before
	protected void setUp () throws Exception {
		super.setUp();
	}
	
	@After
    protected void tearDown() throws Exception {
    	super.tearDown();
    }
	
	/* (non-Javadoc)
	 * @see org.dbunit.DatabaseTestCase#getDataSet()
	 */
	@Override
	protected IDataSet getDataSet () throws Exception {
		return null; 
//			new XmlDataSet(new FileInputStream(
//					new File("resource/testdb/smallTestDB.xml")));
	}

    protected DatabaseOperation getSetUpOperation() throws Exception {
        return DatabaseOperation.NONE;
    }

    protected DatabaseOperation getTearDownOperation() throws Exception {
        return DatabaseOperation.NONE;
    }
    
    protected IDatabaseConnection getConnection() throws Exception {
    	return TestOptions.getInstance();
    }
	        
    protected void testSingleQuery (String query, String resultSet) 
    		throws Exception {
    	ITable[] expecteds;
    	ITable actualResult = null;
    	String xmlResultSet;
    	
    	xmlResultSet = transformStringResultSetToXML(resultSet);
    	log.debug("query:\n" + query + "\n\nexpected result:\n" 
    			+ xmlResultSet);
    	expecteds = (new XmlDataSet(new StringReader(xmlResultSet)))
    			.getTables(); 
		
		try {
				actualResult = getConnection().createQueryTable(
						"expectedResult", query);
	    		Assertion.assertEquals(expecteds, actualResult, query);
		}
		catch (Exception e)
		{
			System.out.println("QUERY: " + query + "\n\n");
			throw new Exception(query, e);
		}
    }
	
	private String transformStringResultSetToXML (String resultSet) {
		String[] lines;
		String[] columns;
		String[] rows;
		int numColumns;
		StringBuffer result;
		String value;
		
		result = new StringBuffer();
		result.append(header);
		
		/* split lines */
		lines = resultSet.split("\n");
		
		/* get columns */
		columns = lines[1].split("\\|");
		numColumns = columns.length;
		
		/* output columns */
		for (int i = 0; i < numColumns; i++) {
			result.append("\t\t<column>" + columns[i].trim() + "</column>\n");
		}
		
		/* get lines and output them */
		for (int i = 3; i < lines.length; i++) {
			rows = lines[i].split("\\|");
			
			/* replace escapted '|' characters */
			for(int j = 0; j < numColumns; j++) {
				rows[j] = rows[j].replaceAll("\\$MID\\$", "|");
			}
			
			result.append("\t\t<row>\n\t\t\t");
			
			for (int j = 0; j < rows.length; j++) {
				if (rows[j].trim().equals("")) {
					result.append("<null></null>");
				}
				else if (rows[j].trim().equals("EMPTYSTRING")) {
					result.append("<value></value>");
				}
				else {
					value = rows[j].trim();
					value = escapeXml(value);
					if(value.startsWith("@"))
						value = value.replace('@', ' ');
					result.append("<value>" + value + "</value>");
				}
			}
			result.append("\n\t\t</row>\n");
		}
		
		
		result.append(footer);
		
		//System.out.println(result.toString());
		
		return result.toString();
	}
    
	private String escapeXml (String xml) {
		StringBuilder result;
		char[] chars;
		
		result = new StringBuilder();
		chars = xml.toCharArray();
		
		for(int i = 0; i < chars.length; i++) {
			if (chars[i] == '<')
				result.append("&lt;");
			else if (chars[i] == '>')
				result.append("&gt;");
			else
				result.append(chars[i]);
		}		
		
		return result.toString();
	}
	
	public static void loadToDB (String fileName) throws Exception {
		Connection con = TestOptions.getInstance().getConnection();
		
		ModelLoader.getInstance().loadToInst(fileName);
		DatabaseScenarioLoader.getInstance().loadScenario(con);
	}
}
