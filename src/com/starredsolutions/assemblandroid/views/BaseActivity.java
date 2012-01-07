package com.starredsolutions.assemblandroid.views;

import com.starredsolutions.assemblandroid.UIController;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;

public abstract class BaseActivity extends Activity
{
    /*********************************************************************************************
     * ACTIVITY WORKFLOW METHODS
     *********************************************************************************************/
    
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UIController.getInstance().onActivityCreated(this);
    }
    
    @Override protected void onResume() {
        super.onResume();
        UIController.getInstance().onActivityResumed(this);
    }
    
    @Override public void onConfigurationChanged(Configuration newConfig) {
        // this prevents LauncherActivity recreation on Configuration changes (device orientation changes or hardware keyboard open/close).
        // just do nothing on these changes:
        super.onConfigurationChanged(null);
    }
        
    @Override protected void onPause() {
        super.onPause();
        UIController.getInstance().onActivityPaused(this);
    }
    
    @Override protected void onDestroy() {
        super.onDestroy();
        UIController.getInstance().onActivityDestroyed(this);
    }
    
    /*********************************************************************************************
     * INTERNAL METHODS
     *********************************************************************************************/
    protected void log(String func, String msg) {
        Log.i( getLogTag() , getClass().getSimpleName() + "::" + func + "() " + msg);
    }
    
    protected String getLogTag() {
        return getClass().getSimpleName();
    }
}
