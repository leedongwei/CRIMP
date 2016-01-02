package com.nusclimb.live.crimp.hello;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;

import com.nusclimb.live.crimp.hello.route.RouteFragment;
import com.nusclimb.live.crimp.hello.scan.ScanFragment;
import com.nusclimb.live.crimp.hello.score.ScoreFragment;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
class CrimpFragmentPagerAdapter extends FragmentPagerAdapter {
    private int count = 1;

    private RouteFragment mRouteFragment;
    private ScanFragment mScanFragment;
    private ScoreFragment mScoreFragment;

    public void setCount(int count){
        this.count = count;
        notifyDataSetChanged();
    }

    public CrimpFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return count;
    }

    //this is called when notifyDataSetChanged() is called
    @Override
    public int getItemPosition(Object object) {
        switch(count){
            case 1:
                if(object instanceof RouteFragment)
                    return POSITION_UNCHANGED;
                else
                    return POSITION_NONE;
            case 2:
                if(object instanceof RouteFragment || object instanceof ScanFragment)
                    return POSITION_UNCHANGED;
                else
                    return POSITION_NONE;
            case 3:
                return POSITION_UNCHANGED;
        }

        // refresh all fragments when data set changed
        return PagerAdapter.POSITION_NONE;
    }

    @Override
    public Fragment getItem(int position) {
        switch(position){
            case 0:
                mRouteFragment = RouteFragment.newInstance();
                return mRouteFragment;
            case 1:
                mScanFragment = ScanFragment.newInstance();
                return mScanFragment;
            case 2:
                mScoreFragment = ScoreFragment.newInstance();
                return mScoreFragment;
            default:
                return null;
        }
    }

    public RouteFragment getRouteFragment(){
        return mRouteFragment;
    }

    public ScanFragment getScanFragment(){
        return mScanFragment;
    }

    public ScoreFragment getScoreFragment(){
        return mScoreFragment;
    }
}
