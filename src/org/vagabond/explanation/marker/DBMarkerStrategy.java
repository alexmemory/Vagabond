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
public interface DBMarkerStrategy {

	public enum CreateStrategy {
		CreateOnSource,
		CreateOnTarget
	}
	
	public Set<Marker_Type> getBinaryOperationOutput (Set<Marker_Type> leftTypes, Set<Marker_Type> rightTypes);
	public CreateStrategy getCreateStrategy (Marker_Type typeToCreate);
	
}
