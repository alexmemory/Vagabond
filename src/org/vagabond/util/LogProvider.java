package org.vagabond.util;

import org.apache.log4j.Logger;

public interface LogProvider {

	Logger getLogger(String name);
	Logger getLogger(Class<?> clazz);
}
