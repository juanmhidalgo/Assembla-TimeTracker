/**
 * 
 */
package com.starredsolutions.assemblandroid.adapter;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;
import android.widget.TextView;

import com.starredsolutions.assemblandroid.R;
import com.starredsolutions.assemblandroid.models.Ticket;
import com.starredsolutions.assemblandroid.provider.AssemblaContract.Tickets;

/**
 * @author Juan M. Hidalgo <juan@starredsolutions.com.ar>
 *
 */
public class TicketCursorAdapter extends CursorAdapter implements Filterable{
	protected ArrayList<Ticket> items;
	protected Context ctx;
	/*
	private int[] colors = new int[] {
			//0x30FF0000, 0x300000FF 
				0xffcc0000,  // Red
				0xffcc8a00,  // orange
				0xFFcccc00,  // jaune
				0xff00cc00,  // vert,
				0xff0000cc	 // bleu
			};
	
	*/
	
	
	/**
	 * 
	 * @param context
	 * @param c
	 * @param autoRequery
	 */
	public TicketCursorAdapter(Context context, Cursor c,	boolean autoRequery) {
		super(context, c, autoRequery);
		mContent = context.getContentResolver();
	}
	
	
	/*@Override
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
	}*/
	

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		((TextView) view.findViewById(R.id.ticket_name)).setText(cursor.getString(cursor.getColumnIndex(Tickets.SUMMARY)));
		
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		return li.inflate(R.layout.ticket_list_item, null);
	}
	
	@Override
    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        if (getFilterQueryProvider() != null) {
            return getFilterQueryProvider().runQuery(constraint);
        }
        StringBuilder buffer = null;
        String[] args = null;
        if (constraint != null) {
            buffer = new StringBuilder();
            buffer.append("UPPER(");
            buffer.append(Tickets.SUMMARY);
            buffer.append(") GLOB ?");
            args = new String[] { "*" + constraint.toString().toUpperCase() + "*" };
        }
        return mContent.query(Tickets.CONTENT_URI, Tickets.PROJECTION,buffer == null ? null : buffer.toString(), args, null);
	}
	
	private ContentResolver mContent;
}
