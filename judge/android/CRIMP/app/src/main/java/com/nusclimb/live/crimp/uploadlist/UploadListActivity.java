package com.nusclimb.live.crimp.uploadlist;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.nusclimb.live.crimp.CrimpApplication;
import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.common.QueueObject;

import java.util.List;
import java.util.Queue;

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

        setContentView(R.layout.activity_upload_list);

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
                intent.putExtra(getString(R.string.intent_x_user_id),
                        element.getRequest().getxUserId());
                intent.putExtra(getString(R.string.intent_x_auth_token),
                        element.getRequest().getxAuthToken());
                intent.putExtra(getString(R.string.intent_cid),
                        element.getRequest().getClimberId());
                intent.putExtra(getString(R.string.intent_rid),
                        element.getRequest().getRouteId());
                intent.putExtra(getString(R.string.intent_score),
                        element.getRequest().getScore());

                startActivity(intent);
            }
        }

    }
}