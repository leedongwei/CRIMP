package com.nusclimb.live.crimp.uploadlist;

import android.app.ListActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.nusclimb.live.crimp.R;

public class UploadListActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_list);
    }

    //TODO
    public void setButtonState(boolean enabled){

    }
}
