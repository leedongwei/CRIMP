package com.nusclimb.live.crimp.hello;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * ViewPager to be used in HelloActivity for swiping between different HelloActivityFragments.
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class HelloActivityViewPager extends ViewPager {
    /**
     * true: user can swipe between fragments using swiping gesture
     * false: otherwise
     */
    private boolean isAllowSwiping;

    public HelloActivityViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.isAllowSwiping = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
       return this.isAllowSwiping && super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return this.isAllowSwiping && super.onInterceptTouchEvent(event);
    }

    /**
     * Set whether swiping gesture will switch between pages.
     *
     * @param enabled whether swiping is allowed.
     */
    public void setIsAllowSwiping(boolean enabled) {
        this.isAllowSwiping = enabled;
    }
}
