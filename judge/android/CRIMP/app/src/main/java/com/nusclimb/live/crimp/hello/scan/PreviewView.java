package com.nusclimb.live.crimp.hello.scan;

import android.content.Context;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * A subclass of SurfaceView for displaying of camera preview.
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 *
 */
public class PreviewView extends SurfaceView {
    private static final String TAG = PreviewView.class.getSimpleName();
    private final boolean DEBUG = false;
    public boolean isReady = false;

    public PreviewView(Context context) {
        super(context);
        if (DEBUG) Log.d(TAG, "Create with context");

        // deprecated setting, but required on Android versions prior to 3.0
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }
}