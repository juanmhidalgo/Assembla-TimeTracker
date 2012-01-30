/**
 * 
 */
package com.starredsolutions.assemblandroid.ui;

import android.os.Bundle;
import android.widget.TabHost;

import com.starredsolutions.assemblandroid.R;
import com.starredsolutions.assemblandroid.ui.fragments.TaskListFragment;
import com.starredsolutions.assemblandroid.ui.fragments.TicketDetailFragment;
import com.starredsolutions.utils.TabManager;

/**
 * @author Juan M. Hidalgo <juan@starredsolutions.com.ar>
 */
public class TicketDetailActivity extends ActionBarActivity{
	TabHost mTabHost;
    TabManager mTabManager;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.fragment_tabs);
        setTitle(R.string.tickets_detail_title);

        mTabHost = (TabHost)findViewById(android.R.id.tabhost);
        mTabHost.setup();
        
        mTabManager = new TabManager(this, mTabHost, R.id.realtabcontent);
        
        mTabManager.addTab(mTabHost.newTabSpec("details").setIndicator("Details"),
        		TicketDetailFragment.class, null);
        
        mTabManager.addTab(mTabHost.newTabSpec("tasks").setIndicator("Tasks"),
                TaskListFragment.class, null);
        
        if (savedInstanceState != null) {
            mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
        }
    
    }
	
	@Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("tab", mTabHost.getCurrentTabTag());
    }
}
