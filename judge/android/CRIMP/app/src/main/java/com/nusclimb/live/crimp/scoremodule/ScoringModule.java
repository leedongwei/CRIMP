package com.nusclimb.live.crimp.scoremodule;

import android.support.v4.app.Fragment;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public abstract class ScoringModule extends Fragment {
    public abstract void onScoreChange(String accumulatedScore, String currentScore);
}
