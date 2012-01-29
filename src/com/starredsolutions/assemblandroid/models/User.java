package com.starredsolutions.assemblandroid.models;

import java.util.List;

import com.starredsolutions.assemblandroid.AssemblaAPIAdapter;
import com.starredsolutions.assemblandroid.TimeTrackerApplication;
import com.starredsolutions.assemblandroid.exceptions.AssemblaAPIException;
import com.starredsolutions.assemblandroid.exceptions.XMLParsingException;
import com.starredsolutions.net.RestfulException;


public class User {
	String username = null;
	String password = null;
	
	List<Space> spaces = null;
	
	
	/**
	 * WARNING: this methods caches the records on the first call
	 * @return ArrayList<T> records
	 * @throws XMLParsingException 
	 * @throws RestfulException 
	 * @throws AssemblaAPIException 
	 */
	public List<Space> getMySpaces() throws XMLParsingException, AssemblaAPIException, RestfulException {
		if (spaces == null)
			spaces = AssemblaAPIAdapter.getInstance(TimeTrackerApplication.getInstance().getApplicationContext()).getMySpaces();
		
		return spaces;
	}
}
