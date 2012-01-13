package com.starredsolutions.assemblandroid.views;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.starredsolutions.assemblandroid.R;
import com.starredsolutions.assemblandroid.TimeTrackerApplication;
import com.starredsolutions.assemblandroid.UIController;
import com.starredsolutions.assemblandroid.adapter.TicketCursorAdapter;
import com.starredsolutions.assemblandroid.asyncTask.IAsynctaskObserver;
import com.starredsolutions.utils.ActivityHelper;


public class TicketListingActivity extends ListActivity implements IAsynctaskObserver
{
    /*********************************************************************************************
     * CONSTANTS
     *********************************************************************************************/
    static private final String TAG = "AssemblaTT";
	
    /*********************************************************************************************
     * VARIABLES
     *********************************************************************************************/
    private TimeTrackerApplication _app;

	private ProgressDialog _loadingDialog;

	private ListView _listView;
    
    
	/*********************************************************************************************
     * ONE-LINER METHODS
     *********************************************************************************************/
    
    protected String getLogTag() { return TAG; }
    
    
    final ActivityHelper mActivityHelper = ActivityHelper.createInstance(this);
    private static TicketCursorAdapter adapter;
    /*********************************************************************************************
     * ACTIVITY WORKFLOW METHODS
     *********************************************************************************************/

    @Override protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        UIController.getInstance().onActivityCreated(this);
        _app = TimeTrackerApplication.getInstance();
        
        setContentView(R.layout.ticket_list);
        mActivityHelper.setupActionBar(getString(R.string.tickets_title) , 0, true);
        
        /*adapter = new TicketCursorAdapter(this, R.layout.ticket_list_item, _app.ticketsForList());
        adapter.setNotifyOnChange(true);
        setListAdapter(adapter);
        */
        
        _listView = getListView();
        _listView.setOnItemClickListener( new OnItemClickListener() {
        	public void onItemClick( AdapterView<?> parent, View view, int position, long id ) {
        		// When clicked, show a toast with the TextView text
        		//Log.i(TAG, "OnItemClickListener :: Position:" + Integer.toString(position) + " / id:" + Long.toString(id) + " / " + allTickets.get(position).toString());
        		
        		_app.selectTicket( position );
					
				// Launch another activity
                Intent myIntent = new Intent(TicketListingActivity.this, TicketDetailsActivity.class);
                TicketListingActivity.this.startActivity(myIntent);
        	}
        });
        
        loadTickets();
    }

    @Override protected void onResume() {
        super.onResume();
        onUpdate();
    }
    
    @Override protected void onDestroy() {
        super.onDestroy();
        onUpdate();
    }
    
    /**
     * Called when the options button (bottom left) is pressed
     */
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.tickets_listing_activity, menu);
        return true;
    }

    // This method is called once the menu is selected
    @Override public boolean onOptionsItemSelected(MenuItem item) {
        
        switch (item.getItemId()) {
            case R.id.preferences:
                // Launch Preference activity
                startActivity( new Intent(this, EditPreferencesActivity.class) );
                // Some feedback to the user
                Toast.makeText(this, "Here you can maintain your user credentials.", Toast.LENGTH_SHORT).show();
                break;
            
            case R.id.reload:
                loadTickets(true);
                break;
        }
        return true;
    }
	
	
	/*********************************************************************************************
     * PUBLIC METHODS
     *********************************************************************************************/
    
    /**
     * Called when tickets are ready
     */
	public void onUpdate() {
		if (_app.ticketsReady()) {
			hideLoadingDialog();
		}
	}

	public void onUpdateFailed(Exception exception) {
		hideLoadingDialog();
	}

    
    /*********************************************************************************************
     * INTERNAL METHODS
     *********************************************************************************************/
    
    private void loadTickets(){
        loadTickets(false);
    }
    
    private void loadTickets(boolean forceReload)
    {
        if (forceReload || !_app.ticketsReady()) {
            //Log.i(TAG, "tickets not there : NEED TO LOAD TICKETS");
            // Delay loading of records to avoid blocking the UI
            // Note: onUpdate() will be called when the task completes
            _loadingDialog = ProgressDialog.show(this, "", "Loading tickets. Please wait...", true);
            _app.fetchTicketsLater( this );
        } else {
            // Log.i(TAG, "tickets already there");
            // If records were already loaded, use the existing data
            onUpdate();
        }
    }
    
    private void hideLoadingDialog() {
        if (_loadingDialog != null) {
            _loadingDialog.cancel();
            _loadingDialog = null;
        }
    }
}