package com.nusclimb.live.crimp.hello;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.util.Collection;
import java.util.List;

//http://stackoverflow.com/questions/13877681/how-can-i-add-a-hint-to-the-spinner-widget
public class SpinnerAdapterWithHint extends ArrayAdapter<SpinnerItem> {
    private final String TAG = SpinnerAdapterWithHint.class.getSimpleName();

    public SpinnerAdapterWithHint(Context context, int resource) {
        super(context, resource);
    }

    public SpinnerAdapterWithHint(Context context, int resource, List<SpinnerItem> objects) {
        super(context, resource, objects);
    }

    @Override
    public int getCount() {
        if (super.getCount() == 1)
            return 1;
        else
            return super.getCount() - 1;

    }

    @Override
    public SpinnerItem getItem(int position) {
        return super.getItem(position);
    }

    public int getSuperCount(){
        return super.getCount();
    }

    public int getLastPosition() {
        return super.getCount()-1;
    }
}