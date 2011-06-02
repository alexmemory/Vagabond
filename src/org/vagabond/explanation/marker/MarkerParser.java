package org.vagabond.explanation.marker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

import org.apache.log4j.Logger;

public class MarkerParser {

	static Logger log = Logger.getLogger(MarkerParser.class);
	
	private static MarkerParser inst = new MarkerParser ();
	
	private MarkerParser () {
		
	}
	
	public static MarkerParser getInstance () {
		return inst;
	}
	
	public ISingleMarker parseMarker (String marker) throws Exception {
		String[] split;
		ISingleMarker result;
		
		marker = marker.trim();
		switch(marker.charAt(0)) {
		case 'A':
			split = marker.substring(2, marker.length() - 1).split(",");
			result = MarkerFactory.newAttrMarker(split[0], split[1], split[2]);
			break;
		case 'T':
			split = marker.substring(2, marker.length() - 1).split(",");
			result =  MarkerFactory.newTupleMarker(split[0], split[1]);
			break;
		default:
			throw new Exception ("unknown marker type <" 
					+ marker.charAt(0) + ">");
		}
		
		log.debug("parsed marker: <" + result + ">");
		
		return result;
	}
	
	public IMarkerSet parseMarkers (InputStream in) throws Exception {
		IMarkerSet result;
		BufferedReader read;
		ISingleMarker mark;
		
		read = new BufferedReader (new InputStreamReader (in));
		
		result = MarkerFactory.newMarkerSet();
		
		while(read.ready()) {
			mark = parseMarker(read.readLine());
			result.add(mark);
		}
		
		log.debug("parsed markers from input stream :<" + result + ">");
		
		return result;
	}
	
	public Vector<ITupleMarker> parseWL (String wl) throws Exception {
		String elemString;
		Vector<ITupleMarker> result;
		Vector<String> elems;
		
		result = new Vector<ITupleMarker> ();
		
		elemString = wl.substring(wl.indexOf('{') + 1, wl.indexOf('}') + 1);
		elems = getElems (elemString);
		
		for(String elem: elems) {
			if (elem.matches("\\s*null\\s*"))
				result.add(null);
			else 
				result.add((ITupleMarker) parseMarker(elem));
		}
		
		return result;
	}
	
	public IMarkerSet parseSet (String set) throws Exception {
		String elemString;
		Vector<String> elems;
		IMarkerSet result;
		StringBuffer element;
		
		result = MarkerFactory.newMarkerSet();
		
		// handle empty set
		if (set.matches("\\s*\\{\\s*\\}\\s*"))
			return result;
		
		elemString = set.substring(set.indexOf('{') + 1, set.lastIndexOf('}') + 1);
		elems = getElems (elemString);
		
		for (String elem: elems) {
			result.add(parseMarker(elem));
		}
		
		log.debug("parsed marker set: <" + result + ">");
		
		return result;
	}
	
	private Vector<String> getElems (String elemString) {
		Vector<String> elems = new Vector<String> ();
		StringBuffer element;
		int bracketDepth = 0;
		
		element = new StringBuffer();
		for(char c: elemString.toCharArray()) {
			switch(c) {
			case '(':
				bracketDepth++;
				element.append(c);
				break;
			case ')':
				bracketDepth--;
				element.append(c);
				break;
			case ',':
				if (bracketDepth == 0) {
					elems.add(element.toString());
					element = new StringBuffer();
				}
				else
					element.append(c);
				break;
			case '}':
				elems.add(element.toString());
				break;
			default:
				element.append(c);
			}
		}
		
		return elems;
	}
}
