package com.starredsolutions.assemblandroid.views;

import android.os.Bundle;


public class LauncherActivity extends BaseActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // This class exists only to allow the UIController to see if it needs to restart
        // another activity.
        // This prevents to reload the projects if we don't need them.
        
        finish();
    }
    
}