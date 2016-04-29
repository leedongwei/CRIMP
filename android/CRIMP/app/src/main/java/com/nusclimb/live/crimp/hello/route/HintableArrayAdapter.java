package com.nusclimb.live.crimp.hello.route;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nusclimb.live.crimp.R;

import java.util.List;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class HintableArrayAdapter extends ArrayAdapter<String> {
    private final String mHint;

    public HintableArrayAdapter(Context context, int resource, @NonNull String hint) {
        super(context, resource);
        mHint = hint;
        insert(mHint, 0);
    }

    public HintableArrayAdapter(Context context, int resource, List<String> objects,
                                @NonNull String hint) {
        super(context, resource, objects);
        mHint = hint;
        insert(mHint, 0);
    }

    @Override
    public void clear(){
        super.clear();
        insert(mHint, 0);
    }

    @Override
    public void remove(String object){
        throw new UnsupportedOperationException();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        if(position == 0) {
            TextView v = (TextView)super.getView(position, convertView, parent);
            v.setTextColor(getContext().getResources().getColor(R.color.black_38));
            return v;
        }
        else{
            return super.getView(position, convertView, parent);
        }
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View mView;
        if(position == 0){
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