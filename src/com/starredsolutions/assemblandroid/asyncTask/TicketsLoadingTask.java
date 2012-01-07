package com.starredsolutions.assemblandroid.asyncTask;

import com.starredsolutions.assemblandroid.models.Space;
import com.starredsolutions.assemblandroid.models.Ticket;
import com.starredsolutions.utils.MyTimer;

import android.util.Log;

public class TicketsLoadingTask extends AssemblaAsyncTask<Void, Void, ParsedArrayList<Ticket>>
{
	public static final String LOG_TAG = TicketsLoadingTask.class.getSimpleName();
	
	private Space _space = null;
	private ITicketsLoadingListener _listener;
	public void setLoadingListener(ITicketsLoadingListener listener) { _listener = listener; }
	
	public TicketsLoadingTask(IAsynctaskObserver observer, Space space)
	{
		super();
		_space = space;
		addObserver(observer);
	}
	
	@Override
	protected ParsedArrayList<Ticket> doInBackground(Void... params)
	{
		MyTimer.start(Ticket.TIMER_LOADING);
		ParsedArrayList<Ticket> tickets;
		try
		{
			tickets = _space.reloadTickets(false, false);
	        _count  = tickets.size();
		}
		catch (Exception e)
		{
			// Wait for onPostExecute() to throw the exception in the UI thread
			// http://stackoverflow.com/questions/1739515/android-asynctask-and-error-handling
			_exception = e;
			tickets    = null;
			_count     = 0;
		}
      	MyTimer.stop(Ticket.TIMER_LOADING);
      	
      	_loadingSeconds = (int) MyTimer.get(Ticket.TIMER_LOADING).seconds();
      	_parsingSeconds = (int) MyTimer.get(Ticket.TIMER_PARSING).seconds();
        
		return tickets;
	}
	
	protected void onPostExecute(ParsedArrayList<Ticket> tickets)
	{
      	_listener.ticketsLoadReport(_count, _loadingSeconds, _parsingSeconds, _exception);
      	
		notifyObservers();
		clearObservers();
		
		_listener.onTicketsLoaded();
	}
}