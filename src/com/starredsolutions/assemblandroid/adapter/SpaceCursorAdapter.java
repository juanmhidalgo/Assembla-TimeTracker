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
import com.starredsolutions.assemblandroid.provider.AssemblaContract.Spaces;

/**
 * @author Juan M. Hidalgo <juan@starredsolutions.com.ar>
 *
 */
public class SpaceCursorAdapter extends CursorAdapter implements Filterable{
	
	/**
	 * 
	 * @param context
	 * @param textViewResourceId
	 * @param items
	 */
	public SpaceCursorAdapter(Context context, Cursor c,	boolean autoRequery) {
		super(context, c, autoRequery);
		mContent = context.getContentResolver();
	}
	

	/**
	 * 
	 * @param context
	 * @param c
	 * @param flags
	 */
	public SpaceCursorAdapter(Context context, Cursor c, int flags) {
		super(context,c,flags);
		mContent = context.getContentResolver();
	}


	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		((TextView) view.findViewById(R.id.space_name)).setText(cursor.getString(1));
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		return li.inflate(R.layout.space_list_item, null);
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
            buffer.append(Spaces.NAME);
            buffer.append(") GLOB ?");
            args = new String[] { "*" + constraint.toString().toUpperCase() + "*" };
        }
        return mContent.query(Spaces.CONTENT_URI, Spaces.PROJECTION,buffer == null ? null : buffer.toString(), args, null);
	}
	
	private ContentResolver mContent;
}
