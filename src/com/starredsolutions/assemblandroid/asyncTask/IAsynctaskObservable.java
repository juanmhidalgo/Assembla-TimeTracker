package com.starredsolutions.assemblandroid.asyncTask;



public interface IAsynctaskObservable {
	public void addObserver(IAsynctaskObserver observer);
	public void removeObserver(IAsynctaskObserver observer);
	public void notifyObservers();
}
