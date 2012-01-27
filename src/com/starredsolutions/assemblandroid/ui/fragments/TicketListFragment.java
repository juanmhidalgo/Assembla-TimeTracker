/**
 * 
 */
package com.starredsolutions.assemblandroid.ui.fragments;

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
import com.starredsolutions.assemblandroid.adapter.TicketCursorAdapter;
import com.starredsolutions.assemblandroid.provider.AssemblaContract.Spaces;
import com.starredsolutions.assemblandroid.provider.AssemblaContract.Tickets;

/**
 * @author Juan M. Hidalgo <juan@starredsolutions.com.ar>
 *
 */
public class TicketListFragment extends ListFragment implements
		LoaderCallbacks<Cursor> {
	TicketCursorAdapter mAdapter;
	boolean mDualPane;
    int mCurCheckPosition = 0;
    String mSpaceId = null;
    int mTicketId = 0;
	int mTicketNumber = 0;
	String mTicketDescription = null;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Give some text to display if there is no data.
        //setEmptyText(getString(R.string.no_quotes));
        
        // We haven't a menu item to show in action bar.
        setHasOptionsMenu(false);
        
     // Create an empty adapter we will use to display the loaded data.
        mAdapter = new TicketCursorAdapter(getActivity(), null, 0);
        setListAdapter(mAdapter);

        // Start out with a progress indicator.
        setListShown(false);

        mSpaceId = getActivity().getIntent().getStringExtra(Spaces.SPACE_ID);
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
            mTicketNumber = savedInstanceState.getInt(Tickets.NUMBER, 0);
            mTicketId = savedInstanceState.getInt(Tickets.TICKET_ID, 0);
            mTicketDescription= savedInstanceState.getString(Tickets.DESCRIPTION);
            
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
        outState.putInt(Tickets.TICKET_ID, mTicketId);
        outState.putInt(Tickets.NUMBER, mTicketNumber);
        outState.putString(Tickets.DESCRIPTION, mTicketDescription);
    }
	
	@Override
    public void onListItemClick(ListView l, View v, int position, long id) {
		
		try{
			Cursor c =  (Cursor) l.getItemAtPosition(position);
			if(c != null){
				mTicketNumber = c.getInt(c.getColumnIndex(Tickets.NUMBER));
				mTicketId = c.getInt(c.getColumnIndex(Tickets.TICKET_ID));
				mTicketDescription = c.getString(c.getColumnIndex(Tickets.DESCRIPTION));
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
    	
    	//TODO Implements Dual Pane
    	if(mDualPane){
    		// We can display everything in-place with fragments, so update
            // the list to highlight the selected item and show the data.
            getListView().setItemChecked(index, true);
    	}else{
    		Uri uri = Tickets.buildTicketUri(String.valueOf(index));
    		Intent it = new Intent(Intent.ACTION_VIEW, uri);
    		it.putExtra(Tickets.SPACE_ID, mSpaceId);
    		it.putExtra(Tickets.NUMBER, mTicketNumber);
    		it.putExtra(Tickets.TICKET_ID, mTicketId);
    		it.putExtra(Tickets.DESCRIPTION, mTicketDescription);

    		startActivity(it);
    	}
    }
	
	
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(getActivity(), Tickets.CONTENT_URI, Tickets.PROJECTION, Tickets.SPACE_ID + "= ?", new String[]{mSpaceId}, null);
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