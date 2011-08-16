package org.vagabond.util;

import org.apache.log4j.Logger; import org.vagabond.util.LogProviderHolder;

public interface LogProvider {

	Logger getLogger(String name);
	Logger getLogger(Class clazz);
}
