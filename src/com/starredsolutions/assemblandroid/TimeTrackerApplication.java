package com.starredsolutions.assemblandroid;

import java.util.ArrayList;
import java.util.List;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.starredsolutions.assemblandroid.TimeTrackerModel.ACTIVITY;
import com.starredsolutions.assemblandroid.asyncTask.IAsynctaskObserver;
import com.starredsolutions.assemblandroid.asyncTask.IProjectsLoadingListener;
import com.starredsolutions.assemblandroid.asyncTask.ITasksLoadingListener;
import com.starredsolutions.assemblandroid.asyncTask.ITicketsLoadingListener;
import com.starredsolutions.assemblandroid.asyncTask.ITimeEntrySavingListener;
import com.starredsolutions.assemblandroid.asyncTask.ParsedArrayList;
import com.starredsolutions.assemblandroid.asyncTask.ProjectsLoadingTask;
import com.starredsolutions.assemblandroid.asyncTask.SaveTimeEntryTask;
import com.starredsolutions.assemblandroid.asyncTask.TasksLoadingTask;
import com.starredsolutions.assemblandroid.asyncTask.TicketsLoadingTask;
import com.starredsolutions.assemblandroid.exceptions.AssemblaAPIException;
import com.starredsolutions.assemblandroid.exceptions.XMLParsingException;
import com.starredsolutions.assemblandroid.models.Space;
import com.starredsolutions.assemblandroid.models.Task;
import com.starredsolutions.assemblandroid.models.Task.State;
import com.starredsolutions.assemblandroid.models.Ticket;
import com.starredsolutions.assemblandroid.views.DialogThemedActivity;
import com.starredsolutions.net.RestfulException;


/**
 * GRASP Controller for all the time entry tasks
 * 
 * FIXME: because of onActivityChanged(), saveState() is called way too many times!
 * FIXME: sometimes if Android loads the same screen twice
 * 
 * @author david
 */
public class TimeTrackerApplication extends Application
        implements ExceptionManagerIF, ITasksLoadingListener, ITicketsLoadingListener, IProjectsLoadingListener, ITimeEntrySavingListener
{
    /*********************************************************************************************
     * SINGLETON PATTERN
     *********************************************************************************************/
    static private TimeTrackerApplication instance;
    static public  TimeTrackerApplication getInstance() { return instance; }
    
    
    /*********************************************************************************************
     * CONSTANTS
     *********************************************************************************************/
    static private final String TAG = "AssemblaTT";
    static private final String SNAPSHOT_FILE = "mysnapshot.dat";
    
    
    
    /*********************************************************************************************
     * VARIABLES
     *********************************************************************************************/
	private TimeTrackerModel _model;    

    private SnapshotManager _snapshotManager;
	
	private AssemblaAPIAdapter _assemblaAdapter;

	private SharedPreferences _preferences;
	
	private boolean _starting = false;
	
	
	
	/*********************************************************************************************
     * SIMPLE GETTERS & SETTERS
     *********************************************************************************************/
	public boolean  isRecordingTimeEntry() { return _model.isRecordingTimeEntry(); }
	public ACTIVITY getSavedPreviousActivity() { return _model.curActivity(); }     // only call this after restoreState()
	
	
	/*********************************************************************************************
     * PUBLIC METHODS
     *********************************************************************************************/
	/**
	 * SINGLETON: A private constructor enforces the Singleton pattern by preventing external instantiation.
	 * (EDIT: damn, Android needs a public constructor to initiate it)
	 */
	public TimeTrackerApplication() {
	    _starting = true;
	    
		instance = this;
	}
	
	/**
	 * Manages a non-fatal Exception
	 * @param Exception e
	 */
	public void manageException(Exception e) 
	{
		Log.e(TAG, e.toString() );
		e.printStackTrace();
	
		boolean fatal = false;
		if(e instanceof XMLParsingException && ((XMLParsingException)e).getFatal())
			fatal = true;
		
		String title = createErrorDialogTitle(e, fatal);			
		showErrorDialog(title, e.toString(), fatal);
	}
	
	private String createErrorDialogTitle(Exception e, boolean fatal)
	{
		String title = (fatal ? "FATAL: " : "");
		
		if(e instanceof RestfulException)
			title += "Communication  Error";
		else if(e instanceof XMLParsingException)
			title += "XML Parsing Error";
		else if(e instanceof AssemblaAPIException)
			title += "Unexpected Answer Error";

		else
			title += e.getClass().getSimpleName();
		
		return title;
	}
	
	private void showErrorDialog(String title, String message, boolean fatal)
	{
		// Show Error Dialog
		Intent intent = new Intent(this, DialogThemedActivity.class);
		intent.putExtra("title", title );
		intent.putExtra("message", message );
		
		if (fatal) {
			intent.putExtra("fatal", true);
			//Log.e(TAG, "Fatal Exception, app will now closes");
		}
		
		intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
		startActivity( intent );
		errorDialogActive = true;
	}
	
	private boolean errorDialogActive = false;
	
	/**
	 * Manage the exception for the application.
	 * 
	 * @param e Exception
	 * @param fatal If fatal the exception is not rescued
	 */
	
	public void onErrorDialogQuit() {
		errorDialogActive = false;
	}
	
	
	/**
	 * Created when this class is instanciated by the Android environment
	 * 
	 * http://stackoverflow.com/questions/456211/activity-restart-on-rotation-android
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		/*
		_starting = true;
		Log.i(TAG, "TimeTrackingApplication starting... ");
		
		try {
			_assemblaAdapter = AssemblaAPIAdapter.getInstance();

		} catch (XMLParsingException e) {
			// FATAL Exception
			manageException(e);
			return;
		}
		
		// Load preferences
		this._preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		reloadPreferences();

        _snapshotManager = new SnapshotManager(this);
        restoreState();
        _starting = false;*/
	}
	
	public void reloadPreferences()
	{
		String username  = this._preferences.getString("username", "");
		String password  = this._preferences.getString("password", "");
		
		//Log.i(TAG, "TimeTrackingManager.setPreferences() user=" + username + "; pass=" + password);
		try {
			_assemblaAdapter.setCredentials(username, password);
		} catch (Exception e) {
			manageException(e);
		}
	}
	
	public ArrayList<Space> getSpaces(){
		return _model.spaces();
	}
	
	public ArrayList<String> projectNamesForList()
	{
		ArrayList<String> projectNames = new ArrayList<String>();
        
        for (Space space : _model.spaces() ) {
        	projectNames.add(space.listItemText());
    	}
        
        return projectNames;
	}
	
	public ArrayList<Ticket> ticketsForList()
	{
		return _model.curSpace().getTickets();
	}
	
	public ArrayList<Task> tasksForList()
	{
		return _model.curTicket().getTasks();
	}
	
	public void selectProject(int position)
	{
		_model.setCurSpace( _model.spaces().get(position) );
		saveState();
	}
	
	public Space curProject() {
		return _model.curSpace();
	}
	
	public void selectTicket(int position) {
		if (_model.curSpace() != null) {
			_model.setCurTicket( _model.curSpace().getTickets().get(position) );
	        saveState();
		}
	}
	
	public Ticket curTicket() {
		return _model.curTicket();
	}
	
	
	public Task startTimeEntry() {
		_model.setCurTask( new Task(_model.curSpace().id(), _model.curTicket().id()) );
		_model.curTask().start();
		Log.i(TAG, "Time entry starting for: Ticket #" + 
				Integer.toString(_model.curTicket().id()) + " '" + _model.curTicket().name() + 
				"' (Space = '" + _model.curSpace().name() + "')");

        saveState();
        
		return _model.curTask();
	}
	
	public Task curTask() {
		return _model.curTask();
	}
	
	/**
	 * @return current paused state (true = paused)
	 */
	public State togglePauseTimeEntry() {
		switch (_model.curTask().state()) {
			case STARTED: _model.curTask().pause(); break;
			case STOPPED: _model.curTask().resume(); break;
			case PAUSED:  _model.curTask().resume(); break;
		}
        saveState();
        
		return _model.curTask().state();
	}
	
	public void stopTimeEntry() {
		_model.curTask().stop();
        saveState();
	}
	
	

    
    
    /**
     * Saves the application state encapsulated in _model to disk
     */
    public void saveState() {
        Log.i(TAG, "Saving application state to disk.");
        try {
            _snapshotManager.create(_model, SNAPSHOT_FILE);
//            printBackStack();
        } catch (Exception e) {
            String msg = "WARNING: Unable to save application state to disk."; 
            Log.w(TAG, msg);
            e.printStackTrace();
            //Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        }
    }
    
    
    /**
     * Restores the application state encapsulated in _model from disk
     */
    public void restoreState() {
        Log.i(TAG, "Restoring application state from disk.");
        try {
            _model = _snapshotManager.restore(SNAPSHOT_FILE);
//            printBackStack();
        } catch (Exception e)
        {
            String msg;
            if (e instanceof ClassNotFoundException)
            {
                msg = "ERROR: Unable to load application state from disk (Class Incompatible).";
                Log.e(TAG, msg);
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            } else {
                msg = "WARNING: Unable to load application state from disk. Reason: " + e.getMessage(); 
                Log.w(TAG, msg);
            }
            _model = new TimeTrackerModel();
            
            e.printStackTrace();
        }
    }
    
    
    /**
     * Called by the activity once it gets resumed, so to allow the Application to keep track of the current
     * visible activity.
     */
    public void onActivityChanged(ACTIVITY activity) {
        if (!_starting) {
            Log.i(TAG, "Switched to activity: " + activity);
            _model.setCurActivity(activity);
        } else {
            Log.i(TAG, "Android started activity: " + activity + ", but we ignore it as app is still starting.");
        }
        saveState();
    }
	
	
	
	/*************************************************************************
	 * Projects Loading Task
	 *************************************************************************/
	
	private ProjectsLoadingTask _projectsTask;
	
	public void fetchProjectsLater(IAsynctaskObserver observer) {
		_projectsTask = new ProjectsLoadingTask(observer, _model);
		_projectsTask.setLoadingListener(this);
		_projectsTask.execute();
	}
    
    public void onProjectsLoaded() {
    	//Log.i(TAG, "dispatchProjectsLoaded");
    	// Free memory
    	_projectsTask.cancel(true);
    	_projectsTask = null;
    }
    
    /**
     * @return true if projects have been loaded already.
     */
    public boolean projectsReady() {
    	return (_model.spaces() == null) ? false : true;
    }
    
    
    
    /*************************************************************************
	 * Tickets Loading Task
	 *************************************************************************/
	TicketsLoadingTask _ticketsTask;
	public void fetchTicketsLater(IAsynctaskObserver observer)
	{
		_ticketsTask = new TicketsLoadingTask(observer, _model.curSpace());
		_ticketsTask.execute();
		_ticketsTask.setLoadingListener(this);
	}
    
    public void onTicketsLoaded()
    {
    	_ticketsTask.cancel(true);
    	_ticketsTask = null;
    }
    
    /**
     * @return true if a space has been selected, and its tickets have been loaded already.
     */
    public boolean ticketsReady()
    {
    	if (_model.curSpace() != null && _model.curSpace().ticketsLoaded())
    		return true;
    	else
    		return false;
    }
    
    
    /*************************************************************************
	 * Tasks Loading Task
	 *************************************************************************/
    
    TasksLoadingTask _tasksTask;
	public void fetchTasksLater(IAsynctaskObserver observer) {
		_tasksTask = new TasksLoadingTask(observer, _model.curTicket());
		_tasksTask.execute();
		_tasksTask.setLoadingListener(this);
	}
    
    /**
     * @return true if a space has been selected, and its tickets have been loaded already.
     */
    public boolean tasksReady() {
    	if (_model.curTicket() != null && _model.curTicket().tasksLoaded())
    		return true;
    	else
    		return false;
    }    
    
    /*************************************************************************
	 * Save Time Entry Task
	 *************************************************************************/
    SaveTimeEntryTask _saveTimeEntryTask;

	public void saveTimeEntryLater(String description, boolean markToTest, IAsynctaskObserver observer) {
    	//Log.i(TAG, "saveTimeEntryLater");

		// Allow to save a task with an empty description
		if (description.length() == 0) {
			description = ".";
		}
		
		_model.curTask().setDescription( description );
		
		// 2. Update the project
		Ticket ticket = _model.curTicket();
		ticket.addWorkingHours( _model.curTask().elapsedHours() );
		ticket.setAssignedToId( _assemblaAdapter.getUserId() );   // ugly ;-)
		ticket.setLastLogMessage( description );
		
		if (markToTest) {
			ticket.setStatus( Ticket.STATUS_TEST );
		} else if (ticket.status() == Ticket.STATUS_NEW) {
			ticket.setStatus( Ticket.STATUS_ACCEPTED );
		}
		
		_saveTimeEntryTask = new SaveTimeEntryTask(observer, _model.curTask(), _model.curTicket(), _model.curSpace());
		_saveTimeEntryTask.execute();
		_saveTimeEntryTask.setSavingListener(this);
	}
	
    
    public void onTimeEntrySaved()
    {    
    	_saveTimeEntryTask.cancel(true);
    	_saveTimeEntryTask = null;
    }

	public void onTasksLoaded()
	{
		_tasksTask.cancel(true);
    	_tasksTask = null;
	}

	public void tasksLoadReport(int count, int loadingSeconds, int parsingSeconds, Exception e)
	{
		ParsedArrayList<Task> tasks = _model.curTicket().getTasks();
		if (tasks != null)
		{
	        int totalTasks = tasks.size() + tasks.getSkippedItems();	        
	        toast(String.format("Found %d/%d tasks available.", tasks.size(), totalTasks));
		}
		else if(e != null)
			manageException(e);
	}

	public void ticketsLoadReport(int count, int loadingSeconds, int parsingSeconds, Exception e)
	{
		ParsedArrayList<Ticket> tickets = _model.curSpace().getTickets();
		if(tickets != null)
		{
			int totalTickets = tickets.size() + tickets.getSkippedItems();
			toast(String.format("Found %d/%d tickets available.", tickets.size(), totalTickets));
		}
		else if(e != null)
			manageException(e);
	}

	public void projectsLoadReport(int count, int loadingSeconds, int parsingSeconds, Exception e)
	{
		ArrayList<Space> spaces = _model.spaces();
		if(spaces != null)
			toast(String.format("Found %d spaces available.", spaces.size()));
		else if(e != null)
			manageException(e);
	}
	
	protected void toast(String message)
	{
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}
	
	private void printBackStack() {
	    final ActivityManager       am           = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    final List<RunningTaskInfo> taskInfoList = am.getRunningTasks(1024);
	    
	    if (!taskInfoList.isEmpty())
	    {
	        final String ourAppPackageName = getPackageName();
	        
	        Log.i(TAG, "printBackStack() Current Activity: " + _model.curActivity() + " ; is recording TimeEntry = " + isRecordingTimeEntry() );
	        Log.i(TAG, "printBackStack() Running tasks in " + ourAppPackageName);
	        
	        for (RunningTaskInfo taskInfo : taskInfoList)
	        {
	            if (ourAppPackageName.equals(taskInfo.baseActivity.getPackageName())) {
                    Log.i(TAG, "\t * TopActivity = " + taskInfo.topActivity.getShortClassName() + " (base = " + taskInfo.baseActivity.getShortClassName() + ")");
                }
	        }
	    }
	}
}
