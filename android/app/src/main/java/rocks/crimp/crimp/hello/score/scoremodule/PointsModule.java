package rocks.crimp.crimp.hello.score.scoremodule;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import rocks.crimp.crimp.R;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class PointsModule implements ScoreModule, View.OnClickListener{
    private TextView mPointsView;
    private Button mTop;

    private int mPoints;
    private Context mContext;
    private ScoreModuleInterface mParent;

    public PointsModule(View rootView, Context context, ScoreModuleInterface scoreModuleInterface,
                        int points){
        mParent = scoreModuleInterface;
        mContext = context;
        mPoints = points;
        mPointsView = (TextView) rootView.findViewById(R.id.scoring_points_text);
        mTop = (Button) rootView.findViewById(R.id.scoring_t_button);

        mTop.setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.scoring_t_button:
                mParent.append("T");
                break;
        }
    }

    @Override
    public void notifyScore(@Nullable String score) {
        mPointsView.setText(String.valueOf(mPoints));
    }
}