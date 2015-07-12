package com.nusclimb.live.crimp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.view.ViewGroup;

import com.nusclimb.live.crimp.hello.HelloActivity;
import com.nusclimb.live.crimp.hello.RouteFragment;
import com.nusclimb.live.crimp.hello.ScanFragment;
import com.nusclimb.live.crimp.hello.TestFrag3;

/**
 * Created by user on 03-Jul-15.
 */
public class CrimpFragmentPagerAdapter extends FragmentPagerAdapter {
    private int crimpInternalCount;
    private HelloActivity mActivity;
    private int _count = 3;

    public CrimpFragmentPagerAdapter(FragmentManager fm, HelloActivity mActivity) {
        super(fm);
        this.mActivity = mActivity;
    }

    @Override
    public Fragment getItem(int i) {
        // This method name is misleading. This method will be call when
        // FragmentPagerAdapter needs an item and it does not exist. This
        // method instantiate the item.
        switch (i) {
            case 0:
                RouteFragment mRouteFragment = new RouteFragment();
                mRouteFragment.setArguments(mActivity.getBundle());

                return mRouteFragment;

            case 1:
                ScanFragment mScanFragment = new ScanFragment();
                mScanFragment.setArguments(mActivity.getBundle());

                return mScanFragment;

            case 2:
                return new TestFrag3();

            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        // TODO make this dynamic
        return _count;
    }


    public void set_count(int i){
        _count = i;
        notifyDataSetChanged();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch(position){
            case 0:
                return "Route";
            case 1:
                return "Scan";
            case 2:
                return "Score";
            default:
                return null;
        }
    }


}