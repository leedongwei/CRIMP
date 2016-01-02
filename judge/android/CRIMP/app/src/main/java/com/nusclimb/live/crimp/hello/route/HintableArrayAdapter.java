package com.nusclimb.live.crimp.hello.route;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nusclimb.live.crimp.R;

import java.util.List;

/**
 * ArrayAdapter that display hint item with different text color and hide hint
 * item from dropdown list. HintableArrayAdapter is backed by an array of
 * {@link HintableSpinnerItem} objects. HintableArrayAdapter has a reference to
 * the array. In order for modification to the array to be reflected properly in the associated
 * {@link android.widget.AdapterView AdapterView}, we should call methods (i.e. add, addAll, clear,
 * insert, remove, sort) provided by this class and then call notifyDataSetChanged. The array
 * should not be modified directly.
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
            v.setTextColor(getContext().getResources().getColor(R.color.hint_color));
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

        View mView;
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

    /**
     * Find the position of the first hint item in this adapter.
     *
     * @return position of the first hint item.
     */
    public int getFirstHintPosition(){
        int position = -1;
        int i=0;
        boolean isFoundFirstHint = false;
        while(!isFoundFirstHint && i<getCount()){
            if(getItem(i).isHint()){
                isFoundFirstHint = true;
                position = i;
            }

            i++;
        }

        return position;
    }
}
