/**
 * 
 */
package org.vagabond.explanation.marker;

/**
 * @author lord_pretzel
 *
 */
public class DefaultDBMarkerStrategy {

	private static DefaultDBMarkerStrategy inst = new DefaultDBMarkerStrategy(null);
	private DBMarkerStrategy strat;
	
	private DefaultDBMarkerStrategy (DBMarkerStrategy strat) {
		this.strat = strat;
	}
	
	public static DBMarkerStrategy getStrat() {
		return inst.strat;
	}
	
	public static void setStratgey(DBMarkerStrategy strat) {
		inst.strat = strat;
	}
	
}
