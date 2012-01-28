package com.starredsolutions.assemblandroid.views;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.starredsolutions.assemblandroid.R;
import com.starredsolutions.assemblandroid.TimeTrackerApplication;
import com.starredsolutions.assemblandroid.UIController;
import com.starredsolutions.assemblandroid.asyncTask.IAsynctaskObserver;
import com.starredsolutions.assemblandroid.models.Task;
import com.starredsolutions.assemblandroid.models.Ticket;


public class TicketDetailsActivity extends BaseActivity implements IAsynctaskObserver
{
    /*********************************************************************************************
     * CONSTANTS
     *********************************************************************************************/
    private static final String TAG = "AssemblaTT";
	
    /*********************************************************************************************
     * VARIABLES
     *********************************************************************************************/
    private TimeTrackerApplication _app;
	private ProgressDialog _loadingDialog;
	private ListView _listView;
	
	private TextView _txtName;
	private TextView _txtStatus;
	private TextView _txtHours;
	private TextView _txtDescription;

	private Ticket _ticket;
    
    
	/*********************************************************************************************
     * ONE-LINER METHODS
     *********************************************************************************************/
    
    protected String getLogTag() { return TAG; }
    
    
    
    /*********************************************************************************************
     * ACTIVITY WORKFLOW METHODS
     *********************************************************************************************/
    
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Data
        _app = TimeTrackerApplication.getInstance();
        
        _ticket = _app.curTicket();
        
        setContentView(R.layout.ticket_details);
        
        LinearLayout layout = (LinearLayout) findViewById(R.id.ticketDetailsLayout);
         
        _txtName = (TextView) layout.findViewById(R.id.txtName);
        _txtStatus = (TextView) layout.findViewById(R.id.txtStatus);
        _txtHours = (TextView) layout.findViewById(R.id.txtHours);
        _txtDescription = (TextView) layout.findViewById(R.id.txtDescription);
        
        //_listView = (ListView)findViewById(R.id.taskListView);
        
        
        loadTasks(false);
    }
    
    @Override protected void onResume() {
        super.onResume();
        
        updateFields();
    }

	
	/*********************************************************************************************
     * PUBLIC METHODS
     *********************************************************************************************/

    public void onStartClicked(View v) {
        UIController.getInstance().onTicketStartClicked(this);
    }
    

    public void onUpdate()
    {
        if(!_app.tasksReady())
            return;
        
        ArrayList<Task> tasks = _app.tasksForList();
        
        float hours = getWorkedHours(tasks);
        _ticket.setWorkedHours(hours);
        
        updateFields();
        
        String[] from = {"A", "B"};
        int[] to = { R.id.taskTime, R.id.taskDescription };
        ArrayList<HashMap<String, String>> items = createItems(tasks);
        
        TaskListAdapter adapter = new TaskListAdapter(this, items, R.layout.task_list_item, from, to);
        _listView.setAdapter(adapter);
        
        hideLoadingDialog();
    }

    public void onUpdateFailed(Exception exception)
    {
        log("onUpdateFailed", exception.getMessage());
        hideLoadingDialog();
    }
    
    
	
	/*********************************************************************************************
     * INTERNAL METHODS
     *********************************************************************************************/
    
	private void loadTasks(boolean forceReload)
    {
    	if(forceReload || !_app.tasksReady())
    	{
    		_loadingDialog = ProgressDialog.show(this, "", "Loading time entries. Please wait...", true);
    		_app.fetchTasksLater(this);
    	}
    	else
    		onUpdate();    
    }
    
    private void updateFields()
    {
        _txtName.setText(_ticket.shortFormattedName());
        
        _txtStatus.setText("Status: " + _ticket.getStatusName() +  (_ticket.unassigned() ? "   (UNASSIGNED)" : "")  );
        
        int percent = Math.round( 100 * _ticket.workedHours() / _ticket.workingHours() );
        _txtHours.setText("Hours:  " 
                + _ticket.workedHoursHuman() + " / " + _ticket.workingHoursHuman() 
                + String.format("   (%d%%)", percent) );
        
        _txtDescription.setText("Description: \n" + _ticket.description());
    }
    
    private float getWorkedHours(ArrayList<Task> tasks)
    {
        float hours = 0;
        for(Task task : tasks)
            hours += task.elapsedHours();

        return hours;
    }
    
    private ArrayList<HashMap<String,String>> createItems(ArrayList<Task> tasks)
    {
        ArrayList<HashMap<String, String>> items = new ArrayList<HashMap<String,String>>();
        if(tasks != null)
        {
            for(Task t : tasks)
            {
                HashMap<String,String> hash = new HashMap<String,String>();
                hash.put("A", t.elapsedHours()+"h");
                hash.put("B", t.getDescription());
                items.add(hash);
            }
        }
        
        return items;
    }
	
	private void hideLoadingDialog()
	{
		if (_loadingDialog != null) {
        	_loadingDialog.cancel();
        	_loadingDialog = null;
        }
	}
}
