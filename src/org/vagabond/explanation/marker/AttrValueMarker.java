package org.vagabond.explanation.marker;

import static org.vagabond.util.HashFNV.fnv;

import org.apache.log4j.Logger;
import org.vagabond.util.LogProviderHolder;
import org.vagabond.util.LoggerUtil;

public class AttrValueMarker implements IAttributeValueMarker {

	static Logger log = LogProviderHolder.getInstance().getLogger(AttrValueMarker.class);
	
	private final int hash;
	private final int relId;
	private final int attrId;
	private final int tidId;
	
	public AttrValueMarker(int relId, int tidId, int attrId) {
		this.relId = relId;
		this.attrId = attrId;
		this.tidId = tidId;
		this.hash = computeHash();
	}
	
	public AttrValueMarker (int relId, String tid, int attrId) throws Exception {
		assert(relId >= 0 && attrId >= 0);
		this.relId = relId;
		this.attrId = attrId;
		this.tidId = ScenarioDictionary.getInstance().getTidInt(tid, relId);
		this.hash = computeHash();
	}
	
	public AttrValueMarker (String relName, String tid, String attrName) throws Exception {
		this.relId = ScenarioDictionary.getInstance().getRelId(relName);
		this.attrId = ScenarioDictionary.getInstance().getAttrId(this.relId, attrName);
		this.tidId = ScenarioDictionary.getInstance().getTidInt(tid, relId);
		this.hash = computeHash();
	}
	
	public AttrValueMarker (String relName, String tid, int attrId) throws Exception {
		this.relId = ScenarioDictionary.getInstance().getRelId(relName);
		this.attrId = attrId;
		this.tidId = ScenarioDictionary.getInstance().getTidInt(tid, relId);
		this.hash = computeHash();
	}
	
	

	private int computeHash() {
		int val = fnv(tidId);
		val = fnv(relId, val);
		return fnv(attrId, val);
	}
	
	@Override
	public boolean equals(Object other) {
		IAttributeValueMarker otherAttr;
		
		if (other == null)
			return false;
		
		if (this == other)
			return true;
		
		if (other instanceof IAttributeValueMarker) {
			otherAttr = (IAttributeValueMarker) other;
			if (otherAttr.getAttrId() != this.attrId)
				return false;
			if (otherAttr.getRelId() != this.relId)
				return false;
			if (otherAttr.getTidId() != this.tidId)
				return false;
			return true;
		}
		
		return false;
	}
	
	@Override
	public int hashCode () {
		return hash; 
	}

	@Override
	public boolean isSubsumed(ISingleMarker other) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getRel() {
		return ScenarioDictionary.getInstance().getRelName(relId);
	}
	
	@Override
	public int getRelId() {
		return relId;
	}

	@Override
	public String getTid() {
		try {
			return ScenarioDictionary.getInstance().getTidString(tidId, relId);
		}
		catch (Exception e) {
			LoggerUtil.logExceptionAndFail(e, log);
		}
		return null; // keep compiler quiet
	}

	@Override
	public String getAttrName() {
		return ScenarioDictionary.getInstance().getAttrName(relId, attrId);
	}

	@Override
	public int getAttrId () {
		return attrId;
	}
	
	public String toString () {
		return "('" + getRel() + "'(" + relId + ")," 
				+ getTid() + ",'" 
				+ getAttrName() + "'(" + attrId + "))";
	}

	@Override
	public int getSize() {
		return 1;
	}

	@Override
	public String toUserString() {
		return "relation: " + getRel() + " tuple: " + getTid() + " attribute: " + getAttrName();
	}

	@Override
	public String toUserStringNoRel() {
		return " tuple: " + getTid() + " attribute: " + getAttrName();
	}

	@Override
	public int getTidId() {
		return tidId;
	}
	
	
}

