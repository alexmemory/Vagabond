package org.vagabond.mapping.scenarioToDB;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.vagabond.explanation.marker.DBMarkerSet;
import org.vagabond.util.LogProviderHolder;

public class MaterializedViewsBroker {

	static Logger log = LogProviderHolder.getInstance().getLogger(MaterializedViewsBroker.class);
	
	private static MaterializedViewsBroker instance;
	private static int maxViewId = 0;
		
	private HashMap<DBMarkerSet, Integer> vm = new HashMap<DBMarkerSet, Integer>();
	
	private MaterializedViewsBroker () {
	}
	
	public static synchronized MaterializedViewsBroker getInstance() {
		if (instance == null) {
			instance = new MaterializedViewsBroker();
		}
		
		return instance;
	}
	
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	
	public synchronized int getViewHandler(DBMarkerSet markers) {
		if (vm.containsKey(markers))
			return vm.get(markers);

		maxViewId++;
		vm.put(markers, maxViewId);
		return maxViewId;
	}
	
	public synchronized int getNextViewId() {
		return maxViewId+1;
	}
	
	public void decompose() {
		for (DBMarkerSet o : vm.keySet()) {
			o.decompose();
		}
		vm.clear();
		maxViewId = 0;
	}
	
}
