package org.vagabond.mapping.scenarioToDB;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.vagabond.explanation.marker.MarkerSetFlattenedView;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.util.LogProviderHolder;
import org.vagabond.xmlmodel.DataType;
import org.vagabond.xmlmodel.RelInstanceFileType;
import org.vagabond.xmlmodel.RelInstanceType;
import org.vagabond.xmlmodel.RelInstanceType.Row;

public class MaterializedViewsBroker {

	static Logger log = LogProviderHolder.getInstance().getLogger(MaterializedViewsBroker.class);
	
	private static MaterializedViewsBroker instance;
	private static int maxViewId = 0;
		
	private HashMap<MarkerSetFlattenedView, Integer> vm = new HashMap<MarkerSetFlattenedView, Integer>();
	
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
	
	public synchronized int getViewHandler(MarkerSetFlattenedView markers) {
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
		for (MarkerSetFlattenedView o : vm.keySet()) {
			o.decompose();
		}
		vm.clear();
		maxViewId = 0;
	}
	
}
