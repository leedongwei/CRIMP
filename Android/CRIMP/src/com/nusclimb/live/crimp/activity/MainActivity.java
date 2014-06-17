package com.nusclimb.live.crimp.activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.nusclimb.live.crimp.R;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

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
		
		Log.d(TAG, "MainActivity onCreate.");
	}
	
	@Override
	protected void onStart(){
		super.onStart();
		Log.d(TAG, "MainActivity onStart");
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
	
	
	
	/**
	 * Add a listener to round selection spinner. The item selected will determine the list
	 * for route selection spinner. On receiving item selected events, we modify route selection
	 * spinner, notify routeAdapter and store the selection in global variable selectedRound.
	 */
	public void addListenerToRoundSpinner(){
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
	public void setRouteSpinnerAdapter(){
		Spinner spinner = (Spinner) findViewById(R.id.route_spinner);
		
		routeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, routeList);
		routeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(routeAdapter);
	}
	
	/**
	 * Method to call when Next button is clicked. Perform creation of intention and calling 
	 * the QRScan activity.
	 */
	public void next(View view){
		Log.d(TAG, "Next button clicked.");
		
		EditText username = (EditText) findViewById(R.id.username);
		Spinner routeSpinner = (Spinner) findViewById(R.id.route_spinner);
		String selectedRoute = (String) routeSpinner.getSelectedItem();
		
		String packageName = getString(R.string.package_name);
		
		/*
		// Preparing to start QRScanActivity
		Intent intent = new Intent(this, QRScanActivity.class);
		intent.putExtra(packageName + getString(R.string.intent_username) , username.getText().toString());
		intent.putExtra(packageName + getString(R.string.intent_round), selectedRound);
		intent.putExtra(packageName + getString(R.string.intent_route), selectedRoute);
		
		startActivity(intent);
		*/
	}
}
