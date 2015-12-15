package com.nusclimb.live.crimp.hello;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.common.Categories;
import com.nusclimb.live.crimp.common.User;
import com.nusclimb.live.crimp.common.json.CategoriesResponseBody;
import com.nusclimb.live.crimp.common.json.ReportResponseBody;
import com.nusclimb.live.crimp.common.spicerequest.CategoriesRequest;
import com.nusclimb.live.crimp.common.spicerequest.ReportRequest;
import com.nusclimb.live.crimp.service.CrimpService;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for category and route selection. Activity containing this Fragment must implement
 * RouteFragmentToActivityMethods interface to allow this fragment to communicate with the attached
 * Activity and possibly other Fragments. Information from the Activity is passed to this Fragment
 * through arguments.
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class RouteFragment extends CrimpFragment implements SwipeRefreshLayout.OnRefreshListener{
    private final String TAG = RouteFragment.class.getSimpleName();

    RouteFragmentToActivityMethods mToActivityMethod;   //This is how we will communicate with
                                                        //Hello Activity.

    private User mUser = null;
    private Categories categoryInfo = null;

    private HintableArrayAdapter categoryAdapter;
    private HintableArrayAdapter routeAdapter;

    private State mState = State.START;

    private SpiceManager spiceManager = new SpiceManager(CrimpService.class);

    // UI references
    private SwipeRefreshLayout mSwipeLayout;
    // UI references (category request form)
    private LinearLayout mCategoryForm;
    private TextView mCategoryStatusText;
    private Button mCategoryRetryButton;
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
    private ProgressBar mProgressBar;
    private TextView mStatusText;
    private Button mRetryButton;

    public static RouteFragment newInstance(Bundle bundle) {
        RouteFragment myFragment = new RouteFragment();

        Bundle args = new Bundle(bundle);
        myFragment.setArguments(args);

        return myFragment;
    }



    /*=========================================================================
     * Interface methods
     *=======================================================================*/
    @Override
    public CharSequence getPageTitle() {
        return "Route";
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.route_category_request_retry_button:
                categoryInfo = null;
                changeState(State.PICKING);
                break;
            case R.id.route_next_button:
                changeState(State.VERIFYING_1);
                break;
            case R.id.route_yes_button:
                changeState(State.VERIFYING_2);
                break;
            case R.id.route_no_button:
                changeState(State.PICKING);
                break;
            case R.id.route_retry_button:
                if(mState == State.CATEGORY_FAIL)
                    changeState(State.START);
                else
                    changeState(State.PICKING);
                break;
        }
    }

    @Override
    public void onRefresh(){
        Log.v(TAG, "onRefresh ");
        changeState(State.START);
    }



    /*=========================================================================
     * Fragment lifecycle methods
     *=======================================================================*/
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mToActivityMethod = (RouteFragmentToActivityMethods) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement RouteFragmentToActivityMethods");
        }

        Log.v(TAG, "COMPLETE ONATTACH");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        // Create the view for this fragment. Set references for view object that we will be
        // using in this class.

        // Inflating rootView.
        View rootView = inflater.inflate(R.layout.fragment_route, container, false);

        // Get UI references.
        //TODO not sure if this will give us the correct view
        mSwipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_layout);
        mCategoryForm = (LinearLayout) rootView.findViewById(R.id.route_category_request_viewgroup);
        mCategoryStatusText = (TextView) rootView.findViewById(R.id.route_category_request_status_text);
        mCategoryRetryButton = (Button) rootView.findViewById(R.id.route_category_request_retry_button);
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
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.route_wheel_progressbar);
        mStatusText = (TextView) rootView.findViewById(R.id.route_status_text);
        mRetryButton = (Button) rootView.findViewById(R.id.route_retry_button);

        // Set button on click listener
        mCategoryRetryButton.setOnClickListener(this);
        mNextButton.setOnClickListener(this);
        mNoButton.setOnClickListener(this);
        mYesButton.setOnClickListener(this);
        mRetryButton.setOnClickListener(this);

        mSwipeLayout.setOnRefreshListener(this);

        Log.v(TAG, "COMPLETE ONCREATEVIEW");

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // This is the final lifecycle state before fragment is visible.
        // Initialize all objects here.
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState == null){
            // Initialize mState
            mState = State.START;

            // Initialize mUser
            if(mUser == null)
                mUser = new User();

            Bundle args = getArguments();
            mUser.setUserId(args.getString(getString(R.string.bundle_x_user_id)));
            mUser.setAuthToken(args.getString(getString(R.string.bundle_x_auth_token)));
            mUser.setUserName(args.getString(getString(R.string.bundle_user_name)));
            mUser.setFacebookAccessToken(args.getString(getString(R.string.bundle_access_token)));

            // Initialize categoryInfo
            categoryInfo = new Categories();
        }
        else{
            // Initialize mState
            mState = State.toEnum(savedInstanceState.getInt(getString(R.string.bundle_route_state)));

            // Initialize mUser
            if(mUser == null)
                mUser = new User();

            mUser.setUserId(savedInstanceState.getString(getString(R.string.bundle_x_user_id)));
            mUser.setAuthToken(savedInstanceState.getString(getString(R.string.bundle_x_auth_token)));
            mUser.setUserName(savedInstanceState.getString(getString(R.string.bundle_user_name)));
            mUser.setFacebookAccessToken(savedInstanceState.getString(getString(R.string.bundle_access_token)));

            ArrayList<String> cNameList = savedInstanceState.getStringArrayList(getString(R.string.bundle_category_name_list));
            ArrayList<String> cIdList = savedInstanceState.getStringArrayList(getString(R.string.bundle_category_id_list));
            ArrayList<Integer> cCountList = savedInstanceState.getIntegerArrayList(getString(R.string.bundle_category_route_count_list));
            ArrayList<String> rNameList = savedInstanceState.getStringArrayList(getString(R.string.bundle_route_name_list));
            ArrayList<String> rIdList = savedInstanceState.getStringArrayList(getString(R.string.bundle_route_id_list));
            ArrayList<String> rScoreList = savedInstanceState.getStringArrayList(getString(R.string.bundle_route_score_list));
            byte[] cFinalizeArray = savedInstanceState.getByteArray(getString(R.string.bundle_category_finalize_list));
            ArrayList<String> cStartList = savedInstanceState.getStringArrayList(getString(R.string.bundle_category_start_list));
            ArrayList<String> cEndList = savedInstanceState.getStringArrayList(getString(R.string.bundle_category_end_list));

            // Initialize categoryInfo
            categoryInfo = new Categories(cNameList, cIdList, cCountList, rNameList, rIdList,
                    rScoreList, cFinalizeArray, cStartList, cEndList);
        }

        Log.v(TAG, "COMPLETE ONACTIVITYCREATED. mState="+mState+" mUser="+mUser.toString()
                +" mCategories="+categoryInfo.toString());
    }

    @Override
    public void onStart(){
        super.onStart();
        spiceManager.start(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        switch(mState){
            case START:
            case CATEGORY_FAIL:
            case PICKING:
            case VERIFYING_1:
            case VERIFY_1_NOT_OK:
            case VERIFY_1_FAIL:
            case VERIFYING_2:
            case VERIFYING_2_FAIL:
                changeState(mState);
                break;
            case ALL_OK:
                changeState(State.PICKING);
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if(mUser != null){
            outState.putString(getString(R.string.bundle_x_user_id), mUser.getUserId());
            outState.putString(getString(R.string.bundle_x_auth_token), mUser.getAuthToken());
            outState.putString(getString(R.string.bundle_user_name), mUser.getUserName());
            outState.putString(getString(R.string.bundle_access_token), mUser.getFacebookAccessToken());
        }

        if(categoryInfo != null){
            ArrayList<String> categoryNameList = categoryInfo.getCategoryNameList();
            ArrayList<String> categoryIdList = categoryInfo.getCategoryIdList();
            ArrayList<Integer> categoryRouteCountList = categoryInfo.getCategoryRouteCountList();
            ArrayList<String> routeNameList = categoryInfo.getRouteNameList();
            ArrayList<String> routeIdList = categoryInfo.getRouteIdList();
            ArrayList<String> routeScoreList = categoryInfo.getRouteScoreList();
            byte[] categoryFinalizeArray = categoryInfo.getCategoryFinalizeArray();
            ArrayList<String> categoryStartList = categoryInfo.getCategoryStartList();
            ArrayList<String> categoryEndList = categoryInfo.getCategoryEndList();

            outState.putStringArrayList(getString(R.string.bundle_category_name_list), categoryNameList);
            outState.putStringArrayList(getString(R.string.bundle_category_id_list),categoryIdList);
            outState.putIntegerArrayList(getString(R.string.bundle_category_route_count_list), categoryRouteCountList);
            outState.putStringArrayList(getString(R.string.bundle_route_name_list), routeNameList);
            outState.putStringArrayList(getString(R.string.bundle_route_id_list), routeIdList);
            outState.putStringArrayList(getString(R.string.bundle_route_score_list), routeScoreList);
            outState.putByteArray(getString(R.string.bundle_category_finalize_list), categoryFinalizeArray);
            outState.putStringArrayList(getString(R.string.bundle_category_start_list), categoryStartList);
            outState.putStringArrayList(getString(R.string.bundle_category_end_list), categoryEndList);
        }

        outState.putInt(getString(R.string.bundle_route_state), mState.getValue());
    }

    @Override
    public void onStop(){
        spiceManager.shouldStop();
        super.onStop();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mToActivityMethod = null;
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
            case START:
                mSwipeLayout.setRefreshing(true);
                showCategoryRequestForm(false);
                showSpinnerForm(false);
                showReplaceForm(false);
                showProgressForm(false);
                break;
            case CATEGORY_FAIL:
                mSwipeLayout.setRefreshing(false);
                showCategoryRetryButton(true);
                // Display status message implicitly.
                showCategoryRequestForm(true);
                showSpinnerForm(false);
                showReplaceForm(false);
                showProgressForm(false);
                break;
            case PICKING:
                mSwipeLayout.setRefreshing(false);
                showCategoryRequestForm(false);
                initHelloText();
                // Displaying of which item in spinner is done elsewhere.
                showSpinnerForm(true);
                showReplaceForm(false);
                showProgressForm(false);
                break;
            case VERIFYING_1:
                mSwipeLayout.setRefreshing(false);
                showCategoryRequestForm(false);
                showSpinnerForm(false);
                showReplaceForm(false);
                // Display status message implicitly.
                showProgressBar(true);
                showRetryButton(false);
                showProgressForm(true);
                break;
            case VERIFY_1_NOT_OK:
                mSwipeLayout.setRefreshing(false);
                showCategoryRequestForm(false);
                showSpinnerForm(false);
                // Display replace message implicitly. Update is done elsewhere.
                showReplaceForm(true);
                showProgressForm(false);
                break;
            case VERIFY_1_FAIL:
                mSwipeLayout.setRefreshing(false);
                showCategoryRequestForm(false);
                showSpinnerForm(false);
                showReplaceForm(false);
                // Display status message implicitly.
                showProgressBar(false);
                showRetryButton(true);
                showProgressForm(true);
                break;
            case VERIFYING_2:
                mSwipeLayout.setRefreshing(false);
                showCategoryRequestForm(false);
                showSpinnerForm(false);
                showReplaceForm(false);
                // Display status message implicitly.
                showProgressBar(true);
                showProgressForm(true);
                break;
            case VERIFYING_2_FAIL:
                mSwipeLayout.setRefreshing(false);
                showCategoryRequestForm(false);
                showSpinnerForm(false);
                showReplaceForm(false);
                // Display status message implicitly.
                showProgressBar(false);
                showRetryButton(true);
                showProgressForm(true);
                break;
            case ALL_OK:
                mSwipeLayout.setRefreshing(false);
                showCategoryRequestForm(false);
                showSpinnerForm(false);
                showReplaceForm(false);
                // Display status message implicitly.
                showProgressBar(false);
                showProgressForm(false);
                break;
            default:
                break;
        }
    }

    /**
     * Method to control what is performed at different state.
     */
    private void doWork(){
        String selectedCategoryId;
        String selectedRouteId;

        switch (mState){
            case START:
                categoryAdapter = null;
                routeAdapter = null;
                CategoriesRequest mCategoriesRequest = new CategoriesRequest(mUser.getUserId(),
                        mUser.getAuthToken(), getActivity());
                spiceManager.execute(mCategoriesRequest, new CategoriesRequestListener());
                break;
            case CATEGORY_FAIL:
                break;
            case PICKING:
                if(categoryAdapter == null || routeAdapter == null){
                    String categoryHint = getString(R.string.route_fragment_category_hint);
                    String routeHint = getString(R.string.route_fragment_route_hint);

                    List<HintableSpinnerItem> categoryList = categoryInfo.getCategoriesSpinnerListCopy(categoryHint, routeHint);
                    categoryAdapter = new HintableArrayAdapter(getActivity(),
                            android.R.layout.simple_spinner_item, categoryList);

                    List<HintableSpinnerItem> routeList = ((Categories.CategoryItem) (categoryList.get(0))).getRoutes();
                    routeAdapter = new HintableArrayAdapter(getActivity(), android.R.layout.simple_spinner_item,routeList);

                    categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    routeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    // Apply the adapter to the spinner
                    mCategorySpinner.setAdapter(categoryAdapter);
                    mRouteSpinner.setAdapter(routeAdapter);

                    mCategorySpinner.setOnItemSelectedListener(new CategoriesSpinnerListener());
                    mRouteSpinner.setOnItemSelectedListener(new RouteSpinnerListener());
                }
                break;
            case VERIFYING_1:
                selectedCategoryId = ((HintableSpinnerItem) mCategorySpinner.getSelectedItem()).getId();
                selectedRouteId = ((HintableSpinnerItem) mRouteSpinner.getSelectedItem()).getId();

                ReportRequest mReportRequest1 = new ReportRequest(mUser.getUserId(),
                        mUser.getAuthToken(), selectedCategoryId, selectedRouteId,
                        false, getActivity());
                spiceManager.execute(mReportRequest1, new ReportRequestListener());
                break;
            case VERIFY_1_NOT_OK:
                break;
            case VERIFY_1_FAIL:
                break;
            case VERIFYING_2:
                selectedCategoryId = ((HintableSpinnerItem) mCategorySpinner.getSelectedItem()).getId();
                selectedRouteId = ((HintableSpinnerItem) mRouteSpinner.getSelectedItem()).getId();

                ReportRequest mReportRequest2 = new ReportRequest(mUser.getUserId(),
                        mUser.getAuthToken(), selectedCategoryId, selectedRouteId,
                        true, getActivity());
                spiceManager.execute(mReportRequest2, new ReportRequestListener());
                break;
            case VERIFYING_2_FAIL:
                break;
            case ALL_OK:
                // TODO
                // Call a method to spawn the next tab into existence. use contract method.
                ((RouteFragmentToActivityMethods) getActivity())
                        .createAndSwitchToScanFragment(mUser, categoryInfo);
                break;
            default:
                break;
        }
    }


    /*=========================================================================
     * UI methods
     *=======================================================================*/
    private void showCategoryRequestForm(boolean show){
        mCategoryForm.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void updateCategoryStatusText(int resId) {
        mCategoryStatusText.setText(resId);
    }

    private void showCategoryRetryButton(boolean show) {
        mCategoryRetryButton.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showSpinnerForm(boolean show) {
        mSpinnerForm.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void initHelloText(){
        String userName = null;
        if(mUser != null){
            userName = mUser.getUserName();
        }
        mHelloText.setText(getActivity().getString(R.string.route_fragment_greeting) +
                userName +
                getActivity().getString(R.string.route_fragment_question));
    }

    private void enableRouteSpinner(boolean enable){
        mRouteSpinner.setEnabled(enable);
    }

    private void enableNextButtonIfPossible(boolean enable){
        boolean isHint = ((Categories.RouteItem) mRouteSpinner.getSelectedItem()).isHint();
        if(!enable)
            mNextButton.setEnabled(false);
        else
            mNextButton.setEnabled(!isHint);
    }

    private void showReplaceForm(boolean show){
        mReplaceForm.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void updateReplaceText(String currentJudge){
        String question = currentJudge+getString(R.string.route_fragment_replace_question1)+
                ((Categories.CategoryItem)mCategorySpinner.getSelectedItem()).toString()+
                getString(R.string.route_fragment_replace_question2)+
                ((Categories.RouteItem)mRouteSpinner.getSelectedItem()).toString()+
                getString(R.string.route_fragment_replace_question3)+
                currentJudge+
                getString(R.string.route_fragment_replace_question4);

        mReplaceText.setText(question);
    }

    private void showProgressForm(boolean show){
        mProgressForm.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showProgressBar(boolean show){
        mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void updateStatusText(int resId){
        mStatusText.setText(resId);
    }

    private void showRetryButton(boolean show){
        mRetryButton.setVisibility(show ? View.VISIBLE : View.GONE);
    }




    /*=========================================================================
     * Listener classes
     *=======================================================================*/
    /**
     * Listener for CategoriesRequest.
     */
    private class CategoriesRequestListener implements
            RequestListener<CategoriesResponseBody> {
        private final String TAG = CategoriesRequestListener.class.getSimpleName();

        @Override
        public void onRequestFailure(SpiceException e) {
            mSwipeLayout.setRefreshing(false);
            if(mState == State.START)
                updateCategoryStatusText(R.string.route_fragment_status_category_fail);
                categoryInfo = null;
                changeState(State.CATEGORY_FAIL);
        }

        @Override
        public void onRequestSuccess(CategoriesResponseBody result) {
            mSwipeLayout.setRefreshing(false);
            if(mState == State.START) {
                categoryInfo = new Categories(result);
                changeState(State.PICKING);
            }
        }
    }

    /**
     * Listener for ReportRequest.
     */
    private class ReportRequestListener implements RequestListener<ReportResponseBody> {
        private final String TAG = ReportRequestListener.class.getSimpleName();

        @Override
        public void onRequestFailure(SpiceException e) {
            if(mState == State.VERIFYING_1){
                updateStatusText(R.string.route_fragment_status_report_in_fail);
                changeState(State.VERIFY_1_FAIL);
            }
            if(mState == State.VERIFYING_2){
                updateStatusText(R.string.route_fragment_status_report_in_fail);
                changeState(State.VERIFYING_2_FAIL);
            }
        }

        @Override
        public void onRequestSuccess(ReportResponseBody result) {
            String currentJudge = result.getAdminName();

            if(result.getState() == 1){
                if(mState == State.VERIFYING_1 || mState == State.VERIFYING_2){
                    mUser.setCategoryId(result.getCategoryId());
                    mUser.setRouteId(result.getRouteId());
                    changeState(State.ALL_OK);
                }
            }
            else{
                if(mState == State.VERIFYING_1){
                    updateReplaceText(currentJudge);
                    changeState(State.VERIFY_1_NOT_OK);
                }
                else if(mState == State.VERIFYING_2){
                    // This should not even happen
                    updateStatusText(R.string.route_fragment_status_report_in_fail);
                    changeState(State.VERIFYING_2_FAIL);
                }
            }
        }
    }

    private class CategoriesSpinnerListener implements AdapterView.OnItemSelectedListener{

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Categories.CategoryItem selectedCategory =
                    (Categories.CategoryItem)parent.getAdapter().getItem(position);

            if(selectedCategory.isHint()){
                enableRouteSpinner(false);
                mRouteSpinner.setSelection(routeAdapter.getFirstHintPosition());
            }
            else{
                enableRouteSpinner(true);

                // Clear mRouteSpinner list, repopulate with updated route list and set selection
                // to first hint item.
                HintableArrayAdapter routeAdapter = (HintableArrayAdapter)mRouteSpinner.getAdapter();
                routeAdapter.clear();
                routeAdapter.addAll(selectedCategory.getRoutes());
                routeAdapter.notifyDataSetChanged();
                mRouteSpinner.setSelection(routeAdapter.getFirstHintPosition());
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    private class RouteSpinnerListener implements AdapterView.OnItemSelectedListener{

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            // onItemSelected will only be called by user using spinner if there is a change in
            // the selected item (i.e. selecting a different item). Will not be fired by user if
            // there is no change in selection (e.g. item1 selected -> click spinner ->
            // select back item1).
            Categories.RouteItem selectedRoute =
                    (Categories.RouteItem)parent.getAdapter().getItem(position);

            // Find out if selected route is a hint. Enable next button if selectedRoute is not
            // hint.
            if(selectedRoute.isHint()){
                enableNextButtonIfPossible(false);
            }
            else{
                enableNextButtonIfPossible(true);
            }

            // Route selection has changed. Destroy other tabs.
            mToActivityMethod.destroyOtherTab();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            Log.v(TAG, "ON NOTHING SELECTED");
        }
    }




    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface RouteFragmentToActivityMethods {
        public void createAndSwitchToScanFragment(User user, Categories categoriesInfo);
        public void destroyOtherTab();
    }

    private enum State{
        START(0),
        CATEGORY_FAIL(1),
        PICKING(2),
        VERIFYING_1(3),
        VERIFY_1_NOT_OK(4),
        VERIFY_1_FAIL(5),
        VERIFYING_2(6),
        VERIFYING_2_FAIL(7),
        ALL_OK(8);

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
                    return START;
                case 1:
                    return CATEGORY_FAIL;
                case 2:
                    return PICKING;
                case 3:
                    return VERIFYING_1;
                case 4:
                    return VERIFY_1_NOT_OK;
                case 5:
                    return VERIFY_1_FAIL;
                case 6:
                    return VERIFYING_2;
                case 7:
                    return VERIFYING_2_FAIL;
                case 8:
                    return ALL_OK;
                default:
                    return null;
            }
        }
    }

}