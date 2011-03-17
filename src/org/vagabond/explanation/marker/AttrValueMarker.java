package org.vagabond.explanation.marker;

import javax.xml.validation.Schema;

import org.apache.log4j.Logger;

public class AttrValueMarker implements IAttributeValueMarker {

	static Logger log = Logger.getLogger(AttrValueMarker.class);
	
	int relId;
	int attrId;
	int tid;
	
	public AttrValueMarker () {
		relId = 0;
		attrId = 0;
		tid = 0;
	}
	
	public AttrValueMarker (int relId, int tid, int attrId) {
		this.relId = relId;
		this.attrId = attrId;
		this.tid = tid;
	}
	
	public AttrValueMarker (String relName, String tid, String attrName) throws Exception {
		this.relId = SchemaResolver.getInstance().getRelId(relName);
		this.attrId = SchemaResolver.getInstance().getAttrId(this.relId, attrName);
		this.tid = Integer.parseInt(tid);
	}
	
	@Override
	public boolean equals(Object other) {
		IAttributeValueMarker otherAttr;
		
		if (other == null)
			return false;
		
		if (other instanceof IAttributeValueMarker) {
			otherAttr = (IAttributeValueMarker) other;
			if (!otherAttr.getAttrName().equals(this.getAttrName()))
				return false;
			if (!otherAttr.getRelName().equals(this.getRelName()))
				return false;
			if (!otherAttr.getTid().equals(this.getTid()))
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
	public String getRelName() {
		return SchemaResolver.getInstance().getRelName(relId);
	}

	@Override
	public String getTid() {
		return "" + tid;
	}

	@Override
	public String getAttrName() {
		return SchemaResolver.getInstance().getAttrName(relId, attrId);
	}
	
	public String toString () {
		return "(" + getRelName() + "," + getTid() + "," + getAttrName() + ")";
	}

	@Override
	public int getSize() {
		return 1;
	}

	@Override
	public void setValues(String relName, String tid, String attrName) throws Exception {
			this.relId = SchemaResolver.getInstance().getRelId(relName);
			this.attrId = SchemaResolver.getInstance().getAttrId(this.relId, attrName);
			this.tid = Integer.parseInt(tid);
	}
	
	
}

