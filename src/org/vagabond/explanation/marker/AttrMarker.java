package org.vagabond.explanation.marker;

public class AttrMarker implements ISchemaMarker {

	private int relId;
	private int attrId;
	private int hash = -1;
	private String cachedToString = null;
	
	public AttrMarker () {
		relId = -1;
		attrId = -1;
	}
	
	public AttrMarker (String relName, String attrName) throws Exception {
		relId = ScenarioDictionary.getInstance().getRelId(relName);
		attrId = ScenarioDictionary.getInstance().getAttrId(relId, attrName);
	}
	
	public AttrMarker (int relId, int attrId) {
		this.relId = relId;
		this.attrId = attrId;
	}
	
	@Override
	public String toString () {
		if (cachedToString == null)
			cachedToString = "(" + getRelName() + ", " + getAttrName() + ")";
		
		return cachedToString;
	}
	
	public String getRelName () {
		return ScenarioDictionary.getInstance().getRelName(relId);
	}
	
	public String getAttrName() {
		return ScenarioDictionary.getInstance().getAttrName(relId, attrId);
	}
	
	
	
	@Override
	public boolean equals (Object other) {
		if (other == null)
			return false;
		
		if (other == this)
			return true;
		
		if (other instanceof AttrMarker) {
			AttrMarker a = (AttrMarker) other;
			return a.relId == this.relId 
					&& a.attrId == this.attrId;
		}
		
		return false;
	}
	
	@Override
	public int hashCode () {
		if (hash == -1)
			hash = 13 * relId + attrId;
		
		return hash;
	}

	public int getRelId() {
		return relId;
	}

	public void setRelId(int relId) {
		this.relId = relId;
	}

	public int getAttrId() {
		return attrId;
	}

	public void setAttrId(int attrId) {
		this.attrId = attrId;
	}
	
}
