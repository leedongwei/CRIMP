package com.nusclimb.live.crimp.activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.json.RoundInfoMap;
import com.nusclimb.live.crimp.json.Score;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * First activity of CRIMP
 * 
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 *
 */
public class MainActivity extends ActionBarActivity {
	private final String TAG = MainActivity.class.getSimpleName();
	
	private List<String> routeList = new ArrayList<String>();
	private ArrayAdapter<String> routeAdapter;
	private String selectedRound;
	
	/*=========================================================================
	 * Activity lifecycle methods.
	 *========================================================================*/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		addListenerToRoundSpinner();
		setRouteSpinnerAdapter();
	
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		
		Log.d(TAG, "MainActivity onCreate.");
	}
	
	@Override
	protected void onStart(){
		super.onStart();
		Log.d(TAG, "MainActivity onStart.");
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		Log.d(TAG, "MainActivity onResume.");
	}
	
	@Override
	protected void onPause(){
		Log.d(TAG, "MainActivity onPause.");
		super.onPause();
	}
	
	@Override
	protected void onStop(){
		Log.d(TAG, "MainActivity onStop.");
		super.onStop();
	}
	
	@Override
	protected void onDestroy(){
		Log.d(TAG, "MainActivity onDestroy.");
		super.onDestroy();
	}
	
	
	
	/*=========================================================================
	 * Override methods
	 *=======================================================================*/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	    	case R.id.preferences:
	    		// Launch settings activity
	    	    Intent i = new Intent(this, SettingsActivity.class);
	    	    startActivity(i);
		        return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	
	
	/*=========================================================================
	 * Public methods
	 *=======================================================================*/
	/**
	 * Method to call when Next button is clicked. Perform creation of intention and calling 
	 * the QRScan activity.
	 */
	public void next(View view){
		Log.d(TAG, "Next button clicked.");
		
		EditText username = (EditText) findViewById(R.id.username);
		
		if(username.getText().length() == 0){
			// Do not allow user to get past this activity if
			// judge name is not entered.
			Context context = getApplicationContext();
			CharSequence text = "Route judge name is required!";
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		}
		else{
			Spinner routeSpinner = (Spinner) findViewById(R.id.route_spinner);
			String selectedRoute = (String) routeSpinner.getSelectedItem();
			
			String packageName = getString(R.string.package_name);
			
			// Preparing to start QRScanActivity
			Intent intent = new Intent(this, QRScanActivity.class);
			intent.putExtra(packageName + getString(R.string.intent_username) , username.getText().toString());
			intent.putExtra(packageName + getString(R.string.intent_round), selectedRound);
			intent.putExtra(packageName + getString(R.string.intent_route), selectedRoute);
			
			startActivity(intent);
		}
	}
	
	
	
	/*=========================================================================
	 * Private methods
	 *=======================================================================*/
	/**
	 * Add a listener to round selection spinner. The item selected will determine the list
	 * for route selection spinner. On receiving item selected events, we modify route selection
	 * spinner, notify routeAdapter and store the selection in global variable selectedRound.
	 */
	private void addListenerToRoundSpinner(){
		Spinner round_spinner = (Spinner) findViewById(R.id.round_spinner);
		round_spinner.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				Resources res = getResources();
				selectedRound = parent.getItemAtPosition(pos).toString();
				
				// Currently there are only 3 route list for Qualifier and Final. 
				// TODO I hardcoded this segment. Might want to improve this.
				switch(pos){
				case 0:
				case 2:
				case 4:
				case 6:
				{
					// 6 routes
					String[] route_array = res.getStringArray(R.array.route_qualifier_novice);
					routeList.clear();
					routeList.addAll(Arrays.asList(route_array));
					break;
				}
					
				case 8:
				case 10:
				case 12:
				case 14:
				{
					// 5 routes
					String[] route_array = res.getStringArray(R.array.route_qualifier);
					routeList.clear();
					routeList.addAll(Arrays.asList(route_array));
					break;
				}
				
				case 1:
				case 3:
				case 5:
				case 7:
				case 9:
				case 11:
				{
					// 3 routes
					String[] route_array = res.getStringArray(R.array.route_final);
					routeList.clear();
					routeList.addAll(Arrays.asList(route_array));
					break;
				}
				
				case 13:
				case 15:
				{
					// TODO 3+1 routes. Implementation to be determined
					break;
				}
				
				default:
					Log.e(TAG, "Unknown round selected in spinner.");
				}
				
				routeAdapter.notifyDataSetChanged();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// We are not doing anything here.
			}
		});
	}
	
	/**
	 * Create and set an ArrayAdapter for route spinner so that we can modify the item list.
	 */
	private void setRouteSpinnerAdapter(){
		Spinner spinner = (Spinner) findViewById(R.id.route_spinner);
		
		routeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, routeList);
		routeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(routeAdapter);
	}
	
}
