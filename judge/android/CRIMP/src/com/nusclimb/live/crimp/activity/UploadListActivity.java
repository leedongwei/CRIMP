package com.nusclimb.live.crimp.activity;

import java.util.List;
import java.util.Queue;

import com.nusclimb.live.crimp.CrimpApplication;
import com.nusclimb.live.crimp.QueueObject;
import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.UploadStatus;
import com.nusclimb.live.crimp.adapter.UploadTaskAdapter;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

/**
 * Activity to view list of pending upload tasks.
 * 
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 *
 */
public class UploadListActivity extends ListActivity {	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_uploadlist);

		Queue<QueueObject> uploadQueue = ((CrimpApplication)getApplicationContext()).getQueue();
		UploadTaskAdapter adapter = new UploadTaskAdapter(
				this,
				(List<QueueObject>)uploadQueue );

		((CrimpApplication)getApplicationContext()).setQueueAdapter(adapter);
		
		// Bind to our new adapter.
		setListAdapter(adapter);
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		((CrimpApplication)getApplicationContext()).setUploadListActivity(this);
		
		if( !((CrimpApplication)getApplicationContext()).getIsPause() ){
			setButtonState(false);
		}
		else{
			setButtonState(true);
		}
		
		((UploadTaskAdapter)getListAdapter()).notifyDataSetChanged();
	}
	
	@Override
	protected void onPause(){
		((CrimpApplication)getApplicationContext()).setUploadListActivity(null);
		super.onPause();
	}
	
	public void setButtonState(boolean enabled){
		Button button = (Button) findViewById(R.id.uploadlist_resume_button);
		button.setEnabled(enabled);
	}
	
	public void resume(View view){
		((CrimpApplication)getApplicationContext()).resumeUpload();
	}
	
	@Override
	protected void onListItemClick (ListView l, View v, int position, long id){
		// Only react to first item.
		if(position == 0){
			QueueObject element = (QueueObject) getListAdapter().getItem(position);
			
			// Only react if error occurs.
			if( (element.getStatus() == UploadStatus.ERROR_DOWNLOAD) || 
					(element.getStatus() == UploadStatus.ERROR_UPLOAD) ){
				String packageName = getString(R.string.package_name);
				
				// Preparing to start QRScanActivity
				Intent intent = new Intent(this, UploadEditActivity.class);
				intent.putExtra(packageName + getString(R.string.intent_dl_url),
						element.getRequest().getBaseUrl());
				intent.putExtra(packageName + getString(R.string.intent_ul_url), 
						element.getSubmit().getBaseUrl());
				intent.putExtra(packageName + getString(R.string.intent_j_name), 
						element.getSubmit().getUploadContent().getJ_name());
				intent.putExtra(packageName + getString(R.string.intent_auth_code),
						element.getSubmit().getUploadContent().getAuth_code());
				intent.putExtra(packageName + getString(R.string.intent_r_id), 
						element.getSubmit().getUploadContent().getR_id());
				intent.putExtra(packageName + getString(R.string.intent_c_id), 
						element.getSubmit().getUploadContent().getC_id());
				intent.putExtra(packageName + getString(R.string.intent_score_append), 
						element.getSubmit().getUploadContent().getCurrentScore());
				
				startActivity(intent);
			}
		}
	}
}
