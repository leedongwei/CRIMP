package com.nusclimb.live.crimp.hello.scan;

import com.nusclimb.live.crimp.R;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;

class ScanFragmentHandler extends Handler{
    private static final String TAG = ScanFragmentHandler.class.getSimpleName();
    private final boolean DEBUG = false;

    private final ScanFragment fragment;
    private boolean running;

    public ScanFragmentHandler(ScanFragment fragment){
        super();
        this.fragment = fragment;
        if (DEBUG) Log.d(TAG, "ScanFragmentHandler constructed");
    }

    public void setRunning(boolean running){
        this.running = running;
    }

    @Override
    public void handleMessage(Message message) {
        if(!running){
            if (DEBUG) Log.d(TAG+".handleMessage()", "ScanFragmentHandler received msg but not running.");
            return;
        }
        switch (message.what) {
            case R.id.decode_succeeded:
                if (DEBUG) Log.d(TAG + ".handleMessage()", "ScanFragmentHandler receive msg 'succeed'. running:" + running);
                String result = (String) message.obj;
                String[] climberInfo = result.split(";");
                fragment.updateClimberWithScanResult(climberInfo[0], climberInfo[1]);
                fragment.changeState(ScanFragment.State.NOT_SCANNING);

                // Get instance of Vibrator from current Context
                Vibrator v = (Vibrator) fragment.getActivity().getSystemService(Context.VIBRATOR_SERVICE);

                // Vibrate for 100 milliseconds
                v.vibrate(100);
                break;
            case R.id.decode_failed:
                if(DEBUG) Log.d(TAG, "decode_failed msg received. running:"+running);
                fragment.changeState(ScanFragment.State.SCANNING);
                break;
            default:
                Log.w(TAG+".handleMessage()", "ScanFragmentHandler received unknown msg.");
                break;
        }
    }

    /**
     * This method will be called when QRScanActivity enter onPause() state.
     * Kills the DecodeThread and do stuff.
     */
    public void onPause(){
        if (DEBUG) Log.d(TAG, "onPause");
        running = false;
        Message quit = Message.obtain(fragment.getDecodeHandler(), R.id.quit);
        quit.sendToTarget();
    }
}
