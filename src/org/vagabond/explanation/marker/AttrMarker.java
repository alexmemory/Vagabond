package org.vagabond.explanation.marker;

import static org.vagabond.util.HashFNV.*;

import org.apache.log4j.Logger;
import org.vagabond.explanation.marker.ScenarioDictionary;
import org.vagabond.util.LoggerUtil;

public class AttrMarker implements ISchemaMarker {

	static Logger log = Logger.getLogger(AttrMarker.class);
	
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
	
	public AttrMarker (int relId, int attrId) throws Exception {
		this.relId = relId;
		this.attrId = attrId;
		if (!ScenarioDictionary.getInstance().validateAttrId(relId, attrId))
			throw new Exception ("Invalid relId <" + relId + "> or attrId <" + attrId + ">");
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
		try {
			return ScenarioDictionary.getInstance().getAttrName(relId, attrId);
		} catch (Exception e) { // should never be executed anyways
			LoggerUtil.logException(e, log);
			return null;
		}
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
		if (hash == -1) {
			hash = fnv(relId);
			hash = fnv(attrId, hash);
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

	public int getAttrId() {
		return attrId;
	}

	public void setAttrId(int attrId) {
		this.attrId = attrId;
		hash = -1;
	}
	
}
