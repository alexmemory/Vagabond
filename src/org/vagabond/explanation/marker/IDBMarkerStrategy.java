/**
 * 
 */
package org.vagabond.explanation.marker;

import java.util.Set;

import org.vagabond.util.Enums.Marker_Type;

/**
 * @author lord_pretzel
 *
 */
public interface IDBMarkerStrategy {

	public enum CreateStrategy {
		CreateOnSource,
		CreateOnTarget
	}
	
	public Set<Marker_Type> getBinaryOperationOutput (Set<Marker_Type> leftTypes, Set<Marker_Type> rightTypes);
	public void setBinaryOperationOutput(Set<Marker_Type> leftTypes, Set<Marker_Type> rightTypes, Set<Marker_Type> outTypes);
	public CreateStrategy getCreateStrategy ();
	public void setCreateStrategy (CreateStrategy strat);
	
}
