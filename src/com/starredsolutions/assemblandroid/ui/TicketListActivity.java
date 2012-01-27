/**
 * 
 */
package com.starredsolutions.assemblandroid.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.starredsolutions.assemblandroid.Constants;
import com.starredsolutions.assemblandroid.R;
import com.starredsolutions.utils.ActivityHelper;

/**
 * @author Juan M. Hidalgo <juan@starredsolutions.com.ar>
 *
 */
public class TicketListActivity extends FragmentActivity {
	private static final String TAG = "TicketListActivity"; 
	private static final boolean LOGV = Log.isLoggable(TAG, Log.VERBOSE) || Constants.DEVELOPER_MODE;
	protected final ActivityHelper mActivityHelper = ActivityHelper.createInstance(this);
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.ticket_list);
        mActivityHelper.setupActionBar(getString(R.string.tickets_title), R.color.darkdarkgray, false);
        
    }

}
