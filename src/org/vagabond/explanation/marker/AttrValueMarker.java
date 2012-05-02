package org.vagabond.explanation.marker;

import org.apache.log4j.Logger;
import org.vagabond.util.LogProviderHolder;
import static org.vagabond.util.HashFNV.*;

public class AttrValueMarker implements IAttributeValueMarker {

	static Logger log = LogProviderHolder.getInstance().getLogger(AttrValueMarker.class);
	
	private int hash = 0;
	private int relId;
	private int attrId;
	private String tid;
	
	public AttrValueMarker () {
		relId = -1;
		attrId = -1;
		tid = null;
	}
	
	public AttrValueMarker (int relId, String tid, int attrId) {
		this.relId = relId;
		this.attrId = attrId;
		this.tid = tid;
	}
	
	public AttrValueMarker (String relName, String tid, String attrName) throws Exception {
		this.relId = ScenarioDictionary.getInstance().getRelId(relName);
		this.attrId = ScenarioDictionary.getInstance().getAttrId(this.relId, attrName);
		this.tid = tid;
	}
	
	public AttrValueMarker (String relName, String tid, int attrId) throws Exception {
		this.relId = ScenarioDictionary.getInstance().getRelId(relName);
		this.attrId = attrId;
		this.tid = tid;
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
			if (!otherAttr.getAttrName().equals(this.getAttrName()))
				return false;
			if (!otherAttr.getRel().equals(this.getRel()))
				return false;
			if (!otherAttr.getTid().equals(this.getTid()))
				return false;
			return true;
		}
		
		return false;
	}
	
	@Override
	public int hashCode () {
		if (hash == 0) {
			hash = fnv(tid.getBytes());
			hash = fnv(relId, relId);
			hash = fnv(attrId);
		}
		
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
		return "" + tid;
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
	public void setValues(String relName, String tid, String attrName) throws Exception {
			this.relId = ScenarioDictionary.getInstance().getRelId(relName);
			this.attrId = ScenarioDictionary.getInstance().getAttrId(this.relId, attrName);
			this.tid = tid;
			hash = 0;
	}

	@Override
	public String toUserString() {
		return "relation: " + getRel() + " tuple: " + getTid() + " attribute: " + getAttrName();
	}

	@Override
	public String toUserStringNoRel() {
		return " tuple: " + getTid() + " attribute: " + getAttrName();
	}
	
	
}

