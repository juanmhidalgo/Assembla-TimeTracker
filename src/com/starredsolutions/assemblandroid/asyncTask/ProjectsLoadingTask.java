package com.starredsolutions.assemblandroid.asyncTask;

import java.util.ArrayList;

import com.starredsolutions.assemblandroid.TimeTrackerModel;
import com.starredsolutions.assemblandroid.models.Space;
import com.starredsolutions.utils.MyTimer;


import android.util.Log;


public class ProjectsLoadingTask extends AssemblaAsyncTask<Void, Void, ArrayList<Space>>
{
	private static final String LOG_TAG = ProjectsLoadingTask.class.getSimpleName();
	
	private TimeTrackerModel _model;
	
	private IProjectsLoadingListener _listener;
	
	public void setLoadingListener(IProjectsLoadingListener listener) { _listener = listener; }
	
	public ProjectsLoadingTask(IAsynctaskObserver observer, TimeTrackerModel model)
	{
		super();
		addObserver(observer);
		_model = model;
	}
    	
	@Override
	protected ArrayList<Space> doInBackground(Void... params)
	{
		ArrayList<Space> spaces = null;
		//Log.i(TAG, "doInBackground");
		MyTimer.start(Space.TIMER_LOADING);
        try
        {
        	spaces = _model.reloadSpaces();     
        	_count = spaces.size();
		}
        catch (Exception e)
		{
			_exception = e;
			_count = 0;
		}
        
        MyTimer.stop(Space.TIMER_LOADING);
        
        _loadingSeconds = (int) MyTimer.get(Space.TIMER_LOADING).seconds();
        _parsingSeconds = (int) MyTimer.get(Space.TIMER_PARSING).seconds();
        
		return spaces;
	}
		
	protected void onPostExecute(ArrayList<Space> spaces) 
	{
		_listener.projectsLoadReport(_count, _loadingSeconds, _parsingSeconds, _exception);
		
		notifyObservers();
		clearObservers();
		
		_listener.onProjectsLoaded();
	}
}