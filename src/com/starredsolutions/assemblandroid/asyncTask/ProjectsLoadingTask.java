package com.starredsolutions.assemblandroid.asyncTask;

import java.util.ArrayList;

import com.starredsolutions.assemblandroid.TimeTrackerModel;
import com.starredsolutions.assemblandroid.models.Space;


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
        
        
        _loadingSeconds = 0;
        _parsingSeconds = 0;
        
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