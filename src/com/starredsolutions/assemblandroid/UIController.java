package com.starredsolutions.assemblandroid;

import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.starredsolutions.assemblandroid.TimeTrackerModel.ACTIVITY;
import com.starredsolutions.assemblandroid.views.ProjectsListingActivity;
import com.starredsolutions.assemblandroid.views.TicketDetailsActivity;
import com.starredsolutions.assemblandroid.views.TicketListingActivity;
import com.starredsolutions.assemblandroid.views.TimeEntryActivity;


public class UIController
{
    /*********************************************************************************************
     * CONSTANTS
     *********************************************************************************************/
    static private final String LOG_TAG = UIController.class.getSimpleName();
    
    
    /*********************************************************************************************
     * VARIABLES
     *********************************************************************************************/
    private TimeTrackerApplication _app;
    
    private boolean _launched = false;
    
    
    /*********************************************************************************************
     * SINGLETON PATTERN
     *********************************************************************************************/
    static private final UIController __instance = new UIController();
    static public UIController getInstance() { return __instance; }
    
    
    private UIController() {
        log("Initializing UIController");
        _app = TimeTrackerApplication.getInstance();
    }
    
    
    /*********************************************************************************************
     * PUBLIC METHODS
     *********************************************************************************************/
    public void onActivityCreated(Activity activity) {
        log( activity.getClass().getSimpleName() + "\t ===>  CREATED");
        
        if (!_launched)
        {
            //ACTIVITY previous = _app.getSavedPreviousActivity();
            //if (previous != null && previous != ACTIVITY.LAUNCHING && previous != ACTIVITY.PROJECTS_LISTING)
            if ( _app.isRecordingTimeEntry() )
            {
                //log("onActivityCreated", "***** Manually restoring previous activity: " + previous );
                log("***** Manually restoring previous activity: TimeEntry" );
                
                // Rebuild Activity stack
                startActivity(activity, ProjectsListingActivity.class);
                startActivity(activity, TicketListingActivity.class);
                startActivity(activity, TicketDetailsActivity.class);
                startActivity(activity, TimeEntryActivity.class);
            } else {
                log("***** Launching default activity.");
                startActivity(activity, ProjectsListingActivity.class);
            }
            
            _launched = true;
        }
    }
    
    public void onActivityResumed(Activity activity) {
        log(activity.getClass().getSimpleName() + "\t ===> resumed");
        
        if (activity instanceof ProjectsListingActivity)
            _app.onActivityChanged( ACTIVITY.PROJECTS_LISTING );
        else if (activity instanceof TicketListingActivity)
            _app.onActivityChanged( ACTIVITY.TICKETS_LISTING );
        else if (activity instanceof TicketDetailsActivity)
            _app.onActivityChanged( ACTIVITY.TICKET_DETAILS );
        else if (activity instanceof TimeEntryActivity)
            _app.onActivityChanged( ACTIVITY.TIME_ENTRY );
        
//        printBackStack(activity);
    }
    
    public void onActivityPaused(Activity activity) {
        log(activity.getClass().getSimpleName() + "\t ===> paused");
    }

    public void onActivityDestroyed(Activity activity) {
        log(activity.getClass().getSimpleName() + "\t ===> DESTROYED");
    }


    public void onProjectSelected(Activity activity, int position) {
        _app.selectProject(position);
        
        startActivity(activity, TicketListingActivity.class);
    }
    
    public void onTicketSelected(Activity activity, int position) {
        _app.selectTicket(position);
        
        startActivity(activity, TicketDetailsActivity.class);
    }
    
    public void onTicketStartClicked(Activity activity) {
        _app.startTimeEntry();
        
        startActivity(activity, TimeEntryActivity.class);
    }
    
    
    /*********************************************************************************************
     * INTERNAL METHODS
     *********************************************************************************************/
    @SuppressWarnings("rawtypes")
    private void startActivity(Activity caller, Class newActivityClass)
    {
        Intent intent = new Intent(caller, newActivityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        caller.startActivity( intent );
    }
    
    private void printBackStack(Context context) {
        final ActivityManager       am           = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<RunningTaskInfo> taskInfoList = am.getRunningTasks(1024);
        
        if (!taskInfoList.isEmpty())
        {
            final String ourAppPackageName = context.getPackageName();
            
            //Log.i(TAG, "printBackStack() Current Activity: " + _model.curActivity() + " ; is recording TimeEntry = " + isRecordingTimeEntry() );
            //Log.i(TAG, "printBackStack() Running tasks in " + ourAppPackageName);
            
            for (RunningTaskInfo taskInfo : taskInfoList)
            {
                if (ourAppPackageName.equals(taskInfo.baseActivity.getPackageName())) {
                    log("printBackStack() " +
                            taskInfo.numRunning + "/" + taskInfo.numActivities + " activities running. " +
                            " [Top=" + taskInfo.topActivity.getShortClassName() + 
                            "] [Base=" + taskInfo.baseActivity.getShortClassName() + "]");
                }
            }
        }
    }
    
    private void log(String msg) {
        Log.i(LOG_TAG, msg);
    }
    
}
