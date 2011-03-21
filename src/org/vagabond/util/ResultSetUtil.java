package org.vagabond.util;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;


public class ResultSetUtil {

	static Logger log = Logger.getLogger(ResultSetUtil.class);
	
	private static ResultSetUtil instance = new ResultSetUtil();
	
	private ResultSetUtil () {
		
	}
	
	public static ResultSetUtil getInstance() {
		return instance;
	}
	
	public static List<String> getBaseRelsForProvSchema (String[] attrs) {
		Set<String> rels;
		
		rels = new HashSet<String> ();
		
		for(String attr: attrs) {
			if (isProvAttr(attr))
				rels.add(getNumberedRelFromProvName(attr));
		}
		
		return new ArrayList<String> (rels);
	}
	
	public static String[] getResultColumns (ResultSet rs) throws SQLException {
		String[] result;
		int colCount;
		ResultSetMetaData meta = rs.getMetaData();
		
		colCount = meta.getColumnCount();
		result = new String[colCount];
		
		for(int i = 0; i < colCount; i++) {
			result[i] = meta.getColumnName(i+1);
		}
		
		return result;
	}
	
	public static String[] splitProvAttrName (String name) {
		Vector<String> result;
		char[] chars;
		StringBuffer temp;
		
		result = new Vector<String> ();
		temp = new StringBuffer ();
		chars = name.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] == '_' && chars[i+1] == '_') {
				i++;
				temp.append('_');
			}
			else if (chars[i] == '_') {
				result.add(temp.toString());
				temp = new StringBuffer ();
			}
			else {
				temp.append(chars[i]);
			}
		}
		result.add(temp.toString());
		
		return result.toArray(new String[] {});
	}
	
	public static String getAttrFromProvName (String name) {
		String[] split;
		
		split = splitProvAttrName (name);
		return split[split.length - 1];
	}
	
	public static String getNumberedRelFromProvName (String name) {
		String[] split;
		
		split = splitProvAttrName (name);
		if (split.length == 4)
			return split[2];
		else 
			return split[2] + "_" + split[3];
	}
	
	public static String getRelFromProvName (String name) {
		String[] split;
		
		split = splitProvAttrName (name);
		return split[2];
	}
	
	public static String getBaseRel (String numberedRel) {
		if (numberedRel.contains("_"))
			return numberedRel.substring(0, numberedRel.lastIndexOf('_'));
		return numberedRel;
	}
	
	public static boolean isProvAttr (String name) {
		return name.startsWith("prov_");
	}
}
