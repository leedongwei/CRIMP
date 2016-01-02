package com.nusclimb.live.crimp.hello;

import android.support.v4.app.Fragment;
import android.view.View;

/**
 * A parent class for Fragments to be used in HelloActivity.
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public abstract class HelloActivityFragment extends Fragment implements View.OnClickListener{
    /**
     * Method to handle navigating away from this Fragment. This method serves as a way for
     * HelloActivity to inform this Fragment that the user is navigating away from this Fragment.
     */
    public abstract void onNavigateAway();

    /**
     * Method to handle navigating to this Fragment. This method serves as a way for HelloActivity
     * to inform this Fragment that the user is navigating to this Fragment.
     */
    public abstract void onNavigateTo();
}