package com.starredsolutions.assemblandroid.views;

import com.starredsolutions.assemblandroid.R;
import com.starredsolutions.assemblandroid.TimeTrackerApplication;
import com.starredsolutions.assemblandroid.asyncTask.IAsynctaskObserver;
import com.starredsolutions.assemblandroid.models.Task;
import com.starredsolutions.assemblandroid.models.Ticket;
import com.starredsolutions.assemblandroid.models.Task.State;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


public class TimeEntryActivity extends BaseActivity implements IAsynctaskObserver
{
    /*********************************************************************************************
     * CONSTANTS
     *********************************************************************************************/
	static private final String TAG = "AssemblaTT";

    /*********************************************************************************************
     * VARIABLES
     *********************************************************************************************/
	private TimeTrackerApplication _app;
	
	private Button   _btnPauseResume;
	private TextView _txtElapsedTime;
	private EditText _editLogMessage;
	
	private ShowElapsedTimeTask _showTimerTask;

	private ProgressDialog _loadingDialog;
    
    
	/*********************************************************************************************
     * ONE-LINER METHODS
     *********************************************************************************************/
    
    protected String getLogTag() { return TAG; }
    
    
    /*********************************************************************************************
     * ACTIVITY WORKFLOW METHODS
     *********************************************************************************************/
    
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        _app = TimeTrackerApplication.getInstance();
        
        Task task     = _app.curTask();
        Ticket ticket = _app.curTicket();
        
        // View
        setContentView(R.layout.time_entry);
        
        LinearLayout layout = (LinearLayout) findViewById(R.id.timeEntryLayout);
        
        TextView txtView = (TextView) layout.findViewById(R.id.txtSpaceName);
        txtView.setText( "Project: " + _app.curProject().name() );
        txtView = (TextView) layout.findViewById(R.id.txtTicketName);
        txtView.setText( "Ticket:  " + ticket.longFormattedName() );
        txtView = (TextView) layout.findViewById(R.id.txtTicketDescription);
        txtView.setText( "Description: \n" + ticket.description() );
        txtView = (TextView) layout.findViewById(R.id.txtStartedAt);
        txtView.setText( "Started at: " + task.getBeginAt().toString() );
        
        _btnPauseResume = (Button) layout.findViewById(R.id.btnPauseResume);
        _btnPauseResume.setText(  (task.getState()==State.STARTED) ? "Pause" : "Resume");
        
        _txtElapsedTime = (TextView) layout.findViewById(R.id.txtElapsedTime);
        _editLogMessage = (EditText) layout.findViewById(R.id.editLogMessage);
        _editLogMessage.setText(ticket.lastLogMessage());
        
        // Start the task:
        _showTimerTask = new ShowElapsedTimeTask();
        _showTimerTask.execute(task);
    }
    
    @Override protected void onDestroy() {
        super.onDestroy();
        _showTimerTask.cancel(true);
    }
    
    /**
     * Override the default back button behaviour to force saving of time entry.
     * 
     * The Activity won't finish until the task is commited.
     */
    @Override public void onBackPressed() {
        commitTimeEntry(false); 
    }

	
    /*********************************************************************************************
     * PUBLIC METHODS
     *********************************************************************************************/

    public void onPauseResumeClicked(View v) {
        if (_app.togglePauseTimeEntry() == State.STARTED)
            _btnPauseResume.setText( "Pause" );
        else
            _btnPauseResume.setText( "Resume" );
    }
    
    public void onStopClicked(View v) {
        commitTimeEntry(false);
    }
    
    public void onMarkToTestClicked(View v) {
        commitTimeEntry(true);
    }
    
    /**
     * Since the back button's behavior is overwritten, we need a way out.
     * @param v
     */
    public void onCancelClicked(View v) {
        //Log.i(TAG, "onCancelClicked");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you cancel this time entry ?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                TimeEntryActivity.this.finish();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    
    public void onUpdate() {
        //Log.i(TAG, "Timeentry onUpdate!!!");
        
        Toast.makeText( getApplicationContext(), 
                    "Time Entry Saved! " + _app.curTask().elapsedTime() + " hours logged.", 
                    Toast.LENGTH_LONG).show();
        
        finish();
    }

    public void onUpdateFailed(Exception exception) {
        log("onUpdateFailed", exception.getMessage());
        hideLoadingDialog();
        _btnPauseResume.setText( "Resume" );
    }
    
    
    /*********************************************************************************************
     * INTERNAL METHODS
     *********************************************************************************************/
    private void commitTimeEntry(boolean markToTest) {
    	// The controller will automatically update the data
    	_app.stopTimeEntry();
    	_app.saveTimeEntryLater( _editLogMessage.getText().toString(), markToTest, this );
    	
    	_loadingDialog = ProgressDialog.show(this, "", "Saving time entry. Please wait...", true);
    }
	
	private void hideLoadingDialog() {
		if (_loadingDialog != null) {
        	_loadingDialog.cancel();
        	_loadingDialog = null;
        }
	}
	
    
    /*********************************************************************************************
     * INNER CLASS
     *********************************************************************************************/
    private class ShowElapsedTimeTask extends AsyncTask<Task, Task, Void>
    {
    	private Task task;

		@Override
		protected Void doInBackground(Task... params) {
			Log.i(TAG, "ShowElapsedTimeTask::doInBackground");
			if (params.length > 0) { 
				task = params[0];
				
				try {
					do {
						Thread.sleep(1 * 1000);
						publishProgress(task);
					} while ( !isCancelled() );
					
					//Log.i(TAG, "Task has been gracefully cancelled!");
				
				} catch (InterruptedException e) {
					//e.printStackTrace();
					Log.i(TAG, "ShowElapsedTimeTask has been interrupted!");
					cancel(true);
				}
				
				return null;
			} else {
				Log.e(TAG, "Missing Task argument!");
				return null;
			}
		}
    	
		protected void onProgressUpdate(Task... params) {
			//Log.i(TAG, "onProgressUpdate  ");
			if (params.length == 0)  return ;
			task = params[0];
			
			_txtElapsedTime.setText( "Elapsed Time: " + task.elapsedTime() );
		}
    	
		protected void onCancelled() {
			Log.i(TAG, "ShowElapsedTime::onCancelled");
		}
    }

}
