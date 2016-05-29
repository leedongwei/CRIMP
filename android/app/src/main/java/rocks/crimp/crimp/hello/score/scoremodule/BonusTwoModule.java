package rocks.crimp.crimp.hello.score.scoremodule;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import rocks.crimp.crimp.R;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class BonusTwoModule implements ScoreModule, View.OnClickListener {
    private TextView mBestResult;
    private Button mBeeOne;
    private Button mBeeTwo;
    private Button mTop;
    private ImageButton mBackspace;

    private Context mContext;
    private ScoreModuleInterface mParent;

    public BonusTwoModule(View rootView, Context context, ScoreModuleInterface scoreModuleInterface){
        mContext = context;
        mParent = scoreModuleInterface;
        mBestResult = (TextView) rootView.findViewById(R.id.scoring_best_result_text);
        mBeeOne = (Button) rootView.findViewById(R.id.scoring_b1_button);
        mBeeTwo = (Button) rootView.findViewById(R.id.scoring_b2_button);
        mTop = (Button) rootView.findViewById(R.id.scoring_t_button);
        mBackspace = (ImageButton) rootView.findViewById(R.id.scoring_backspace_button);

        mBeeOne.setOnClickListener(this);
        mBeeTwo.setOnClickListener(this);
        mTop.setOnClickListener(this);
        mBackspace.setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.scoring_b1_button:
                mParent.append("B1");
                break;
            case R.id.scoring_b2_button:
                mParent.append("B2");
                break;
            case R.id.scoring_t_button:
                mParent.append("T ");
                break;
            case R.id.scoring_backspace_button:
                mParent.backspace();
                mParent.backspace();
                break;
        }
    }

    @Override
    public void notifyScore(@Nullable String score) {
        if(score == null){
            mBestResult.setText("-");
            return;
        }

        if(score.contains("T ")){
            mBestResult.setText("Top");
        }
        else if(score.contains("B2")){
            mBestResult.setText("Bonus 2");
        }
        else if(score.contains("B1")){
            mBestResult.setText("Bonus 1");
        }
        else {
            mBestResult.setText("-");
        }
    }
}
