package org.vagabond.mapping.model;

import java.util.ArrayList;
import java.util.List;
/**
 * Exception for errors in XML validation.
 * 
 * @author Boris Glavic
 */
public class ValidationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final List<?> errors;
	
	public ValidationException () {
		errors = new ArrayList ();
	}
	
	public ValidationException (final List<?> errors) {
		this.errors = errors;
	}
	
	public ValidationException (final String message, final List<?> errors) {
		super(message);
		this.errors = errors;
	}
	
	public ValidationException (final String message) {
		super(message);
		errors = new ArrayList();
	}
	
		
	public String getMessage () {
		return super.getMessage() + getErrorString();
	}
	
	public String getLocalizedMessage () {
		return super.getLocalizedMessage() + getErrorString();
	}
	
	public String getErrorString () {
		final StringBuffer result = new StringBuffer ();
		
		for(final Object error: errors) {
			result.append("\n" + error.toString());
		}
		
		return result.toString();
	}
}
