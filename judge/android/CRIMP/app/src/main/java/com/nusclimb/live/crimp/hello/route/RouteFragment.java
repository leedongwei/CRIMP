package com.nusclimb.live.crimp.hello.route;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
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
import com.nusclimb.live.crimp.hello.HelloActivityFragment;
import com.nusclimb.live.crimp.service.CrimpService;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.List;

/**
 * Fragment for category and route selection. Activity containing this Fragment must implement
 * RouteFragmentToActivityMethods interface to allow this fragment to communicate with the attached
 * Activity and possibly other Fragments. Information from the Activity is passed to this Fragment
 * through arguments.
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class RouteFragment extends HelloActivityFragment implements SwipeRefreshLayout.OnRefreshListener{
    private final String TAG = RouteFragment.class.getSimpleName();
    private final boolean DEBUG = false;

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

    private RouteFragmentToActivityMethods mToActivityMethod;   //This is how we will communicate with Hello Activity.
    private HintableArrayAdapter categoryAdapter;
    private HintableArrayAdapter routeAdapter;
    private State mState = State.START;
    private SpiceManager spiceManager = new SpiceManager(CrimpService.class);
    private int categorySpinnerIndex;
    private int routeSpinnerIndex;

    // UI references
    private SwipeRefreshLayout mSwipeLayout;
    private ProgressBar mProgressBar;
    private TextView mStatusText;
    private Button mRetryButton;
    private TextView mHelloText;
    private Spinner mCategorySpinner;
    private Spinner mRouteSpinner;
    private Button mNextButton;
    private TextView mReplaceText;
    private LinearLayout mYesNoLayout;
    private Button mYesButton;
    private Button mNoButton;

    public static RouteFragment newInstance() {
        return new RouteFragment();
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

        if (DEBUG) Log.d(TAG, "RouteFragment onAttach");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        // Create the view for this fragment. Set references for view object that we will be
        // using in this class.

        // Inflating rootView.
        View rootView = inflater.inflate(R.layout.fragment_route, container, false);

        // Get UI references.
        mSwipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_layout);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.route_wheel_progressbar);
        mStatusText = (TextView) rootView.findViewById(R.id.route_request_status_text);
        mRetryButton = (Button) rootView.findViewById(R.id.route_retry_button);
        mHelloText = (TextView) rootView.findViewById(R.id.route_hello_text);
        mCategorySpinner = (Spinner) rootView.findViewById(R.id.route_category_spinner);
        mRouteSpinner = (Spinner) rootView.findViewById(R.id.route_route_spinner);
        mNextButton = (Button) rootView.findViewById(R.id.route_next_button);
        mReplaceText = (TextView) rootView.findViewById(R.id.route_replace_text);
        mYesNoLayout = (LinearLayout) rootView.findViewById(R.id.route_yes_no_viewgroup);
        mYesButton = (Button) rootView.findViewById(R.id.route_yes_button);
        mNoButton = (Button) rootView.findViewById(R.id.route_no_button);

        // Set button on click listener
        mNextButton.setOnClickListener(this);
        mNoButton.setOnClickListener(this);
        mYesButton.setOnClickListener(this);
        mRetryButton.setOnClickListener(this);

        mSwipeLayout.setOnRefreshListener(this);

        if (DEBUG) Log.d(TAG, "RouteFragment onCreateView");

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // This is the final lifecycle state before fragment is visible.
        // Initialize all objects here.
        super.onActivityCreated(savedInstanceState);

        // Note to self: A fragment's onSaveInstanceState() will only be called when its hosting
        // Activity call its onSaveInstanceState() method. Therefore we decide to not use the
        // parameter savedInstanceState as it this fragment's onSaveInstanceState method might not
        // be called. Instead, we use our own bundle to store and restore information and send it
        // to HelloActivity to keep.
        Bundle mySaveInstanceState = mToActivityMethod.restoreRouteInstance();

        mState = State.toEnum(mySaveInstanceState.getInt(getString(R.string.bundle_route_state), State.START.getValue()));
        categorySpinnerIndex = mySaveInstanceState.getInt(getString(R.string.bundle_category_spinner_selected_index), 0);
        routeSpinnerIndex = mySaveInstanceState.getInt(getString(R.string.bundle_route_spinner_selected_index), 0);

        if (DEBUG) Log.d(TAG, "onActivityCreated. mState:"+mState+" categoryIndex:"+categorySpinnerIndex
                +" routeIndex:"+routeSpinnerIndex );
    }

    @Override
    public void onStart(){
        super.onStart();
        spiceManager.start(getActivity());
        if (DEBUG) Log.d(TAG, "RouteFragment onStart");
    }

    @Override
    public void onResume() {
        super.onResume();

        Bundle mBundle = mToActivityMethod.restoreRouteInstance();
        State state = State.toEnum(mBundle.getInt(getString(R.string.bundle_route_state), State.START.getValue()));
        categorySpinnerIndex = mBundle.getInt(getString(R.string.bundle_category_spinner_selected_index), 0);
        routeSpinnerIndex = mBundle.getInt(getString(R.string.bundle_route_spinner_selected_index), 0);

        if (DEBUG) Log.d(TAG, "RouteFragment onResume. mState:" + mState + " categoryIndex:"
                +categorySpinnerIndex+" routeIndex:"+routeSpinnerIndex);

        changeState(state);
    }

    @Override
    public void onPause() {
        if (DEBUG) Log.d(TAG, "RouteFragment onPause");

        State state;
        switch (mState){
            case START:
            case CATEGORY_FAIL:
                state = State.START;
                break;
            default:
                state = State.PICKING;
                break;
        }

        Bundle outState = new Bundle();
        outState.putInt(getString(R.string.bundle_route_state), state.getValue());
        categorySpinnerIndex = mCategorySpinner.getSelectedItemPosition();
        routeSpinnerIndex = mRouteSpinner.getSelectedItemPosition();
        outState.putInt(getString(R.string.bundle_category_spinner_selected_index), categorySpinnerIndex);
        outState.putInt(getString(R.string.bundle_route_spinner_selected_index), routeSpinnerIndex);

        mToActivityMethod.saveRouteInstance(outState);

        super.onPause();
    }

    @Override
    public void onStop(){
        if (DEBUG) Log.d(TAG, "RouteFragment onStop");
        spiceManager.shouldStop();
        super.onStop();
    }

    @Override
    public void onDetach() {
        if (DEBUG) Log.d(TAG, "RouteFragment onDetach");
        mToActivityMethod = null;
        super.onDetach();
    }



    /*=========================================================================
     * Main flow methods.
     *=======================================================================*/
    /**
     * Set {@code mState} to {@code state}. Changes to {@code mState} must
     * go through this method.
     *
     * @param state Hello state to set {@code mState} to.
     */
    private void changeState(State state) {
        if (DEBUG) Log.d(TAG, "Change state: " + mState + " -> " + state);

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
                mSwipeLayout.setEnabled(false);
                if(!mSwipeLayout.isRefreshing())
                    mSwipeLayout.setRefreshing(true);
                mProgressBar.setVisibility(View.GONE);
                mStatusText.setVisibility(View.GONE);
                mRetryButton.setVisibility(View.GONE);
                showSpinnerForm(false);
                showReplaceForm(false);
                break;
            case CATEGORY_FAIL:
                mSwipeLayout.setEnabled(false);
                if(mSwipeLayout.isRefreshing())
                    mSwipeLayout.setRefreshing(false);
                mProgressBar.setVisibility(View.INVISIBLE);
                mStatusText.setText(R.string.route_fragment_status_category_fail);
                mStatusText.setVisibility(View.VISIBLE);
                mRetryButton.setVisibility(View.VISIBLE);
                showSpinnerForm(false);
                showReplaceForm(false);
                break;
            case PICKING:
                mSwipeLayout.setEnabled(true);
                if(mSwipeLayout.isRefreshing())
                    mSwipeLayout.setRefreshing(false);
                mProgressBar.setVisibility(View.GONE);
                mStatusText.setVisibility(View.GONE);
                mRetryButton.setVisibility(View.GONE);
                initHelloText();
                setupSpinner();
                showSpinnerForm(true);
                showReplaceForm(false);
                User userFromActivity = mToActivityMethod.getUser();
                if(userFromActivity.getCategoryId()!=null &&
                        userFromActivity.getRouteId()!=null){
                    // We already have categoryId and routeId. This implies that we have already
                    // complete report request to server.
                    enableNextButtonIfPossible(false);
                }
                else{
                    enableNextButtonIfPossible(true);
                }
                break;
            case VERIFYING_1:
                mSwipeLayout.setEnabled(false);
                if(mSwipeLayout.isRefreshing())
                    mSwipeLayout.setRefreshing(false);
                mProgressBar.setVisibility(View.VISIBLE);
                mStatusText.setText(R.string.route_fragment_status_report_in);
                mStatusText.setVisibility(View.VISIBLE);
                mRetryButton.setVisibility(View.GONE);
                showSpinnerForm(false);
                showReplaceForm(false);
                break;
            case VERIFY_1_NOT_OK:
                mSwipeLayout.setEnabled(false);
                if(mSwipeLayout.isRefreshing())
                    mSwipeLayout.setRefreshing(false);
                mProgressBar.setVisibility(View.GONE);
                mStatusText.setVisibility(View.GONE);
                mRetryButton.setVisibility(View.GONE);
                showSpinnerForm(false);
                showReplaceForm(true);
                break;
            case VERIFY_1_FAIL:
                mSwipeLayout.setEnabled(false);
                if(mSwipeLayout.isRefreshing())
                    mSwipeLayout.setRefreshing(false);
                mProgressBar.setVisibility(View.INVISIBLE);
                mStatusText.setText(R.string.route_fragment_status_report_in_fail);
                mStatusText.setVisibility(View.VISIBLE);
                mRetryButton.setVisibility(View.VISIBLE);
                showSpinnerForm(false);
                showReplaceForm(false);
                break;
            case VERIFYING_2:
                mSwipeLayout.setEnabled(false);
                if(mSwipeLayout.isRefreshing())
                    mSwipeLayout.setRefreshing(false);

                mProgressBar.setVisibility(View.VISIBLE);
                mStatusText.setText(R.string.route_fragment_status_report_in);
                mStatusText.setVisibility(View.VISIBLE);
                mRetryButton.setVisibility(View.GONE);
                showSpinnerForm(false);
                showReplaceForm(false);
                break;
            case VERIFYING_2_FAIL:
                mSwipeLayout.setEnabled(false);
                if(mSwipeLayout.isRefreshing())
                    mSwipeLayout.setRefreshing(false);
                mProgressBar.setVisibility(View.INVISIBLE);
                mStatusText.setText(R.string.route_fragment_status_report_in_fail);
                mStatusText.setVisibility(View.VISIBLE);
                mRetryButton.setVisibility(View.VISIBLE);
                showSpinnerForm(false);
                showReplaceForm(false);
                break;
            case ALL_OK:
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
        User userFromActivity;

        switch (mState){
            case START:
                if(mCategorySpinner.getOnItemSelectedListener()!=null)
                    mCategorySpinner.setOnItemSelectedListener(null);
                if(mRouteSpinner.getOnItemSelectedListener()!=null)
                    mRouteSpinner.setOnItemSelectedListener(null);
                mCategorySpinner.setAdapter(null);
                mRouteSpinner.setAdapter(null);
                categoryAdapter = null;
                routeAdapter = null;
                mToActivityMethod.setCategories(null);
                CategoriesRequest mCategoriesRequest = new CategoriesRequest(mToActivityMethod.getUser().getUserId(),
                        mToActivityMethod.getUser().getAuthToken(), getActivity());
                spiceManager.execute(mCategoriesRequest, new CategoriesRequestListener());
                break;
            case CATEGORY_FAIL:
                break;
            case PICKING:
                break;
            case VERIFYING_1:
                selectedCategoryId = ((HintableSpinnerItem) mCategorySpinner.getSelectedItem()).getId();
                selectedRouteId = ((HintableSpinnerItem) mRouteSpinner.getSelectedItem()).getId();

                userFromActivity = mToActivityMethod.getUser();
                ReportRequest mReportRequest1 = new ReportRequest(userFromActivity.getUserId(),
                        userFromActivity.getAuthToken(), selectedCategoryId, selectedRouteId,
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

                userFromActivity = mToActivityMethod.getUser();
                ReportRequest mReportRequest2 = new ReportRequest(userFromActivity.getUserId(),
                        userFromActivity.getAuthToken(), selectedCategoryId, selectedRouteId,
                        true, getActivity());
                spiceManager.execute(mReportRequest2, new ReportRequestListener());
                break;
            case VERIFYING_2_FAIL:
                break;
            case ALL_OK:
                selectedCategoryId = ((HintableSpinnerItem) mCategorySpinner.getSelectedItem()).getId();
                selectedRouteId = ((HintableSpinnerItem) mRouteSpinner.getSelectedItem()).getId();
                mToActivityMethod.onCategoryRouteSelected(selectedCategoryId, selectedRouteId);

                // Call a method to spawn the next tab into existence.
                mToActivityMethod.createAndSwitchToScanFragment();
                break;
            default:
                break;
        }
    }



    /*=========================================================================
     * UI methods
     *=======================================================================*/
    private void showSpinnerForm(boolean show) {
        mHelloText.setVisibility(show ? View.VISIBLE : View.GONE);
        mCategorySpinner.setVisibility(show ? View.VISIBLE : View.GONE);
        mRouteSpinner.setVisibility(show ? View.VISIBLE : View.GONE);
        mNextButton.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void initHelloText(){
        String userName = null;
        if(mToActivityMethod.getUser() != null){
            userName = mToActivityMethod.getUser().getUserName();
        }
        mHelloText.setText(getActivity().getString(R.string.route_fragment_greeting) +
                userName +
                getActivity().getString(R.string.route_fragment_question));
    }

    private void enableRouteSpinner(boolean enable){
        mRouteSpinner.setEnabled(enable);
    }

    private void enableNextButtonIfPossible(boolean enable){
        Categories.RouteItem selectedItem = (Categories.RouteItem)mRouteSpinner.getSelectedItem();
        if(selectedItem==null){
            if (DEBUG) Log.d(TAG, "selectedItem is null");
        }
        else {
            boolean isHint = ((Categories.RouteItem) mRouteSpinner.getSelectedItem()).isHint();
            if (!enable)
                mNextButton.setEnabled(false);
            else
                mNextButton.setEnabled(!isHint);
        }
    }

    private void showReplaceForm(boolean show){
        mReplaceText.setVisibility(show ? View.VISIBLE : View.GONE);
        mYesNoLayout.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void updateReplaceText(String currentJudge){
        String question = currentJudge+getString(R.string.route_fragment_replace_question1)+
                mCategorySpinner.getSelectedItem().toString()+
                getString(R.string.route_fragment_replace_question2)+
                mRouteSpinner.getSelectedItem().toString()+
                getString(R.string.route_fragment_replace_question3)+
                currentJudge+
                getString(R.string.route_fragment_replace_question4);

        mReplaceText.setText(question);
    }

    /**
     * This method handles setting up the spinner. It contruct adapter for spinner, set adapter
     * to spinner, set selection for spinner and set listener to spinner.
     */
    private void setupSpinner(){
        if(categoryAdapter == null || routeAdapter == null){
            String categoryHint = getString(R.string.route_fragment_category_hint);
            String routeHint = getString(R.string.route_fragment_route_hint);

            Categories categoryFromActivity = mToActivityMethod.getCategories();
            List<HintableSpinnerItem> categoryList = categoryFromActivity.getCategoriesSpinnerListCopy(categoryHint, routeHint);
            categoryAdapter = new HintableArrayAdapter(getActivity(),
                    android.R.layout.simple_spinner_item, categoryList);

            List<HintableSpinnerItem> routeList = ((Categories.CategoryItem) (categoryList.get(categorySpinnerIndex))).getRoutes();
            routeAdapter = new HintableArrayAdapter(getActivity(), android.R.layout.simple_spinner_item,routeList);

            categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            routeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }
        if(mCategorySpinner.getAdapter()==null)
            mCategorySpinner.setAdapter(categoryAdapter);
        if(mRouteSpinner.getAdapter()==null)
            mRouteSpinner.setAdapter(routeAdapter);

        // Note to self: passing false as animate parameter will not trigger onSelectListener.
        mCategorySpinner.setSelection(categorySpinnerIndex, false);
        mRouteSpinner.setSelection(routeSpinnerIndex,false);

        if(mCategorySpinner.getOnItemSelectedListener()==null)
            mCategorySpinner.setOnItemSelectedListener(new CategoriesSpinnerListener());
        if(mRouteSpinner.getOnItemSelectedListener()==null)
            mRouteSpinner.setOnItemSelectedListener(new RouteSpinnerListener());
    }

    /*=========================================================================
     * Interface methods
     *=======================================================================*/
    @Override
    public void onClick(View v) {
        switch(v.getId()){
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
        // Note to self: Playing the refresh animation (i.e. SwipeLayout.setRefreshing(true))
        // and calling notifyDatasetChanged in FragmentPagerAdapter seems to lead to nullpointer
        // exception. I fix this by updating fragment count in HelloActivityFragmentPagerAdapter only
        // AFTER SwipeLayout.setRefreshing(false). Might want to investigate this somemore.
        changeState(State.START);
    }

    @Override
    public void onNavigateAway(){
        if(DEBUG) Log.d(TAG, "NavigateAway");

        State state;
        switch (mState){
            case START:
            case CATEGORY_FAIL:
                state = State.START;
                break;
            default:
                state = State.PICKING;
        }

        Bundle outState = new Bundle();
        outState.putInt(getString(R.string.bundle_route_state), state.getValue());
        categorySpinnerIndex = mCategorySpinner.getSelectedItemPosition();
        routeSpinnerIndex = mRouteSpinner.getSelectedItemPosition();
        outState.putInt(getString(R.string.bundle_category_spinner_selected_index), categorySpinnerIndex);
        outState.putInt(getString(R.string.bundle_route_spinner_selected_index), routeSpinnerIndex);

        mToActivityMethod.saveRouteInstance(outState);
    }

    @Override
    public void onNavigateTo(){
        if(DEBUG) Log.d(TAG, "NavigateTo");

        Bundle mBundle = mToActivityMethod.restoreRouteInstance();
        State state = State.toEnum(mBundle.getInt(getString(R.string.bundle_route_state), State.START.getValue()));
        categorySpinnerIndex = mBundle.getInt(getString(R.string.bundle_category_spinner_selected_index), 0);
        routeSpinnerIndex = mBundle.getInt(getString(R.string.bundle_route_spinner_selected_index), 0);

        changeState(state);
    }

    /*=========================================================================
     * Subclasses and interface
     *=======================================================================*/
    /**
     * Listener for CategoriesRequest.
     */
    private class CategoriesRequestListener implements
            RequestListener<CategoriesResponseBody> {
        private final String TAG = CategoriesRequestListener.class.getSimpleName();

        @Override
        public void onRequestFailure(SpiceException e) {
            if(mSwipeLayout.isRefreshing())
                mSwipeLayout.setRefreshing(false);
            if(mState == State.START) {
                mToActivityMethod.setCategories(new Categories());
                mToActivityMethod.onSpinnerSelectionChange();
                categorySpinnerIndex = 0;
                routeSpinnerIndex = 0;
                changeState(State.CATEGORY_FAIL);
            }
        }

        @Override
        public void onRequestSuccess(CategoriesResponseBody result) {
            if(mSwipeLayout.isRefreshing())
                mSwipeLayout.setRefreshing(false);
            if(mState == State.START) {
                mToActivityMethod.setCategories(new Categories(result));
                mToActivityMethod.onSpinnerSelectionChange();
                categorySpinnerIndex = 0;
                routeSpinnerIndex = 0;
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
                mToActivityMethod.onCategoryRouteSelected(null, null);
                changeState(State.VERIFY_1_FAIL);
            }
            if(mState == State.VERIFYING_2){
                mToActivityMethod.onCategoryRouteSelected(null, null);
                changeState(State.VERIFYING_2_FAIL);
            }
        }

        @Override
        public void onRequestSuccess(ReportResponseBody result) {
            String currentJudge = result.getAdminName();

            if(result.getState() == 1){
                if(mState == State.VERIFYING_1 || mState == State.VERIFYING_2){
                    mToActivityMethod.onCategoryRouteSelected(result.getCategoryId(), result.getRouteId());
                    enableNextButtonIfPossible(false);
                    changeState(State.ALL_OK);
                }
            }
            else{
                if(mState == State.VERIFYING_1){
                    updateReplaceText(currentJudge);
                    mToActivityMethod.onCategoryRouteSelected(null, null);
                    changeState(State.VERIFY_1_NOT_OK);
                }
                else if(mState == State.VERIFYING_2){
                    // This should not even happen
                    mStatusText.setText(R.string.route_fragment_status_report_in_fail);
                    mToActivityMethod.onCategoryRouteSelected(null, null);
                    changeState(State.VERIFYING_2_FAIL);
                }
            }
        }
    }

    /**
     * Listener for category spinner. We will need to update the list of item in routeSpinner adapter
     * whenever we change our selected category. We also need to update route spinner's enable state.
     * Next button's enable state and updating fragmentPagerAdapter are done by route spinner listener.
     */
    private class CategoriesSpinnerListener implements AdapterView.OnItemSelectedListener{
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            categorySpinnerIndex = mCategorySpinner.getSelectedItemPosition();
            Categories.CategoryItem selectedCategory =
                    (Categories.CategoryItem)parent.getAdapter().getItem(position);

            if (DEBUG) Log.d(TAG, "CategorySpinnerListener selected item:" + selectedCategory.getText());

            if(selectedCategory.isHint()){
                enableRouteSpinner(false);
                mRouteSpinner.setSelection(routeAdapter.getFirstHintPosition(), false);
            }
            else{
                enableRouteSpinner(true);

                // Clear mRouteSpinner list, repopulate with updated route list and set selection
                // to first hint item.
                HintableArrayAdapter routeAdapter = (HintableArrayAdapter)mRouteSpinner.getAdapter();
                routeAdapter.clear();
                routeAdapter.addAll(selectedCategory.getRoutes());
                routeAdapter.notifyDataSetChanged();
                mRouteSpinner.setSelection(routeAdapter.getFirstHintPosition(), false);
            }

            // This method is called because category was changed by user.
            mToActivityMethod.onSpinnerSelectionChange();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    /**
     * Listener for route spinner. We will update next button's enable state and fragmentPagerAdapter
     * whenever route selected is change.
     */
    private class RouteSpinnerListener implements AdapterView.OnItemSelectedListener{

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            // onItemSelected will only be called by user using spinner if there is a change in
            // the selected item (i.e. selecting a different item). Will not be fired by user if
            // there is no change in selection (e.g. item1 selected -> click spinner ->
            // select back item1).
            routeSpinnerIndex = mRouteSpinner.getSelectedItemPosition();
            Categories.RouteItem selectedRoute =
                    (Categories.RouteItem)parent.getAdapter().getItem(position);

            if (DEBUG) Log.d(TAG, "RouteSpinnerListener selected item: "+selectedRoute.getText());

            // Find out if selected route is a hint. Enable next button if selectedRoute is not
            // hint.
            if(selectedRoute.isHint()){
                enableNextButtonIfPossible(false);
            } else{
                enableNextButtonIfPossible(true);
            }

            mToActivityMethod.onSpinnerSelectionChange();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
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
        void createAndSwitchToScanFragment();
        void onSpinnerSelectionChange();
        User getUser();
        Categories getCategories();
        void onCategoryRouteSelected(String categoryId, String routeId);
        void setCategories(Categories categories);
        void saveRouteInstance(Bundle bundle);
        Bundle restoreRouteInstance();
    }
}