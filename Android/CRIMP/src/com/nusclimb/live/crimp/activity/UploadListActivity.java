package com.nusclimb.live.crimp.activity;

import java.util.List;
import java.util.Queue;

import com.nusclimb.live.crimp.CrimpApplication;
import com.nusclimb.live.crimp.QueueObject;
import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.UploadTaskAdapter;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class UploadListActivity extends ListActivity {
	private static final String TAG = UploadListActivity.class.getSimpleName();
	
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
		
	}
}
