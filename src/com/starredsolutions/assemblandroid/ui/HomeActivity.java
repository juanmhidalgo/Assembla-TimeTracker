/**
 * 
 */
package com.starredsolutions.assemblandroid.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.starredsolutions.assemblandroid.Constants;
import com.starredsolutions.assemblandroid.R;
import com.starredsolutions.assemblandroid.provider.AssemblaContract;
import com.starredsolutions.assemblandroid.sync.authenticator.AuthenticatorActivity;
import com.starredsolutions.utils.ActivityHelper;

/**
 * @author Juan M. Hidalgo <juan@starredsolutions.com.ar>
 *
 */
public class HomeActivity extends FragmentActivity{
	static private final String TAG = "HomeActivity";
	static private final boolean LOGV = Log.isLoggable(TAG, Log.VERBOSE);
	protected final ActivityHelper mActivityHelper = ActivityHelper.createInstance(this);
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     
        
        final AccountManager am = AccountManager.get(this);
		Account[] ac =  am.getAccountsByType(Constants.ACCOUNT_TYPE);
		if(ac.length == 0){
			final Intent intent = new Intent(this, AuthenticatorActivity.class);
	        intent.putExtra(AuthenticatorActivity.PARAM_AUTHTOKEN_TYPE, Constants.AUTHTOKEN_TYPE);
	        //intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE,      response);
	        final Bundle bundle = new Bundle();
	        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
	        startActivity(intent);
		}else{
			if(!ContentResolver.isSyncActive(ac[0], AssemblaContract.CONTENT_AUTHORITY) && !ContentResolver.isSyncPending(ac[0], AssemblaContract.CONTENT_AUTHORITY)){
				if(LOGV) Log.v(TAG, "Request Sync");
				ContentResolver.requestSync(ac[0], AssemblaContract.CONTENT_AUTHORITY, new Bundle());
			}
		}
        
		
		
        setContentView(R.layout.home_activity);
        mActivityHelper.setupActionBar(getString(R.string.app_name), R.color.darkdarkgray, false);
        
	}
}
