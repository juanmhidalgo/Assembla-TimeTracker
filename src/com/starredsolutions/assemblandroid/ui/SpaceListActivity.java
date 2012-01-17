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

import com.starredsolutions.assemblandroid.R;
import com.starredsolutions.assemblandroid.adapter.SpaceCursorAdapter;
import com.starredsolutions.assemblandroid.provider.AssemblaContract.Spaces;
import com.starredsolutions.utils.ActivityHelper;

/**
 * @author Juan M. Hidalgo <juan@starredsolutions.com.ar>
 *
 */
public class SpaceListActivity extends ListActivity implements OnItemClickListener{
	private static final String TAG = "SpaceListActivity"; 
	private static final boolean LOGV = Log.isLoggable(TAG, Log.VERBOSE); 
	protected final ActivityHelper mActivityHelper = ActivityHelper.createInstance(this);

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.space_list);
        mActivityHelper.setupActionBar(getString(R.string.spaces_title), R.color.darkdarkgray, false);
        
        final ListView lv = getListView();
        lv.setTextFilterEnabled(true);
        lv.setOnItemClickListener( this );
        
        
        Cursor cursor = managedQuery(Spaces.CONTENT_URI, Spaces.PROJECTION, null, null, null);
        SpaceCursorAdapter adapter = new SpaceCursorAdapter(this, cursor, false);
        setListAdapter(adapter);
	}

	public void onItemClick(AdapterView<?>parent, View view, int position, long id) {
		String space_id = null;
		String space_name = null;
		try{
			Cursor c =  (Cursor) parent.getItemAtPosition(position);
			if(c != null){
				space_id = c.getString(c.getColumnIndex(Spaces.SPACE_ID));
				space_name = c.getString(c.getColumnIndex(Spaces.NAME));
			}
		}catch(Exception e){
			Log.e(TAG, "onItemClick", e);
		}
		Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);

		Intent it = new Intent(Intent.ACTION_VIEW, uri);
		it.putExtra(Spaces.SPACE_ID, space_id);
		it.putExtra(Spaces.NAME, space_name);
		startActivity(it);
		
	}
}
