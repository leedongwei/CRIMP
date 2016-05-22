package rocks.crimp.crimp.hello.score.scoremodule;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import rocks.crimp.crimp.R;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class TopBonusModule extends ScoreModule implements View.OnClickListener{
    private TextView mTopCounter;
    private TextView mBonusCounter;
    private Button mPlusOne;
    private Button mBonus;
    private Button mTop;
    private ImageButton mBackspace;

    private Context mContext;
    private ScoreModuleInterface mParent;

    public TopBonusModule(View rootView, Context context, ScoreModuleInterface scoreModuleInterface){
        mParent = scoreModuleInterface;
        mContext = context;
        mTopCounter = (TextView) rootView.findViewById(R.id.scoring_t_text);
        mBonusCounter = (TextView) rootView.findViewById(R.id.scoring_b_text);
        mPlusOne = (Button) rootView.findViewById(R.id.scoring_plus_one_button);
        mBonus = (Button) rootView.findViewById(R.id.scoring_t_button);
        mTop = (Button) rootView.findViewById(R.id.scoring_b_button);
        mBackspace = (ImageButton) rootView.findViewById(R.id.scoring_backspace_button);

        mPlusOne.setOnClickListener(this);
        mBonus.setOnClickListener(this);
        mTop.setOnClickListener(this);
        mBackspace.setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.scoring_plus_one_button:
                mParent.append("1");
                break;
            case R.id.scoring_b_button:
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
    public void notifyScore(String score) {
        int firstTee = score.indexOf("T");
        int firstBee = score.indexOf("B");

        if(firstTee != -1){
            // plus one to make it one-based
            mTopCounter.setText(String.valueOf(firstTee+1));

            if(firstBee != -1 && firstBee<firstTee){
                // plus one to make it one-based
                mBonusCounter.setText(String.valueOf(firstBee+1));
            }
            else{
                // plus one to make it one-based
                mBonusCounter.setText(String.valueOf(firstTee+1));
            }
        }
        else{
            // plus one to make it one-based
            mTopCounter.setText("-");

            if(firstBee != -1){
                mBonusCounter.setText(String.valueOf(firstBee+1));
            }
            else{
                mBonusCounter.setText("-");
            }
        }
    }
}
