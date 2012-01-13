/**
 * 
 */
package com.starredsolutions.assemblandroid.ui;

import android.app.ListActivity;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ListView;

import com.starredsolutions.assemblandroid.R;
import com.starredsolutions.assemblandroid.adapter.TicketCursorAdapter;
import com.starredsolutions.assemblandroid.provider.AssemblaContract.Spaces;
import com.starredsolutions.assemblandroid.provider.AssemblaContract.Tickets;
import com.starredsolutions.utils.ActivityHelper;

/**
 * @author Juan M. Hidalgo <juan@starredsolutions.com.ar>
 *
 */
public class TicketListActivity extends ListActivity{
	protected final ActivityHelper mActivityHelper = ActivityHelper.createInstance(this);
	private Uri mUri;
	private Cursor mCursor;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.space_list);
        mActivityHelper.setupActionBar(getString(R.string.tickets_title), R.color.darkdarkgray, false);
        
        final ListView lv = getListView();
        lv.setTextFilterEnabled(true);

        mUri = getIntent().getData();
        String space_id = getIntent().getStringExtra(Spaces.SPACE_ID);
        
        long spId = ContentUris.parseId(mUri);
        if(space_id == null){
	        Cursor spCursor = managedQuery(mUri, new String[]{ Spaces._ID,Spaces.SPACE_ID}, null, null, null);
	        if(spCursor != null && spCursor.moveToFirst()){
	        	space_id = spCursor.getString(1);
	        	spCursor.close();
	        }
        }
        mCursor = managedQuery(Tickets.buildTicketBySpaceUri(spId), Tickets.PROJECTION, Tickets.SPACE_ID + "= ?", new String[]{space_id}, null);
        TicketCursorAdapter adapter = new TicketCursorAdapter(this, mCursor, false);
        setListAdapter(adapter);
	}
}
