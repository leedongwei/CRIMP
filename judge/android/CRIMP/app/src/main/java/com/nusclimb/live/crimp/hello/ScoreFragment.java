package com.nusclimb.live.crimp.hello;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.common.BusProvider;
import com.nusclimb.live.crimp.common.busevent.ScoreOnResume;

/**
 * Created by weizhi on 16/7/2015.
 */
public class ScoreFragment extends Fragment {
    private final String TAG = ScoreFragment.class.getSimpleName();

    // UI references
    private TextView mRouteIdText;
    private EditText mClimberIdEdit;
    private EditText mClimberNameEdit;
    private EditText mAccumulatedEdit;
    private EditText mCurrentSessionEdit;
    private TextView mTBText;
    private Button mPlusOneButton;
    private Button mBonusButton;
    private Button mTopButton;
    private Button mSubmitButton;
    private Button mBackspaceButton;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_score, container, false);

        mRouteIdText = (TextView) rootView.findViewById(R.id.scoring_route_text);
        mClimberIdEdit = (EditText) rootView.findViewById(R.id.scoring_climber_id_edit);
        mClimberNameEdit = (EditText) rootView.findViewById(R.id.scoring_climber_name_edit);
        mCurrentSessionEdit = (EditText) rootView.findViewById(R.id.scoring_score_current_edit);
        mTBText = (TextView) rootView.findViewById(R.id.scoring_t_b_text);
        mPlusOneButton = (Button) rootView.findViewById(R.id.scoring_plus_one_button);
        mBonusButton = (Button) rootView.findViewById(R.id.scoring_b_button);
        mTopButton = (Button) rootView.findViewById(R.id.scoring_t_button);
        mSubmitButton = (Button) rootView.findViewById(R.id.scoring_submit_button);
        mBackspaceButton = (Button) rootView.findViewById(R.id.scoring_backspace_button);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);


    }

    @Override
    public void onResume(){
        super.onResume();
        Log.d(TAG + ".onResume()", "Send ScoreOnResume event to bus.");

        BusProvider.getInstance().post(new ScoreOnResume());
    }

    /*=========================================================================
     * Button press methods
     *=======================================================================*/
    public void plusOne(){
        mCurrentSessionEdit.append("1");

        mBackspaceButton.setEnabled(true);
    }

    public void bonus(){
        mCurrentSessionEdit.append("B");

        mBonusButton.setEnabled(false);
        mBackspaceButton.setEnabled(true);
    }

    public void top(){
        mCurrentSessionEdit.append("T");
        mBackspaceButton.setEnabled(true);
        mPlusOneButton.setEnabled(false);
        mBonusButton.setEnabled(false);
        mTopButton.setEnabled(false);


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
    }

    public void submit(){

    }




}
