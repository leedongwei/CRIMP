package com.nusclimb.live.crimp.scoremodule;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.nusclimb.live.crimp.R;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class TopBonus2Scoring extends Fragment implements View.OnClickListener{
    private final String TAG = TopBonus2Scoring.class.getSimpleName();

    private ScoringModuleToFragmentMethods mParentFragment;

    private TextView mBestResult;
    private Button mBonusOneButton;
    private Button mBonusTwoButton;
    private Button mTopButton;
    private Button mBackspaceButton;

    public static TopBonus2Scoring newInstance(Context context){
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
        View rootView = inflater.inflate(R.layout.fragment_top_bonus_scoring, container, false);

        /*
        mTText = (TextView) rootView.findViewById(R.id.scoring_t_text);
        mBText = (TextView) rootView.findViewById(R.id.scoring_b_text);
        mPlusOneButton = (Button) rootView.findViewById(R.id.scoring_plus_one_button);
        mBonusButton = (Button) rootView.findViewById(R.id.scoring_b_button);
        mTopButton = (Button) rootView.findViewById(R.id.scoring_t_button);
        mSubmitButton = (Button) rootView.findViewById(R.id.scoring_submit_button);
        mBackspaceButton = (Button) rootView.findViewById(R.id.scoring_backspace_button);

        mBackspaceButton.setOnClickListener(this);
        mBonusButton.setOnClickListener(this);
        mPlusOneButton.setOnClickListener(this);
        mTopButton.setOnClickListener(this);

        Log.d(TAG, "onCreateView");
*/
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
        /*
        switch(v.getId()){
            case R.id.scoring_b_button:
                mParentFragment.appendStringToAccumulated("B");
                mBonusButton.setEnabled(false);
                mBackspaceButton.setEnabled(true);
                break;
            case R.id.scoring_backspace_button:
                break;
            case R.id.scoring_t_button:
                mParentFragment.appendStringToAccumulated("T");
                mBackspaceButton.setEnabled(true);
                mPlusOneButton.setEnabled(false);
                mBonusButton.setEnabled(false);
                mTopButton.setEnabled(false);
                break;
            case R.id.scoring_plus_one_button:
                mParentFragment.appendStringToAccumulated("1");
                mBackspaceButton.setEnabled(true);
                break;
        }
        */
    }
}
