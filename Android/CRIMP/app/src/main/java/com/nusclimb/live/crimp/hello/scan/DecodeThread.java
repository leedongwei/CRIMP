package com.nusclimb.live.crimp.hello.scan;

import android.graphics.Point;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.nusclimb.live.crimp.R;

import timber.log.Timber;

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
    // Bundle keys
    public static final String BARCODE_BITMAP = "barcode_bitmap";
    public static final String BARCODE_SCALED_FACTOR = "barcode_scaled_factor";

    private DecodeHandler mDecodeHandler;
    private Handler mainThreadHandler;

    public DecodeThread(Handler mainThreadHandler){
        this.mainThreadHandler = mainThreadHandler;
    }

    @Override
    public void run(){
        Timber.d("DecodeThread begin running");
        Looper.prepare();
        mDecodeHandler = new DecodeHandler(mainThreadHandler);
        Looper.loop();
        Timber.d("DecodeThread terminating");
    }

    /**
     * Getter for the DecodeHandler object associated with this DecodeThread.
     *
     * @return DecodeHandler object
     */
    public DecodeHandler getHandler(){
        return mDecodeHandler;
    }
}