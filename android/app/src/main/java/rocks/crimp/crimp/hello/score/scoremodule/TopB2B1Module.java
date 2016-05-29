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
public class TopB2B1Module implements ScoreModule, View.OnClickListener {
    private TextView mBestResult;
    private Button mPlusOne;
    private Button mBonus1;
    private Button mBonus2;
    private Button mTop;
    private ImageButton mBackspace;

    private Context mContext;
    private ScoreModuleInterface mParent;

    public TopB2B1Module(View rootView, Context context, ScoreModuleInterface scoreModuleInterface) {
        mParent = scoreModuleInterface;
        mContext = context;
        mBestResult = (TextView) rootView.findViewById(R.id.scoring_best_result_text);
        mPlusOne = (Button) rootView.findViewById(R.id.scoring_plus_one_button);
        mBonus1 = (Button) rootView.findViewById(R.id.scoring_b1_button);
        mBonus2 = (Button) rootView.findViewById(R.id.scoring_b2_button);
        mTop = (Button) rootView.findViewById(R.id.scoring_t_button);
        mBackspace = (ImageButton) rootView.findViewById(R.id.scoring_backspace_button);

        mPlusOne.setOnClickListener(this);
        mBonus1.setOnClickListener(this);
        mBonus2.setOnClickListener(this);
        mTop.setOnClickListener(this);
        mBackspace.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.scoring_plus_one_button:
                mParent.append("1");
                break;
            case R.id.scoring_b1_button:
                mParent.append("b");
                break;
            case R.id.scoring_b2_button:
                mParent.append("B");
                break;
            case R.id.scoring_t_button:
                mParent.append("T");
                break;
            case R.id.scoring_backspace_button:
                mParent.backspace();
                break;
        }
    }

    @Override
    public void notifyScore(@Nullable String score) {
        if (score == null) {
            mBestResult.setText("-");
            return;
        }

        int firstTee = score.indexOf("T");
        if(firstTee != -1){
            if(firstTee == 0){
                mBestResult.setText("Flash");
                return;
            }
            else{
                mBestResult.setText("Top");
                return;
            }
        }

        int firstBee2 = score.indexOf("B");
        if(firstBee2 != -1){
            mBestResult.setText("Bonus 2");
            return;
        }

        int firstBee1 = score.indexOf("b");
        if(firstBee1 != -1){
            mBestResult.setText("Bonus 1");
            return;
        }

        mBestResult.setText("-");
    }
}