package com.starredsolutions.assemblandroid.asyncTask;

public interface ITasksLoadingListener
{
	public void onTasksLoaded();
	public void tasksLoadReport(int count, int loadingSeconds, int parsingSeconds, Exception e);
}
