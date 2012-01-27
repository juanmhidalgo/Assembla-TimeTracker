/**
 * 
 */
package com.starredsolutions.assemblandroid.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.starredsolutions.assemblandroid.R;
import com.starredsolutions.utils.ActivityHelper;

/**
 * @author Juan M. Hidalgo <juan@starredsolutions.com.ar>
 *
 */
public class SpaceListActivity extends FragmentActivity{
	private static final String TAG = "SpaceListActivity"; 
	private static final boolean LOGV = Log.isLoggable(TAG, Log.VERBOSE); 
	protected final ActivityHelper mActivityHelper = ActivityHelper.createInstance(this);

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.space_list);
        mActivityHelper.setupActionBar(getString(R.string.spaces_title), R.color.darkdarkgray, false);
        
    }
}
