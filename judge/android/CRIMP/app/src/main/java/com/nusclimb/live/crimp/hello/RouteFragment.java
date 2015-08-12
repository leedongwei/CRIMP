package com.nusclimb.live.crimp.hello;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.nusclimb.live.crimp.common.json.CategoriesHeadResponseBody;
import com.nusclimb.live.crimp.common.json.CategoriesResponseBody;
import com.nusclimb.live.crimp.common.json.ReportResponseBody;
import com.nusclimb.live.crimp.common.spicerequest.CategoriesHeadRequest;
import com.nusclimb.live.crimp.common.spicerequest.CategoriesRequest;
import com.nusclimb.live.crimp.common.spicerequest.ReportRequest;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Fragment for category and route selection. Activity containing this Fragment must implement
 * FragmentToActivityMethods interface to allow this fragment to communicate with the attached
 * Activity and possibly other Fragments. Information from the Activity is passed to this Fragment
 * through arguments.
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class RouteFragment extends Fragment implements View.OnClickListener{
    private final String TAG = RouteFragment.class.getSimpleName();

    private User mUser = null;
    private State mState = State.START;
    private State shouldState = State.PICKING;      // State that mState should change to after
                                                    // categories is ready.
    private Categories categoryInfo;
    private String currentJudge;

    private HintableArrayAdapter categoryAdapter;
    private HintableArrayAdapter routeAdapter;

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

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.route_next_button:
                changeState(State.IN_FIRST_REQUEST);
                break;
            case R.id.route_yes_button:
                changeState(State.IN_SECOND_REQUEST);
                break;
            case R.id.route_no_button:
                changeState(State.PICKING);
                break;
            case R.id.route_retry_button:
                if(mState == State.VERIFY_SIGNATURE_FAIL)
                    changeState(State.VERIFY_SIGNATURE);
                if(mState == State.REQUEST_CATEGORIES_FAIL)
                    changeState(State.REQUEST_CATEGORIES);
                break;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        // Create the view for this fragment. Set references for view object that we will be
        // using in this class.

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
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.route_wheel_progressbar);
        mStatusText = (TextView) rootView.findViewById(R.id.route_status_text);
        mRetryButton = (Button) rootView.findViewById(R.id.route_retry_button);

        // Set button on click listener
        mNextButton.setOnClickListener(this);
        mNoButton.setOnClickListener(this);
        mRetryButton.setOnClickListener(this);
        mRetryButton.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // This is the final lifecycle state before fragment is visible.
        // Initialize all objects here.

        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState == null){
            shouldState = State.PICKING;
        }
        else{
            shouldState = State.toEnum(savedInstanceState.getInt(
                    getString(R.string.bundle_route_state)));
        }

        // Initialize mUser
        if(mUser == null) {
            Bundle args = getArguments();
            mUser = new User();
            mUser.setUserId(args.getString(getString(R.string.bundle_x_user_id)));
            mUser.setAuthToken(args.getString(getString(R.string.bundle_x_auth_token)));
            mUser.setUserName(args.getString(getString(R.string.bundle_user_name)));
            mUser.setFacebookAccessToken(args.getString(getString(R.string.bundle_access_token)));
        }

        // Read Categories information from persistent storage
        String FILENAME = getString(R.string.categories_file);
        FileInputStream fis = null;
        CategoriesResponseBody mCategoriesResponseBody = null;
        try {
            fis = getActivity().openFileInput(FILENAME);
            ObjectMapper mapper = new ObjectMapper();
            mCategoriesResponseBody =
                    mapper.readValue(fis, CategoriesResponseBody.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            mCategoriesResponseBody = null;
        } catch (IOException e) {
            e.printStackTrace();
            mCategoriesResponseBody = null;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {}
            }
        }

        // Initialize categoryInfo
        if (mCategoriesResponseBody == null) {
            categoryInfo = null;
        } else {
            categoryInfo = new Categories(mCategoriesResponseBody);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        changeState(State.START);
    }

    @Override
    public void onPause() {
        switch(shouldState){
            case PICKING:
            case IN_FIRST_REQUEST:
            case FIRST_REQUEST_NOT_OK:
            case FIRST_REQUEST_FAILED:
                shouldState = State.PICKING;
                break;
            case REPLACE_QUESTION:
            case IN_SECOND_REQUEST:
            case SECOND_REQUEST_OK:
            case SECOND_REQUEST_FAILED:
                shouldState = State.REPLACE_QUESTION;
                break;
            case JUDGE_OK:
                shouldState = State.JUDGE_OK;
                break;
        }

        if(categoryInfo != null) {
            String FILENAME = getString(R.string.categories_file);

            FileOutputStream fos = null;
            try {
                fos = getActivity().openFileOutput(FILENAME, Context.MODE_PRIVATE);
                ObjectMapper mapper = new ObjectMapper();
                mapper.writeValue(fos, categoryInfo.getJSON());
            } catch(FileNotFoundException e) {
                e.printStackTrace();
            } catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (JsonGenerationException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    fos.close();
                }catch(IOException e) {}
            }

        }
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        switch(shouldState){
            case PICKING:
            case IN_FIRST_REQUEST:
            case FIRST_REQUEST_NOT_OK:
            case FIRST_REQUEST_FAILED:
                shouldState = State.PICKING;
                break;
            case REPLACE_QUESTION:
            case IN_SECOND_REQUEST:
            case SECOND_REQUEST_OK:
            case SECOND_REQUEST_FAILED:
                shouldState = State.REPLACE_QUESTION;
                break;
            case JUDGE_OK:
                shouldState = State.JUDGE_OK;
                break;
        }

        outState.putInt(getString(R.string.bundle_route_state), shouldState.getValue());
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
                showRetryButton(false);
                showProgressBar(true);
                showProgressForm(true);
                showSpinnerForm(false);
                showReplaceForm(false);
                break;
            case NO_CATEGORIES:
                showRetryButton(false);
                showProgressBar(true);
                updateStatusText(R.string.route_fragment_status_no_categories);
                showProgressForm(true);
                showSpinnerForm(false);
                showReplaceForm(false);
                break;
            case HAS_CATEGORIES:
                showRetryButton(false);
                showProgressBar(true);
                updateStatusText(R.string.route_fragment_status_has_categories);
                showProgressForm(true);
                showSpinnerForm(false);
                showReplaceForm(false);
                break;
            case VERIFY_SIGNATURE:
                showRetryButton(false);
                showProgressBar(true);
                updateStatusText(R.string.route_fragment_status_verify_signature);
                showProgressForm(true);
                showSpinnerForm(false);
                showReplaceForm(false);
                break;
            case VERIFY_SIGNATURE_FAIL:
                showRetryButton(true);
                showProgressBar(false);
                updateStatusText(R.string.route_fragment_status_signature_fail);
                showProgressForm(true);
                showSpinnerForm(false);
                showReplaceForm(false);
                break;
            case SIGNATURE_EXPIRE:
                showRetryButton(false);
                showProgressBar(true);
                updateStatusText(R.string.route_fragment_status_expire);
                showProgressForm(true);
                showSpinnerForm(false);
                showReplaceForm(false);
                break;
            case SIGNATURE_NOT_EXPIRE:
                showRetryButton(false);
                showProgressBar(true);
                updateStatusText(R.string.route_fragment_status_not_expire);
                showProgressForm(true);
                showSpinnerForm(false);
                showReplaceForm(false);
                break;
            case REQUEST_CATEGORIES:
                showRetryButton(false);
                showProgressBar(true);
                updateStatusText(R.string.route_fragment_status_request_category);
                showProgressForm(true);
                showSpinnerForm(false);
                showReplaceForm(false);
                break;
            case REQUEST_CATEGORIES_FAIL:
                showRetryButton(true);
                showProgressBar(false);
                updateStatusText(R.string.login_activity_categories_fail);
                showProgressForm(true);
                showSpinnerForm(false);
                showReplaceForm(false);
                break;
            case CATEGORIES_READY:
                showRetryButton(false);
                showProgressBar(true);
                updateStatusText(R.string.route_fragment_status_ready);
                showProgressForm(true);
                showSpinnerForm(false);
                showReplaceForm(false);
                break;
            case PICKING:
                initHelloText();
                enableRouteSpinner(false);
                showSpinnerForm(true);
                showReplaceForm(false);
                showProgressForm(false);
                enableNextButtonIfPossible(true);
                break;
            case IN_FIRST_REQUEST:
                showSpinnerForm(true);
                showReplaceForm(false);
                updateStatusText(R.string.route_fragment_status_report_in);
                showRetryButton(false);
                showProgressBar(true);
                showProgressForm(true);
                enableNextButtonIfPossible(false);
                break;
            case FIRST_REQUEST_FAILED:
                showSpinnerForm(true);
                showReplaceForm(false);
                updateStatusText(R.string.route_fragment_status_report_in_fail);
                showRetryButton(false);
                showProgressBar(false);
                showProgressForm(true);
                enableNextButtonIfPossible(true);
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
                showRetryButton(false);
                showProgressBar(true);
                showProgressForm(true);
                enableYesNo(false);
                break;
            case SECOND_REQUEST_FAILED:
                showSpinnerForm(false);
                updateReplaceText();
                showReplaceForm(true);
                updateStatusText(R.string.route_fragment_status_report_in_fail);
                showRetryButton(false);
                showProgressBar(false);
                showProgressForm(true);
                enableYesNo(true);
                break;
            case JUDGE_OK:
                showSpinnerForm(true);
                showReplaceForm(false);
                showProgressForm(false);
                enableNextButtonIfPossible(false);
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
                if(categoryInfo == null)
                    changeState(State.NO_CATEGORIES);
                else
                    changeState(State.HAS_CATEGORIES);
                break;
            case NO_CATEGORIES:
                changeState(State.REQUEST_CATEGORIES);
                break;
            case HAS_CATEGORIES:
                if(((FragmentToActivityMethods)getActivity()).isOfflineMode()) {
                    changeState(State.CATEGORIES_READY);
                }
                else{
                    changeState(State.VERIFY_SIGNATURE);
                }
                break;
            case VERIFY_SIGNATURE:
                CategoriesHeadRequest mCategoriesHeadRequest =
                        new CategoriesHeadRequest(mUser.getUserId(),
                                mUser.getAuthToken(), getActivity());

                ((FragmentToActivityMethods) getActivity()).getSpiceManager().
                        execute(mCategoriesHeadRequest, new CategoriesHeadRequestListener());
                break;
            case VERIFY_SIGNATURE_FAIL:
                break;
            case SIGNATURE_EXPIRE:
                changeState(State.REQUEST_CATEGORIES);
                break;
            case SIGNATURE_NOT_EXPIRE:
                changeState(State.CATEGORIES_READY);
                break;
            case REQUEST_CATEGORIES:
                shouldState = State.PICKING;

                CategoriesRequest mCategoriesRequest =
                        new CategoriesRequest(mUser.getUserId(),
                                mUser.getAuthToken(), getActivity());

                ((FragmentToActivityMethods) getActivity()).getSpiceManager().
                        execute(mCategoriesRequest, new CategoriesRequestListener());
                break;
            case REQUEST_CATEGORIES_FAIL:
                break;
            case CATEGORIES_READY:
                String categoryHint = getString(R.string.route_fragment_category_hint);
                String routeHint = getString(R.string.route_fragment_route_hint);

                categoryAdapter = new HintableArrayAdapter(getActivity(),
                        android.R.layout.simple_spinner_item,
                        categoryInfo.getCategoriesSpinnerListCopy(categoryHint, routeHint));
                categoryAdapter.setDropDownViewResource(
                        android.R.layout.simple_spinner_dropdown_item);
                mCategorySpinner.setAdapter(categoryAdapter);
                mCategorySpinner.setOnItemSelectedListener(new CategoriesSpinnerListener());

                routeAdapter = new HintableArrayAdapter(getActivity(),
                        android.R.layout.simple_spinner_item,
                        categoryInfo.getCategorySpinnerItem(0).getRoutes());
                routeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mRouteSpinner.setAdapter(routeAdapter);
                mRouteSpinner.setOnItemSelectedListener(new RouteSpinnerListener());

                changeState(shouldState);
                break;
            case PICKING:

                break;
            case IN_FIRST_REQUEST:
                selectedCategoryId = ((HintableSpinnerItem) mCategorySpinner.getSelectedItem()).getId();
                selectedRouteId = ((HintableSpinnerItem) mRouteSpinner.getSelectedItem()).getId();

                ReportRequest mReportRequest1 = new ReportRequest(mUser.getUserId(),
                        mUser.getAuthToken(), selectedCategoryId, selectedRouteId,
                        false, getActivity());

                ((FragmentToActivityMethods)getActivity()).getSpiceManager().execute(
                        mReportRequest1, new ReportRequestListener());
                break;
            case FIRST_REQUEST_OK:
                changeState(State.JUDGE_OK);
                break;
            case FIRST_REQUEST_NOT_OK:
                changeState(State.REPLACE_QUESTION);
                break;
            case FIRST_REQUEST_FAILED:
                break;
            case REPLACE_QUESTION:
                break;
            case IN_SECOND_REQUEST:
                selectedCategoryId = ((HintableSpinnerItem) mCategorySpinner.getSelectedItem()).getId();
                selectedRouteId = ((HintableSpinnerItem) mRouteSpinner.getSelectedItem()).getId();

                ReportRequest mReportRequest2 = new ReportRequest(mUser.getUserId(),
                        mUser.getAuthToken(), selectedCategoryId, selectedRouteId,
                        false, getActivity());

                ((FragmentToActivityMethods)getActivity()).getSpiceManager().execute(
                        mReportRequest2, new ReportRequestListener());

                break;
            case SECOND_REQUEST_OK:
                changeState(State.JUDGE_OK);
                break;
            case SECOND_REQUEST_FAILED:
                break;
            case JUDGE_OK:
                break;
        }
    }





    private void showSpinnerForm(boolean show){
        mSpinnerForm.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void initHelloText(){
        mHelloText.setText(getActivity().getString(R.string.route_fragment_greeting) +
                getArguments().getString(getString(R.string.bundle_user_name)) +
                getActivity().getString(R.string.route_fragment_question));
    }

    private void enableRouteSpinner(boolean enable){
        mRouteSpinner.setEnabled(enable);
    }

    private void enableNextButtonIfPossible(boolean enable){
        boolean isHint = ((Categories.RouteItem)mRouteSpinner.getSelectedItem()).isHint();
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
                ((Categories.CategoryItem)mCategorySpinner.getSelectedItem()).toString()+
                getString(R.string.route_fragment_replace_question2)+
                ((Categories.RouteItem)mRouteSpinner.getSelectedItem()).toString()+
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

    private void showProgressBar(boolean show){
        mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void updateStatusText(int resId){
        mStatusText.setText(resId);
    }

    private void showRetryButton(boolean show){
        mRetryButton.setVisibility(show ? View.VISIBLE : View.GONE);
    }





    /**
     * Listener for CategoriesHeadRequest
     */
    private class CategoriesHeadRequestListener implements
            RequestListener<CategoriesHeadResponseBody> {
        private final String TAG = CategoriesHeadRequestListener.class.getSimpleName();

        @Override
        public void onRequestFailure(SpiceException e) {
            if(mState == State.VERIFY_SIGNATURE)
                changeState(State.VERIFY_SIGNATURE_FAIL);
        }

        @Override
        public void onRequestSuccess(CategoriesHeadResponseBody result) {
            if(mState == State.VERIFY_SIGNATURE) {
                String mySignature = categoryInfo.getJSON().getSignature();
                String serverSignature = result.getSignature();
                if (mySignature.equals(serverSignature)) {
                    changeState(State.SIGNATURE_NOT_EXPIRE);
                }
                else {
                    changeState(State.SIGNATURE_EXPIRE);
                }
            }
        }
    }

    /**
     * Listener for CategoriesRequest.
     */
    private class CategoriesRequestListener implements
            RequestListener<CategoriesResponseBody> {
        private final String TAG = CategoriesRequestListener.class.getSimpleName();

        @Override
        public void onRequestFailure(SpiceException e) {
            if(mState == State.REQUEST_CATEGORIES)
                changeState(State.REQUEST_CATEGORIES_FAIL);
        }

        @Override
        public void onRequestSuccess(CategoriesResponseBody result) {
            if(mState == State.REQUEST_CATEGORIES) {
                categoryInfo = new Categories(result);
                changeState(State.CATEGORIES_READY);
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
            if(mState == State.IN_FIRST_REQUEST){
                changeState(State.FIRST_REQUEST_FAILED);
            }
            if(mState == State.IN_SECOND_REQUEST){
                changeState(State.SECOND_REQUEST_FAILED);
            }
        }

        @Override
        public void onRequestSuccess(ReportResponseBody result) {
            currentJudge = result.getAdminName();

            if(result.getState() == 1){
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
                    changeState(State.SECOND_REQUEST_FAILED);
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
    public interface FragmentToActivityMethods {
        public SpiceManager getSpiceManager();
        public boolean isOfflineMode();
    }

    private enum State{
        START(0),
        NO_CATEGORIES(1),
        HAS_CATEGORIES(2),
        VERIFY_SIGNATURE(3),
        VERIFY_SIGNATURE_FAIL(4),
        //REQUEST_SIGNATURE_NOT_OK(5),
        //REQUEST_SIGNATURE_OK(6),
        SIGNATURE_EXPIRE(7),
        SIGNATURE_NOT_EXPIRE(8),
        REQUEST_CATEGORIES(9),
        REQUEST_CATEGORIES_FAIL(10),
        //REQUEST_CATEGORIES_NOT_OK(11),
        //REQUEST_CATEGORIES_OK(12),
        CATEGORIES_READY(13),
        PICKING(14),                // Picking category and route. Can stay in this state.
        IN_FIRST_REQUEST(15),       // Sending request to be judge. Force=false.
        FIRST_REQUEST_OK(16),       // CRIMP server reply ok.
        FIRST_REQUEST_NOT_OK(17),   // CRIMP server reply not ok.
        FIRST_REQUEST_FAILED(18),   // No/unknown response from CRIMP server.
        REPLACE_QUESTION(19),       // Ask user to force replace. Can stay in this state.
        IN_SECOND_REQUEST(20),      // Sending request to be judge. Force=true.
        SECOND_REQUEST_OK(21),      // CRIMP server reply ok.
        //SECOND_REQUEST_NOT_OK(22),  // CRIMP server reply not ok.
        SECOND_REQUEST_FAILED(23),  // No/unknown response from CRIMP server.
        JUDGE_OK(24);              // User become judge. Can stay in this state.

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
                    return NO_CATEGORIES;
                case 2:
                    return HAS_CATEGORIES;
                case 3:
                    return VERIFY_SIGNATURE;
                case 4:
                    return VERIFY_SIGNATURE_FAIL;
                case 5:
                    return null;
                //case 6:
                //    return REQUEST_SIGNATURE_OK;
                case 7:
                    return SIGNATURE_EXPIRE;
                case 8:
                    return SIGNATURE_NOT_EXPIRE;
                case 9:
                    return REQUEST_CATEGORIES;
                case 10:
                    return REQUEST_CATEGORIES_FAIL;
                case 11:
                    return null;
                //case 12:
                //    return REQUEST_CATEGORIES_OK;
                case 13:
                    return CATEGORIES_READY;
                case 14:
                    return PICKING;
                case 15:
                    return IN_FIRST_REQUEST;
                case 16:
                    return FIRST_REQUEST_OK;
                case 17:
                    return FIRST_REQUEST_NOT_OK;
                case 18:
                    return FIRST_REQUEST_FAILED;
                case 19:
                    return REPLACE_QUESTION;
                case 20:
                    return IN_SECOND_REQUEST;
                case 21:
                    return SECOND_REQUEST_OK;
                //case 22:
                //    return SECOND_REQUEST_NOT_OK;
                case 23:
                    return SECOND_REQUEST_FAILED;
                case 24:
                    return JUDGE_OK;
                default:
                    return null;
            }
        }
    }

}