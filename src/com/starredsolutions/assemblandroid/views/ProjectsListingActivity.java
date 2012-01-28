package com.starredsolutions.assemblandroid.views;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
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
import com.starredsolutions.assemblandroid.adapter.SpaceCursorAdapter;
import com.starredsolutions.assemblandroid.asyncTask.IAsynctaskObserver;


public class ProjectsListingActivity extends ListActivity implements IAsynctaskObserver{
    /*********************************************************************************************
     * CONSTANTS
     *********************************************************************************************/
    static private final String TAG = "AssemblaTT";
	
	/*********************************************************************************************
     * VARIABLES
     *********************************************************************************************/
    private TimeTrackerApplication _app;
	private ProgressDialog _loadingDialog;
	
	private static SpaceCursorAdapter adapter;
	
	

	/*********************************************************************************************
     * ACTIVITY WORKFLOW METHODS
     *********************************************************************************************/
    
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UIController.getInstance().onActivityCreated(this);

        _app = TimeTrackerApplication.getInstance();

        setContentView(R.layout.project_list);
        
        
        
        ListView lv = getListView();
        lv.setTextFilterEnabled(true);

        lv.setOnItemClickListener( new OnItemClickListener(){
        	public void onItemClick( AdapterView<?> parent, View view, int position, long id ){
        		UIController.getInstance().onProjectSelected( ProjectsListingActivity.this, position );
        	}
        });
        
        loadProjects(false);
    }
    
    /**
     * Called when the options button (bottom left) is pressed
     */
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        log("onCreateOptionsMenu", "");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.projects_listing_activity, menu);
        return true;
    }

    // This method is called once the menu is selected
    @Override public boolean onOptionsItemSelected(MenuItem item) {
        log("onOptionsItemSelected", "");
        
        switch (item.getItemId()) {
            case R.id.preferences:
                // Launch Preference activity
                startActivity( new Intent(this, EditPreferencesActivity.class) );
                // Some feedback to the user
                Toast.makeText(this, "Here you can maintain your user credentials.", Toast.LENGTH_SHORT).show();
                break;
            
            case R.id.reload:
                loadProjects(true);
                break;
        }
        return true;
    }
    
    /**
     * Called to retrieve per-instance state from an activity before being killed so that the 
     * state can be restored in onCreate(Bundle) or onRestoreInstanceState(Bundle) (the Bundle 
     * populated by this method will be passed to both).
     */
    @Override protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
    }
    
    @Override public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(null);
    }
    
    @Override protected void onResume() {
        super.onResume();
        UIController.getInstance().onActivityResumed(this);
        onUpdate();
    }
    
    @Override protected void onPause() {
        super.onResume();
        UIController.getInstance().onActivityPaused(this);
    }
    
    @Override protected void onDestroy() {
        super.onDestroy();
        UIController.getInstance().onActivityDestroyed(this);
    }


    /*********************************************************************************************
     * PUBLIC METHODS
     *********************************************************************************************/

    /**
     * Called when projects are loaded ( via app.fetchProjectsLater() )
     */
	public void onUpdate() {
		//Log.i(TAG, "onUpdate");
		if (_app.projectsReady()) {
	        hideLoadingDialog();
		}
	}

	public void onUpdateFailed(Exception exception) {
		log("onUpdateFailed", exception.getMessage());
		hideLoadingDialog();
	}
	
	
	/*********************************************************************************************
     * INTERNAL METHODS
     *********************************************************************************************/
    
    private void loadProjects(boolean forceReload) {
        if (forceReload || !_app.projectsReady()) {
            // Delay loading of records to avoid blocking the UI
            // Note: onUpdate() will be called when the task completes
            _loadingDialog = ProgressDialog.show(this, "", "Loading projects. Please wait...", true);
            _app.fetchProjectsLater( this );
        } else {
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
	
	protected void log(String func, String msg) {
        Log.i( TAG , getClass().getSimpleName() + "::" + func + "() " + msg);
    }
}
