package com.starredsolutions.assemblandroid.models;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;


public class Task implements Serializable,Cloneable{
    // http://stackoverflow.com/questions/285793/why-should-i-bother-about-serialversionuid
    private static final long serialVersionUID = 7255606712122917833L;
    
	static public enum State {STARTED, PAUSED, STOPPED};

	
	static private final long MS_PER_HOUR   = 1000 * 60 * 60; // 1000ms * 60sec * 60min
	static private final long MS_PER_MINUTE = 1000 * 60;
	static private final long MS_PER_SECOND = 1000;
	
	private long _id = 0;
	private int id = -1;
	private Date beginAt = null;
	private long beginAt_n = 0; //nanosegundos
	private long endAt_n = 0; //nanosegundos
	private Date lastResumedAt = null;
	private Date endAt = null;
//	private boolean billed = false;
	private float hours = 0.0f;
	private String description = null;
	private String spaceId = null;
	private int ticketId = -1;
	private int ticketNumber = -1;
	private String userId = null;
	private State state;
	private long milliseconds = 0;
	
	
	public Task() {
		
	}
	
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
		return "[Task #" + Integer.toString(id) + " " + state + "]" +
			" ; beginAt = " + (beginAt != null ? beginAt.toString() : "") + 
			" ; resumeAt = " + (lastResumedAt != null ? lastResumedAt.toString() : "") + 
			" ; endAt = " + (endAt != null ? endAt.toString() : "") + 
			" ; hours = " + hours +  
			" ; milliseconds = " + Long.toString(elapsedMS()) + 
			" ; description = " + description;
	}
	
	
	/**
	 * 
	 */
	public void start() {
		this.beginAt = this.lastResumedAt = Calendar.getInstance().getTime();
		this.beginAt_n = System.nanoTime();
		this.state = State.STARTED;
	}
	
	/**
	 * @deprecated
	 */
	public void pause() {
		Date now = new Date();
		this.milliseconds += (now.getTime() - lastResumedAt.getTime());
		this.state = State.PAUSED;
	}

	/**
	 * @deprecated
	 */
	public void resume() {
		this.lastResumedAt = new Date();
		this.state = State.STARTED;
	}
	
	/**
	 * 
	 */
	public void stop() {
		this.endAt = Calendar.getInstance().getTime();
		this.endAt_n = System.nanoTime();
		this.milliseconds += (endAt.getTime() - lastResumedAt.getTime());
		this.hours = elapsedHours();
		this.state = State.STOPPED;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isStarted(){
		return this.state == State.STARTED;
	}
	
	/**
	 * @return live elapsed time in ms
	 */
	public long elapsedMS() {
		Date now = new Date();
		
		if (state == State.STARTED) { 
			return milliseconds + (now.getTime() - lastResumedAt.getTime());
		} else {
			return milliseconds;
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public float elapsedHours() {
		return ( (this.endAt_n - this.beginAt_n) / 1000000000.0f) / 3600f;
	}

	
	public String elapsedTime() {
		long ms = elapsedMS();
		
		int hours   = (int) (ms / MS_PER_HOUR);
		int minutes = Math.round( (float) ((ms % MS_PER_HOUR) / MS_PER_MINUTE));
		int seconds = Math.round( (float) ((ms % MS_PER_MINUTE) / MS_PER_SECOND));
		
		return String.format("%d:%02d:%02d", hours, minutes, seconds);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the beginAt
	 */
	public Date getBeginAt() {
		return beginAt;
	}

	/**
	 * @param beginAt the beginAt to set
	 */
	public void setBeginAt(Date beginAt) {
		this.beginAt = beginAt;
	}

	/**
	 * @return the lastResumedAt
	 */
	public Date getLastResumedAt() {
		return lastResumedAt;
	}

	/**
	 * @param lastResumedAt the lastResumedAt to set
	 */
	public void setLastResumedAt(Date lastResumedAt) {
		this.lastResumedAt = lastResumedAt;
	}

	/**
	 * @return the endAt
	 */
	public Date getEndAt() {
		return endAt;
	}

	/**
	 * @param endAt the endAt to set
	 */
	public void setEndAt(Date endAt) {
		this.endAt = endAt;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the spaceId
	 */
	public String getSpaceId() {
		return spaceId;
	}

	/**
	 * @param spaceId the spaceId to set
	 */
	public void setSpaceId(String spaceId) {
		this.spaceId = spaceId;
	}

	/**
	 * @return the ticketId
	 */
	public int getTicketId() {
		return ticketId;
	}

	/**
	 * @param ticketId the ticketId to set
	 */
	public void setTicketId(int ticketId) {
		this.ticketId = ticketId;
	}

	/**
	 * @return the ticketNumber
	 */
	public int getTicketNumber() {
		return ticketNumber;
	}

	/**
	 * @param ticketNumber the ticketNumber to set
	 */
	public void setTicketNumber(int ticketNumber) {
		this.ticketNumber = ticketNumber;
	}

	/**
	 * @return the userId
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * @param userId the userId to set
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}

	/**
	 * @return the state
	 */
	public State getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(State state) {
		this.state = state;
	}
	/**
	 * 
	 * @return
	 */
	public float getHours(){
		return this.hours;
	}
	
	/**
	 * @param hours the hours to set
	 */
	public void setHours(float hours) {
		this.hours = hours;
	}

	/**
	 * @return the _id
	 */
	public long get_id() {
		return _id;
	}

	/**
	 * @param _id the _id to set
	 */
	public void set_id(long _id) {
		this._id = _id;
	}
	
	/**
	 * 
	 */
	public Task clone(){
		try{
			return (Task) super.clone();
		}catch ( CloneNotSupportedException e ){
			return null;
		}
	}
}
