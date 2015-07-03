package com.nusclimb.live.crimp.hello;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.common.json.ReportResponse;
import com.nusclimb.live.crimp.common.spicerequest.ReportRequest;
import com.nusclimb.live.crimp.qr.QRScanActivity;
import com.nusclimb.live.crimp.service.CrimpService;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.ArrayList;
import java.util.List;

public class HelloActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private final String TAG = HelloActivity.class.getSimpleName();

    // Various state of activity.
    private enum HelloState {
        PICKING,                // Picking category and route.
        IN_FIRST_REQUEST,       // Sending request to be judge. Force=false.
        FIRST_REQUEST_OK,       // CRIMP server reply ok.
        FIRST_REQUEST_NOT_OK,   // CRIMP server reply not ok.
        FIRST_REQUEST_FAILED,   // No/unknown response from CRIMP server.
        REPLACE_QUESTION,       // Ask user to force replace.
        IN_SECOND_REQUEST,      // Sending request to be judge. Force=true.
        SECOND_REQUEST_OK,      // CRIMP server reply ok.
        SECOND_REQUEST_NOT_OK,  // CRIMP server reply not ok.
        SECOND_REQUEST_FAILED,  // No/unknown response from CRIMP server.
        JUDGE_OK                // User become judge.
    }

    // RoboSpice stuff
    private SpiceManager spiceManager = new SpiceManager(
            CrimpService.class);
    private String reportRequestCacheKey;
    private String currentJudge;
    private String routeId;

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
    private TextView mVerificationStatus;
    private Button mNextButton;
    private RelativeLayout mReplaceForm;
    private TextView mReplaceText;
    private Button mYesButton;
    private Button mNoButton;

    // Activity state
    private HelloState mState = HelloState.PICKING;


    /**
     * Initialize the category spinner. Create a spinner adapter, populate adapter with
     * categories and hint, attach it to spinner and set initial selection.
     */
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

    /**
     * Initialize the route spinner. Create a spinner adapter, populate adapter with
     * only the hint, attach it to spinner, set initial selection and disable spinner.
     */
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

    /**
     * Populate route spinner with hint and routes name (i.e route 1, route 2, ... ) base
     * on number of routes for the category.
     *
     * @param routeCount Number of routes for selected category in category spinner.
     */
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

    /**
     * Set {@code mState} to {@code state}. Changes to {@code mState} must
     * go through this method.
     *
     * @param state Hello state to set {@code mState} to.
     */
    private void changeState(HelloState state){
        Log.d(TAG, "Change state: " + mState + "->" + state);

        mState = state;
        updateUI();
        doWork();
    }

    /**
     * Method to control which UI element is visible at different state.
     */
    private void updateUI(){
        switch (mState){
            case PICKING:
                initHelloQuestion();
                showHelloForm(true);
                showReplaceForm(false);
                showVerificationForm(false);
                enableNext(true);
                break;
            case IN_FIRST_REQUEST:
                initHelloQuestion();
                showHelloForm(true);
                showReplaceForm(false);
                updateVerificationStatus(R.string.hello_activity_status_report_in);
                showVerificationForm(true);
                enableNext(false);
                break;
            case FIRST_REQUEST_OK:
                break;
            case FIRST_REQUEST_NOT_OK:
                break;
            case FIRST_REQUEST_FAILED:
                initHelloQuestion();
                showHelloForm(true);
                showReplaceForm(false);
                updateVerificationStatus(R.string.hello_activity_status_report_in_fail);
                showVerificationForm(true);
                enableNext(false);
                break;
            case REPLACE_QUESTION:
                showHelloForm(false);
                updateReplaceText();
                showReplaceForm(true);
                showVerificationForm(false);
                enableYesNo(true);
                break;
            case IN_SECOND_REQUEST:
                showHelloForm(false);
                updateReplaceText();
                showReplaceForm(true);
                updateVerificationStatus(R.string.hello_activity_status_report_in);
                showVerificationForm(true);
                enableYesNo(false);
                break;
            case SECOND_REQUEST_OK:
                break;
            case SECOND_REQUEST_NOT_OK:
                // TODO this shldnt happen. server reject even when we set force to true.
                break;
            case SECOND_REQUEST_FAILED:
                showHelloForm(false);
                updateReplaceText();
                showReplaceForm(true);
                updateVerificationStatus(R.string.hello_activity_status_report_in_fail);
                showVerificationForm(false);
                enableYesNo(false);
                break;
            case JUDGE_OK:
                break;
        }
    }

    /**
     * Method to control what is performed at different state.
     */
    private void doWork(){
        switch (mState){
            case PICKING:
                break;
            case IN_FIRST_REQUEST:
                if(reportRequestCacheKey == null){
                    CategorySpinnerItem selectedCategory = (CategorySpinnerItem) mCategorySpinner.getSelectedItem();
                    RouteSpinnerItem selectedRoute = (RouteSpinnerItem) mRouteSpinner.getSelectedItem();

                    routeId = selectedCategory.getCategoryId()+selectedRoute.getRouteNumber();

                    ReportRequest mReportRequest = new ReportRequest(xUserId,xAuthToken
                            ,routeId, true, this);

                    reportRequestCacheKey = mReportRequest.createCacheKey();
                    spiceManager.execute(mReportRequest, reportRequestCacheKey,
                            DurationInMillis.ALWAYS_EXPIRED,
                            new ReportRequestListener());
                }
                break;
            case FIRST_REQUEST_OK:
                changeState(HelloState.JUDGE_OK);
                break;
            case FIRST_REQUEST_NOT_OK:
                changeState(HelloState.REPLACE_QUESTION);
                break;
            case FIRST_REQUEST_FAILED:
                changeState(HelloState.IN_FIRST_REQUEST);
                break;
            case REPLACE_QUESTION:
                break;
            case IN_SECOND_REQUEST:
                if(reportRequestCacheKey == null) {
                    CategorySpinnerItem selectedCategory = (CategorySpinnerItem) mCategorySpinner.getSelectedItem();
                    RouteSpinnerItem selectedRoute = (RouteSpinnerItem) mRouteSpinner.getSelectedItem();

                    routeId = selectedCategory.getCategoryId() + selectedRoute.getRouteNumber();

                    ReportRequest mReportRequest = new ReportRequest(xUserId, xAuthToken
                            , routeId, true, this);

                    reportRequestCacheKey = mReportRequest.createCacheKey();
                    spiceManager.execute(mReportRequest, reportRequestCacheKey,
                            DurationInMillis.ALWAYS_EXPIRED,
                            new ReportRequestListener());
                }
                break;
            case SECOND_REQUEST_OK:
                changeState(HelloState.JUDGE_OK);
                break;
            case SECOND_REQUEST_NOT_OK:
                // TODO This shldnt happen. Server reject even when we force=true
                changeState(HelloState.JUDGE_OK);
                break;
            case SECOND_REQUEST_FAILED:
                changeState(HelloState.IN_SECOND_REQUEST);
                break;
            case JUDGE_OK:
                // LAUNCH activity
                launchQRScanAcitivty();
                break;
        }
    }

    /**
     * Method to launch QRScanActivity.
     */
    private void launchQRScanAcitivty(){
        // TODO launchQRScanAcitivty
        Log.d(TAG, "Launching QRScanActivity. mState: " + mState);

        // Putting stuff into intent.

        Intent intent = new Intent(getApplicationContext(), QRScanActivity.class);
        intent.putExtra(getString(R.string.package_name) + getString(R.string.login_activity_intent_xUserId), xUserId);
        intent.putExtra(getString(R.string.package_name) + getString(R.string.login_activity_intent_xAuthToken), xAuthToken);
        intent.putExtra(getString(R.string.package_name) + getString(R.string.intent_route_id), routeId);

        startActivity(intent);

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



    /*=========================================================================
     * Inner class
     *=======================================================================*/
    /**
     * RequestListener for receiving response of report request.
     *
     * @author Lin Weizhi (ecc.weizhi@gmail.com)
     */
    private class ReportRequestListener implements RequestListener<ReportResponse> {
        private final String TAG = ReportRequestListener.class.getSimpleName();

        @Override
        public void onRequestFailure(SpiceException e) {
            Log.d(TAG, "ReportRequest fail. mState="+mState);

            reportRequestCacheKey = null;

            if(mState == HelloState.IN_FIRST_REQUEST){
                changeState(HelloState.FIRST_REQUEST_FAILED);
            }
            else if(mState == HelloState.IN_SECOND_REQUEST){
                changeState(HelloState.SECOND_REQUEST_FAILED);
            }
        }

        @Override
        public void onRequestSuccess(ReportResponse result) {
            Log.d(TAG, "ReportRequest succeed. mState="+mState);

            reportRequestCacheKey = null;

            if(result.getState() == 1){
                if(mState == HelloState.IN_FIRST_REQUEST){
                    changeState(HelloState.FIRST_REQUEST_OK);
                }
                else if(mState == HelloState.IN_SECOND_REQUEST){
                    changeState(HelloState.SECOND_REQUEST_OK);
                }
            }
            else{
                if(mState == HelloState.IN_FIRST_REQUEST){
                    changeState(HelloState.FIRST_REQUEST_NOT_OK);
                }
                else if(mState == HelloState.IN_SECOND_REQUEST){
                    changeState(HelloState.SECOND_REQUEST_NOT_OK);
                }
            }
        }
    }



    /*=========================================================================
     * UI methods
     *=======================================================================*/
    private void showHelloForm(boolean show){
        mHelloForm.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void initHelloQuestion(){
        mQuestionView.setText(getString(R.string.hello_activity_greeting) +
                Profile.getCurrentProfile().getName() +
                getString(R.string.hello_activity_question));
    }

    private void enableNext(boolean enable){
        if(enable){
            if(mRouteSpinner.isEnabled() &&
                    !((RouteSpinnerItem)mRouteSpinner.getSelectedItem()).isHint())
            {
                mNextButton.setEnabled(enable);
            }
        }
        else {
            mNextButton.setEnabled(enable);
        }
    }

    private void showReplaceForm(boolean show){
        mReplaceForm.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void updateReplaceText(){
        String question = currentJudge+getString(R.string.hello_activity_replace_question1)+
                ((CategorySpinnerItem)mCategorySpinner.getSelectedItem()).getItemString()+
                getString(R.string.hello_activity_replace_question2)+
                ((RouteSpinnerItem)mRouteSpinner.getSelectedItem()).getItemString()+
                getString(R.string.hello_activity_replace_question3)+
                currentJudge+
                getString(R.string.hello_activity_replace_question4);

        mReplaceText.setText(question);
    }

    private void enableYesNo(boolean enable){
        mYesButton.setEnabled(enable);
        mNoButton.setEnabled(enable);
    }

    private void showVerificationForm(boolean show){
        mVerificationForm.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void updateVerificationStatus(int resId){
        mVerificationStatus.setText(resId);
    }



    /*=========================================================================
     * Activity Lifecycle methods
     *=======================================================================*/
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
        mVerificationStatus = (TextView) findViewById(R.id.verification_status);
        mNextButton = (Button) findViewById(R.id.hello_next);
        mReplaceForm = (RelativeLayout) findViewById(R.id.replace_form);
        mReplaceText = (TextView) findViewById(R.id.replace_text);
        mYesButton = (Button) findViewById(R.id.yes_button);
        mNoButton = (Button) findViewById(R.id.no_button);

        // Setup for UI.
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
    protected void onResume(){
        super.onResume();
        Log.d(TAG, "In onResume(). mState:" + mState);

        changeState(mState);
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



    /*=========================================================================
     * Button onClick methods
     *=======================================================================*/
    public void next(View view){
        if(mState == HelloState.PICKING)
            changeState(HelloState.IN_FIRST_REQUEST);
    }

    public void yes(View view){
        if(mState == HelloState.REPLACE_QUESTION)
            changeState(HelloState.IN_SECOND_REQUEST);
    }

    public void no(View view){
        if(mState == HelloState.REPLACE_QUESTION)
            changeState(HelloState.PICKING);
    }
}
