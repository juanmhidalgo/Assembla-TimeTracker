package com.starredsolutions.assemblandroid.asyncTask;

public interface IAsynctaskObserver {
	public void onUpdate();
	public void onUpdateFailed(Exception exception);
}
