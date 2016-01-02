package com.nusclimb.live.crimp.hello.score;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.common.Climber;
import com.nusclimb.live.crimp.common.User;
import com.nusclimb.live.crimp.common.json.ActiveMonitorResponseBody;
import com.nusclimb.live.crimp.common.json.GetScoreResponseBody;
import com.nusclimb.live.crimp.common.spicerequest.ActiveMonitorRequest;
import com.nusclimb.live.crimp.common.spicerequest.GetScoreRequest;
import com.nusclimb.live.crimp.hello.HelloActivityFragment;
import com.nusclimb.live.crimp.hello.score.scoremodule.ScoringModule;
import com.nusclimb.live.crimp.hello.score.scoremodule.ScoringModuleToFragmentMethods;
import com.nusclimb.live.crimp.hello.score.scoremodule.TopBonus2Scoring;
import com.nusclimb.live.crimp.hello.score.scoremodule.TopBonusScoring;
import com.nusclimb.live.crimp.service.CrimpService;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

/**
 * Fragment for assigning scores to climber. Activity containing this Fragment must
 * implement ScoreFragmentToActivityMethods interface to allow this fragment to communicate
 * with the attached Activity and possibly other Fragments. Information from the Activity is
 * passed to this Fragment through arguments.
 *
 * This class must contain a child fragment ScoringModule which decides how the scoring is calculated.
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class ScoreFragment extends HelloActivityFragment implements ScoringModuleToFragmentMethods {
    private final String TAG = ScoreFragment.class.getSimpleName();
    private final boolean DEBUG = false;

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
    private Button mSubmitButton;

    public static ScoreFragment newInstance() {
        return new ScoreFragment();
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

        if (DEBUG) Log.d(TAG, "ScoreFragment onAttach");
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
        mSubmitButton = (Button) rootView.findViewById(R.id.scoring_submit_button);

        mSubmitButton.setOnClickListener(this);

        if (DEBUG) Log.d(TAG, "onCreateView");
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

        String scoreType = mToActivityMethod.getScoringType();
        if(scoreType!=null){
            switch (scoreType){
                case "0":
                    mScoringModule = TopBonusScoring.newInstance();
                    break;
                case "1":
                    mScoringModule = TopBonus2Scoring.newInstance();
                    break;
            }

            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.scoring_score_fragment, mScoringModule).commit();
        }
        else{
            Log.e(TAG, "Cannot find score type of selected route");
        }
        if (DEBUG) Log.d(TAG, "onActivityCreated");
    }

    @Override
    public void onStart(){
        super.onStart();
        spiceManager.start(getActivity());
        if (DEBUG) Log.d(TAG, "onStart");
    }

    @Override
    public void onResume(){
        super.onResume();
        if (DEBUG) Log.d(TAG, "onResume");
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
        if (DEBUG) Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    public void onStop(){
        if (DEBUG) Log.d(TAG, "onStop");
        spiceManager.shouldStop();
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(getString(R.string.bundle_score_state), mState.getValue());

        if (DEBUG) Log.d(TAG, "ScoreFragment onSaveInstance");
    }

    @Override
    public void onDetach() {
        if (DEBUG) Log.d(TAG, "ScoreFragment onDetach");
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
        if (DEBUG) Log.d(TAG + ".changeState()", mState + " -> " + state);

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
                break;
            case NOT_QUERYING:
                userFromActivity = mToActivityMethod.getUser();
                climberFromActivity = mToActivityMethod.getClimber();
                mRouteIdText.setText(userFromActivity.getRouteId());
                mClimberIdEdit.setText(climberFromActivity.getClimberId());
                mClimberNameEdit.setText(climberFromActivity.getClimberName());
                mAccumulatedEdit.setText(climberFromActivity.getTotalScore());
                //Don't touch mCurrentSessionEdit.
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



    /*=========================================================================
     * SpiceRequest Listener class
     *=======================================================================*/
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



    /*=========================================================================
     * Other methods
     *=======================================================================*/
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.scoring_submit_button:
                submit();
                break;
        }
    }

    @Override
    public void onNavigateAway(){
        if(DEBUG) Log.d(TAG, "NavigateAway");
        mToActivityMethod.resetClimber();
    }

    @Override
    public void onNavigateTo(){
        if(DEBUG) Log.d(TAG, "NavigateTo");
        mRouteIdText.setText(null);
        mClimberIdEdit.setText(null);
        mClimberNameEdit.setText(null);
        mAccumulatedEdit.setText(null);
        mCurrentSessionEdit.setText(null);
    }

    @Override
    public void appendStringToAccumulated(String s){
        mCurrentSessionEdit.append(s);
    }

    @Override
    public void backspaceAccumulated(int numberOfCharacters){
        String current = mCurrentSessionEdit.getText().toString();
        if(current.length() >= numberOfCharacters){
            current = current.substring(0, current.length()-numberOfCharacters);
            mCurrentSessionEdit.setText(current);
        }
    }

    private void submit(){
        new AlertDialog.Builder(getActivity())
                .setTitle("Submit score")
                .setMessage("Are you sure you want to submit score?")
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Do stuff
                        mToActivityMethod.onSubmit(mCurrentSessionEdit.getText().toString());

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
        String getScoringType();
        void onSubmit(String currentScore);
        void saveScoreInstance(Bundle bundle);
        Bundle restoreScoreInstance();
        void resetClimber();
    }
}
