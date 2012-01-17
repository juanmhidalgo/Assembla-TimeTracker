/**
 * 
 */
package com.starredsolutions.assemblandroid.ui;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.starredsolutions.assemblandroid.Constants;
import com.starredsolutions.assemblandroid.R;
import com.starredsolutions.assemblandroid.adapter.TaskCursorAdapter;
import com.starredsolutions.assemblandroid.provider.AssemblaContract.Tasks;
import com.starredsolutions.assemblandroid.provider.AssemblaContract.Tickets;
import com.starredsolutions.utils.ActivityHelper;

/**
 * @author Juan M. Hidalgo <juan@starredsolutions.com.ar>
 *
 */
public class TicketDetailActivity extends Activity {
	private static final String TAG = "TicketDetailActivity"; 
	private static final boolean LOGV = Log.isLoggable(TAG, Log.VERBOSE) || Constants.DEVELOPER_MODE;
	protected final ActivityHelper mActivityHelper = ActivityHelper.createInstance(this);
	private Uri mUri;
	private Cursor mCursor;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ticket_details);
		
		mActivityHelper.setupActionBar(getString(R.string.tickets_detail_title), R.color.darkdarkgray, false);
		
		String space_id = getIntent().getStringExtra(Tickets.SPACE_ID);
		int ticket_number = getIntent().getIntExtra(Tickets.NUMBER,0); 
		
		final ListView lv = (ListView) findViewById(R.id.taskListView);
		if(lv != null){
			mCursor = managedQuery(Tasks.CONTENT_URI, Tasks.PROJECTION, Tasks.SPACE_ID + "= ? AND " + Tasks.TICKET_NUMBER + " = ?", new String[]{space_id, String.valueOf(ticket_number)}, null);
			TaskCursorAdapter adapter = new TaskCursorAdapter(this, mCursor, false);
			lv.setAdapter(adapter);
		}
	}
}
