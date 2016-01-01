package com.nusclimb.live.crimp.hello;

import android.support.v4.app.Fragment;
import android.view.View;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public abstract class CrimpFragment extends Fragment implements View.OnClickListener{
    public abstract CharSequence getPageTitle();
    //public abstract void reinitialize();
    public abstract void onNavigateAway();
    public abstract void onNavigateTo();
}