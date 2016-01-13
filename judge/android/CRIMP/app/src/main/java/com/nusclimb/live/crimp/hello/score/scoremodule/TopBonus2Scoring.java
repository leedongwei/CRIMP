package com.nusclimb.live.crimp.hello.score.scoremodule;

import android.app.Activity;
import android.content.Context;
import android.media.Image;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.nusclimb.live.crimp.R;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class TopBonus2Scoring extends ScoringModule implements View.OnClickListener{
    private final String TAG = TopBonus2Scoring.class.getSimpleName();

    private ScoringModuleToFragmentMethods mParentFragment;

    private TextView mBestResult;
    private Button mBonusOneButton;
    private Button mBonusTwoButton;
    private Button mTopButton;
    private ImageButton mBackspaceButton;

    private String currentScore = null;
    private String accumulatedScore = null;

    public static TopBonus2Scoring newInstance(){
        Log.d("TopBonus2Scoring", "newInstance");
        TopBonus2Scoring myFragment = new TopBonus2Scoring();

        Bundle args = new Bundle();

        myFragment.setArguments(args);
        return myFragment;
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        Log.d(TAG, "onAttach");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try {
            mParentFragment = (ScoringModuleToFragmentMethods) getParentFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("parentFragment must implement ScoreFragmentToActivityMethods");
        }

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_top_bonus2_scoring, container, false);

        mBestResult = (TextView) rootView.findViewById(R.id.scoring_best_result_text);
        mBonusOneButton = (Button) rootView.findViewById(R.id.scoring_b1_button);
        mBonusTwoButton = (Button) rootView.findViewById(R.id.scoring_b2_button);
        mTopButton = (Button) rootView.findViewById(R.id.scoring_t_button);
        mBackspaceButton = (ImageButton) rootView.findViewById(R.id.scoring_backspace_button);

        mBonusOneButton.setOnClickListener(this);
        mBonusTwoButton.setOnClickListener(this);
        mTopButton.setOnClickListener(this);
        mBackspaceButton.setOnClickListener(this);

        Log.d(TAG, "onCreateView");

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated");
    }

    @Override
    public void onStart(){
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    public void onPause(){
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    public void onStop(){
        Log.d(TAG, "onStop");
        super.onStop();
    }
    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState");
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach");
        super.onDetach();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.scoring_b1_button:
                mParentFragment.appendStringToAccumulated("B1");
                mBonusOneButton.setEnabled(false);
                mBackspaceButton.setEnabled(true);
                calculateScore("B1");
                break;
            case R.id.scoring_b2_button:
                mParentFragment.appendStringToAccumulated("B2");
                mBonusOneButton.setEnabled(false);
                mBonusTwoButton.setEnabled(false);
                mBackspaceButton.setEnabled(true);
                calculateScore("B2");
                break;
            case R.id.scoring_t_button:
                mParentFragment.appendStringToAccumulated("T ");
                mBonusOneButton.setEnabled(false);
                mBonusTwoButton.setEnabled(false);
                mTopButton.setEnabled(false);
                mBackspaceButton.setEnabled(true);
                calculateScore("T ");
                break;
            case R.id.scoring_backspace_button:
                currentScore = currentScore.substring(0, currentScore.length()-2);
                String scoreString = accumulatedScore + currentScore;
                if(scoreString.indexOf('T') != -1){
                    mBonusOneButton.setEnabled(false);
                    mBonusTwoButton.setEnabled(false);
                    mTopButton.setEnabled(false);
                }
                else if(scoreString.contains("B2")){
                    mBonusOneButton.setEnabled(false);
                    mBonusTwoButton.setEnabled(false);
                    mTopButton.setEnabled(true);
                }
                else if(scoreString.contains("B1")){
                    mBonusOneButton.setEnabled(false);
                    mBonusTwoButton.setEnabled(true);
                    mTopButton.setEnabled(true);
                }
                else{   //This is not possible
                    mBonusOneButton.setEnabled(true);
                    mBonusTwoButton.setEnabled(true);
                    mTopButton.setEnabled(true);
                }
                if(currentScore.length()==0)
                    mBackspaceButton.setEnabled(false);
                mParentFragment.backspaceAccumulated(2);
                calculateScore("");
                break;
        }
        // Get instance of Vibrator from current Context
        Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);

        // Vibrate for 50 milliseconds
        vibrator.vibrate(50);
    }

    @Override
    public void onScoreChange(String accumulatedScore, String currentScore) {
        this.accumulatedScore = accumulatedScore;
        this.currentScore = currentScore;
        calculateScore("");
    }

    private void calculateScore(String append){
        Log.d(TAG, "["+accumulatedScore+"]["+currentScore+"]["+append+"]");
        currentScore = currentScore + append;
        String scoreString = accumulatedScore + currentScore;
        if(scoreString.indexOf('T') != -1){
            mBestResult.setText("Top");
        }
        else if(scoreString.contains("B2")){
            mBestResult.setText("Bonus 2");
        }
        else if(scoreString.contains("B1")){
            mBestResult.setText("Bonus 1");
        }
        else{
            mBestResult.setText("-");
        }
    }
}
