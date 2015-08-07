package com.nusclimb.live.crimp.hello;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * ArrayAdapter that display hint item with different text color and hide hint
 * item from dropdown list.
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class HintableArrayAdapter extends ArrayAdapter<HintableSpinnerItem> {
    public HintableArrayAdapter(Context context, int resource, List<HintableSpinnerItem> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        HintableSpinnerItem item = getItem(position);
        if(item.isHint()) {
            TextView v = (TextView)super.getView(position, convertView, parent);
            v.setTextColor(Color.RED);  // TODO change to hint color
            return v;
        }
        else{
            return super.getView(position, convertView, parent);
        }
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent)
    {
        HintableSpinnerItem item = getItem(position);

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
}
