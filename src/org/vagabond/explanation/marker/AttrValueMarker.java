package org.vagabond.explanation.marker;

import javax.xml.validation.Schema;

import org.apache.log4j.Logger;

public class AttrValueMarker implements IAttributeValueMarker {

	static Logger log = Logger.getLogger(AttrValueMarker.class);
	
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
		this.relId = SchemaResolver.getInstance().getRelId(relName);
		this.attrId = SchemaResolver.getInstance().getAttrId(this.relId, attrName);
		this.tid = tid;
	}
	
	public AttrValueMarker (String relName, String tid, int attrId) throws Exception {
		this.relId = SchemaResolver.getInstance().getRelId(relName);
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
			hash = tid.hashCode() * 31 * 31 + relId * 31 + attrId;
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
		return SchemaResolver.getInstance().getRelName(relId);
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
		return SchemaResolver.getInstance().getAttrName(relId, attrId);
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
			this.relId = SchemaResolver.getInstance().getRelId(relName);
			this.attrId = SchemaResolver.getInstance().getAttrId(this.relId, attrName);
			this.tid = tid;
	}
	
	
}

