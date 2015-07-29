package com.nusclimb.live.crimp.uploadlist;

import com.nusclimb.live.crimp.CrimpApplication;
import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.common.QueueObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.widget.EditText;

/**
 * Activity to edit upload tasks.
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 *
 */
public class UploadEditActivity extends Activity{
    private String xUserId, xAuthToken, cid, rid, score;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_upload_edit);

        // Read intent
        String packageName = getString(R.string.package_name);
        Intent intent = getIntent();
        xUserId = intent.getStringExtra(getString(R.string.intent_x_user_id));
        xAuthToken = intent.getStringExtra(getString(R.string.intent_x_auth_token));
        cid = intent.getStringExtra(getString(R.string.intent_cid));
        rid = intent.getStringExtra(getString(R.string.intent_rid));
        score = intent.getStringExtra(getString(R.string.intent_score));

        // Update UI
        ((EditText) findViewById(R.id.edit_xuserid_edit)).setText(xUserId);
        ((EditText) findViewById(R.id.edit_xauthtoken_edit)).setText(xAuthToken);
        ((EditText) findViewById(R.id.edit_cid_edit)).setText(cid);
        ((EditText) findViewById(R.id.edit_rid_edit)).setText(rid);
        ((EditText) findViewById(R.id.edit_score_edit)).setText(score);
    }

    public void save(View view){
        new AlertDialog.Builder(this)
                .setTitle("Save changes")
                .setMessage("Do you want to save changes?")
                .setPositiveButton(R.string.uploadedit_activity_save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Continue with save
                        QueueObject element = ((CrimpApplication)getApplicationContext()).getQueue().peek();

                        xUserId = ((EditText) findViewById(R.id.edit_xuserid_edit)).getText().toString();
                        xAuthToken = ((EditText) findViewById(R.id.edit_xauthtoken_edit)).getText().toString();
                        cid = ((EditText) findViewById(R.id.edit_cid_edit)).getText().toString();
                        rid = ((EditText) findViewById(R.id.edit_rid_edit)).getText().toString();
                        score = ((EditText) findViewById(R.id.edit_score_edit)).getText().toString();

                        element.getRequest().setxUserId(xUserId);
                        element.getRequest().setxAuthToken(xAuthToken);
                        element.getRequest().setClimberId(cid);
                        element.getRequest().setRouteId(rid);
                        element.getRequest().setScore(score);

                        // Navigate up from this activity.
                        NavUtils.navigateUpFromSameTask(UploadEditActivity.this);
                    }
                })
                .setNegativeButton(R.string.uploadedit_activity_no_save, new DialogInterface.OnClickListener() {
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
                .setPositiveButton(R.string.uploadedit_activity_cancel_task, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with cancel
                        ((CrimpApplication)getApplicationContext()).getQueue().poll();
                        ((CrimpApplication)getApplicationContext()).modifyUploadTotalCount(-1);

                        // Navigate up from this activity.
                        NavUtils.navigateUpFromSameTask(UploadEditActivity.this);
                    }
                })
                .setNegativeButton(R.string.uploadedit_activity_no_cancel_task, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
