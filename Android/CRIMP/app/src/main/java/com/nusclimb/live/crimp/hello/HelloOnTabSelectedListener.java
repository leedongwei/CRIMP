package com.nusclimb.live.crimp.hello;

import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class HelloOnTabSelectedListener implements TabLayout.OnTabSelectedListener{
    private final ViewPager mViewPager;
    private final TabLayout mTabLayout;

    public HelloOnTabSelectedListener(ViewPager viewPager, TabLayout tabLayout) {
        mViewPager = viewPager;
        this.mTabLayout = tabLayout;
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        PagerAdapter adapter = mViewPager.getAdapter();

        if(adapter instanceof HelloFragmentAdapter){
            boolean canDisplay = ((HelloFragmentAdapter) adapter).getCanDisplay()[tab.getPosition()];
            if(canDisplay){
                mViewPager.setCurrentItem(tab.getPosition());
            }
            else{
                TabLayout.Tab prevTab = mTabLayout.getTabAt(mViewPager.getCurrentItem());
                if(prevTab != null) prevTab.select();
            }
        }
        else{
            mViewPager.setCurrentItem(tab.getPosition());
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        // No-op
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        // No-op
    }
}
