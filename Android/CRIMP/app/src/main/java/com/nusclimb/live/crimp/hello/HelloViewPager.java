package com.nusclimb.live.crimp.hello;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import timber.log.Timber;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class HelloViewPager extends ViewPager {
    private boolean restrictSwipe = true;
    private float initialXValue;
    private float prevXValue;
    private float recentDiffX;
    private float overallDiffX;


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
        Timber.d("onTouchEventStart: action:%d", event.getAction());
        boolean flag;
        int currentItem = getCurrentItem();
        boolean[] canDisplay;
        HelloFragmentAdapter adapter = (HelloFragmentAdapter) getAdapter();
        if(adapter != null) {
            canDisplay = adapter.getCanDisplay();
        }
        else{
            // If we don't even have adapter why do we even bother.
            return super.onTouchEvent(event);
        }

        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                initialXValue = event.getX();
                prevXValue = event.getX();
                flag = super.onTouchEvent(event);
                break;
            case MotionEvent.ACTION_MOVE:
                recentDiffX = event.getX() - prevXValue;
                overallDiffX = event.getX() - initialXValue;
                prevXValue = event.getX();

                if(overallDiffX > 0){
                    // swipe right overall
                    if(currentItem != 0 && !canDisplay[currentItem-1]){
                        flag = false;
                    }
                    else {
                        flag = super.onTouchEvent(event);
                    }
                }
                else if(overallDiffX < 0){
                    // swipe left overall
                    if(currentItem < (canDisplay.length-1) && !canDisplay[currentItem+1]){
                        flag = false;
                    }
                    else {
                        flag = super.onTouchEvent(event);
                    }
                }
                else{
                    flag = super.onTouchEvent(event);
                }
                break;
            default:
                flag = super.onTouchEvent(event);
        }

        Timber.d("onTouchEvent: init: %f, xPosition:%f, event:%d, %b", initialXValue, event.getX(), event.getAction(), flag);
        return flag;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        Timber.d("onInterceptTouchEvent: action:%d", event.getAction());
        boolean flag;
        int currentItem = getCurrentItem();
        boolean[] canDisplay;
        HelloFragmentAdapter adapter = (HelloFragmentAdapter) getAdapter();
        if(adapter != null) {
            canDisplay = adapter.getCanDisplay();
        }
        else{
            // If we don't even have adapter why do we even bother.
            return super.onInterceptTouchEvent(event);
        }

        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                initialXValue = event.getX();
                prevXValue = event.getX();
                flag = super.onInterceptTouchEvent(event);
                break;
            case MotionEvent.ACTION_MOVE:
                recentDiffX = event.getX() - prevXValue;
                overallDiffX = event.getX() - initialXValue;
                prevXValue = event.getX();

                if(overallDiffX > 0){
                    // swipe right overall
                    if(currentItem != 0 && !canDisplay[currentItem-1]){
                        flag = false;
                    }
                    else {
                        flag = super.onInterceptTouchEvent(event);
                    }
                }
                else if(overallDiffX < 0){
                    // swipe left overall
                    if(currentItem < (canDisplay.length-1) && !canDisplay[currentItem+1]){
                        flag = false;
                    }
                    else {
                        flag = super.onInterceptTouchEvent(event);
                    }
                }
                else{
                    flag = super.onInterceptTouchEvent(event);
                }
                break;
            default:
                flag = super.onInterceptTouchEvent(event);
        }

        if(flag){
            initialXValue = event.getX();
        }

        Timber.d("onInterceptEvent: xPosition:%f, event:%d, %b", event.getX(), event.getAction(), flag);
        return flag;
    }
}
