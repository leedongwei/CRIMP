package com.nusclimb.live.crimp.hello;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.Spinner;

/**
 * Created by Zhi on 5/31/2015.
 */
public class SpinnerWithHint extends Spinner{
    public SpinnerWithHint(Context context){
        super(context);
    }

    public SpinnerWithHint(Context context, int mode){
        super(context, mode);
    }

    public SpinnerWithHint(Context context, AttributeSet attrs){
        super(context, attrs);
    }

    public SpinnerWithHint(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
    }

    public SpinnerWithHint(Context context, AttributeSet attrs, int defStyleAttr, int mode){
        super(context, attrs, defStyleAttr, mode);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SpinnerWithHint(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, int mode){
        super(context, attrs, defStyleAttr, defStyleRes, mode);
    }

}
