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
	private IDBMarkerStrategy strat;
	
	private DefaultDBMarkerStrategy (IDBMarkerStrategy strat) {
		this.strat = strat;
	}
	
	public static IDBMarkerStrategy getStrat() {
		return inst.strat;
	}
	
	public static void setStratgey(IDBMarkerStrategy strat) {
		inst.strat = strat;
	}
	
}
