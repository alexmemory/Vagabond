package org.vagabond.explanation.marker;

import org.apache.log4j.Logger;
import org.vagabond.util.LogProviderHolder;

public class TupleMarker implements ITupleMarker {

	static Logger log = LogProviderHolder.getInstance().getLogger(TupleMarker.class);
	
	private int relId;
	private String tid;
	
	public TupleMarker () {
		
	}
	
	public TupleMarker (int relId, String tid) {
		this.relId = relId;
		this.tid = tid;
	}
	
	public TupleMarker (String rel, String tid) throws Exception {
		this.relId = SchemaResolver.getInstance().getRelId(rel);
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
	public int getSize() {
		return SchemaResolver.getInstance().getTupleSize(relId);
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
		return SchemaResolver.getInstance().getRelName(relId);
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
		return relId + tid.charAt(0);
	}
	
	public int getRelId() {
		return relId;
	}

	public void setRelId(int relId) {
		this.relId = relId;
	}
}
