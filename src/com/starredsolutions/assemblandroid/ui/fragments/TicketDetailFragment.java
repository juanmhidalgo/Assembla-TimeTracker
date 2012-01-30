/**
 * 
 */
package com.starredsolutions.assemblandroid.ui.fragments;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.starredsolutions.assemblandroid.Constants;
import com.starredsolutions.assemblandroid.R;
import com.starredsolutions.assemblandroid.TimeTrackerApplication;
import com.starredsolutions.assemblandroid.provider.AssemblaContract.Tickets;
import com.starredsolutions.utils.base.OnCompleteListener;

/**
 * @author Juan M. Hidalgo <juan@starredsolutions.com.ar>
 *
 */
public class TicketDetailFragment extends Fragment implements LoaderCallbacks<Cursor> {
	private static final String TAG = "TicketDetailFragment"; 
	private static final boolean LOGV = Log.isLoggable(TAG, Log.VERBOSE) || Constants.DEVELOPER_MODE;
	private TimeTrackerApplication _app;

	private String mSpaceId = null;
	private String mSummary = null;
	private int mTicketNumber = 0;
	private int _id = 0;
	private int mTicketId = 0;
	
	private TextView _summaryView;
	private TextView _statusView;
	private TextView _priorityView;
	private TextView _hoursView;
	private TextView _descriptionView;
	private Button btnStart;
	private Button btnStop;
	
	@Override 
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		_app = (TimeTrackerApplication) getActivity().getApplicationContext();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState){
		View v = inflater.inflate(R.layout.fragment_ticket_detail, container,false);
		btnStart = (Button) v.findViewById(R.id.btnStart);
		btnStop = (Button) v.findViewById(R.id.btnStop);
		
		_summaryView = (TextView) v.findViewById(R.id.ticket_summary);
		_statusView = (TextView) v.findViewById(R.id.ticket_status);
		_priorityView = (TextView) v.findViewById(R.id.ticket_priority);
		_hoursView = (TextView) v.findViewById(R.id.ticket_hours);
		_descriptionView = (TextView) v.findViewById(R.id.ticket_description);
		 
		
		btnStart.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(LOGV) Log.v(TAG,"Start Ticket [ space_id=" + mSpaceId + ", ticket_id=" +  String.valueOf(mTicketId)+ ", ticket_number=" +  String.valueOf(mTicketNumber)+"]" );
				TimeTrackerApplication.getInstance().startTicketTask(_id, mSpaceId, mTicketId, mTicketNumber,mSummary);
				v.setVisibility(View.GONE);
				btnStop.setVisibility(View.VISIBLE);
				
			}
		});
		btnStop.setOnClickListener(new OnClickListener() {
			public void onClick(final View v) {
				if(LOGV) Log.v(TAG,"Stop Ticket [ space_id=" + mSpaceId + ", ticket_id=" +  String.valueOf(mTicketId)+ ", ticket_number=" +  String.valueOf(mTicketNumber)+"]" );
				
				final ProgressDialog mProgressDialog = ProgressDialog.show(getActivity(), "", "Sending to Assembla...");
				TimeTrackerApplication.getInstance().stopTicketTask(new OnCompleteListener() {
					
					public void onComplete(boolean result, String message) {
						getActivity().runOnUiThread(new Runnable() {
							public void run() {
								mProgressDialog.dismiss();
								v.setVisibility(View.GONE);
								btnStart.setVisibility(View.VISIBLE);
							}
						});
					}
				});
			}
		});
		
		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		getLoaderManager().initLoader(0, null, this);
	}

	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if(LOGV) Log.v(TAG,getActivity().getIntent().getDataString());
		
		return new CursorLoader(getActivity(), getActivity().getIntent().getData(), Tickets.PROJECTION, null, null, null);
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		if(data.moveToFirst()){
			mSpaceId = data.getString(data.getColumnIndex(Tickets.SPACE_ID));
			mSummary = data.getString(data.getColumnIndex(Tickets.SUMMARY));
			mTicketNumber = data.getInt(data.getColumnIndex(Tickets.NUMBER));
			_id = data.getInt(data.getColumnIndex(Tickets._ID));
			mTicketId = data.getInt(data.getColumnIndex(Tickets.TICKET_ID));
			
			_summaryView.setText(mSummary);
			_statusView.setText(data.getString(data.getColumnIndex(Tickets.STATUS)));
			_priorityView.setText(Integer.toString(data.getInt(data.getColumnIndex(Tickets.PRIORITY))));
			_hoursView.setText( Float.toString(data.getFloat(data.getColumnIndex(Tickets.WORKING_HOURS))));
			_descriptionView.setText(data.getString(data.getColumnIndex(Tickets.DESCRIPTION)));
			
			
			if(_app.getCurrentTask()!=null){
				btnStart.setVisibility(View.GONE);
				if(_app.getCurrentTask().getSpaceId().equals(mSpaceId) && _app.getCurrentTask().getTicketNumber() == mTicketNumber){
					btnStop.setVisibility(View.VISIBLE);
				}
			}

		}else{
			if(LOGV) Log.v(TAG,"Ticket Not found");
			getActivity().finish();
		}
		
	}

	public void onLoaderReset(Loader<Cursor> loader) {
		
		
	}
}
