package com.nusclimb.live.crimp.qr;

import android.graphics.Point;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.nusclimb.live.crimp.hello.ScanFragment;

/**
 * Worker thread for handling decode operation. A DecodeHandler instance will
 * be created by this thread. Any changes to the user interface must always be
 * performed from within the main thread therefore DecodeThread will hold a reference
 * to the main thread handler for inter-thread communication.
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
    private Handler mainThreadHandler;

    private final String PREFIX;	// Magic string to check if QR Code is valid
    private Point previewResolution;	// Size of the previewView

    public DecodeThread(Handler mainThreadHandler, String qrPrefix, Point previewResolution){
        this.mainThreadHandler = mainThreadHandler;
        PREFIX = qrPrefix;
        this.previewResolution = previewResolution;
        Log.d(TAG, "DecodeThread constructed.");
    }

    @Override
    public void run(){
        Log.d(TAG, "DecodeThread begin running");
        Looper.prepare();
        mDecodeHandler = new DecodeHandler(mainThreadHandler, PREFIX, previewResolution);
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