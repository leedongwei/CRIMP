package com.nusclimb.live.crimp.hello;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.nusclimb.live.crimp.R;

/**
 * Created by weizhi on 16/7/2015.
 */
public class ScoreFragment extends Fragment {
    private final String TAG = ScoreFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_score, container, false);
        Bundle args = getArguments();
        return rootView;
    }


}
