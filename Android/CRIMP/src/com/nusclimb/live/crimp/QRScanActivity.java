package com.nusclimb.live.crimp;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;

public class QRScanActivity extends ActionBarActivity {
	private String routeJudge, round;
	private int route;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_qrscan);
		
		Intent intent = getIntent();
		routeJudge = intent.getStringExtra(getResources().getText(R.string.package_name) + ".username");
		round = intent.getStringExtra(getResources().getText(R.string.package_name) + ".round");
		route = intent.getIntExtra(getResources().getText(R.string.package_name) + ".route", -1);
		
		demo();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.qrscan, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void demo(){
		RelativeLayout QRScanLayout = (RelativeLayout)findViewById(R.id.QRScan);
		TextView demoText = new TextView(this);
		demoText.setText(routeJudge + "\n" + round + "\n" + route);
		QRScanLayout.addView(demoText);
	}

}
