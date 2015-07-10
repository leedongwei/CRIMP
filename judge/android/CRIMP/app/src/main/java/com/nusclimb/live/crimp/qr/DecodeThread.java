package com.nusclimb.live.crimp.qr;

import android.os.Looper;
import android.util.Log;

import com.nusclimb.live.crimp.hello.ScanFragment;

/**
 * Worker thread for handling decode operation. A DecodeHandler instance will
 * be associated with this thread.
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 *
 */
public class DecodeThread extends Thread{
    private static final String TAG = DecodeThread.class.getSimpleName();

    // Bundle keys
    public static final String BARCODE_BITMAP = "barcode_bitmap";
    public static final String BARCODE_SCALED_FACTOR = "barcode_scaled_factor";

    private DecodeHandler mDecodeHandler;
    private ScanFragment fragment;

    public DecodeThread(ScanFragment fragment){
        this.fragment = fragment;
        Log.d(TAG, "DecodeThread constructed.");
    }

    @Override
    public void run(){
        Log.d(TAG, "DecodeThread begin running");
        Looper.prepare();
        mDecodeHandler = new DecodeHandler(fragment);
        Looper.loop();
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