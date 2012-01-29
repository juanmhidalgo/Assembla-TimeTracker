/**
 * 
 */
package com.starredsolutions.assemblandroid.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.TabHost;

import com.starredsolutions.assemblandroid.Constants;
import com.starredsolutions.assemblandroid.R;
import com.starredsolutions.assemblandroid.ui.fragments.TaskListFragment;
import com.starredsolutions.assemblandroid.ui.fragments.TicketDetailFragment;
import com.starredsolutions.utils.TabManager;

/**
 * @author Juan M. Hidalgo <juan@starredsolutions.com.ar>
 * TODO Separate in tabs
 */
public class TicketDetailActivity extends ActionBarActivity{
	private static final String TAG = "TicketDetailActivity"; 
	private static final boolean LOGV = Log.isLoggable(TAG, Log.VERBOSE) || Constants.DEVELOPER_MODE;
	TabHost mTabHost;
    TabManager mTabManager;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.fragment_tabs);
        setTitle(R.string.tickets_detail_title);

        mTabHost = (TabHost)findViewById(android.R.id.tabhost);
        mTabHost.setup();
        
        mTabManager = new TabManager(this, mTabHost, R.id.realtabcontent);
        
        mTabManager.addTab(mTabHost.newTabSpec("details").setIndicator("Details"),
        		TicketDetailFragment.class, null);
        
        mTabManager.addTab(mTabHost.newTabSpec("tasks").setIndicator("Tasks"),
                TaskListFragment.class, null);
        
        if (savedInstanceState != null) {
            mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
        }
        /*
        
        final Button btnStart = (Button) findViewById(R.id.btnStart);
		final Button btnStop = (Button) findViewById(R.id.btnStop);
		
		final String space_id = getIntent().getStringExtra(Tickets.SPACE_ID);
		final String description = getIntent().getStringExtra(Tickets.DESCRIPTION);
		final int ticket_number = getIntent().getIntExtra(Tickets.NUMBER,0); 
		final long _id = ContentUris.parseId(getIntent().getData());
		final int ticket_id = getIntent().getIntExtra(Tickets.TICKET_ID,0);
        
		TimeTrackerApplication _app = (TimeTrackerApplication) getApplicationContext();
		Task currentTask = _app.getCurrentTask();
		if(currentTask!=null){
			btnStart.setVisibility(View.GONE);
			if(currentTask.getSpaceId().equals(space_id) && currentTask.getTicketNumber() == ticket_number){
				btnStop.setVisibility(View.VISIBLE);
			}
		}
		
		
		btnStart.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(LOGV) Log.v(TAG,"Start Ticket [ space_id=" + space_id + ", ticket_id=" +  String.valueOf(ticket_id)+ ", ticket_number=" +  String.valueOf(ticket_number)+"]" );
				TimeTrackerApplication.getInstance().startTicketTask(_id, space_id, ticket_id, ticket_number,description);
				v.setVisibility(View.GONE);
				btnStop.setVisibility(View.VISIBLE);
				
			}
		});
		btnStop.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(LOGV) Log.v(TAG,"Stop Ticket [ space_id=" + space_id + ", ticket_id=" +  String.valueOf(ticket_id)+ ", ticket_number=" +  String.valueOf(ticket_number)+"]" );
				TimeTrackerApplication.getInstance().stopTicketTask();
				v.setVisibility(View.GONE);
				btnStart.setVisibility(View.VISIBLE);
			}
		});*/
    }
	
	@Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("tab", mTabHost.getCurrentTabTag());
    }
}
