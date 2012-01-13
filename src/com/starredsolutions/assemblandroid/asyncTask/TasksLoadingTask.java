package com.starredsolutions.assemblandroid.asyncTask;

import com.starredsolutions.assemblandroid.models.Task;
import com.starredsolutions.assemblandroid.models.Ticket;

public class TasksLoadingTask extends AssemblaAsyncTask<Void, Void, ParsedArrayList<Task>> implements IAsynctaskObservable
{
	private static final String LOG_TAG = TasksLoadingTask.class.getSimpleName();
	
   	private Ticket _ticket = null;	
   	private ITasksLoadingListener _listener;
   	
   	public void setLoadingListener(ITasksLoadingListener listener) { _listener = listener; }
    	
   	public TasksLoadingTask(IAsynctaskObserver observer, Ticket ticket)
   	{
   		addObserver(observer);
   		_ticket = ticket;
   	}
    	
   	@Override
	protected ParsedArrayList<Task> doInBackground(Void... params) {
		//Log.i(TAG, "doInBackground");
   		
   		ParsedArrayList<Task> tasks;
		try {
			tasks  = _ticket.reloadTasks();
	        _count = tasks.size();
		} catch (Exception e) {
			// Wait for onPostExecute() to throw the exception in the UI thread
			// http://stackoverflow.com/questions/1739515/android-asynctask-and-error-handling
			_exception = e;
			tasks      = null;
			_count     = 0;
		}
		
        
        _loadingSeconds = 0;
        _parsingSeconds = 0;
        
		return tasks;
	}
		
	protected void onPostExecute(ParsedArrayList<Task> tasks)
	{
		_listener.tasksLoadReport(_count, _loadingSeconds, _parsingSeconds, _exception);
		
		notifyObservers();
		clearObservers();
		
		_listener.onTasksLoaded();
	}	
}