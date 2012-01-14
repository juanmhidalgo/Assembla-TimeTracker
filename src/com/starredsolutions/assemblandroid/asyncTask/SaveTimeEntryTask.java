package com.starredsolutions.assemblandroid.asyncTask;

import com.starredsolutions.assemblandroid.AssemblaAPIAdapter;
import com.starredsolutions.assemblandroid.TimeTrackerApplication;
import com.starredsolutions.assemblandroid.models.Space;
import com.starredsolutions.assemblandroid.models.Task;
import com.starredsolutions.assemblandroid.models.Ticket;

public class SaveTimeEntryTask extends AssemblaAsyncTask<Void, Void, Void>
{
	private Space  _space  = null;
	private Ticket _ticket = null;
	private Task   _task   = null;
	
	private ITimeEntrySavingListener _listener;
	public void setSavingListener(ITimeEntrySavingListener listener) { _listener = listener; }
	
	public SaveTimeEntryTask(IAsynctaskObserver observer, Task task, Ticket ticket, Space space)
	{
		super();
		
		_task = task;
		_ticket = ticket;
		_space = space;
		
		addObserver(observer);
	}
	
	@Override
	protected Void doInBackground(Void... params)
	{
		//Log.i(TAG, "doInBackground");
		try {
			AssemblaAPIAdapter.getInstance(TimeTrackerApplication.getInstance().getApplicationContext()).saveTimeEntry(_space, _ticket, _task);
		} catch (Exception e) {
			_exception = e;
		}
		return null;
	}
	
	protected void onPostExecute(Void param)
	{
		notifyObservers();
		clearObservers();
		_listener.onTimeEntrySaved();
	}
}