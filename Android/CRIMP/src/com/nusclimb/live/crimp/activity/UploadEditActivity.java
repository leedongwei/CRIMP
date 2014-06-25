package com.nusclimb.live.crimp.activity;

import com.nusclimb.live.crimp.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
		
	}
	
	public void cancel(View view){
		
	}
	
}
