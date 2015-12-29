package com.nusclimb.live.crimp.hello;

import android.app.Activity;
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
public class RouteFragment extends CrimpFragment implements SwipeRefreshLayout.OnRefreshListener{
    private final String TAG = RouteFragment.class.getSimpleName();

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

    private RouteFragmentToActivityMethods mToActivityMethod;   //This is how we will communicate with
                                                                //Hello Activity.
    private HintableArrayAdapter categoryAdapter;
    private HintableArrayAdapter routeAdapter;
    private State mState = State.START;
    private SpiceManager spiceManager = new SpiceManager(CrimpService.class);
    private int categorySpinnerIndex;
    private int routeSpinnerIndex;

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

    public static RouteFragment newInstance() {
        RouteFragment myFragment = new RouteFragment();

        return myFragment;
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

        Log.d(TAG, "RouteFragment onAttach");
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

        Log.d(TAG, "RouteFragment onCreateView");

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // This is the final lifecycle state before fragment is visible.
        // Initialize all objects here.
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState == null){
            Log.d(TAG, "RouteFragment onActivityCreated without savedInstanceState");
            // Initialize mState
            mState = State.START;

            //TODO Persist spinner selection
            categorySpinnerIndex = 0;
            routeSpinnerIndex = 0;
        }
        else{
            Log.d(TAG, "RouteFragment onActivityCreated with savedInstanceState");

            // Initialize mState. We must have mState in savedInstanceState.
            mState = State.toEnum(savedInstanceState.getInt(getString(R.string.bundle_route_state)));

            //TODO Persist spinner selection
            categorySpinnerIndex = savedInstanceState.getInt(getString(R.string.bundle_category_spinner_selected_index));
            routeSpinnerIndex = savedInstanceState.getInt(getString(R.string.bundle_route_spinner_selected_index));
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        spiceManager.start(getActivity());
        Log.d(TAG, "RouteFragment onStart");
    }

    @Override
    public void onResume() {
        Log.d(TAG, "RouteFragment onResume");
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
            case ALL_OK:
                changeState(mState);
                break;
        }
    }

    @Override
    public void onPause() {
        Log.d(TAG, "RouteFragment onPause");
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(getString(R.string.bundle_route_state), mState.getValue());

        //TODO Persist spinner selection
        outState.putInt(getString(R.string.bundle_category_spinner_selected_index), categorySpinnerIndex);
        outState.putInt(getString(R.string.bundle_route_spinner_selected_index), routeSpinnerIndex);

        Log.d(TAG, "RouteFragment onSaveInstanceState.");
    }

    @Override
    public void onStop(){
        Log.d(TAG, "RouteFragment onStop");
        spiceManager.shouldStop();
        super.onStop();
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "RouteFragment onDetach");
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
        Log.d(TAG, "Change state: " + mState + " -> " + state);

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
                User userFromActivity = mToActivityMethod.getUser();
                if(userFromActivity.getCategoryId()!=null && userFromActivity.getRouteId()!=null){
                    // We already have categoryId and routeId. This implies that we have already
                    // complete report request to server.
                    enableNextButtonIfPossible(false);
                }
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
                categoryAdapter = null;
                routeAdapter = null;
                CategoriesRequest mCategoriesRequest = new CategoriesRequest(mToActivityMethod.getUser().getUserId(),
                        mToActivityMethod.getUser().getAuthToken(), getActivity());
                spiceManager.execute(mCategoriesRequest, new CategoriesRequestListener());
                break;
            case CATEGORY_FAIL:
                break;
            case PICKING:
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

                mCategorySpinner.setSelection(categorySpinnerIndex, false);
                mRouteSpinner.setSelection(routeSpinnerIndex,false);

                if(mCategorySpinner.getOnItemSelectedListener()==null)
                    mCategorySpinner.setOnItemSelectedListener(new CategoriesSpinnerListener());
                if(mRouteSpinner.getOnItemSelectedListener()==null)
                    mRouteSpinner.setOnItemSelectedListener(new RouteSpinnerListener());
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
                changeState(State.PICKING);

                // Call a method to spawn the next tab into existence. use contract method.
                mToActivityMethod.createAndSwitchToScanFragment();
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
            Log.d(TAG, "selectedItem is null");
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
        mReplaceForm.setVisibility(show ? View.VISIBLE : View.GONE);
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
     * Interface methods
     *=======================================================================*/
    @Override
    public CharSequence getPageTitle() {
        return "Route";
    }

    @Override
    public void restart(){
        changeState(State.START);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.route_category_request_retry_button:
                mToActivityMethod.setCategories(null);
                changeState(State.START);
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
        changeState(State.START);
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
            mSwipeLayout.setRefreshing(false);
            if(mState == State.START)
                updateCategoryStatusText(R.string.route_fragment_status_category_fail);
                mToActivityMethod.setCategories(null);
                changeState(State.CATEGORY_FAIL);
        }

        @Override
        public void onRequestSuccess(CategoriesResponseBody result) {
            mSwipeLayout.setRefreshing(false);
            if(mState == State.START) {
                mToActivityMethod.setCategories(new Categories(result));
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
                mToActivityMethod.onCategoryRouteSelected(null, null);
                changeState(State.VERIFY_1_FAIL);
            }
            if(mState == State.VERIFYING_2){
                updateStatusText(R.string.route_fragment_status_report_in_fail);
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
                    categorySpinnerIndex = mCategorySpinner.getSelectedItemPosition();
                    routeSpinnerIndex = mRouteSpinner.getSelectedItemPosition();
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
                    updateStatusText(R.string.route_fragment_status_report_in_fail);
                    mToActivityMethod.onCategoryRouteSelected(null, null);
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

            Log.d(TAG, "CategorySpinnerListener selected item:" + selectedCategory.getText());

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

    private class RouteSpinnerListener implements AdapterView.OnItemSelectedListener{

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            // onItemSelected will only be called by user using spinner if there is a change in
            // the selected item (i.e. selecting a different item). Will not be fired by user if
            // there is no change in selection (e.g. item1 selected -> click spinner ->
            // select back item1).
            Categories.RouteItem selectedRoute =
                    (Categories.RouteItem)parent.getAdapter().getItem(position);

            Log.d(TAG, "RouteSpinnerListener selected item: "+selectedRoute.getText());

            // Find out if selected route is a hint. Enable next button if selectedRoute is not
            // hint.
            if(selectedRoute.isHint()){

            } else{
                enableNextButtonIfPossible(true);

            }

            mToActivityMethod.onSpinnerSelectionChange();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
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
        void createAndSwitchToScanFragment();
        void onSpinnerSelectionChange();
        User getUser();
        Categories getCategories();
        void onCategoryRouteSelected(String categoryId, String routeId);
        void setCategories(Categories categories);
    }
}