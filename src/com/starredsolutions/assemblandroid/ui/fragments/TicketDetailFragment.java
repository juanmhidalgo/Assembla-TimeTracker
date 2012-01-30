/**
 * 
 */
package com.starredsolutions.assemblandroid.ui.fragments;

import android.app.ProgressDialog;
import android.content.ContentUris;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.starredsolutions.assemblandroid.Constants;
import com.starredsolutions.assemblandroid.R;
import com.starredsolutions.assemblandroid.TimeTrackerApplication;
import com.starredsolutions.assemblandroid.models.Task;
import com.starredsolutions.assemblandroid.provider.AssemblaContract.Tickets;
import com.starredsolutions.utils.base.OnCompleteListener;

/**
 * @author Juan M. Hidalgo <juan@starredsolutions.com.ar>
 *
 */
public class TicketDetailFragment extends Fragment {
	private static final String TAG = "TicketDetailFragment"; 
	private static final boolean LOGV = Log.isLoggable(TAG, Log.VERBOSE) || Constants.DEVELOPER_MODE;

	@Override 
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState){
		View v = inflater.inflate(R.layout.fragment_ticket_detail, container,false);
		final Button btnStart = (Button) v.findViewById(R.id.btnStart);
		final Button btnStop = (Button) v.findViewById(R.id.btnStop);
		
		
		
		final String space_id = getActivity().getIntent().getStringExtra(Tickets.SPACE_ID);
		final String description = getActivity().getIntent().getStringExtra(Tickets.DESCRIPTION);
		final int ticket_number = getActivity().getIntent().getIntExtra(Tickets.NUMBER,0); 
		final long _id = ContentUris.parseId(getActivity().getIntent().getData());
		final int ticket_id = getActivity().getIntent().getIntExtra(Tickets.TICKET_ID,0);
		
		TimeTrackerApplication _app = (TimeTrackerApplication) getActivity().getApplicationContext();
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
				
				final ProgressDialog mProgressDialog = ProgressDialog.show(getActivity(), "", "Sending to Assembla...");
				TimeTrackerApplication.getInstance().stopTicketTask(new OnCompleteListener() {
					
					public void onComplete(boolean result, String message) {
						getActivity().runOnUiThread(new Runnable() {
							public void run() {
								mProgressDialog.dismiss();
							}
						});
						
					}
				});
				v.setVisibility(View.GONE);
				btnStart.setVisibility(View.VISIBLE);
			}
		});
		
		return v;
	}
}
