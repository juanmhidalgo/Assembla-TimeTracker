/**
 * 
 */
package com.starredsolutions.assemblandroid.ui.fragments;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ListView;

import com.starredsolutions.assemblandroid.R;
import com.starredsolutions.assemblandroid.adapter.SpaceCursorAdapter;
import com.starredsolutions.assemblandroid.provider.AssemblaContract.Spaces;

/**
 * @author Juan M. Hidalgo <juan@starredsolutions.com.ar>
 *
 */
public class SpaceListFragment extends ListFragment implements
		LoaderCallbacks<Cursor> {
	SpaceCursorAdapter mAdapter;
	boolean mDualPane;
    int mCurCheckPosition = 0;
    String mSpaceId = null;

	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Give some text to display if there is no data.
        //setEmptyText(getString(R.string.no_quotes));
        
        // We haven't a menu item to show in action bar.
        setHasOptionsMenu(false);
        
     // Create an empty adapter we will use to display the loaded data.
        mAdapter = new SpaceCursorAdapter(getActivity(), null, 0);
        setListAdapter(mAdapter);

        // Start out with a progress indicator.
        setListShown(false);

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(0, null, this);
        
        
        // Check to see if we have a frame in which to embed the details
        // fragment directly in the containing UI.
        View detailsFrame = getActivity().findViewById(R.id.details);
        mDualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;
        
        if (savedInstanceState != null) {
            // Restore last state for checked position.
            mCurCheckPosition = savedInstanceState.getInt("curChoice", 0);
            mSpaceId = savedInstanceState.getString("spaceId");
        }
        
        if (mDualPane) {
            // In dual-pane mode, the list view highlights the selected item.
            getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            // Make sure our UI is in the correct state.
            showDetails(mCurCheckPosition);
        }
        
	}
	
	@Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("curChoice", mCurCheckPosition);
        outState.putString("spaceId", mSpaceId);
    }
	
	@Override
    public void onListItemClick(ListView l, View v, int position, long id) {
		try{
			Cursor c =  (Cursor) l.getItemAtPosition(position);
			if(c != null){
				mSpaceId = c.getString(c.getColumnIndex(Spaces.SPACE_ID));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
        showDetails(position);
    }
	
	/**
     * Helper function to show the details of a selected item, either by
     * displaying a fragment in-place in the current UI, or starting a
     * whole new activity in which it is displayed.
     */
    void showDetails(int index) {
    	mCurCheckPosition = index;
    	
    	
    	if(mDualPane){
    		// We can display everything in-place with fragments, so update
            // the list to highlight the selected item and show the data.
            getListView().setItemChecked(index, true);
    	}else{
    		Uri uri = ContentUris.withAppendedId(getActivity().getIntent().getData(), index);
    		Intent it = new Intent(Intent.ACTION_VIEW, uri);
    		it.putExtra(Spaces.SPACE_ID, mSpaceId);
    		startActivity(it);
    	}
    }
	
	
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(getActivity(), Spaces.CONTENT_URI, Spaces.PROJECTION, null, null, null);
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		// Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mAdapter.swapCursor(data);
        
		 // The list should now be shown.
        if (isResumed()) {
            setListShown(true);
        } else {
            setListShownNoAnimation(true);
        }
		
	}

	public void onLoaderReset(Loader<Cursor> loader) {
		// This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);
	}

}
