/**
 * 
 */
package com.starredsolutions.assemblandroid.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.starredsolutions.assemblandroid.Constants;
import com.starredsolutions.assemblandroid.R;

/**
 * @author Juan M. Hidalgo <juan@starredsolutions.com.ar>
 *
 */
public class TicketListActivity extends ActionBarActivity {
	private static final String TAG = "TicketListActivity"; 
	private static final boolean LOGV = Log.isLoggable(TAG, Log.VERBOSE) || Constants.DEVELOPER_MODE;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.ticket_list);
        setTitle(R.string.tickets_title);
        
    }

}
