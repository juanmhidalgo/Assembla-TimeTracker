package com.starredsolutions.assemblandroid.exceptions;


public abstract class AbstractTimeTrackerException extends Exception {
	private static final long serialVersionUID = 2629662784746273349L;

	/**
	 * Copy these two methods TimeTrackerException(String) & TimeTrackerException(String,Throwable) 
	 * in the child class.
	 **/
	
	public AbstractTimeTrackerException(String detailMessage) {
		super(detailMessage);
	}
	
	public AbstractTimeTrackerException(String detailMessage, Throwable cause) {
		super(detailMessage, cause);
	}
	
	@Override
	public String toString() {
		Throwable cause = getCause();
		
		return cause.getMessage();
	}
}
