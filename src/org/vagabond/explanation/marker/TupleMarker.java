package org.vagabond.explanation.marker;

import static org.vagabond.util.HashFNV.*;

import org.apache.log4j.Logger;
import org.vagabond.util.HashFNV;
import org.vagabond.util.LogProviderHolder;
import org.vagabond.util.LoggerUtil;

public class TupleMarker implements ITupleMarker {

	static Logger log = LogProviderHolder.getInstance().getLogger(TupleMarker.class);
	
	private final int relId;
	private final int tidId;
	private final int hash;
	
	public TupleMarker (int relId, int tidId) {
		this.relId = relId;
		this.tidId = tidId;
		this.hash = computeHash();
	}
	
	public TupleMarker (int relId, String tid) throws Exception {
		this.relId = relId;
		this.tidId = ScenarioDictionary.getInstance().getTidInt(tid, relId);
		this.hash = computeHash();
	}
	
	public TupleMarker (String rel, String tid) throws Exception {
		this.relId = ScenarioDictionary.getInstance().getRelId(rel);
		this.tidId = ScenarioDictionary.getInstance().getTidInt(tid, relId);
		this.hash = computeHash();
	}
	
	private int computeHash() {
		int val = fnv(tidId);
		return fnv(relId, val);
	}
	
	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;
		
		if (this == other)
			return true;
		
		if (other instanceof ITupleMarker) {
			ITupleMarker oMarker = (ITupleMarker) other;
			
			if (this.relId != oMarker.getRelId())
				return false;
			if (this.tidId != oMarker.getTidInt())
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
		try {
			return ScenarioDictionary.getInstance().getTidString(tidId, relId);
		}
		catch (Exception e) {
			LoggerUtil.logExceptionAndFail(e, log);
		}
		return null;
	}

	@Override
	public int getTidInt() {
		return tidId;
	}

	@Override
	public String getRel() {
		return ScenarioDictionary.getInstance().getRelName(relId);
	}

	@Override
	public String toString () {
		return "('" + getRel() + "'(" + getRelId() + "),'" + tidId + "')"; 
	}

	public String toUserString () {
		return "relation: " + getRel() + " tuple: " + getTid();
	}
	
	public String toUserStringNoRel () {
		return "tuple: " + getTid(); 
	}
	
	@Override
	public int hashCode () {
		return hash;
	}
	
	public int getRelId() {
		return relId;
	}

	@Override
	public int getSize() {
		return ScenarioDictionary.getInstance().getTupleSize(relId);
	}

	@Override
	public int getTidId() {
		return tidId;
	}
}
