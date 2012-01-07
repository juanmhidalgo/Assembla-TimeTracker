package com.starredsolutions.net;

public class RestfulException extends Exception {
	private static final long serialVersionUID = 1L;

	public RestfulException(String detailMessage) {
		super(detailMessage);
	}
	
	public RestfulException(String detailMessage, Throwable cause) {
		super(detailMessage, cause);
	}
	
	@Override
	public String toString() {
		Throwable cause = getCause();
		if (cause != null) {
			return String.format("%s: \"%s\" (caused by %s: \"%s\")", 
				getClassName(this), getMessage(), getClassName(getCause()), getCause().getMessage() );
		} else {
			return String.format("%s: \"%s\"", getClassName(this), getMessage() );
		}
	}
	
	/**
	 * @return class name without package
	 */
	static protected String getClassName(Object o) {
		String name = o.getClass().getName();
	    
		int pos = name.lastIndexOf('.') + 1;
	    if ( pos > 0 ) {
	    	name = name.substring( pos );
	    }
	    return name;
	}
}
