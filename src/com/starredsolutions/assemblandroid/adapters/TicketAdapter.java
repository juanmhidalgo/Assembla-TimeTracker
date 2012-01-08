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
import com.starredsolutions.assemblandroid.models.Ticket;

/**
 * @author Juan M. Hidalgo <juan@starredsolutions.com.ar>
 *
 */
public class TicketAdapter extends ArrayAdapter<Ticket> {
	protected ArrayList<Ticket> items;
	protected Context ctx;
	
	private int[] colors = new int[] {
			//0x30FF0000, 0x300000FF 
				0xffcc0000,  // Red
				0xffcc8a00,  // orange
				0xFFcccc00,  // jaune
				0xff00cc00,  // vert,
				0xff0000cc	 // bleu
			};
	/**
	 * 
	 * @param context
	 * @param textViewResourceId
	 * @param items
	 */
	public TicketAdapter(Context context, int textViewResourceId, ArrayList<Ticket> items) {
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
			convertView = vi.inflate(R.layout.ticket_list_item, null);
			holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(R.id.ticket_name);
			holder.hours= (TextView) convertView.findViewById(R.id.ticket_hours);
			convertView.setTag(holder);

		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		
		Ticket tk= items.get(position);
		if(tk != null){
			holder.name.setText(tk.getName());
			holder.hours.setText(String.format("%s / %s", tk.workedHoursHuman(), tk.workingHoursHuman()));
			holder.name.setTextColor(this.colors[tk.getPriority()-1]);
			holder.hours.setTextColor(this.colors[tk.getPriority()-1]);
			
		}
		return convertView;
	}
	
	static class ViewHolder{
		TextView name;
		TextView hours;
	}
}
