/**
 * 
 */
package com.starredsolutions.assemblandroid.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.starredsolutions.assemblandroid.R;
import com.starredsolutions.assemblandroid.TimeTrackerApplication;
import com.starredsolutions.assemblandroid.models.Task;
import com.starredsolutions.assemblandroid.provider.AssemblaContract.Spaces;
import com.starredsolutions.assemblandroid.provider.AssemblaContract.Tickets;

/**
 * @author Juan M. Hidalgo <juan@starredsolutions.com.ar>
 *
 */
public class DashboardFragment extends Fragment {
	Task mCurrentTask;
	Button btnTask;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		
		
		View root = inflater.inflate(R.layout.fragment_dashboard, container);
		root.findViewById(R.id.btnSpaceList).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(Intent.ACTION_VIEW, Spaces.CONTENT_URI));
			}
		});
		
		btnTask = (Button) root.findViewById(R.id.btnCurrentTask); 
		btnTask.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				if(mCurrentTask != null){
					startActivity(new Intent(Intent.ACTION_VIEW,Tickets.buildTicketUri(Long.toString(mCurrentTask.get_id()))));
				}
			}
		});

		
		return root;
	}
	
	@Override
	public void onResume(){
		super.onResume();
		mCurrentTask = ((TimeTrackerApplication) getActivity().getApplicationContext()).getCurrentTask();
		if(mCurrentTask != null){
			btnTask.setVisibility(View.VISIBLE);
		}else{
			btnTask.setVisibility(View.GONE);
		}
	}
}
