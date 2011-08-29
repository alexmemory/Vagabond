package org.vagabond.util;

import org.apache.log4j.Logger;

public class LogProviderHolder implements LogProvider {

	private static LogProviderHolder instance = new LogProviderHolder();
	private LogProvider provider = null;
	
	private LogProviderHolder () {
		
	}
	
	public static LogProviderHolder getInstance () {
		return instance;
	}
	
	public void setLogProvider (LogProvider newProvider) {
		this.provider = newProvider;
	}
	
	@Override
	public Logger getLogger(String name) {
		if (provider == null)
			return Logger.getLogger(name);

		return provider.getLogger(name);
	}

	@Override
	public Logger getLogger(Class<?> clazz) {
		if (provider == null)
			return Logger.getLogger(clazz);

		return provider.getLogger(clazz);	
	}

	
}
