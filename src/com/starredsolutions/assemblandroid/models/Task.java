package com.starredsolutions.assemblandroid.models;

import java.io.Serializable;
import java.util.Date;


public class Task implements Serializable
{
    // http://stackoverflow.com/questions/285793/why-should-i-bother-about-serialversionuid
    private static final long serialVersionUID = 7255606712122917833L;
    
    public static final String TIMER_LOADING = "taskLoading";
    public static final String TIMER_PARSING = "taskParsing";

	static public enum State {STARTED, PAUSED, STOPPED};
	
	static private final long MS_PER_HOUR   = 1000 * 60 * 60; // 1000ms * 60sec * 60min
	static private final long MS_PER_MINUTE = 1000 * 60;
	static private final long MS_PER_SECOND = 1000;
	
	private int id = -1;
	private Date beginAt = null;
	private Date lastResumedAt = null;
	private Date endAt = null;
//	private boolean billed = false;
	private float hours = 0.0f;
	private String description = null;
	private String spaceId = null;
	private int ticketId = -1;
	private int ticketNumber = -1;
	private String userId = null;
	public String getUserId() { return userId; }
	private State state;
	
	private long milliseconds = 0;
	
	// Constructor used when creating a new task from the timer
	public Task(String spaceId, int ticketId) {
		this.spaceId = spaceId;
		this.ticketId = ticketId;
	}
	
	// Constructor used by the XML Parser when reading an existing task
	public Task(int id, Date beginAt, Date endAt, float hours, String description, String spaceId, int ticketId, int ticketNumber, String userId) {
		this.id = id;
		this.beginAt = beginAt;
		this.endAt = endAt;
		this.hours = hours;
		this.milliseconds = Math.round(hours * MS_PER_HOUR);
		this.description = description;
		this.spaceId = spaceId;
		this.ticketId = ticketId;
		this.ticketNumber = ticketNumber;
		this.userId = userId;
		this.state = State.STOPPED;
	}
	
	public String toString() {
		return "[Task #" + Integer.toString(id) + " " + state.toString() + "]" +
			" ; beginAt = " + (beginAt != null ? beginAt.toString() : "") + 
			" ; resumeAt = " + (lastResumedAt != null ? lastResumedAt.toString() : "") + 
			" ; endAt = " + (endAt != null ? endAt.toString() : "") + 
			" ; hours = " + hours +  
			" ; milliseconds = " + Long.toString(elapsedMS()) + 
			" ; description = " + description;
	}
	
	
	public int getId(){ return this.id; }
	public int getTicketId(){ return this.ticketId; }
	public int getTicketNumber(){ return this.ticketNumber; }
	public String getSpaceId(){ return this.spaceId; }
	public String getDescription(){ return this.description; }
	public Date getBeginAt(){ return this.beginAt; }
	public Date getEndAt(){ return this.endAt; }
	public Date getUpdatedAt(){ return this.lastResumedAt; }
	
	public State state() { return this.state; }
	public float hours() { return this.hours; }
	public float getHours() { return this.hours; }
	
	public void start() {
		this.beginAt = this.lastResumedAt = new Date();
		this.state = State.STARTED;
	}
	
	public void pause() {
		Date now = new Date();
		this.milliseconds += (now.getTime() - lastResumedAt.getTime());
		this.state = State.PAUSED;
	}
	
	public void resume() {
		this.lastResumedAt = new Date();
		this.state = State.STARTED;
	}
	
	public void stop() {
		this.endAt = new Date();
		this.milliseconds += (endAt.getTime() - lastResumedAt.getTime());
		this.hours = elapsedHours();
		this.state = State.STOPPED;
	}

	/**
	 * @return live elapsed time in ms
	 */
	public long elapsedMS() {
		Date now = new Date();
		
		if (state == State.STARTED) { 
			//Log.i("AssemblaTT", "[milliseconds started] = " + Long.toString(milliseconds + now.getTime() - lastResumedAt.getTime()) );
			return milliseconds + (now.getTime() - lastResumedAt.getTime());
		} else {
			//Log.i("AssemblaTT", "[milliseconds] = " + Long.toString(milliseconds));
			return milliseconds;
		}
	}
	
	public float elapsedHours() {
		return (float) elapsedMS() / MS_PER_HOUR;
	}
	
	public String elapsedTime() {
		long ms = elapsedMS();
		
		int hours   = (int) (ms / MS_PER_HOUR);
		int minutes = Math.round( (float) ((ms % MS_PER_HOUR) / MS_PER_MINUTE));
		int seconds = Math.round( (float) ((ms % MS_PER_MINUTE) / MS_PER_SECOND));
		
		return String.format("%d:%02d:%02d", hours, minutes, seconds);
	}

	public Date beginAt() {   return this.beginAt;  }
	public Date endAt() {   return this.endAt;  }

	public void setDescription(String description) {
		this.description = description;
	}

	public String description() {   return this.description;  }
}
