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

import com.starredsolutions.assemblandroid.R;
import com.starredsolutions.assemblandroid.provider.AssemblaContract.Spaces;

/**
 * @author Juan M. Hidalgo <juan@starredsolutions.com.ar>
 *
 */
public class DashboardFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View root = inflater.inflate(R.layout.fragment_dashboard, container);
		root.findViewById(R.id.btnSpaceList).setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				startActivity(new Intent(Intent.ACTION_VIEW, Spaces.CONTENT_URI));
			}
		});
		
		return root;
	}
}
