package com.nusclimb.live.crimp.hello;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.nusclimb.live.crimp.CrimpApplication;
import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.common.Categories;
import com.nusclimb.live.crimp.common.Climber;
import com.nusclimb.live.crimp.common.QueueObject;
import com.nusclimb.live.crimp.common.User;
import com.nusclimb.live.crimp.common.json.ActiveMonitorResponseBody;
import com.nusclimb.live.crimp.common.json.GetScoreResponseBody;
import com.nusclimb.live.crimp.common.spicerequest.ActiveMonitorRequest;
import com.nusclimb.live.crimp.service.CrimpService;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.ArrayList;

/**
 * Created by weizhi on 16/7/2015.
 */
public class ScoreFragment extends CrimpFragment {
    private final String TAG = ScoreFragment.class.getSimpleName();

    private enum State{
        QUERYING(0),
        NOT_QUERYING(1);

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
                    return QUERYING;
                case 1:
                    return NOT_QUERYING;
                default:
                    return null;
            }
        }
    }

    private ScoreFragmentToActivityMethods mToActivityMethod;   //This is how we will communicate with
                                                                //Hello Activity.

    private State mState;
    private SpiceManager spiceManager = new SpiceManager(CrimpService.class);
    private User mUser = null;
    private Categories mCategories = null;
    private Climber mClimber = null;

    // UI references
    private TextView mRouteIdText;
    private EditText mClimberIdEdit;
    private EditText mClimberNameEdit;
    private EditText mAccumulatedEdit;
    private EditText mCurrentSessionEdit;
    private TextView mBText;
    private TextView mTText;
    private Button mPlusOneButton;
    private Button mBonusButton;
    private Button mTopButton;
    private Button mSubmitButton;
    private Button mBackspaceButton;

    public static ScoreFragment newInstance(User user, Categories categories,
                                            Climber climber, Context context) {
        ScoreFragment myFragment = new ScoreFragment();

        Bundle args = new Bundle();
        if(user != null){
            args.putString(context.getString(R.string.bundle_x_user_id), user.getUserId());
            args.putString(context.getString(R.string.bundle_x_auth_token), user.getAuthToken());
            args.putString(context.getString(R.string.bundle_user_name), user.getUserName());
            args.putString(context.getString(R.string.bundle_access_token), user.getFacebookAccessToken());
            args.putString(context.getString(R.string.bundle_category_id), user.getCategoryId());
            args.putString(context.getString(R.string.bundle_route_id), user.getRouteId());
        }

        if(categories != null){
            ArrayList<String> categoryNameList = categories.getCategoryNameList();
            ArrayList<String> categoryIdList = categories.getCategoryIdList();
            ArrayList<Integer> categoryRouteCountList = categories.getCategoryRouteCountList();
            ArrayList<String> routeNameList = categories.getRouteNameList();
            ArrayList<String> routeIdList = categories.getRouteIdList();
            ArrayList<String> routeScoreList = categories.getRouteScoreList();
            byte[] categoryFinalizeArray = categories.getCategoryFinalizeArray();
            ArrayList<String> categoryStartList = categories.getCategoryStartList();
            ArrayList<String> categoryEndList = categories.getCategoryEndList();

            args.putStringArrayList(context.getString(R.string.bundle_category_name_list), categoryNameList);
            args.putStringArrayList(context.getString(R.string.bundle_category_id_list),categoryIdList);
            args.putIntegerArrayList(context.getString(R.string.bundle_category_route_count_list), categoryRouteCountList);
            args.putStringArrayList(context.getString(R.string.bundle_route_name_list), routeNameList);
            args.putStringArrayList(context.getString(R.string.bundle_route_id_list), routeIdList);
            args.putStringArrayList(context.getString(R.string.bundle_route_score_list), routeScoreList);
            args.putByteArray(context.getString(R.string.bundle_category_finalize_list), categoryFinalizeArray);
            args.putStringArrayList(context.getString(R.string.bundle_category_start_list), categoryStartList);
            args.putStringArrayList(context.getString(R.string.bundle_category_end_list), categoryEndList);
        }

        if(climber != null){
            args.putString(context.getString(R.string.bundle_climber_id), climber.getClimberId());
            args.putString(context.getString(R.string.bundle_climber_name), climber.getClimberName());
            args.putString(context.getString(R.string.bundle_total_score), climber.getTotalScore());
        }

        myFragment.setArguments(args);
        return myFragment;
    }



    /*=========================================================================
     * Fragment lifecycle methods
     *=======================================================================*/
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mToActivityMethod = (ScoreFragmentToActivityMethods) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ScoreFragmentToActivityMethods");
        }

        Log.d(TAG, "ScoreFragment onAttach");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_score, container, false);

        mRouteIdText = (TextView) rootView.findViewById(R.id.scoring_route_text);
        mClimberIdEdit = (EditText) rootView.findViewById(R.id.scoring_climber_id_edit);
        mClimberNameEdit = (EditText) rootView.findViewById(R.id.scoring_climber_name_edit);
        mCurrentSessionEdit = (EditText) rootView.findViewById(R.id.scoring_score_current_edit);
        mTText = (TextView) rootView.findViewById(R.id.scoring_t_text);
        mBText = (TextView) rootView.findViewById(R.id.scoring_b_text);
        mPlusOneButton = (Button) rootView.findViewById(R.id.scoring_plus_one_button);
        mBonusButton = (Button) rootView.findViewById(R.id.scoring_b_button);
        mTopButton = (Button) rootView.findViewById(R.id.scoring_t_button);
        mSubmitButton = (Button) rootView.findViewById(R.id.scoring_submit_button);
        mBackspaceButton = (Button) rootView.findViewById(R.id.scoring_backspace_button);
        mAccumulatedEdit = (EditText) rootView.findViewById(R.id.scoring_score_history_edit);

        mBackspaceButton.setOnClickListener(this);
        mBonusButton.setOnClickListener(this);
        mPlusOneButton.setOnClickListener(this);
        mSubmitButton.setOnClickListener(this);
        mTopButton.setOnClickListener(this);

        //mAccumulatedEdit.addTextChangedListener(new AccumulatedScoreTextWatcher());

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState == null){
            // Initialize mState
            mState = State.QUERYING;
            Bundle args = getArguments();

            // Initialize mUser
            if(mUser == null)
                mUser = new User();
            mUser.setUserId(args.getString(getString(R.string.bundle_x_user_id)));
            mUser.setAuthToken(args.getString(getString(R.string.bundle_x_auth_token)));
            mUser.setUserName(args.getString(getString(R.string.bundle_user_name)));
            mUser.setFacebookAccessToken(args.getString(getString(R.string.bundle_access_token)));
            mUser.setCategoryId(args.getString(getString(R.string.bundle_category_id)));
            mUser.setRouteId(args.getString(getString(R.string.bundle_route_id)));

            // Initialize mCategories
            ArrayList<String> cNameList = args.getStringArrayList(getString(R.string.bundle_category_name_list));
            ArrayList<String> cIdList = args.getStringArrayList(getString(R.string.bundle_category_id_list));
            ArrayList<Integer> cCountList = args.getIntegerArrayList(getString(R.string.bundle_category_route_count_list));
            ArrayList<String> rNameList = args.getStringArrayList(getString(R.string.bundle_route_name_list));
            ArrayList<String> rIdList = args.getStringArrayList(getString(R.string.bundle_route_id_list));
            ArrayList<String> rScoreList = args.getStringArrayList(getString(R.string.bundle_route_score_list));
            byte[] cFinalizeArray = args.getByteArray(getString(R.string.bundle_category_finalize_list));
            ArrayList<String> cStartList = args.getStringArrayList(getString(R.string.bundle_category_start_list));
            ArrayList<String> cEndList = args.getStringArrayList(getString(R.string.bundle_category_end_list));
            mCategories = new Categories(cNameList, cIdList, cCountList, rNameList, rIdList,
                    rScoreList, cFinalizeArray, cStartList, cEndList);

            // Initialize mClimber
            if(mClimber == null)
                mClimber = new Climber();
            mClimber.setClimberId(args.getString(getString(R.string.bundle_climber_id)));
            mClimber.setClimberName(args.getString(getString(R.string.bundle_climber_name)));
            mClimber.setTotalScore(args.getString(getString(R.string.bundle_total_score)));
        }
        else{
            mState = State.toEnum(savedInstanceState.getInt(getString(R.string.bundle_score_state)));

            // Initialize mUser
            if(mUser == null)
                mUser = new User();
            mUser.setUserId(savedInstanceState.getString(getString(R.string.bundle_x_user_id)));
            mUser.setAuthToken(savedInstanceState.getString(getString(R.string.bundle_x_auth_token)));
            mUser.setUserName(savedInstanceState.getString(getString(R.string.bundle_user_name)));
            mUser.setFacebookAccessToken(savedInstanceState.getString(getString(R.string.bundle_access_token)));
            mUser.setCategoryId(savedInstanceState.getString(getString(R.string.bundle_category_id)));
            mUser.setRouteId(savedInstanceState.getString(getString(R.string.bundle_route_id)));

            // Initialize mCategories
            ArrayList<String> cNameList = savedInstanceState.getStringArrayList(getString(R.string.bundle_category_name_list));
            ArrayList<String> cIdList = savedInstanceState.getStringArrayList(getString(R.string.bundle_category_id_list));
            ArrayList<Integer> cCountList = savedInstanceState.getIntegerArrayList(getString(R.string.bundle_category_route_count_list));
            ArrayList<String> rNameList = savedInstanceState.getStringArrayList(getString(R.string.bundle_route_name_list));
            ArrayList<String> rIdList = savedInstanceState.getStringArrayList(getString(R.string.bundle_route_id_list));
            ArrayList<String> rScoreList = savedInstanceState.getStringArrayList(getString(R.string.bundle_route_score_list));
            byte[] cFinalizeArray = savedInstanceState.getByteArray(getString(R.string.bundle_category_finalize_list));
            ArrayList<String> cStartList = savedInstanceState.getStringArrayList(getString(R.string.bundle_category_start_list));
            ArrayList<String> cEndList = savedInstanceState.getStringArrayList(getString(R.string.bundle_category_end_list));
            mCategories = new Categories(cNameList, cIdList, cCountList, rNameList, rIdList,
                    rScoreList, cFinalizeArray, cStartList, cEndList);

            // Initialize mClimber
            if(mClimber == null)
                mClimber = new Climber();
            mClimber.setClimberId(savedInstanceState.getString(getString(R.string.bundle_climber_id)));
            mClimber.setClimberName(savedInstanceState.getString(getString(R.string.bundle_climber_name)));
            mClimber.setTotalScore(savedInstanceState.getString(getString(R.string.bundle_total_score)));
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        spiceManager.start(getActivity());
    }

    @Override
    public void onResume(){
        super.onResume();

        /*TODO
        ActiveMonitorRequest mActiveMonitorRequest = new ActiveMonitorRequest(mUser.getUserId(),
                mUser.getAuthToken(), mUser.getCategoryId(), mUser.getRouteId(),
                mClimber.getClimberId(), true, getActivity());
        spiceManager.execute(mActiveMonitorRequest, new ActiveMonitorRequestListener());
        */
        changeState(mState);
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onStop(){
        spiceManager.shouldStop();
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mUser != null){
            outState.putString(getString(R.string.bundle_x_user_id), mUser.getUserId());
            outState.putString(getString(R.string.bundle_x_auth_token), mUser.getAuthToken());
            outState.putString(getString(R.string.bundle_user_name), mUser.getUserName());
            outState.putString(getString(R.string.bundle_access_token), mUser.getFacebookAccessToken());
            outState.putString(getString(R.string.bundle_category_id), mUser.getCategoryId());
            outState.putString(getString(R.string.bundle_route_id), mUser.getRouteId());
        }

        if(mCategories != null){
            ArrayList<String> categoryNameList = mCategories.getCategoryNameList();
            ArrayList<String> categoryIdList = mCategories.getCategoryIdList();
            ArrayList<Integer> categoryRouteCountList = mCategories.getCategoryRouteCountList();
            ArrayList<String> routeNameList = mCategories.getRouteNameList();
            ArrayList<String> routeIdList = mCategories.getRouteIdList();
            ArrayList<String> routeScoreList = mCategories.getRouteScoreList();
            byte[] categoryFinalizeArray = mCategories.getCategoryFinalizeArray();
            ArrayList<String> categoryStartList = mCategories.getCategoryStartList();
            ArrayList<String> categoryEndList = mCategories.getCategoryEndList();

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

        if(mClimber != null){
            outState.putString(getString(R.string.bundle_climber_id), mClimber.getClimberId());
            outState.putString(getString(R.string.bundle_climber_name), mClimber.getClimberName());
            outState.putString(getString(R.string.bundle_total_score), mClimber.getTotalScore());
        }

        outState.putInt(getString(R.string.bundle_score_state), mState.getValue());

        Log.d(TAG, "ScoreFragment onSaveInstance");
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "ScoreFragment onDetach");
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
            case QUERYING:
                mRouteIdText.setText(mUser.getRouteId());
                mClimberIdEdit.setText(mClimber.getClimberId());
                if(mClimber.getClimberName()==null)
                    mClimberNameEdit.setText(null);
                else
                    mClimberNameEdit.setText(mClimber.getClimberName());
                mAccumulatedEdit.setText(null);
                //Don't touch mCurrentSessionEdit.
                mBText.setText("-");
                mTText.setText("-");
                break;
            case NOT_QUERYING:
                mRouteIdText.setText(mUser.getRouteId());
                mClimberIdEdit.setText(mClimber.getClimberId());
                mClimberNameEdit.setText(mClimber.getClimberName());
                mAccumulatedEdit.setText(mClimber.getTotalScore());
                //Don't touch mCurrentSessionEdit.
                //mBText.setText("-");
                //mTText.setText("-");
                calculateAndUpdateBT();
                break;
            default:
                break;
        }
    }

    /**
     * Method to control what is performed at different state.
     */
    private void doWork(){
        switch (mState){
            case QUERYING:
                //Send query request
                break;
            case NOT_QUERYING:
                break;
            default:
                break;
        }
    }















    /**
     * RequestListener for receiving response of active climber request.
     *
     * @author Lin Weizhi (ecc.weizhi@gmail.com)
     */
    private class ActiveMonitorRequestListener implements RequestListener<ActiveMonitorResponseBody> {
        private final String TAG = ActiveMonitorRequestListener.class.getSimpleName();

        @Override
        public void onRequestFailure(SpiceException e) {
            Log.i(TAG+".onRequestFailure()", "fail");
        }

        @Override
        public void onRequestSuccess(ActiveMonitorResponseBody result) {
            Log.i(TAG+".onRequestSuccess()", "success");
        }
    }

    /**
     * RequestListener for receiving response of get score request.
     *
     * @author Lin Weizhi (ecc.weizhi@gmail.com)
     */
    private class GetScoreRequestListener implements RequestListener<GetScoreResponseBody> {
        private final String TAG = GetScoreRequestListener.class.getSimpleName();

        @Override
        public void onRequestFailure(SpiceException e) {
            Log.i(TAG+".onRequestFailure()", "fail");
        }

        @Override
        public void onRequestSuccess(GetScoreResponseBody result) {
            Log.i(TAG + ".onRequestSuccess()", "Received score for cid:"+result.getClimberId());

            if(result.getClimberId().compareTo(mClimber.getClimberId()) == 0){
                mClimber.setTotalScore(result.getScoreString());
                mClimber.setClimberName(result.getClimberName());
            }
            else{
                Log.e(TAG+".onRequestSuccess()", result.getClimberId()+" != "+mClimberIdEdit.getText().toString());
            }
        }
    }


    private void calculateAndUpdateBT(){

    }








    @Override
    public CharSequence getPageTitle() {
        return "Score";
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.scoring_b_button:
                break;
            case R.id.scoring_backspace_button:
                break;
            case R.id.scoring_submit_button:
                break;
            case R.id.scoring_t_button:
                break;
            case R.id.scoring_plus_one_button:
                break;
        }
    }



    /*=========================================================================
     * Button press methods
     *=======================================================================*/
    public void plusOne(){
        mCurrentSessionEdit.append("1");

        mBackspaceButton.setEnabled(true);
        calculateAndUpdateBT();
    }

    public void bonus(){
        mCurrentSessionEdit.append("B");

        mBonusButton.setEnabled(false);
        mBackspaceButton.setEnabled(true);
        calculateAndUpdateBT();
    }

    public void top(){
        mCurrentSessionEdit.append("T");
        mBackspaceButton.setEnabled(true);
        mPlusOneButton.setEnabled(false);
        mBonusButton.setEnabled(false);
        mTopButton.setEnabled(false);

        calculateAndUpdateBT();
    }

    public void backspace(){
        String currentSessionScore = mCurrentSessionEdit.getText().toString();

        if(currentSessionScore.length() == 1)
            mBackspaceButton.setEnabled(false);

        if(currentSessionScore.length() > 0){
            char lastChar = currentSessionScore.charAt(currentSessionScore.length()-1);
            if( lastChar == 'B'){
                mBonusButton.setEnabled(true);
            }
            else if(lastChar == 'T'){
                mTopButton.setEnabled(true);
                mPlusOneButton.setEnabled(true);
                if(!currentSessionScore.contains("B")){
                    mBonusButton.setEnabled(true);
                }
            }
            mCurrentSessionEdit.getText().delete(mCurrentSessionEdit.getText().length()-1, mCurrentSessionEdit.getText().length());
        }

        calculateAndUpdateBT();
    }

    public void submit(){
        new AlertDialog.Builder(getActivity())
                .setTitle("Submit score")
                .setMessage("Are you sure you want to submit score?")
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Do stuff
                        // Make QueueObject
                        QueueObject mQueueObject = new QueueObject(mUser.getUserId(),
                                mUser.getAuthToken(),
                                mUser.getCategoryId(),
                                mUser.getRouteId(),
                                mClimber.getClimberId(),
                                mCurrentSessionEdit.getText().toString(),
                                CrimpService.nextRequestId(),
                                getActivity());

                        // Add to a queue of QueueObject request.
                        ((CrimpApplication)getActivity().getApplicationContext()).addRequest(mQueueObject);

                        //TODO destroy this fragment

                    }
                })
                .setNegativeButton("Don\'t submit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
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
    public interface ScoreFragmentToActivityMethods {
        //public void createAndSwitchToScanFragment(User user, Categories categoriesInfo);
        //public void destroyOtherTabButRoute();
    }




}
