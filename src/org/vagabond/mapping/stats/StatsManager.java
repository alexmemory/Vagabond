package org.vagabond.mapping.stats;

import java.sql.Connection;

import org.apache.log4j.Logger;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.util.ConnectionManager;
import org.vagabond.util.LogProviderHolder;

public class StatsManager {

	static Logger log = LogProviderHolder.getInstance().getLogger(StatsManager.class);
	
	private static StatsManager instance = new StatsManager();;
	
	private StatsData data;
	
	private StatsManager () {
		data = new StatsData();
	}
	
	public static StatsManager getInstance() {
		return instance;
	}
	
	public void refreshStats (Connection con) {
		
	}
	
	public void refreshStats () throws ClassNotFoundException {
		refreshStats(ConnectionManager.
				getInstance().getConnection());
	}
	
	public void initForModel (MapScenarioHolder map) {
		
	}
	
	public void initForModel () {
		initForModel(MapScenarioHolder.getInstance());
	}
}
