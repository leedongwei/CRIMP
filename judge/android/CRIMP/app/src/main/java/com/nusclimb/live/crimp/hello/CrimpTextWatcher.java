package com.nusclimb.live.crimp.hello;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import com.nusclimb.live.crimp.common.BusProvider;
import com.nusclimb.live.crimp.common.busevent.InvalidId;
import com.nusclimb.live.crimp.common.busevent.ValidId;

/**
 * Created by weizhi on 16/7/2015.
 */
public class CrimpTextWatcher implements TextWatcher {
    private final String TAG = CrimpTextWatcher.class.getSimpleName();
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        Log.d(TAG+".beforeTextChanged()", "CharSequence="+s+", start="+start+", count="+count+", after="+after);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        Log.d(TAG+".onTextChanged()", "CharSequence="+s+", start="+start+", before="+before+", count="+count);
    }

    @Override
    public void afterTextChanged(Editable s) {
        Log.d(TAG+".afterTextChanged()", "afterTextChanged");
        if(s.length() == 3){
            BusProvider.getInstance().post(new ValidId());
        }
        else{
            BusProvider.getInstance().post(new InvalidId());
        }

    }
}
