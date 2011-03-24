package org.vagabond.explanation.marker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
		
		switch(marker.charAt(0)) {
		case 'A':
			split = marker.substring(2, marker.length() - 1).split(",");
			return MarkerFactory.newAttrMarker(split[0], split[1], split[2]);
		case 'T':
			split = marker.substring(2, marker.length() - 1).split(",");
			return MarkerFactory.newTupleMarker(split[0], split[1]);
		default:
			throw new Exception ("unknown marker type <" 
					+ marker.charAt(0) + ">");
		}
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
		
		return result;
	}
	
}
