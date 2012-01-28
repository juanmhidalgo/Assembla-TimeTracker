/**
 * 
 */
package com.starredsolutions.assemblandroid.ui;

import android.os.Bundle;
import android.util.Log;

import com.starredsolutions.assemblandroid.R;

/**
 * @author Juan M. Hidalgo <juan@starredsolutions.com.ar>
 *
 */
public class SpaceListActivity extends ActionBarActivity{
	private static final String TAG = "SpaceListActivity"; 
	private static final boolean LOGV = Log.isLoggable(TAG, Log.VERBOSE); 

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.space_list);
        setTitle(R.string.spaces_title);
    }
}
