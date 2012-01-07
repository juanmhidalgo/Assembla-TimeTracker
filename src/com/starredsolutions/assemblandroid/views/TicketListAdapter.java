package com.starredsolutions.assemblandroid.views;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.starredsolutions.assemblandroid.R;

public class TicketListAdapter extends SimpleAdapter
{
	private static final String LOG_TAG = TicketListAdapter.class.getSimpleName();
	
	// http://www.colorschemer.com/online.html
	// http://www.perbang.dk/rgb/347C12/
	// Format: AARRGGBB
	
	private int[] colors = new int[] {
			//0x30FF0000, 0x300000FF 
				0xffcc0000,  // Red
				0xffcc8a00,  // orange
				0xFFcccc00,  // jaune
				0xff00cc00,  // vert,
				0xff0000cc	 // bleu
			};

	public TicketListAdapter(Context context, List<HashMap<String, String>> items, int resource, String[] from, int[] to) {
		super(context, items, resource, from, to);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	  
	  View view = super.getView(position, convertView, parent);
	  Object obj = this.getItem(position);
	  
	  HashMap<String,String> map = (HashMap<String,String>) obj;	  
	  int priority = new Integer((String)map.get("A"))-1;
	  
	  setListItemColor(view, priority);
	  
	  return view;
	}
	
	protected void setListItemColor(View view, int priority)
	{
		TextView item1 = (TextView)view.findViewById(R.id.item1);
		TextView item2 = (TextView)view.findViewById(R.id.item2);
		TextView item3 = (TextView)view.findViewById(R.id.item3);	
		item1.setText("");
		  
		TextView[] items = { item1, item2, item3 };
		for(TextView item : items)
			item.setTextColor(colors[priority]);	
	}
}