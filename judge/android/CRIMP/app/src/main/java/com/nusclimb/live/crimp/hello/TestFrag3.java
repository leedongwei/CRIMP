package com.nusclimb.live.crimp.hello;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nusclimb.live.crimp.R;

/**
 * Created by Zhi on 7/5/2015.
 */
public class TestFrag3 extends Fragment {
    public static final String ARG_SECTION_NUMBER = "section_number";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_test3, container, false);
        Bundle args = getArguments();
        ((TextView) rootView.findViewById(R.id.frag_test3)).setText("text3");
        return rootView;
    }

    public void onResume(){
        super.onResume();
        Log.d("frag3", "onresume");

        //getActivity().getActionBar().setSelectedNavigationItem(1);
    }
}
