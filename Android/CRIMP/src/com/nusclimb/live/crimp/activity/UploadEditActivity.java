package com.nusclimb.live.crimp.activity;

import com.nusclimb.live.crimp.CrimpApplication;
import com.nusclimb.live.crimp.QueueObject;
import com.nusclimb.live.crimp.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.widget.EditText;

public class UploadEditActivity extends Activity{
	private String downloadUrl, uploadUrl, j_name, auth_code, r_id, c_id, appendScore;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_uploadedit);
		
		// Read intent
		String packageName = getString(R.string.package_name);
		Intent intent = getIntent();
		downloadUrl = intent.getStringExtra(packageName + getString(R.string.intent_dl_url));
		uploadUrl = intent.getStringExtra(packageName + getString(R.string.intent_ul_url));
		j_name = intent.getStringExtra(packageName + getString(R.string.intent_j_name));
		auth_code = intent.getStringExtra(packageName + getString(R.string.intent_auth_code));
		r_id = intent.getStringExtra(packageName + getString(R.string.intent_r_id));
		c_id = intent.getStringExtra(packageName + getString(R.string.intent_c_id));
		appendScore = intent.getStringExtra(packageName + getString(R.string.intent_score_append));
		
		// Update UI
		((EditText) findViewById(R.id.edit_dl_url)).setText(downloadUrl);
		((EditText) findViewById(R.id.edit_ul_url)).setText(uploadUrl);
		((EditText) findViewById(R.id.edit_j_name)).setText(j_name);
		((EditText) findViewById(R.id.edit_auth_code)).setText(auth_code);
		((EditText) findViewById(R.id.edit_r_id)).setText(r_id);
		((EditText) findViewById(R.id.edit_c_id)).setText(c_id);
		((EditText) findViewById(R.id.edit_current_score)).setText(appendScore);
	}
	
	public void save(View view){
		new AlertDialog.Builder(this)
	    .setTitle("Save changes")
	    .setMessage("Do you want to save changes?")
	    .setPositiveButton(R.string.dialog_save, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	            // Continue with save
	        	QueueObject element = ((CrimpApplication)getApplicationContext()).getQueue().peek();
	        	
	        	downloadUrl = ((EditText) findViewById(R.id.edit_dl_url)).getText().toString();
	        	uploadUrl = ((EditText) findViewById(R.id.edit_ul_url)).getText().toString();
	        	j_name = ((EditText) findViewById(R.id.edit_j_name)).getText().toString();
	        	auth_code = ((EditText) findViewById(R.id.edit_auth_code)).getText().toString();
	        	r_id = ((EditText) findViewById(R.id.edit_r_id)).getText().toString();
	        	c_id = ((EditText) findViewById(R.id.edit_c_id)).getText().toString();
	        	appendScore = ((EditText) findViewById(R.id.edit_current_score)).getText().toString();
	        	
	        	element.getRequest().setBaseUrl(downloadUrl);
	        	element.getRequest().setClimberId(c_id);
	        	element.getRequest().setR_id(r_id);
	        	
	        	element.getSubmit().setBaseUrl(uploadUrl);
	        	element.getSubmit().getUploadContent().setAll_current(j_name, auth_code, r_id, c_id, appendScore);
	        	
	    		// Navigate up from this activity.
	    		NavUtils.navigateUpFromSameTask(UploadEditActivity.this);
	        }
	     })
	    .setNegativeButton(R.string.dialog_no_save, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	            // Quit without saving
	        	// Navigate up from this activity.
	    		NavUtils.navigateUpFromSameTask(UploadEditActivity.this);
	        }
	     })
	    .setIcon(android.R.drawable.ic_dialog_alert)
	     .show();
	}
	
	public void cancel(View view){
		new AlertDialog.Builder(this)
	    .setTitle("Cancel task")
	    .setMessage("Are you sure you want to cancel this score upload?")
	    .setPositiveButton(R.string.dialog_cancel_task, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	            // continue with cancel
	        	((CrimpApplication)getApplicationContext()).getQueue().poll();
	    		((CrimpApplication)getApplicationContext()).modifyUploadTotalCount(-1);
	    		
	    		// Navigate up from this activity.
	    		NavUtils.navigateUpFromSameTask(UploadEditActivity.this);
	        }
	     })
	    .setNegativeButton(R.string.dialog_no_cancel_task, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	            // do nothing
	        }
	     })
	    .setIcon(android.R.drawable.ic_dialog_alert)
	     .show();
	}
}
