package com.starredsolutions.assemblandroid;

import java.io.Serializable;
import java.util.ArrayList;

import com.starredsolutions.assemblandroid.exceptions.AssemblaAPIException;
import com.starredsolutions.assemblandroid.exceptions.XMLParsingException;
import com.starredsolutions.assemblandroid.models.Space;
import com.starredsolutions.assemblandroid.models.Task;
import com.starredsolutions.assemblandroid.models.Ticket;
import com.starredsolutions.net.RestfulException;


public class TimeTrackerModel
{
    /*********************************************************************************************
     * ENUMS
     *********************************************************************************************/
    static public enum ACTIVITY {
        LAUNCHING, PROJECTS_LISTING, TICKETS_LISTING, TICKET_DETAILS, TIME_ENTRY
    }
    
    /*********************************************************************************************
     * VARIABLES
     *********************************************************************************************/
    
    private ArrayList<Space> _spaces;
    
    private Space  _currentSpace;
    private Ticket _currentTicket;
    private Task   _currentTask;
    private ACTIVITY _currentActivity;
    
    
    
    /*********************************************************************************************
     * SIMPLE GETTERS & SETTERS
     *********************************************************************************************/
    public ArrayList<Space> spaces() { return _spaces; }
    public Space  curSpace()  { return _currentSpace; }
    public Ticket curTicket() { return _currentTicket; }
    public Task   curTask()   { return _currentTask; }
    public ACTIVITY curActivity() { return _currentActivity; }
    public boolean isRecordingTimeEntry() { return _currentActivity == ACTIVITY.TIME_ENTRY; }
    
    public void setSpaces(ArrayList<Space> spaces) { _spaces = spaces; }
    public void setCurSpace(Space space) { _currentSpace = space; }
    public void setCurTicket(Ticket ticket) { _currentTicket = ticket; }
    public void setCurTask(Task task) { _currentTask = task; }
    public void setCurActivity(ACTIVITY activity) { _currentActivity = activity; }
    
	
   
    
    /*********************************************************************************************
     * INNER CLASS : Memento Pattern
     * 
     * Notes:
     * - Each serialized object must implement the Serializable interface.
     * - Use the transient keyword to prevent an attribute from being serialized.
     * 
     *********************************************************************************************/
    static private class Memento implements Serializable
    {
        // http://stackoverflow.com/questions/285793/why-should-i-bother-about-serialversionuid
        private static final long serialVersionUID = 4207059325574501816L;

        private final ACTIVITY _currentActivity;
        
        private final ArrayList<Space> _spaces;
        
        private final Space  _currentSpace;
        private final Ticket _currentTicket;
        private final Task   _currentTask;


        public Memento(ACTIVITY activity, ArrayList<Space> spaces, Space currentSpace, Ticket currentTicket, Task currentTask)
        {
            _currentActivity = activity;
            _spaces = spaces;
            _currentSpace = currentSpace;
            _currentTicket = currentTicket;
            _currentTask = currentTask;
        }
    }
    
    
    
    /*********************************************************************************************
     * PUBLIC METHODS
     *********************************************************************************************/
    
    /**
     * Default Constructor
     */
    public TimeTrackerModel() 
    {
        _spaces        = null;
        _currentSpace  = null;
        _currentTicket = null;
        _currentTask   = null;
        _currentActivity  = ACTIVITY.LAUNCHING;
    }
    
    /**
     * @param memento object to create the model from
     */
    public TimeTrackerModel( Serializable readOnlyMemento ) {
        restoreMemento( readOnlyMemento );
    }
    
    
    /**
     * @return a memento from current object
     */
    public Serializable createMemento() {
        return new Memento(_currentActivity, _spaces, _currentSpace, _currentTicket, _currentTask);
    }
    
    
    /**
     * Since the Caretaker (SnapshotManager) does not need to know what it is serialising (it knows only the memento 
     * read only interface : Serializable), this method allows the target (this class) to handle its private inner
     * class (Memento).
     * 
     * @param memento to restore into current object
     */
    public void restoreMemento(Serializable readOnlyMemento) 
    {
        Memento memento = (Memento) readOnlyMemento;
        
//        System.err.println("MEMENTO RESTORE FOUND : " + memento.serializedme);

        _currentActivity = memento._currentActivity;
        _spaces          = memento._spaces;
        _currentSpace    = memento._currentSpace;
        _currentTicket   = memento._currentTicket;
        _currentTask     = memento._currentTask;
    }
    
	public ArrayList<Space> reloadSpaces() throws AssemblaAPIException, XMLParsingException, RestfulException
	{
		return _spaces = AssemblaAPIAdapter.getInstance().getMySpaces();
	}

}
