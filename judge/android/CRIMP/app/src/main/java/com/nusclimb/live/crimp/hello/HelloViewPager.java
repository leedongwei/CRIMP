package com.nusclimb.live.crimp.hello;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class HelloViewPager extends ViewPager {
    private static final String TAG = "HelloViewPager";
    private static final boolean DEBUG = true;

    private boolean restrictSwipe = true;
    private float initialXValue;

    public HelloViewPager(Context context) {
        super(context);
    }

    public HelloViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setRestriction(boolean restrict){
        this.restrictSwipe = restrict;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(restrictSwipe && !isValidSwipe(event)){
            if(DEBUG) Log.d(TAG, "Invalid swipe");
            return false;
        }

        return super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if(restrictSwipe && !isValidSwipe(event)){
            if(DEBUG) Log.d(TAG, "Invalid swipe");
            return false;
        }

        return super.onInterceptTouchEvent(event);
    }

    private boolean isValidSwipe(MotionEvent event){
        boolean isValid = true;

        SwipeDirection direction = checkSwipeDirection(event);
        int currentItem = getCurrentItem();
        HelloPagerAdapter adapter = (HelloPagerAdapter) getAdapter();

        boolean[] canDisplay;
        if(adapter != null){
            canDisplay = adapter.getCanDisplay();

            if(DEBUG) Log.d(TAG, "curr: "+currentItem+" canDisplay:" + canDisplay[0]+","+
                    canDisplay[1]+","+canDisplay[2]+" direction:"+direction);

            switch(direction){
                case None:
                    break;
                case LeftToRight:
                    if(currentItem > 0 && !canDisplay[currentItem-1]) {
                        isValid = false;
                    }
                    break;
                case RightToLeft:
                    if(currentItem < adapter.getCount()-1 && !canDisplay[currentItem+1]){
                        isValid = false;
                    }
                    break;
            }

        }

        return isValid;
    }

    private enum SwipeDirection{
        None,
        LeftToRight,
        RightToLeft;
    }

    private SwipeDirection checkSwipeDirection(MotionEvent event){
        if(event.getAction()==MotionEvent.ACTION_DOWN) {
            initialXValue = event.getX();
            return SwipeDirection.None;
        }

        if(event.getAction()==MotionEvent.ACTION_MOVE) {
            float diffX = event.getX() - initialXValue;
            if(DEBUG) Log.d(TAG, "diffx: "+diffX);
            if (diffX > 0 ) {
                if(DEBUG) Log.d(TAG, "Detected left to right swipe");
                return SwipeDirection.LeftToRight;
            }
            else if(diffX < 0){
                if(DEBUG) Log.d(TAG, "Detected right to left swipe");
                return SwipeDirection.RightToLeft;
            }
        }

        return SwipeDirection.None;
    }

}
