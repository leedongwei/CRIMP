package com.nusclimb.live.crimp.hello;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.facebook.Profile;
import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.common.BusProvider;
import com.nusclimb.live.crimp.common.busevent.RouteFinish;
import com.nusclimb.live.crimp.common.busevent.RouteNotFinish;
import com.nusclimb.live.crimp.common.json.ReportResponse;
import com.nusclimb.live.crimp.common.spicerequest.ReportRequest;
import com.nusclimb.live.crimp.service.CrimpService;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class RouteFragment extends Fragment implements AdapterView.OnItemSelectedListener{
    private final String TAG = RouteFragment.class.getSimpleName();

    private enum State{
        PICKING(0),                // Picking category and route. Can stay in this state.
        IN_FIRST_REQUEST(1),       // Sending request to be judge. Force=false.
        FIRST_REQUEST_OK(2),       // CRIMP server reply ok.
        FIRST_REQUEST_NOT_OK(3),   // CRIMP server reply not ok.
        FIRST_REQUEST_FAILED(4),   // No/unknown response from CRIMP server.
        REPLACE_QUESTION(5),       // Ask user to force replace. Can stay in this state.
        IN_SECOND_REQUEST(6),      // Sending request to be judge. Force=true.
        SECOND_REQUEST_OK(7),      // CRIMP server reply ok.
        SECOND_REQUEST_NOT_OK(8),  // CRIMP server reply not ok.
        SECOND_REQUEST_FAILED(9),  // No/unknown response from CRIMP server.
        JUDGE_OK(10);              // User become judge. Can stay in this state.

        private final int value;

        State(int value){
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static State toEnum(int i){
            switch(i){
                case 0:
                    return PICKING;
                case 1:
                    return IN_FIRST_REQUEST;
                case 2:
                    return FIRST_REQUEST_OK;
                case 3:
                    return FIRST_REQUEST_NOT_OK;
                case 4:
                    return FIRST_REQUEST_FAILED;
                case 5:
                    return REPLACE_QUESTION;
                case 6:
                    return IN_SECOND_REQUEST;
                case 7:
                    return SECOND_REQUEST_OK;
                case 8:
                    return SECOND_REQUEST_NOT_OK;
                case 9:
                    return SECOND_REQUEST_FAILED;
                case 10:
                    return JUDGE_OK;
                default:
                    return null;
            }
        }
    }

    // Information retrieved from intent.
    private String xUserId;
    private String xAuthToken;
    private List<SpinnerItem> categorySpinnerItemList;

    // UI references (spinner form)
    private LinearLayout mSpinnerForm;
    private TextView mHelloText;
    private Spinner mCategorySpinner;
    private Spinner mRouteSpinner;
    private Button mNextButton;

    // UI references (replace form)
    private RelativeLayout mReplaceForm;
    private TextView mReplaceText;
    private Button mYesButton;
    private Button mNoButton;

    // UI references (progress form)
    private LinearLayout mProgressForm;
    private TextView mStatusText;

    // Activity state
    private State mState;

    // RoboSpice info
    private SpiceManager spiceManager = new SpiceManager(CrimpService.class);
    private String currentJudge;

    private String routeId;


    /*=========================================================================
     * Spinner Setup/update methods
     *=======================================================================*/
    /**
     * Initialize the category spinner. Create a spinner adapter, populate adapter with
     * categories and hint, attach it to spinner and set initial selection.
     */
    private void setupCategorySpinner(){
        categorySpinnerItemList.add(0, new CategorySpinnerItem(
                getResources().getString(R.string.route_fragment_category_hint), true));

        // Create adapter using the list of categories
        SpinnerAdapterWithHint categoryAdapter= new SpinnerAdapterWithHint(
                getActivity(), android.R.layout.simple_spinner_item, categorySpinnerItemList);

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
                getActivity(), android.R.layout.simple_spinner_item);
        routeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        routeAdapter.add(new RouteSpinnerItem(getString(R.string.route_fragment_route_hint)));

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
        mRouteList.add(new RouteSpinnerItem(getResources().getString(R.string.route_fragment_route_hint)));
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
    private void changeState(State state) {
        Log.d(TAG + ".changeState()", mState + " -> " + state);

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
                showSpinnerForm(true);
                showReplaceForm(false);
                showProgressForm(false);
                enableNextButtonIfPossible(true);
                break;
            case IN_FIRST_REQUEST:
                showSpinnerForm(true);
                showReplaceForm(false);
                updateStatusText(R.string.route_fragment_status_report_in);
                showProgressForm(true);
                enableNextButtonIfPossible(false);
                break;
            case FIRST_REQUEST_OK:
                break;
            case FIRST_REQUEST_NOT_OK:
                break;
            case FIRST_REQUEST_FAILED:
                showSpinnerForm(true);
                showReplaceForm(false);
                updateStatusText(R.string.route_fragment_status_report_in_fail);
                showProgressForm(true);
                enableNextButtonIfPossible(false);
                break;
            case REPLACE_QUESTION:
                showSpinnerForm(false);
                updateReplaceText();
                showReplaceForm(true);
                showProgressForm(false);
                enableYesNo(true);
                break;
            case IN_SECOND_REQUEST:
                showSpinnerForm(false);
                updateReplaceText();
                showReplaceForm(true);
                updateStatusText(R.string.route_fragment_status_report_in);
                showProgressForm(true);
                enableYesNo(false);
                break;
            case SECOND_REQUEST_OK:
                break;
            case SECOND_REQUEST_NOT_OK:
                Log.e(TAG+".onUpdateUI()", "mState = "+mState);
                break;
            case SECOND_REQUEST_FAILED:
                showSpinnerForm(false);
                updateReplaceText();
                showReplaceForm(true);
                updateStatusText(R.string.route_fragment_status_report_in_fail);
                showProgressForm(true);
                enableYesNo(false);
                break;
            case JUDGE_OK:
                showSpinnerForm(true);
                showReplaceForm(false);
                showProgressForm(false);
                enableNextButtonIfPossible(false);
                break;
        }
    }

    /**
     * Method to control what is performed at different state.
     */
    private void doWork(){
        CategorySpinnerItem selectedCategory;
        RouteSpinnerItem selectedRoute;

        switch (mState){
            case PICKING:
                // Tell the world we are in picking stage.
                Log.d(TAG+".doWork()", "Post RouteNotFinish to bus.");
                BusProvider.getInstance().post(new RouteNotFinish());
                break;
            case IN_FIRST_REQUEST:
                selectedCategory = (CategorySpinnerItem) mCategorySpinner.getSelectedItem();
                selectedRoute = (RouteSpinnerItem) mRouteSpinner.getSelectedItem();

                String rId = selectedCategory.getCategoryId() + selectedRoute.getRouteNumber();

                ReportRequest mReportRequest1 = new ReportRequest(xUserId,xAuthToken
                        , rId, false, getActivity());

                spiceManager.execute(mReportRequest1, mReportRequest1.createCacheKey(),
                        DurationInMillis.ALWAYS_EXPIRED,
                        new ReportRequestListener());
                break;
            case FIRST_REQUEST_OK:
                changeState(State.JUDGE_OK);
                break;
            case FIRST_REQUEST_NOT_OK:
                changeState(State.REPLACE_QUESTION);
                break;
            case FIRST_REQUEST_FAILED:
                changeState(State.IN_FIRST_REQUEST);
                break;
            case REPLACE_QUESTION:
                break;
            case IN_SECOND_REQUEST:
                selectedCategory = (CategorySpinnerItem) mCategorySpinner.getSelectedItem();
                selectedRoute = (RouteSpinnerItem) mRouteSpinner.getSelectedItem();

                rId = selectedCategory.getCategoryId() + selectedRoute.getRouteNumber();

                ReportRequest mReportRequest2 = new ReportRequest(xUserId, xAuthToken
                        , rId, true, getActivity());

                spiceManager.execute(mReportRequest2, mReportRequest2.createCacheKey(),
                        DurationInMillis.ALWAYS_EXPIRED,
                        new ReportRequestListener());

                break;
            case SECOND_REQUEST_OK:
                changeState(State.JUDGE_OK);
                break;
            case SECOND_REQUEST_NOT_OK:
                Log.e(TAG + ".doWork()", "mState = " + mState);
                break;
            case SECOND_REQUEST_FAILED:
                changeState(State.IN_SECOND_REQUEST);
                break;
            case JUDGE_OK:
                Log.d(TAG+".doWork()", "Post RouteFinish("+routeId+") to bus.");
                BusProvider.getInstance().post(new RouteFinish(routeId));
                break;
        }
    }


    /*=========================================================================
     * Fragment lifecycle methods
     *=======================================================================*/
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        if(savedInstanceState == null){
            Log.v(TAG+".onCreate()", "No savedState. Recreating fragment.");

            // Get initial info from arguments.
            Bundle args = getArguments();
            xUserId = args.getString(getString(R.string.package_name) +
                    getString(R.string.bundle_x_user_id));
            xAuthToken = args.getString(getString(R.string.package_name) +
                    getString(R.string.bundle_x_auth_token));
            String[] categoryIdList = args.getStringArray(getString(R.string.package_name) +
                    getString(R.string.bundle_category_id_list));
            String[] categoryNameList = args.getStringArray(getString(R.string.package_name) +
                    getString(R.string.bundle_category_name_list));
            int[] categoryRouteCountList = args.getIntArray(getString(R.string.package_name) +
                    getString(R.string.bundle_category_route_count_list));

            categorySpinnerItemList = new ArrayList<SpinnerItem>();
            for(int i=0; i<categoryIdList.length; i++){
                categorySpinnerItemList.add(new CategorySpinnerItem(categoryNameList[i],
                        categoryIdList[i], categoryRouteCountList[i], false));
            }

            mState = State.PICKING;
        }
        else{
            Log.v(TAG+".onCreate()", "SavedState found.");

            // Get initial info from savedInstanceState
            xUserId = savedInstanceState.getString(getString(R.string.package_name) +
                    getString(R.string.bundle_x_user_id));
            xAuthToken = savedInstanceState.getString(getString(R.string.package_name) +
                    getString(R.string.bundle_x_auth_token));
            String[] categoryIdList = savedInstanceState.getStringArray(getString(R.string.package_name) +
                    getString(R.string.bundle_category_id_list));
            String[] categoryNameList = savedInstanceState.getStringArray(getString(R.string.package_name) +
                    getString(R.string.bundle_category_name_list));
            int[] categoryRouteCountList = savedInstanceState.getIntArray(getString(R.string.package_name) +
                    getString(R.string.bundle_category_route_count_list));

            categorySpinnerItemList = new ArrayList<SpinnerItem>();
            for(int i=0; i<categoryIdList.length; i++){
                categorySpinnerItemList.add(new CategorySpinnerItem(categoryNameList[i],
                        categoryIdList[i], categoryRouteCountList[i], false));
            }

            mState = State.toEnum(savedInstanceState.getInt(getString(R.string.bundle_route_state)));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        // Inflating rootView.
        View rootView = inflater.inflate(R.layout.fragment_route, container, false);

        // Get UI references.
        mSpinnerForm = (LinearLayout) rootView.findViewById(R.id.route_spinner_viewgroup);
        mHelloText = (TextView) rootView.findViewById(R.id.route_hello_text);
        mCategorySpinner = (Spinner) rootView.findViewById(R.id.route_category_spinner);
        mRouteSpinner = (Spinner) rootView.findViewById(R.id.route_route_spinner);
        mNextButton = (Button) rootView.findViewById(R.id.route_next_button);
        mReplaceForm = (RelativeLayout) rootView.findViewById(R.id.route_replace_viewgroup);
        mReplaceText = (TextView) rootView.findViewById(R.id.route_replace_text);
        mYesButton = (Button) rootView.findViewById(R.id.route_yes_button);
        mNoButton = (Button) rootView.findViewById(R.id.route_no_button);
        mProgressForm = (LinearLayout) rootView.findViewById(R.id.route_progress_viewgroup);
        mStatusText = (TextView) rootView.findViewById(R.id.route_status_text);

        // Setup for spinners.
        setupCategorySpinner();
        setupRouteSpinner();

        if(savedInstanceState == null){
            Log.v(TAG+".onCreateView()", "No savedState.");
        }
        else{
            Log.v(TAG+".onCreateView()", "SavedState found.");
            mCategorySpinner.setSelection(savedInstanceState.getInt(
                    getString(R.string.bundle_category_spinner_selection)));
            mRouteSpinner.setSelection(savedInstanceState.getInt(
                    getString(R.string.bundle_route_spinner_selection)));
        }

        return rootView;
    }

    @Override
    public void onStart(){
        super.onStart();

        // Guaranteed to have an activity here.
        Log.v(TAG+".onStart()", "Starting spiceManager");
        spiceManager.start(getActivity());

        initHelloText();
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.v(TAG+".onResume()", "mState: "+mState);
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.v(TAG + ".onPause()", "mState: " + mState);

        switch(mState){
            case PICKING:
            case REPLACE_QUESTION:
            case JUDGE_OK:
                break;

            default:
                changeState(State.PICKING);
        }
    }

    @Override
    public void onStop() {
        Log.v(TAG + ".onStop()", "Stopping spiceManager.");
        spiceManager.shouldStop();

        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle args = getArguments();
        outState.putAll(args);

        outState.putInt(getString(R.string.bundle_category_spinner_selection), mCategorySpinner.getSelectedItemPosition());
        outState.putInt(getString(R.string.bundle_route_spinner_selection), mRouteSpinner.getSelectedItemPosition());
        outState.putInt(getString(R.string.bundle_route_state), mState.getValue());
    }



    /*=========================================================================
     * UI methods
     *=======================================================================*/
    private void showSpinnerForm(boolean show){
        mSpinnerForm.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void initHelloText(){
        if(mHelloText == null){
            Log.d(TAG+".initHelloText()", "mHelloText is null.");
            return;
        }

        if(getActivity() == null){
            Log.d(TAG+".initHelloText()", "getActivity() is null.");
            return;
        }

        mHelloText.setText(getActivity().getString(R.string.route_fragment_greeting) +
                Profile.getCurrentProfile().getName() +
                getActivity().getString(R.string.route_fragment_question));
    }

    private void enableNextButtonIfPossible(boolean enable){
        boolean isHint = ((RouteSpinnerItem)mRouteSpinner.getSelectedItem()).isHint();
        if(!enable)
            mNextButton.setEnabled(false);
        else
            mNextButton.setEnabled(!isHint);
    }

    private void showReplaceForm(boolean show){
        mReplaceForm.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void updateReplaceText(){
        String question = currentJudge+getString(R.string.route_fragment_replace_question1)+
                ((CategorySpinnerItem)mCategorySpinner.getSelectedItem()).getItemString()+
                getString(R.string.route_fragment_replace_question2)+
                ((RouteSpinnerItem)mRouteSpinner.getSelectedItem()).getItemString()+
                getString(R.string.route_fragment_replace_question3)+
                currentJudge+
                getString(R.string.route_fragment_replace_question4);

        mReplaceText.setText(question);
    }

    private void enableYesNo(boolean enable){
        mYesButton.setEnabled(enable);
        mNoButton.setEnabled(enable);
    }

    private void showProgressForm(boolean show){
        mProgressForm.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void updateStatusText(int resId){
        mStatusText.setText(resId);
    }

    /*=========================================================================
     * AdapterView.OnItemSelectedListener methods
     *=======================================================================*/
    @Override
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // TODO there is a null pointer exception in this log.
        //Log.d(TAG+".onItemSelected()", "parentId: " + parent.getId() + ", viewId:" + view.getId() +
        //        ", pos:" + pos + ", id" + id);

        // If already in JUDGE_OK and user change the category/route, we need to enter picking state.
        if(mState == State.JUDGE_OK){
            changeState(State.PICKING);
        }

        // Called from category spinner
        if(parent.getId() == mCategorySpinner.getId()){
            CategorySpinnerItem selectedItem = (CategorySpinnerItem)(parent.getSelectedItem());
            if(selectedItem.isHint()){
                Log.v(TAG+".onItemSelected()", "OnItemSelected. Category. Hint.");
                ((TextView)view).setTextColor(getResources().getColor(R.color.hint_color));
            }
            else{
                Log.v(TAG+".onItemSelected()", "OnItemSelected. Category. Not hint.");
                updateRouteSpinner(selectedItem.getRouteCount());
            }
        }

        // Called from route spinner
        if(parent.getId() == mRouteSpinner.getId()){
            SpinnerItem selectedItem = (SpinnerItem)parent.getSelectedItem();
            if(selectedItem.isHint()) {
                Log.v(TAG+".onItemSelected()", "OnItemSelected. Route. Hint.");
                ((TextView) view).setTextColor(getResources().getColor(R.color.hint_color));

                mNextButton.setEnabled(false);
            }
            else{
                Log.v(TAG+".onItemSelected()", "OnItemSelected. Route. Not hint.");
                mNextButton.setEnabled(true);
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
        Log.v(TAG+".onNothingSelected()", "parent:"+parent.getId());
    }



    /**
     * RequestListener for receiving response of report request.
     *
     * @author Lin Weizhi (ecc.weizhi@gmail.com)
     */
    private class ReportRequestListener implements RequestListener<ReportResponse> {
        private final String TAG = ReportRequestListener.class.getSimpleName();

        @Override
        public void onRequestFailure(SpiceException e) {
            Log.i(TAG+".onRequestFailure()", "mState="+mState);

            if(mState == State.IN_FIRST_REQUEST){
                changeState(State.FIRST_REQUEST_FAILED);
            }
            if(mState == State.IN_SECOND_REQUEST){
                changeState(State.SECOND_REQUEST_FAILED);
            }
        }

        @Override
        public void onRequestSuccess(ReportResponse result) {
            Log.i(TAG+".onRequestSuccess()", "mState="+mState);

            currentJudge = result.getAdminName();

            if(result.getState() == 1){
                routeId = result.getRouteId();
                if(mState == State.IN_FIRST_REQUEST){
                    changeState(State.FIRST_REQUEST_OK);
                }
                else if(mState == State.IN_SECOND_REQUEST){
                    changeState(State.SECOND_REQUEST_OK);
                }
            }
            else{
                if(mState == State.IN_FIRST_REQUEST){
                    changeState(State.FIRST_REQUEST_NOT_OK);
                }
                else if(mState == State.IN_SECOND_REQUEST){
                    changeState(State.SECOND_REQUEST_NOT_OK);
                }
            }
        }
    }



    /*=========================================================================
     * Button onClick methods
     *=======================================================================*/
    public void next(){
        if(mState == State.PICKING)
            changeState(State.IN_FIRST_REQUEST);
    }

    public void yes(){
        if(mState == State.REPLACE_QUESTION)
            changeState(State.IN_SECOND_REQUEST);
    }

    public void no(){
        if(mState == State.REPLACE_QUESTION)
            changeState(State.PICKING);
    }
}
