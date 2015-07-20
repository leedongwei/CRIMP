package com.nusclimb.live.crimp.hello;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.nusclimb.live.crimp.CrimpApplication;
import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.common.BusProvider;
import com.nusclimb.live.crimp.common.QueueObject;
import com.nusclimb.live.crimp.common.busevent.AccumulatedScoreChange;
import com.nusclimb.live.crimp.common.busevent.InScoreTab;
import com.nusclimb.live.crimp.common.busevent.ScoreFinish;
import com.nusclimb.live.crimp.common.busevent.ScoreOnResume;
import com.nusclimb.live.crimp.common.json.ActiveClimbersResponse;
import com.nusclimb.live.crimp.common.json.Category;
import com.nusclimb.live.crimp.common.json.Climber;
import com.nusclimb.live.crimp.common.json.ClimbersResponse;
import com.nusclimb.live.crimp.common.json.GetScoreResponse;
import com.nusclimb.live.crimp.common.spicerequest.ClimbersRequest;
import com.nusclimb.live.crimp.common.spicerequest.GetScoreRequest;
import com.nusclimb.live.crimp.service.CrimpService;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by weizhi on 16/7/2015.
 */
public class ScoreFragment extends Fragment {
    private final String TAG = ScoreFragment.class.getSimpleName();

    private boolean isTBStart;

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

    // RoboSpice info
    private SpiceManager spiceManager = new SpiceManager(CrimpService.class);


    /**
     * RequestListener for receiving response of active climber request.
     *
     * @author Lin Weizhi (ecc.weizhi@gmail.com)
     */
    private class ActiveClimbersRequestListener implements RequestListener<ActiveClimbersResponse> {
        private final String TAG = ActiveClimbersRequestListener.class.getSimpleName();

        @Override
        public void onRequestFailure(SpiceException e) {
            Log.i(TAG+".onRequestFailure()", "fail");
        }

        @Override
        public void onRequestSuccess(ActiveClimbersResponse result) {
            Log.i(TAG+".onRequestSuccess()", "success");
        }
    }

    /**
     * RequestListener for receiving response of get score request.
     *
     * @author Lin Weizhi (ecc.weizhi@gmail.com)
     */
    private class GetScoreRequestListener implements RequestListener<GetScoreResponse> {
        private final String TAG = GetScoreRequestListener.class.getSimpleName();

        @Override
        public void onRequestFailure(SpiceException e) {
            Log.i(TAG+".onRequestFailure()", "fail");
        }

        @Override
        public void onRequestSuccess(GetScoreResponse result) {
            Log.i(TAG + ".onRequestSuccess()", "Received score for cid:"+result.getClimberId());

            if(result.getClimberId().compareTo(mClimberIdEdit.getText().toString()) == 0) {
                mAccumulatedEdit.setText(result.getScore());

                if(mClimberNameEdit.getText().length() == 0)
                    mClimberNameEdit.setText(result.getClimberName());
            }
            else{
                Log.e(TAG+".onRequestSuccess()", result.getClimberId()+" != "+mClimberIdEdit.getText().toString());
            }
        }
    }

    @Subscribe
    public void onReceiveInScoreTab(InScoreTab event){
        Log.d(TAG + ".onReceiveInScoreTab()", "Received InScoreTab event.");

        // Update UI
        String cid = ((HelloActivity)getActivity()).getRouteId().substring(0,3)+
                ((HelloActivity)getActivity()).getClimberId();
        mClimberIdEdit.setText(cid);
        String climberName = ((HelloActivity)getActivity()).getClimberName();
        if(climberName!= null && climberName.length()>0){
            mClimberNameEdit.setText(climberName);
        }

        // Make a request to get climber score
        GetScoreRequest mGetScoreRequest = new GetScoreRequest(((HelloActivity) getActivity()).getxUserId(),
                ((HelloActivity) getActivity()).getxAuthToken(),
                ((HelloActivity) getActivity()).getRouteId(),
                mClimberIdEdit.getText().toString(),
                getActivity());
        spiceManager.execute(mGetScoreRequest, mGetScoreRequest.createCacheKey(),
                DurationInMillis.ALWAYS_EXPIRED,
                new GetScoreRequestListener());

        // Update title
        String rid = ((HelloActivity) getActivity()).getRouteId();
        String categoryId = rid.substring(0, 3);
        List<SpinnerItem> categoryList = ((HelloActivity) getActivity()).getCategoryList();

        String categoryFullName = null;
        String routeFullName = null;
        boolean isTitleOk = false;
        int i=0;
        while(!isTitleOk && i<categoryList.size()){
            CategorySpinnerItem s = (CategorySpinnerItem) categoryList.get(i);

            if( s.getCategoryId().compareTo(categoryId) == 0 ){
                categoryFullName = s.getItemString();
                routeFullName = "Route "+rid.substring(3, rid.length());
                isTitleOk = true;
            }
        }

        if(isTitleOk){
            mRouteIdText.setText(categoryFullName + "\n" + routeFullName);
        }
        else{
            Log.e(TAG+".onReceiveInScoreTab()", "Cannot find route full name");
        }
    }

    @Subscribe
    public void onReceiveAccumulatedScoreChange(AccumulatedScoreChange event){
        isTBStart = true;
    }

    private void updateTB(){
        if(isTBStart){
            int attempts = 0;
            int bonus = 0;
            int top = 0;

            // Traverse accumulated
            Editable scores = mAccumulatedEdit.getText();
            for(int i=0; i<scores.length(); i++){
                char score = scores.charAt(i);

                switch(score){
                    case '1':
                        attempts++;
                        break;
                    case 'B':
                        attempts++;
                        if(bonus == 0)
                            bonus = attempts;
                        break;
                    case 'T':
                        attempts++;
                        top = attempts;
                        if(bonus == 0)
                            bonus = attempts;
                        break;
                }
            }

            // Traverse current
            // TODO
            scores = mCurrentSessionEdit.getText();
            for(int i=0; i<scores.length(); i++){
                char score = scores.charAt(i);

                switch(score){
                    case '1':
                        attempts++;
                        break;
                    case 'B':
                        attempts++;
                        if(bonus == 0)
                            bonus = attempts;
                        break;
                    case 'T':
                        attempts++;
                        top = attempts;
                        if(bonus == 0)
                            bonus = attempts;
                        break;
                }
            }

            if(bonus == 0)
                mBText.setText("-");
            else
                mBText.setText(""+bonus);

            if(top == 0)
                mTText.setText("-");
            else
                mTText.setText(""+top);

        }
        else{
            mTText.setText("-");
            mBText.setText("-");
        }
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

        mAccumulatedEdit.addTextChangedListener(new AccumulatedScoreTextWatcher());

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);


    }

    @Override
    public void onStart(){
        super.onStart();
        spiceManager.start(getActivity());
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.d(TAG + ".onResume()", "Send ScoreOnResume event to bus.");

        // Register bus
        BusProvider.getInstance().register(this);
        // Tell the world we are onResume.
        BusProvider.getInstance().post(new ScoreOnResume());
    }

    @Override
    public void onPause(){
        BusProvider.getInstance().unregister(this);
        super.onPause();
    }

    @Override
    public void onStop(){
        spiceManager.shouldStop();
        super.onStop();
    }

    /*=========================================================================
     * Button press methods
     *=======================================================================*/
    public void plusOne(){
        mCurrentSessionEdit.append("1");

        mBackspaceButton.setEnabled(true);
        updateTB();
    }

    public void bonus(){
        mCurrentSessionEdit.append("B");

        mBonusButton.setEnabled(false);
        mBackspaceButton.setEnabled(true);
        updateTB();
    }

    public void top(){
        mCurrentSessionEdit.append("T");
        mBackspaceButton.setEnabled(true);
        mPlusOneButton.setEnabled(false);
        mBonusButton.setEnabled(false);
        mTopButton.setEnabled(false);

        updateTB();
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

        updateTB();
    }

    public void submit(){
        // Make QueueObject
        QueueObject mQueueObject = new QueueObject(((HelloActivity)getActivity()).getxUserId(),
                ((HelloActivity)getActivity()).getxAuthToken(),
                ((HelloActivity)getActivity()).getRouteId(),
                mClimberIdEdit.getText().toString(),
                mCurrentSessionEdit.getText().toString(),
                CrimpService.nextRequestId(),
                getActivity());

        // Add to a queue of QueueObject request.
        ((CrimpApplication)getActivity().getApplicationContext()).addRequest(mQueueObject);

        // Navigate up from this activity.
        BusProvider.getInstance().post(new ScoreFinish());
    }




}
