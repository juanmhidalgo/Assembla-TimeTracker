/**
 * 
 */
package com.starredsolutions.assemblandroid.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TabHost;

import com.starredsolutions.assemblandroid.R;
import com.starredsolutions.assemblandroid.ui.fragments.TaskListFragment;
import com.starredsolutions.assemblandroid.ui.fragments.TicketDetailFragment;
import com.starredsolutions.utils.TabManager;

/**
 * @author Juan M. Hidalgo <juan@starredsolutions.com.ar>
 */
public class TicketDetailActivity extends ActionBarActivity{
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
    }
	
	
	@Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("tab", mTabHost.getCurrentTabTag());
    }
	
	public static class StopTaskDialogFragment extends DialogFragment{
		private static final String TAG = "StopTaskDialogFragment"; 
		static DialogInterface.OnClickListener positiveAction;
		static DialogInterface.OnClickListener negativeAction;
		public static StopTaskDialogFragment newInstance(DialogInterface.OnClickListener positive,DialogInterface.OnClickListener negative){
			StopTaskDialogFragment frag = new StopTaskDialogFragment();
			Bundle args = new Bundle();
            frag.setArguments(args);
            
            if(positive != null){
            	positiveAction = positive;
            }else{
            	positiveAction = new DialogInterface.OnClickListener() {
            		public void onClick(DialogInterface dialog, int which) {
            			Log.d(TAG,"Positive Action");
            		}
            	};
            }
            
            if(negative!= null){
            	negativeAction = negative;
            }else{
            	negativeAction = new DialogInterface.OnClickListener() {
            		public void onClick(DialogInterface dialog, int which) {
            			Log.d(TAG,"Negative Action");
            		}
            	};
            }
            
            return frag;
		}
		@Override
	    public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setStyle(DialogFragment.STYLE_NORMAL,android.R.style.Theme_Dialog);
		}
		
		@Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
			LayoutInflater inflater = LayoutInflater.from(getActivity());
			final View v = inflater.inflate(R.layout.fragment_stop_dialog, null);
			return new AlertDialog.Builder(getActivity())
					.setIcon(android.R.drawable.ic_dialog_info)
					.setView(v)
					.setPositiveButton(R.string.ok, positiveAction)
					.setNegativeButton(R.string.cancel, negativeAction)
					.create();
		}
	}
	
}
