/**
 * 
 */
package com.starredsolutions.assemblandroid.ui.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.starredsolutions.assemblandroid.R;
import com.starredsolutions.assemblandroid.adapter.TaskCursorAdapter;
import com.starredsolutions.assemblandroid.provider.AssemblaContract.Spaces;
import com.starredsolutions.assemblandroid.provider.AssemblaContract.Tasks;
import com.starredsolutions.assemblandroid.provider.AssemblaContract.Tickets;
import com.starredsolutions.assemblandroid.ui.ActionBarActivity;

/**
 * @author Juan M. Hidalgo <juan@starredsolutions.com.ar>
 *
 */
public class TaskListFragment extends ListFragment implements
		LoaderCallbacks<Cursor> {
	TaskCursorAdapter mAdapter;
	boolean mDualPane;
    int mCurCheckPosition = 0;
    String mSpaceId = null;
    int mTicketNumber = 0;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Give some text to display if there is no data.
        //setEmptyText(getString(R.string.no_quotes));
        
        // We haven't a menu item to show in action bar.
        setHasOptionsMenu(true);
        
     // Create an empty adapter we will use to display the loaded data.
        mAdapter = new TaskCursorAdapter(getActivity(), null, 0);
        setListAdapter(mAdapter);

        // Start out with a progress indicator.
        setListShown(false);

        mSpaceId = getActivity().getIntent().getStringExtra(Spaces.SPACE_ID);
        mTicketNumber = getActivity().getIntent().getIntExtra(Tickets.NUMBER,0);
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
        }
        
        if (mDualPane) {
            // In dual-pane mode, the list view highlights the selected item.
            getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            // Make sure our UI is in the correct state.
        }
        
	}
	
	@Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("curChoice", mCurCheckPosition);
    }
	
	
	
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(getActivity(), Tasks.CONTENT_URI, Tasks.PROJECTION, Tasks.SPACE_ID + "= ? AND " + Tasks.TICKET_NUMBER + " = ?", new String[]{mSpaceId, String.valueOf(mTicketNumber)}, null);
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
