package org.vagabond.explanation.marker;

import static org.vagabond.util.HashFNV.*;

import org.apache.log4j.Logger;
import org.vagabond.util.LogProviderHolder;

public class TupleMarker implements ITupleMarker {

	static Logger log = LogProviderHolder.getInstance().getLogger(TupleMarker.class);
	
	private int relId;
	private String tid;
	private int hash = -1;
	
	public TupleMarker () {
		
	}
	
	public TupleMarker (int relId, String tid) {
		this.relId = relId;
		this.tid = tid;
	}
	
	public TupleMarker (String rel, String tid) throws Exception {
		this.relId = ScenarioDictionary.getInstance().getRelId(rel);
		this.tid = tid;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;
		
		if (this == other)
			return true;
		
		if (other instanceof ITupleMarker) {
			ITupleMarker oMarker = (ITupleMarker) other;
			
			if (!(this.getRelId() == oMarker.getRelId()))
				return false;
			if (!this.getTid().equals(oMarker.getTid()))
				return false;
			
			return true;
		}
		
		return false;
	}

	@Override
	public boolean isSubsumed(ISingleMarker other) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getTid() {
		return "" + tid;
	}

	@Override
	public int getTidInt() {
		return Integer.parseInt(tid);
	}

	@Override
	public String getRel() {
		return ScenarioDictionary.getInstance().getRelName(relId);
	}

	@Override
	public String toString () {
		return "('" + getRel() + "'(" + getRelId() + "),'" + tid + "')"; 
	}

	public String toUserString () {
		return "relation: " + getRel() + " tuple: " + getTid();
	}
	
	public String toUserStringNoRel () {
		return "tuple: " + getTid(); 
	}
	
	@Override
	public int hashCode () {
		if (hash == -1) {
			hash = fnv(tid.hashCode());
			hash = fnv(relId, hash);
		}
		return hash;
	}
	
	public int getRelId() {
		return relId;
	}

	public void setRelId(int relId) {
		this.relId = relId;
		hash = -1;
	}

	@Override
	public int getSize() {
		return ScenarioDictionary.getInstance().getTupleSize(relId);
	}
}
