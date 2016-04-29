package com.nusclimb.live.crimp.common;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class MainThreadBus extends Bus {
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public void post(final Object event) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            super.post(event);
        } else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    MainThreadBus.super.post(event);
                }
            });
        }
    }
}