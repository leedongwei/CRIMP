package com.nusclimb.live.crimp.hello;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.nusclimb.live.crimp.hello.Route.RouteFragment;
import com.nusclimb.live.crimp.hello.Scan.ScanFragment;
import com.nusclimb.live.crimp.hello.Score.ScoreFragment;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class HelloFragmentAdapter extends FragmentStatePagerAdapter {
    private static final String TAG = "FragmentAdapter";
    private static final boolean DEBUG = true;

    private static final int COUNT = 3;
    private boolean[] canDisplay = new boolean[COUNT];

    public HelloFragmentAdapter(FragmentManager fm) {
        super(fm);
        for(int i=0; i<COUNT; i++){
            canDisplay[i] = true;
        }
    }

    public boolean[] getCanDisplay(){
        return canDisplay;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch(position) {
            case 0:
                fragment = RouteFragment.newInstance(position, getPageTitle(position).toString());
                break;
            case 1:
                fragment = ScanFragment.newInstance(position, getPageTitle(position).toString());
                break;
            case 2:
                fragment = ScoreFragment.newInstance(position, getPageTitle(position).toString());
                break;
        }

        return fragment;
    }

    @Override
    public CharSequence getPageTitle(int position){
        switch(position) {
            case 0: return "Route";
            case 1: return "Scan";
            case 2: return "Score";
            default: return null;
        }
    }

    @Override
    public int getCount() {
        return COUNT;
    }
}
