/**
 * 
 */
package com.starredsolutions.assemblandroid.adapter;

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
import com.starredsolutions.assemblandroid.provider.AssemblaContract.Tickets;

/**
 * @author Juan M. Hidalgo <juan@starredsolutions.com.ar>
 *
 */
public class TicketCursorAdapter extends CursorAdapter implements Filterable{
	protected Context ctx;

	/**
	 * 
	 * @param context
	 * @param c
	 * @param flags
	 */
	public TicketCursorAdapter(Context context, Cursor c, int flags) {
		super(context,c,flags);
		mContent = context.getContentResolver();
	}
	
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
