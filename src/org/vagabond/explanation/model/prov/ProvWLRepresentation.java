package org.vagabond.explanation.model.prov;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ITupleMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.util.LogProviderHolder;

public class ProvWLRepresentation {

	static Logger log = LogProviderHolder.getInstance().getLogger(ProvWLRepresentation.class);
	
	protected List<String> relNames;
	protected IMarkerSet tuplesInProv;
	protected Vector<Vector<ITupleMarker>> witnessLists; 
	private int hash = -1;
	
	public ProvWLRepresentation () {
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
	
	@Override
	public String toString() {
		StringBuffer result;
		
		result = new StringBuffer();
		result.append("PROVENANCE: relnames <" + relNames + ">\n");
		result.append("\ttuples in prov: <" + tuplesInProv + ">\n");
		result.append("\twitness lists: <" + witnessLists + ">");
		
		return result.toString();
	}
	
	@Override
	public int hashCode () {
		if (hash == -1) {
			hash = relNames.hashCode();
			hash = 13 * hash + tuplesInProv.hashCode();
			hash = 13 * hash + witnessLists.hashCode();
		}
			
		return hash;
	}
	
	@Override
	public boolean equals (Object other) {
		if (other == null)
			return false;
		
		if (other == this)
			return true;
		
		if (! (other instanceof ProvWLRepresentation))
			return false;
		
		ProvWLRepresentation oWl = (ProvWLRepresentation) other;
		
		if (!this.relNames.equals(oWl.relNames))
			return false;
		
		if (!this.tuplesInProv.equals(oWl.tuplesInProv))
			return false;
		
		if (!this.witnessLists.equals(oWl.witnessLists))
			return false;
		
		return true;
	}
}
