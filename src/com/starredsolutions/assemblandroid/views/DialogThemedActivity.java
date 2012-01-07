package com.starredsolutions.assemblandroid.views;

import com.starredsolutions.assemblandroid.R;
import com.starredsolutions.assemblandroid.TimeTrackerApplication;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class DialogThemedActivity extends BaseActivity
{
	private TextView txtMessage;
	private boolean fatal = false;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.dialog_themed_activity);
        
        txtMessage = (TextView) findViewById(R.id.txtMessage);
        
        // Get parameters
        // Read more: http://getablogger.blogspot.com/2008/01/android-pass-data-to-activity.html#ixzz1MjJ8rjnp
        Bundle extras = getIntent().getExtras(); 
        if (extras != null) {
        	setTitle( extras.getString("title") );
        	setMessage( extras.getString("message") );
        	fatal = extras.getBoolean("fatal");
        }
	}
	
	public void setMessage(String message) {
        txtMessage.setText( message );
	}
	
	public void onBtnOKClicked(View v) {
		finish();
		onQuit();
	}
	
	public void onBackPressed() {
		super.onBackPressed(); // Default behaviour: finishes the activity
		onQuit();
	}
	
	/**
	 * Let the controller know
	 */
	public void onQuit() {
		((TimeTrackerApplication) getApplication()).onErrorDialogQuit();
		
		// Shutdown app if error is fatal
		// DOeS NOT WORK : app get restarted
//		if (fatal) {
//			Toast.makeText( this, "FATAL Error ; Exiting...", Toast.LENGTH_SHORT).show();
//			
//			// This is not orthodox!! Should not do that!
//			try {
//				Thread.sleep(100);
//			} catch (InterruptedException e1) { }
//			
//			System.exit(0);
//		}
	}
}
