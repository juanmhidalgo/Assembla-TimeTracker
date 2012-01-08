/**
 * 
 */
package com.starredsolutions.assemblandroid.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.starredsolutions.assemblandroid.R;
import com.starredsolutions.assemblandroid.models.Space;

/**
 * @author Juan M. Hidalgo <juan@starredsolutions.com.ar>
 *
 */
public class SpaceAdapter extends ArrayAdapter<Space> {
	protected ArrayList<Space> items;
	protected Context ctx;
	
	/**
	 * 
	 * @param context
	 * @param textViewResourceId
	 * @param items
	 */
	public SpaceAdapter(Context context, int textViewResourceId, ArrayList<Space> items) {
		super(context,textViewResourceId,items);
		this.ctx = context;
		this.items = items;
	}
	
	/**
	 * 
	 * @return
	 */
	@Override
	public int getCount(){
		return this.items != null ? this.items.size() : 0;
	}
	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			LayoutInflater vi = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = vi.inflate(R.layout.list_item, null);
			holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(R.id.space_name);
			convertView.setTag(holder);

		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		
		Space sp= items.get(position);
		if(sp != null){
			holder.name.setText(sp.getName());
		}
		return convertView;
	}
	
	static class ViewHolder{
		TextView name;
	}
}
