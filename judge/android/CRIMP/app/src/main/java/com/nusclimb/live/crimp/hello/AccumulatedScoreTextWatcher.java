package com.nusclimb.live.crimp.hello;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import com.nusclimb.live.crimp.common.BusProvider;
import com.nusclimb.live.crimp.common.busevent.AccumulatedScoreChange;
import com.nusclimb.live.crimp.common.busevent.ClimberIdChange;

/**
 * Created by weizhi on 16/7/2015.
 */
public class AccumulatedScoreTextWatcher implements TextWatcher {
    private final String TAG = AccumulatedScoreTextWatcher.class.getSimpleName();

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        Log.v(TAG+".beforeTextChanged()", "CharSequence="+s+", start="+start+", count="+count+", after="+after);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        Log.v(TAG+".onTextChanged()", "CharSequence="+s+", start="+start+", before="+before+", count="+count);
    }

    @Override
    public void afterTextChanged(Editable s) {
        Log.d(TAG + ".afterTextChanged()", "afterTextChanged");

        BusProvider.getInstance().post(new AccumulatedScoreChange());
    }
}
