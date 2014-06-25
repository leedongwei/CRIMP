package com.nusclimb.live.crimp.activity;

import com.nusclimb.live.crimp.fragment.SettingsFragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;

/**
 * Setting activity of CRIMP.
 * 
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 *
 */
public class SettingsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
			// Respond to the action bar's Up/Home button
	    	case android.R.id.home:
	    		finish();
		        return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
}