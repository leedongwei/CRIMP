package com.nusclimb.live.crimp.hello;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import timber.log.Timber;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class HelloViewPager extends ViewPager {
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
            Timber.d("Invalid swipe");
            return false;
        }

        return super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if(restrictSwipe && !isValidSwipe(event)){
            Timber.d("Invalid swipe");
            return false;
        }

        return super.onInterceptTouchEvent(event);
    }

    private boolean isValidSwipe(MotionEvent event){
        boolean isValid = true;

        SwipeDirection direction = checkSwipeDirection(event);
        int currentItem = getCurrentItem();
        HelloFragmentAdapter adapter = (HelloFragmentAdapter) getAdapter();

        boolean[] canDisplay;
        if(adapter != null){
            canDisplay = adapter.getCanDisplay();

            Timber.d("currentItem: %d canDisplay:[%b,%b,%b] direction:%s",currentItem,canDisplay[0],
                    canDisplay[1],canDisplay[2],direction);

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
            Timber.d("diffx: %f", diffX);
            if (diffX > 0 ) {
                Timber.d("Detected left to right swipe");
                return SwipeDirection.LeftToRight;
            }
            else if(diffX < 0){
                Timber.d("Detected right to left swipe");
                return SwipeDirection.RightToLeft;
            }
        }

        return SwipeDirection.None;
    }

}
