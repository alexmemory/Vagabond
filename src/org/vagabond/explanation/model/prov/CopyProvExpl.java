package org.vagabond.explanation.model.prov;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ITupleMarker;
import org.vagabond.explanation.marker.MarkerFactory;

public class CopyProvExpl {

	static Logger log = Logger.getLogger(CopyProvExpl.class);
	
	private List<String> relNames;
	private IMarkerSet tuplesInProv;
	private Vector<Vector<ITupleMarker>> witnessLists; 

	
	public CopyProvExpl () {
		witnessLists = new Vector<Vector<ITupleMarker>> ();
		tuplesInProv = MarkerFactory.newMarkerSet();
		relNames = new ArrayList<String> ();
	}

	public List<String> getRelNames() {
		return relNames;
	}

	public void setRelNames(List<String> relNames) {
		this.relNames = relNames;
	}

	public IMarkerSet getTuplesInProv() {
		return tuplesInProv;
	}

	public void setTuplesInProv(IMarkerSet tuplesInProv) {
		this.tuplesInProv = tuplesInProv;
	}
	
	public void addTupleInProv (ITupleMarker tuple) {
		this.tuplesInProv.add(tuple);
	}

	public Vector<Vector<ITupleMarker>> getWitnessLists() {
		return witnessLists;
	}

	public void setWitnessLists(Vector<Vector<ITupleMarker>> witnessLists) {
		this.witnessLists = witnessLists;
	}
	
	public void addWitnessList(Vector<ITupleMarker> witnessList) {
		this.witnessLists.add(witnessList);
	}
	
	public Vector<ITupleMarker> getWitnessList (int i) {
		return this.witnessLists.get(i);
	}	
}
