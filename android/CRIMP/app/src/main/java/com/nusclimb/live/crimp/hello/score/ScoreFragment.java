package com.nusclimb.live.crimp.hello.score;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.nusclimb.live.crimp.R;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class ScoreFragment extends Fragment {
    public static final String TAG = "ScoreFragment";
    public static final boolean DEBUG = true;

    public static final String ARGS_POSITION = "INT_POSITION";
    public static final String ARGS_TITLE = "STRING_TITLE";

    private TextView mCategoryText;
    private TextView mRouteText;
    private EditText mClimberIdText;
    private EditText mClimberNameText;
    private EditText mAccumulatedText;
    private EditText mCurrentText;
    private ViewStub mScoreModuleLayout;
    private Button mSubmitButton;

    public static ScoreFragment newInstance(int position, String title){
        ScoreFragment f = new ScoreFragment();
        // TODO set arguments
        Bundle args = new Bundle();
        args.putInt(ARGS_POSITION, position);
        args.putString(ARGS_TITLE, title);
        f.setArguments(args);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_score, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        mCategoryText = (TextView)view.findViewById(R.id.score_category_text);
        mRouteText = (TextView)view.findViewById(R.id.score_route_text);
        mClimberIdText = (EditText)view.findViewById(R.id.score_climberId_edit);
        mClimberNameText = (EditText)view.findViewById(R.id.score_climberName_edit);
        mAccumulatedText = (EditText)view.findViewById(R.id.score_accumulated_edit);
        mCurrentText = (EditText)view.findViewById(R.id.score_current_edit);
        mScoreModuleLayout = (ViewStub)view.findViewById(R.id.score_score_fragment);
        mSubmitButton = (Button)view.findViewById(R.id.score_submit_button);
    }
}
