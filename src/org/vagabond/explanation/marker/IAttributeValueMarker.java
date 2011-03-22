package org.vagabond.explanation.marker;

public interface IAttributeValueMarker extends ISingleMarker {

	public String getRelName();
	public String getTid();
	public String getAttrName();
	public void setValues (String relName, String tid, String attrName) 
			throws Exception;
}