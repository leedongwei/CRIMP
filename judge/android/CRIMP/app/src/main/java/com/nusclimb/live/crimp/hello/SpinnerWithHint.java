package com.nusclimb.live.crimp.hello;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import com.nusclimb.live.crimp.R;

/**
 * Created by Zhi on 5/31/2015.
 */
public class SpinnerWithHint extends Spinner{
    private final String TAG = SpinnerWithHint.class.getSimpleName();

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

    @Override
    public View getSelectedView (){
        SpinnerItem item = (SpinnerItem)getSelectedItem();
        Log.d(TAG, "selecteditem: "+item.getItemString()+"; "+item.isHint() );

        TextView mTextView = (TextView)super.getSelectedView();
        if(mTextView == null) {
            Log.d(TAG, "selected view is null");
            return null;
        }

        if(((SpinnerItem)getSelectedItem()).isHint())
            mTextView.setTextColor(getResources().getColor(R.color.hint_color));

        return mTextView;
    }

}
