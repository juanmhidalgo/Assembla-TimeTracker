package com.starredsolutions.assemblandroid.asyncTask;

public interface IProjectsLoadingListener
{
	public void onProjectsLoaded();
	public void projectsLoadReport(int count, int loadingSeconds, int parsingSeconds, Exception e);
}