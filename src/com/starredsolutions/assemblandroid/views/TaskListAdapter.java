package com.starredsolutions.assemblandroid.views;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;

public class TaskListAdapter extends SimpleAdapter
{
	private static final String LOG_TAG = TaskListAdapter.class.getSimpleName();
	
	public TaskListAdapter(Context context, List<HashMap<String, String>> items, int resource, String[] from, int[] to)
	{
		super(context, items, resource, from, to);
	}
	
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		View view = super.getView(position, convertView, parent);
		Object obj = this.getItem(position);
		
		HashMap<String,String> map = (HashMap<String,String>) obj;
		
		return view;
	}

}
