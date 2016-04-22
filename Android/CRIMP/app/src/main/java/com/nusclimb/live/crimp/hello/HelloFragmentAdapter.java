package com.nusclimb.live.crimp.hello;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.nusclimb.live.crimp.hello.route.RouteFragment;
import com.nusclimb.live.crimp.hello.scan.ScanFragment;
import com.nusclimb.live.crimp.hello.score.ScoreFragment;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class HelloFragmentAdapter extends FragmentStatePagerAdapter {
    private static final int COUNT = 3;

    private Fragment[] fragmentArray = new Fragment[COUNT];
    private boolean[] canDisplay;
    private Context mContext;

    public HelloFragmentAdapter(FragmentManager fm) {
        super(fm);
        canDisplay = new boolean[COUNT];
        for(int i=0; i<COUNT; i++){
            canDisplay[i] = true;
        }
    }

    public boolean[] getCanDisplay(){
        return canDisplay;
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
    public void destroyItem(ViewGroup container, int position, Object object){
        super.destroyItem(container, position, object);
        fragmentArray[position] = null;
    }

    @Override
    public Fragment getItem(int position) {
        switch(position){
            case 0:
                fragmentArray[position] = RouteFragment.newInstance(position,
                        getPageTitle(position).toString());
                break;
            case 1:
                fragmentArray[position] = ScanFragment.newInstance(position,
                        getPageTitle(position).toString());
                break;
            case 2:
                fragmentArray[position] = ScoreFragment.newInstance(position,
                        getPageTitle(position).toString());
                break;
            default:
                return null;
        }

        return fragmentArray[position];
    }

    @Override
    public int getCount() {
        return COUNT;
    }
}
