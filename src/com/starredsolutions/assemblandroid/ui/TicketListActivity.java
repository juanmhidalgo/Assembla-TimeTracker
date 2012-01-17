/**
 * 
 */
package com.starredsolutions.assemblandroid.ui;

import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.starredsolutions.assemblandroid.Constants;
import com.starredsolutions.assemblandroid.R;
import com.starredsolutions.assemblandroid.adapter.TicketCursorAdapter;
import com.starredsolutions.assemblandroid.provider.AssemblaContract.Spaces;
import com.starredsolutions.assemblandroid.provider.AssemblaContract.Tickets;
import com.starredsolutions.utils.ActivityHelper;

/**
 * @author Juan M. Hidalgo <juan@starredsolutions.com.ar>
 *
 */
public class TicketListActivity extends ListActivity  implements OnItemClickListener{
	private static final String TAG = "TicketListActivity"; 
	private static final boolean LOGV = Log.isLoggable(TAG, Log.VERBOSE) || Constants.DEVELOPER_MODE;
	protected final ActivityHelper mActivityHelper = ActivityHelper.createInstance(this);
	private Uri mUri;
	private Cursor mCursor;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.space_list);
        
        String space_name = getIntent().getStringExtra(Spaces.NAME);
        
       	mActivityHelper.setupActionBar(getString(R.string.tickets_title), R.color.darkdarkgray, false);
        
        final ListView lv = getListView();
        lv.setTextFilterEnabled(true);
        lv.setOnItemClickListener( this );
        
        TextView header = new TextView(this);
        header.setText("[Space] " + space_name);
        header.setPadding(5, 8, 5, 8);
        header.setBackgroundColor(R.color.darkgray);
        header.setTextColor(R.color.white);
        
        lv.addHeaderView(header);
        
        
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
	
	public void onItemClick(AdapterView<?>parent, View view, int position, long id) {
		String space_id = null;
		int ticket_number = 0;
		try{
			Cursor c =  (Cursor) parent.getItemAtPosition(position);
			if(c != null){
				space_id = c.getString(c.getColumnIndex(Tickets.SPACE_ID));
				ticket_number = c.getInt(c.getColumnIndex(Tickets.NUMBER));
				
			}
		}catch(Exception e){
			Log.e(TAG, "onItemClick", e);
		}
		
		Uri uri = Tickets.buildTicketUri(String.valueOf(id));
		if(LOGV) Log.v(TAG,"Uri: " + uri);
		
		Intent it = new Intent(Intent.ACTION_VIEW, uri);
		it.putExtra(Tickets.SPACE_ID, space_id);
		it.putExtra(Tickets.NUMBER, ticket_number);
		startActivity(it);
	}
}
