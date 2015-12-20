package com.nusclimb.live.crimp.qr;

import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.hello.ScanFragment;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;

public class ScanFragmentHandler extends Handler{
    private static final String TAG = ScanFragmentHandler.class.getSimpleName();

    private ScanFragment fragment;
    private boolean running;

    public ScanFragmentHandler(ScanFragment fragment){
        this.fragment = fragment;
        Log.d(TAG, "ScanFragmentHandler constructed");
    }

    public void setRunning(boolean running){
        this.running = running;
    }

    @Override
    public void handleMessage(Message message) {
        if(!running){
            Log.d(TAG+".handleMessage()", "ScanFragmentHandler received msg but not running.");
            return;
        }
        switch (message.what) {
            case R.id.decode_handler_constructed:
                fragment.onReceiveDecodeHandlerConstructed();
                break;
            case R.id.decode_succeeded:
                Log.d(TAG+".handleMessage()", "ScanFragmentHandler receive msg 'succeed'.");
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
        Message quit = Message.obtain(fragment.getDecodeHandler(), R.id.quit);
        quit.sendToTarget();

        Log.d(TAG+".onPause()", "onPause");

        try {
            // Wait at most half a second; should be enough time, and onPause() will timeout quickly
            fragment.getDecodeThread().join(500L);
        } catch (InterruptedException e) {
            // continue
            Log.e(TAG+".onPause()", "Exception while joining thread.");
        }

        // Be absolutely sure we don't send any queued up messages
        removeMessages(R.id.decode_succeeded);
        removeMessages(R.id.decode_failed);
    }
}
