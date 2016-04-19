package com.nusclimb.live.crimp.hello;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import com.nusclimb.live.crimp.CrimpApplication2;
import com.nusclimb.live.crimp.common.event.SwipeTo;
import com.squareup.otto.Subscribe;

/**
 * A parent class for Fragments to be used in HelloActivity.
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class HelloFragment extends Fragment{
    private HelloFragmentInterface mActivity;
    private int position;
    private String title;

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            mActivity = (HelloFragmentInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement HelloFragmentInterface");
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        CrimpApplication2.getBusInstance().register(this);
    }

    @Override
    public void onStop(){
        CrimpApplication2.getBusInstance().unregister(this);
        super.onStop();
    }

    @Subscribe
    public void eventReceived(SwipeTo event) {
        if(event.position == position){
            mActivity.getData();
        }
    }

    public interface HelloFragmentInterface{
        Bundle getData();
        void putData(Bundle data);
    }
}