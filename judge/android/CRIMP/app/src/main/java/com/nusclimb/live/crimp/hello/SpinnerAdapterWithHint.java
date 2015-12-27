package com.nusclimb.live.crimp.hello;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Collection;
import java.util.List;

//http://stackoverflow.com/questions/13877681/how-can-i-add-a-hint-to-the-spinner-widget
//http://stackoverflow.com/questions/9863378/how-to-hide-one-item-in-an-android-spinner
public class SpinnerAdapterWithHint extends ArrayAdapter<SpinnerItem> {
    private final String TAG = SpinnerAdapterWithHint.class.getSimpleName();

    public SpinnerAdapterWithHint(Context context, int resource) {
        super(context, resource);
    }

    public SpinnerAdapterWithHint(Context context, int resource, List<SpinnerItem> objects) {
        super(context, resource, objects);
    }

    @Override
    public SpinnerItem getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent)
    {
        SpinnerItem item = getItem(position);

        View mView = null;
        if(item.isHint()){
            TextView mTextView = new TextView(getContext());
            mTextView.setHeight(0);
            mTextView.setVisibility(View.GONE);
            mView = mTextView;
        }
        else{
            mView = super.getDropDownView(position, null, parent);
        }

        parent.setVerticalScrollBarEnabled(false);
        return mView;
    }

    public int getFirstHintPosition(){
        int position = -1;

        int i = 0;
        while((position == -1) && i<getCount()){
            if(getItem(i).isHint())
                position = i;
            else
                i++;
        }

        return position;
    }
}