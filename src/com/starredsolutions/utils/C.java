package com.starredsolutions.utils;

public class C {
	/**
	 * @return class name without package
	 */
	static public String getClassName(Object o) {
		if (o != null) {
			String name = o.getClass().getName();
		    
			int pos = name.lastIndexOf('.') + 1;
		    if ( pos > 0 ) {
		    	name = name.substring( pos );
		    }
		    return name;
		} else {
			return "Null Object";
		}
	}
}
