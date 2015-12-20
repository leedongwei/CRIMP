package com.nusclimb.live.crimp.hello;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class CrimpViewPager extends ViewPager {

    private boolean isAllowSwiping;

    public CrimpViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.isAllowSwiping = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.isAllowSwiping) {
            return super.onTouchEvent(event);
        }

        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (this.isAllowSwiping) {
            return super.onInterceptTouchEvent(event);
        }

        return false;
    }

    public void setIsAllowSwiping(boolean enabled) {
        this.isAllowSwiping = enabled;
    }
}
