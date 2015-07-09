package com.nusclimb.live.crimp.test;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.service.CrimpService;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Created by Zhi on 7/8/2015.
 */
public class TestActivity extends Activity implements RequestListener<String> {
    private final String TAG = TestActivity.class.getSimpleName();

    private String cacheKey;
    private SpiceManager spiceManager = new SpiceManager(CrimpService.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        Log.d(TAG+".onCreate()","onCreate.");
    }

    protected void onStart(){
        super.onStart();
        Log.d(TAG + ".onStart()", "starting spice manager.");

        spiceManager.start(this);
    }

    protected void onResume(){
        super.onResume();
        Log.d(TAG + ".onResume()", "onResume.");
    }

    protected void onPause(){
        super.onPause();
        Log.d(TAG + ".onPause()", "onPause.");
    }

    protected void onStop(){
        super.onStop();
        Log.d(TAG + ".onStop()", "stopping spice manager");

        spiceManager.shouldStop();
    }

    protected void onDestroy(){
        super.onDestroy();
        Log.d(TAG + ".onDestroy()", "onDestroy.");
    }

    protected void onRestart(){
        super.onRestart();
        Log.d(TAG + ".onRestart()", "onRestart.");
    }

    public void test(View view){
        Log.d(TAG+".test()", "button pressed.");
        TestRequest request = new TestRequest();
        cacheKey = request.createCacheKey();

        Log.d(TAG+".test()", "executing");
        spiceManager.execute(request, cacheKey, DurationInMillis.ALWAYS_EXPIRED, this);
    }

    @Override
    public void onRequestFailure(SpiceException e) {
        Log.d(TAG+"onRequestFailure()", "onRequestFailure");
    }

    @Override
    public void onRequestSuccess(String result) {
        Log.d(TAG+"onRequestSuccess()", result);
    }
}
