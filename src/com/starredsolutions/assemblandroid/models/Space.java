package com.starredsolutions.assemblandroid.models;

import java.io.Serializable;

import com.starredsolutions.assemblandroid.AssemblaAPIAdapter;
import com.starredsolutions.assemblandroid.asyncTask.ParsedArrayList;
import com.starredsolutions.assemblandroid.exceptions.AssemblaAPIException;
import com.starredsolutions.assemblandroid.exceptions.XMLParsingException;
import com.starredsolutions.net.RestfulException;


public class Space implements Serializable
{
    // http://stackoverflow.com/questions/285793/why-should-i-bother-about-serialversionuid
	private static final long serialVersionUID = 6656872344467459407L;
	
	public static final String TIMER_LOADING = "spaceLoading";
    public static final String TIMER_PARSING = "spaceParsing";
	
//    private boolean canJoin = false;
//    private Date createdAt = null;
//    private String defaultShowpage = null;
    private String id = null;
    private String name = null;
    private String description = null;
//    private boolean isCommercial = false;
//    private boolean isManager = false;
//    private boolean isVolunteer = false;
//    private String parentId = null;
//    private int publicPermissions = -1;
//    private int teamPermissions = -1;
//    private Date updatedAt = null;
//    private int watcherPermissions = -1;
//    private String wikiName = null;
	
    private ParsedArrayList<Ticket> tickets = null;
	
	public String id() { return this.id; }
	public String listItemText() { return this.name; }
	
	
	public Space(String id, String name, String description) {
		this.id = id;
		this.name = name;
		this.description = description;
	}
	
	/**
	 * WARNING: this method returns the tickets that we have in memory.
	 * Use reloadTickets() if you want to make sure to have the latest data
	 * 
	 * @return ArrayList<Ticket> tickets
	 * @throws XMLParsingException 
	 * @throws RestfulException 
	 * @throws AssemblaAPIException 
	 */
	public ParsedArrayList<Ticket> getTickets() {
		return tickets;
	}
	
	
	public ParsedArrayList<Ticket> reloadTickets(boolean includeClosed, boolean includeOthers) 
				throws XMLParsingException, AssemblaAPIException, RestfulException
	{
		tickets = AssemblaAPIAdapter.getInstance().getTicketsBySpaceId(this.id, includeClosed, includeOthers);
		return tickets;
	}
	
	/**
	 * @return true if records were already loaded
	 */
	public boolean ticketsLoaded() {
		return !(tickets == null);
	}

	public String name() { return this.name; }
	public String getName() { return this.name; }
	
	public String toString() {
		try {
			return "[Space] id = " + id + 
			" ; name = " + name +
			" ; description = " + description +
			" ; tickets = \n[" + getTickets() + "]";
		} catch (Exception e) {
			return "[Space] id = " + id + 
			" ; name = " + name +
			" ; description = " + description +
			" ; tickets = \n[" + "EXCEPTION raised while retrieving tickets" + "]";
		}
	}
}
