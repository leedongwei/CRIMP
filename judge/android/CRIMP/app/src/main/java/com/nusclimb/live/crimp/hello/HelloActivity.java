package com.nusclimb.live.crimp.hello;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.common.Helper;
import com.nusclimb.live.crimp.common.json.CategoriesResponse;
import com.nusclimb.live.crimp.common.json.Category;
import com.nusclimb.live.crimp.common.json.LoginResponse;
import com.nusclimb.live.crimp.common.json.ReportResponse;
import com.nusclimb.live.crimp.common.spicerequest.ReportRequest;
import com.nusclimb.live.crimp.service.CrimpService;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HelloActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private final String TAG = HelloActivity.class.getSimpleName();

    private SpiceManager spiceManager = new SpiceManager(
            CrimpService.class);
    private String reportRequestCacheKey;

    // Information retrieved from intent.
    private String xUserId;
    private String xAuthToken;
    private List<SpinnerItem> categorySpinnerItemList;

    // UI references
    private TextView mQuestionView;
    private Spinner mCategorySpinner;
    private Spinner mRouteSpinner;
    private LinearLayout mHelloForm;
    private LinearLayout mVerificationForm;
    private ProgressBar mProgressBar;
    private TextView mVerificationStatus;
    private Button mNextButton;
    private RelativeLayout mReplaceForm;
    private TextView mReplaceText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_hello);

        // Retrieve info from intent.
        xUserId = getIntent().getStringExtra(getString(R.string.package_name) +
                getString(R.string.login_activity_intent_xUserId));
        xAuthToken = getIntent().getStringExtra(getString(R.string.package_name) +
                getString(R.string.login_activity_intent_xAuthToken));
        String[] categoryIdList = getIntent().getStringArrayExtra(getString(R.string.package_name) +
                getString(R.string.login_activity_intent_categoryIdList));
        String[] categoryNameList = getIntent().getStringArrayExtra(getString(R.string.package_name) +
                getString(R.string.login_activity_intent_categoryNameList));
        int[] categoryRouteCountList = getIntent().getIntArrayExtra(getString(R.string.package_name) +
                getString(R.string.login_activity_intent_categoryRouteCountList));

        categorySpinnerItemList = new ArrayList<SpinnerItem>();
        for(int i=0; i<categoryIdList.length; i++){
            categorySpinnerItemList.add(new CategorySpinnerItem(categoryNameList[i],
                    categoryIdList[i], categoryRouteCountList[i], false));
        }

        // Get UI references.
        mQuestionView = (TextView) findViewById(R.id.hello_question);
        mCategorySpinner = (Spinner) findViewById(R.id.hello_category_spinner);
        mRouteSpinner = (Spinner) findViewById(R.id.hello_route_spinner);
        mHelloForm = (LinearLayout) findViewById(R.id.hello_form);
        mVerificationForm = (LinearLayout) findViewById(R.id.verification_form);
        mProgressBar = (ProgressBar) findViewById(R.id.loading_wheel);
        mVerificationStatus = (TextView) findViewById(R.id.verification_status);
        mNextButton = (Button) findViewById(R.id.hello_next);
        mReplaceForm = (RelativeLayout) findViewById(R.id.replace_form);
        mReplaceText = (TextView) findViewById(R.id.replace_text);

        // Setup for UI.
        mQuestionView.setText(getString(R.string.hello_activity_greeting)+
                Profile.getCurrentProfile().getName()+
                getString(R.string.hello_activity_question));
        setupCategorySpinner();
        setupRouteSpinner();
    }

    @Override
    protected void onStart(){
        super.onStart();
        Log.d(TAG, "In onStart().");

        spiceManager.start(this);
    }

    @Override
    protected void onStop(){
        super.onStop();
        Log.d(TAG, "In onStop().");
        spiceManager.shouldStop();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_hello, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupCategorySpinner(){
        categorySpinnerItemList.add(0, new CategorySpinnerItem(
                getResources().getString(R.string.hello_activity_category_hint), true));

        // Create adapter using the list of categories
        SpinnerAdapterWithHint categoryAdapter= new SpinnerAdapterWithHint(
                this, android.R.layout.simple_spinner_item, categorySpinnerItemList);

        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner, initialize selection to hint and attach listener.
        mCategorySpinner.setAdapter(categoryAdapter);
        mCategorySpinner.setSelection(categoryAdapter.getFirstHintPosition());
        mCategorySpinner.setOnItemSelectedListener(this);
    }

    private void setupRouteSpinner(){
        // Create adapter using the list of categories
        SpinnerAdapterWithHint routeAdapter= new SpinnerAdapterWithHint(
                this, android.R.layout.simple_spinner_item);
        routeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        routeAdapter.add(new RouteSpinnerItem(getString(R.string.hello_activity_route_hint)));

        // Apply the adapter to the spinner, initialize selection to hint and attach listener.
        mRouteSpinner.setAdapter(routeAdapter);
        mRouteSpinner.setSelection(routeAdapter.getFirstHintPosition());
        mRouteSpinner.setOnItemSelectedListener(this);
        mRouteSpinner.setEnabled(false);
    }

    private void updateRouteSpinner(int routeCount){
        List<RouteSpinnerItem> mRouteList = new ArrayList<RouteSpinnerItem>();
        mRouteList.add(new RouteSpinnerItem(getResources().getString(R.string.hello_activity_route_hint)));
        for(int i=1; i<=routeCount; i++){
            mRouteList.add(new RouteSpinnerItem(i));
        }

        ((SpinnerAdapterWithHint)mRouteSpinner.getAdapter()).clear();
        ((SpinnerAdapterWithHint)mRouteSpinner.getAdapter()).addAll(mRouteList);

        mRouteSpinner.setSelection(((SpinnerAdapterWithHint) mRouteSpinner.getAdapter()).getFirstHintPosition());
        ((TextView)mRouteSpinner.getSelectedView()).setTextColor(getResources().getColor(R.color.hint_color));
        mRouteSpinner.setEnabled(true);
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        Log.d(TAG, "OnItemSelected raw: " + parent.getId() + ", " + view.getId() + ", " + pos + ", " + id);

        // Called from category spinner
        if(parent.getId() == mCategorySpinner.getId()){
            CategorySpinnerItem selectedItem = (CategorySpinnerItem)(parent.getSelectedItem());
            if(selectedItem.isHint()){
                Log.d(TAG, "OnItemSelected. Category. Hint.");
                ((TextView)view).setTextColor(getResources().getColor(R.color.hint_color));
            }
            else{
                Log.d(TAG, "OnItemSelected. Category. Not hint.");
                updateRouteSpinner(selectedItem.getRouteCount());
            }
        }

        // Called from route spinner
        if(parent.getId() == mRouteSpinner.getId()){
            SpinnerItem selectedItem = (SpinnerItem)parent.getSelectedItem();
            if(selectedItem.isHint()) {
                Log.d(TAG, "OnItemSelected. Route. Hint.");
                ((TextView) view).setTextColor(getResources().getColor(R.color.hint_color));

                mNextButton.setEnabled(false);
            }
            else{
                Log.d(TAG, "OnItemSelected. Route. Not hint.");
                mNextButton.setEnabled(true);
            }
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
        Log.d(TAG, "parent:"+parent.getId());
    }


    public void next(View view){
        //TODO perform checks on selection
        CategorySpinnerItem selectedCategory = (CategorySpinnerItem) mCategorySpinner.getSelectedItem();
        RouteSpinnerItem selectedRoute = (RouteSpinnerItem) mRouteSpinner.getSelectedItem();

        String routeId = selectedCategory.getCategoryId()+selectedRoute.getRouteNumber();

        mVerificationStatus.setText(routeId);

        // parse response.
        ReportRequest mReportRequest = new ReportRequest(xUserId,xAuthToken
                ,routeId, true, this);

        reportRequestCacheKey = mReportRequest.createCacheKey();
        spiceManager.execute(mReportRequest, reportRequestCacheKey,
                DurationInMillis.ALWAYS_EXPIRED,
                new ReportRequestListener());


        mNextButton.setVisibility(View.GONE);
        mVerificationForm.setVisibility(View.VISIBLE);
    }

    public void yes(View view){

    }

    public void no(View view){

    }

    private class ReportRequestListener implements RequestListener<ReportResponse> {
        @Override
        public void onRequestFailure(SpiceException e) {
            Log.d(TAG, "ReportRequestListener request fail.");

            reportRequestCacheKey = null;

            //TODO fail
            mVerificationStatus.setText("Fail");
        }

        @Override
        public void onRequestSuccess(ReportResponse result) {
            Log.d(TAG, "ReportRequestListener request succeed.");

            reportRequestCacheKey = null;

            // TODO check succeed

            //TODO change out testuser
            String question = "Dongwei"+getString(R.string.hello_activity_replace_question1)+
                    ((CategorySpinnerItem)mCategorySpinner.getSelectedItem()).getItemString()+
                    getString(R.string.hello_activity_replace_question2)+
                    ((RouteSpinnerItem)mRouteSpinner.getSelectedItem()).getItemString()+
                    getString(R.string.hello_activity_replace_question3)+
                    "Dongwei"+
                    getString(R.string.hello_activity_replace_question4);

            mReplaceText.setText(question);
            mHelloForm.setVisibility(View.GONE);
            mReplaceForm.setVisibility(View.VISIBLE);

            mVerificationStatus.setText("succeed");
        }
    }
}
