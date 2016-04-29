package com.nusclimb.live.crimp.hello.scan;

import com.nusclimb.live.crimp.R;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;

import timber.log.Timber;

class ScanFragmentHandler extends Handler{
    public static final int DECODE_SUCCEED = 1;
    public static final int DECODE_FAIL = 2;

    private ScanFragment fragment;
    private boolean running;

    public ScanFragmentHandler(ScanFragment fragment){
        this.fragment = fragment;
    }

    public void setRunning(boolean running){
        this.running = running;
    }

    @Override
    public void handleMessage(Message message) {
        Timber.d("received message %d", message.what);

        switch (message.what) {
            case DECODE_SUCCEED:
                /*
                if(running){
                    String result = (String) message.obj;
                    String[] climberInfo = result.split(";");
                    fragment.updateClimberWithScanResult(climberInfo[0], climberInfo[1]);
                    fragment.changeState(ScanFragment.State.NOT_SCANNING);

                    // Get instance of Vibrator from current Context
                    Vibrator v = (Vibrator) fragment.getActivity().getSystemService(Context.VIBRATOR_SERVICE);

                    // Vibrate for 100 milliseconds
                    v.vibrate(100);
                }
                */
                break;
            case DECODE_FAIL:
                /*
                if(running){
                    fragment.changeState(ScanFragment.State.SCANNING);
                }
                */
                break;
            default:
                Timber.d("Unknown message received");
                break;
        }
    }
}
