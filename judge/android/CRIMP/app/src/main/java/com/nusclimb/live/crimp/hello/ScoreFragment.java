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
import com.nusclimb.live.crimp.common.spicerequest.GetScoreRequest;
import com.nusclimb.live.crimp.scoremodule.ScoringModule;
import com.nusclimb.live.crimp.scoremodule.ScoringModuleToFragmentMethods;
import com.nusclimb.live.crimp.scoremodule.TopBonusScoring;
import com.nusclimb.live.crimp.service.CrimpService;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.ArrayList;

/**
 * Created by weizhi on 16/7/2015.
 */
public class ScoreFragment extends CrimpFragment implements ScoringModuleToFragmentMethods {
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
    private ScoringModule mScoringModule;

    // UI references
    private TextView mRouteIdText;
    private EditText mClimberIdEdit;
    private EditText mClimberNameEdit;
    private EditText mAccumulatedEdit;
    private EditText mCurrentSessionEdit;

    public static ScoreFragment newInstance() {
        Log.d("ScoreFragment", "newInstance");
        ScoreFragment myFragment = new ScoreFragment();

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
        mAccumulatedEdit = (EditText) rootView.findViewById(R.id.scoring_score_history_edit);

        //mAccumulatedEdit.addTextChangedListener(new AccumulatedScoreTextWatcher());

        Log.d(TAG, "onCreateView");
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState == null){
            // Initialize mState
            mState = State.QUERYING;
        }
        else{
            mState = State.toEnum(savedInstanceState.getInt(getString(R.string.bundle_score_state)));
        }

        mScoringModule = TopBonusScoring.newInstance(this.getActivity());
        getChildFragmentManager().beginTransaction().replace(R.id.scoring_score_fragment, mScoringModule).commit();

        Log.d(TAG, "onActivityCreated");
    }

    @Override
    public void onStart(){
        super.onStart();
        spiceManager.start(getActivity());
        Log.d(TAG, "onStart");
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.d(TAG, "onResume");
        User userFromActivity = mToActivityMethod.getUser();
        Climber climberFromActivity = mToActivityMethod.getClimber();
        String climberIdFromActivity = climberFromActivity.getClimberId();
        ActiveMonitorRequest mActiveMonitorRequest = new ActiveMonitorRequest(userFromActivity.getUserId(),
                userFromActivity.getAuthToken(), userFromActivity.getCategoryId(), userFromActivity.getRouteId(),
                climberIdFromActivity, true, getActivity());
        spiceManager.execute(mActiveMonitorRequest, new ActiveMonitorRequestListener());
        changeState(mState);
    }

    @Override
    public void onPause(){
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    public void onStop(){
        Log.d(TAG, "onStop");
        spiceManager.shouldStop();
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
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
        User userFromActivity;
        Climber climberFromActivity;
        switch (mState){
            case QUERYING:
                userFromActivity = mToActivityMethod.getUser();
                climberFromActivity = mToActivityMethod.getClimber();
                mRouteIdText.setText(userFromActivity.getRouteId());
                mClimberIdEdit.setText(climberFromActivity.getClimberId());
                if(climberFromActivity.getClimberName()==null)
                    mClimberNameEdit.setText(null);
                else
                    mClimberNameEdit.setText(climberFromActivity.getClimberName());
                mAccumulatedEdit.setText(null);
                //Don't touch mCurrentSessionEdit.
                //mBText.setText("-");
                //mTText.setText("-");
                break;
            case NOT_QUERYING:
                userFromActivity = mToActivityMethod.getUser();
                climberFromActivity = mToActivityMethod.getClimber();
                mRouteIdText.setText(userFromActivity.getRouteId());
                mClimberIdEdit.setText(climberFromActivity.getClimberId());
                mClimberNameEdit.setText(climberFromActivity.getClimberName());
                mAccumulatedEdit.setText(climberFromActivity.getTotalScore());
                //Don't touch mCurrentSessionEdit.
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
                GetScoreRequest mGetScoreRequest = new GetScoreRequest(mToActivityMethod.getUser().getUserId(),
                        mToActivityMethod.getUser().getAuthToken(), mToActivityMethod.getUser().getCategoryId(), mToActivityMethod.getUser().getRouteId(),
                        mToActivityMethod.getClimber().getClimberId(), getActivity());
                spiceManager.execute(mGetScoreRequest, new GetScoreRequestListener());
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

            if(result.getClimberId().compareTo(mToActivityMethod.getClimber().getClimberId()) == 0){
                mToActivityMethod.updateClimberInfo(result.getClimberName(), result.getScoreString());
                mScoringModule.onScoreChange(result.getScoreString(), mCurrentSessionEdit.getText().toString());
                changeState(State.NOT_QUERYING);
            }
            else{
                Log.e(TAG+".onRequestSuccess()", result.getClimberId()+" != "+mClimberIdEdit.getText().toString());
            }
        }
    }


    private void calculateAndUpdateBT(){

    }



    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.scoring_submit_button:
                break;
        }
    }




    @Override
    public CharSequence getPageTitle() {
        return "Score";
    }

    @Override
    public void restart(){
        mCurrentSessionEdit.setText(null);
        changeState(State.QUERYING);
    }

    @Override
    public void appendStringToAccumulated(String s){
        mCurrentSessionEdit.append(s);
    }


    /*=========================================================================
     * Button press methods
     *=======================================================================*/
    /*
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
    */

    public void submit(){
        new AlertDialog.Builder(getActivity())
                .setTitle("Submit score")
                .setMessage("Are you sure you want to submit score?")
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Do stuff
                        // Make QueueObject
                        QueueObject mQueueObject = new QueueObject(mToActivityMethod.getUser().getUserId(),
                                mToActivityMethod.getUser().getAuthToken(),
                                mToActivityMethod.getUser().getCategoryId(),
                                mToActivityMethod.getUser().getRouteId(),
                                mToActivityMethod.getClimber().getClimberId(),
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
        User getUser();
        Climber getClimber();
        void updateClimberInfo(String climberName, String totalScore);
    }




}
