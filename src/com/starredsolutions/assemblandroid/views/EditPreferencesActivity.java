package com.starredsolutions.assemblandroid.views;

import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.starredsolutions.assemblandroid.R;
import com.starredsolutions.assemblandroid.UIController;

public class EditPreferencesActivity extends PreferenceActivity
{
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
        
	    UIController.getInstance().onActivityCreated( this );
	    
		addPreferencesFromResource( R.xml.preferences );
	}
	
	@Override
	public void onDestroy() {
    	super.onDestroy();
    	UIController.getInstance().onActivityDestroyed( this );
    	
	}
    
    /**
     * @see http://stackoverflow.com/questions/456211/activity-restart-on-rotation-android
     * 
     * Called by the system when the device configuration changes while your activity is running. 
     * 
     * Note that this will only be called if you have selected configurations you would like to 
     * handle with the configChanges attribute in your manifest. If any configuration change 
     * occurs that is not selected to be reported by that attribute, then instead of reporting it 
     * the system will stop and restart the activity (to have it launched with the new 
     * configuration).
     * 
     * At the time that this function has been called, your Resources object will have been 
     * updated to return resource values matching the new configuration.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	super.onConfigurationChanged(newConfig);
    }

}
