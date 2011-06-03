package org.vagabond.explanation.marker;

public interface IAttributeValueMarker extends ISingleMarker {

	public String getAttrName();
	public int getAttrId();
	public void setValues (String relName, String tid, String attrName) 
			throws Exception;
}
