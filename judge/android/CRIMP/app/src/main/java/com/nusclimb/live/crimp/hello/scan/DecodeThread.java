package com.nusclimb.live.crimp.hello.scan;

import android.graphics.Point;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

/**
 * Worker thread for handling decode operation. A DecodeHandler instance will
 * be created by this thread. Any changes to the user interface must always be
 * performed from within the main thread therefore DecodeThread will hold a reference
 * to the main thread handler for inter-thread communication.
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 *
 */
class DecodeThread extends Thread{
    private static final String TAG = DecodeThread.class.getSimpleName();
    private final boolean DEBUG = false;

    private DecodeHandler mDecodeHandler;
    private final Handler mainHandler;
    private final String qrPrefix;
    private final Point transparentResolution;

    public DecodeThread(Handler mainHandler, String qrPrefix, Point transparentResolution){
        super();
        this.mainHandler = mainHandler;
        this.qrPrefix = qrPrefix;
        this.transparentResolution = transparentResolution;
        if (DEBUG) Log.d(TAG, "DecodeThread constructed.");
    }

    @Override
    public void run(){
        if (DEBUG) Log.d(TAG, "DecodeThread begin running");
        Looper.prepare();

        mDecodeHandler = new DecodeHandler(mainHandler, qrPrefix, transparentResolution);

        Looper.loop();
    }

    public Handler getHandler(){
        return mDecodeHandler;
    }
}