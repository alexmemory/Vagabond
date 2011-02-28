/**
 * 
 */
package org.tramp.util;

import org.apache.log4j.Logger;


/**
 *
 * Part of Project SesamUtil
 * @author Boris Glavic
 *
 */
public class LoggerUtil {

	public static void logException (Exception e, Logger log) {
		log.error(getCompleteTrace(e));
	}
	
	public static void logDebugException (Exception e, Logger log) {
		log.debug(getCompleteTrace(e));
	}
	
	public static String getCompleteTrace (Exception e) {
		StringBuilder trace;
		Throwable exception;
		
		trace = new StringBuilder ();
		trace.append(getStackString (e));
		exception = e;
		while (exception.getCause() != null) {
			exception = exception.getCause();
			trace.append("\ncaused by:\n");
			trace.append(getStackString (exception));
		}
		
		return trace.toString();
	}
	
	private static String getStackString (Throwable e) {
		StringBuilder stackString;
		StackTraceElement[] stack;
		
		stack = e.getStackTrace();
		stackString = new StringBuilder ();
		stackString.append("Exception occured: " + e.getClass().getName() + "\n");
		stackString.append("Message: " + e.getMessage() + "\n");
		for (int i = 0; i < stack.length; i++) {
			stackString.append(stack[i].toString() + "\n");
		}
		return stackString.toString();
	}
	
	public static String arrayToString (String[] array) {
		StringBuilder result;
		
		result = new StringBuilder();
		
		for (String elem: array) {
			result.append("'" + elem + "',");
		}
		result.deleteCharAt(result.length() - 1);
		
		return result.toString();
	}
	
	
}
