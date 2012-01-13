package com.starredsolutions.assemblandroid.models;

import java.io.Serializable;

import com.starredsolutions.assemblandroid.AssemblaAPIAdapter;
import com.starredsolutions.assemblandroid.asyncTask.ParsedArrayList;
import com.starredsolutions.assemblandroid.exceptions.AssemblaAPIException;
import com.starredsolutions.assemblandroid.exceptions.XMLParsingException;
import com.starredsolutions.net.RestfulException;


public class Ticket implements Serializable
{
    // http://stackoverflow.com/questions/285793/why-should-i-bother-about-serialversionuid
    private static final long serialVersionUID = -7491130002781226286L;
    
    static public final int STATUS_NEW      = 0;
	static public final int STATUS_ACCEPTED = 1;
	static public final int STATUS_INVALID  = 2;
	static public final int STATUS_FIXED    = 3;
	static public final int STATUS_TEST     = 4;
	
	public static final String TIMER_LOADING = "ticketLoading";
    public static final String TIMER_PARSING = "ticketParsing";
	
	private int id = -1;
//	private int milestoneId = -1;
//	private int importance = -1;
	private int priority = 3;      // Between 1-Highest and 3-Normal and 5-Lowest.
	private int number = -1;
	private int status = -1;
	private String statusName = null;
	private String spaceId = null;
	private String assignedToId = null;
//	private String componentId = null;
	private String description = null;
	private String summary = null;
	private float workingHours = 0.0f; 	  // Estimated Hours Remaining
	private float workedHours  = 0.0f;    // Actual worked hours
	private String lastLogMessage = null;
	
	private transient ParsedArrayList<Task> tasks = null;
	
	
	public String longFormattedName() {
		String format = unassigned() ? "%s  (%s / %s)  -  UNASSIGNED" : "%s  (%s / %s)";
		
		return String.format(format, this.summary, this.workedHoursHuman(), this.workingHoursHuman());
	}
	
	public String shortFormattedName() {
		return String.format("#%d:  %s", this.number, this.summary);
	}
	
	
	public Ticket(int id, int number, int priority, int status, String statusName, 
				String description, String summary, float workingHours, float workedHours, 
				String spaceId, String assignedToId, String lastLogMessage)
	{
		this.id = id;
		this.number = number;
		this.priority = priority;
		this.status = status;
		this.statusName = statusName;
		this.description = description;
		this.summary = summary;
		this.workingHours = workingHours;
		this.workedHours = workedHours;
		this.spaceId = spaceId;
		this.assignedToId = assignedToId;
		this.lastLogMessage = lastLogMessage;
	}
	
	public ParsedArrayList<Task> getTasks() {
		return tasks;
	}
	
	/**
	 * @return ArrayList<Ticket> tasks
	 * @throws XMLParsingException 
	 * @throws RestfulException 
	 * @throws AssemblaAPIException 
	 */
	public ParsedArrayList<Task> reloadTasks() throws XMLParsingException, AssemblaAPIException, RestfulException {
		tasks = AssemblaAPIAdapter.getInstance().getTasksBySpaceIdAndTicketNumber(this.spaceId, this.number);
		return tasks;
	}
	
	public int id()             { return this.id; }
	public int getId()             { return this.id; }
	public int number()         { return this.number; }
	public int getNumber()         { return this.number; }
	public int priority()       { return this.priority; }
	public int getPriority()       { return this.priority; }
	public String name()        { return this.summary; }
	public String getName()     { return this.summary; }
	public int status()         { return this.status; }
	public String getStatusName()  { return this.statusName; }
	public String description() { return this.description; }
	public String getDescription() { return this.description; }
	public Float workedHours()  { return this.workedHours; }
	public Float workingHours() { return this.workingHours; }
	public Float getWorkingHours() { return this.workingHours; }
	public String lastLogMessage() { return this.lastLogMessage; }
	public boolean unassigned() { return !(this.assignedToId.length() > 0); }
	public String getAssignedToId() { return this.assignedToId; }
	
	public void addWorkingHours(float hours) {
		this.workedHours += hours;
	}
	
	public String workedHoursHuman() {
		int hours = (int) this.workedHours;
		int minutes = Math.round( (this.workedHours-hours) * 60 );
		
		return String.format("%d:%02d", hours, minutes);
	}
	
	public String workingHoursHuman() {
		int hours = (int) this.workingHours;
		int minutes = Math.round( (this.workingHours-hours) * 60 );
		
		return String.format("%d:%02d", hours, minutes);
	}
	
	public String priorityLabel() {
		String label = "";
		
		switch (this.priority) {
			case 1:  label = "*****"; break;
			case 2:  label = " ****"; break;
			case 3:  label = " *** "; break;
			case 4:  label = "  ** "; break;
			case 5:  label = "  *  "; break;
			default: label = "  ?  "; break;
		}
		return label;
	}

	public void setAssignedToId(String userId) {
		this.assignedToId = userId;
	}

	public void setLastLogMessage(String msg) {
		this.lastLogMessage = msg;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public void setWorkedHours(float hours) {
		this.workedHours = hours;
	}
	
	public String toString() {
		return "[Ticket] id = " + Integer.toString(id) + 
		" ; priority = " + Integer.toString(priority) + 
		" ; description = " + description+ 
		" ; summary = " + summary +
		" ; hours = " + Float.toString(workedHours) + " / " + Float.toString(workingHours);
	}

	public boolean tasksLoaded()
	{
		return tasks != null;
	}
}
