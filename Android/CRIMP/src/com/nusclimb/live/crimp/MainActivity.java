package com.nusclimb.live.crimp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.os.Build;

public class MainActivity extends ActionBarActivity {

	private List<Integer> routeList = new ArrayList<Integer>();
	private ArrayAdapter<Integer> routeAdapter;
	private String selectedRound;
	private int selectedRoute;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		addListenerToRoundSpinner();
		setRouteSpinnerAdapter();
		addListenerToRouteSpinner();
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
				
				// Currently there are only 2 route list for Qualifier and Final. A round must be either one.
				if(selectedRound.toLowerCase().contains("qualifier")){
					int[] route_primitive = res.getIntArray(R.array.route_qualifier);
					routeList.clear();
					List<Integer> routeTemp = Helper.primitiveToList(route_primitive);
					routeList.addAll(routeTemp);
				}
				else{
					int[] route_primitive = res.getIntArray(R.array.route_final);
					routeList.clear();
					List<Integer> routeTemp = Helper.primitiveToList(route_primitive);
					routeList.addAll(routeTemp);
				}
				
				routeAdapter.notifyDataSetChanged();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	/**
	 * Create and set an ArrayAdapter for route spinner so that we can modify the item list.
	 */
	public void setRouteSpinnerAdapter(){		
		Spinner spinner = (Spinner) findViewById(R.id.route_spinner);
		
		routeAdapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_item, routeList);
		routeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(routeAdapter);
	}
	
	/**
	 * Add a listener to route selection spinner. On receiving a route selection, the selection
	 * will be stored in global variable selectedRoute.
	 */
	public void addListenerToRouteSpinner(){
		Spinner route_spinner = (Spinner) findViewById(R.id.route_spinner);
		route_spinner.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				Resources res = getResources();
				selectedRoute = (Integer) parent.getItemAtPosition(pos);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	/**
	 * Method to call when Next button is clicked. Perform creation of intention and calling 
	 * the QRScan activity.
	 */
	public void next(View view){
		EditText username = (EditText) findViewById(R.id.username);
		
		Intent intent = new Intent(this, QRScanActivity.class);
		intent.putExtra(getResources().getText(R.string.package_name) + ".username", username.getText().toString());
		intent.putExtra(getResources().getText(R.string.package_name) + ".round", selectedRound);
		intent.putExtra(getResources().getText(R.string.package_name) + ".route", selectedRoute);
		startActivity(intent);
	}
}
