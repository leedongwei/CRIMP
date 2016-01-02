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

    private ScanFragment fragment;
    private boolean running;

    public ScanFragmentHandler(ScanFragment fragment){
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
            case R.id.decode_handler_constructed:
                if(DEBUG) Log.d(TAG, "decode_handler_constructed msg received.");
                fragment.onReceiveDecodeHandlerConstructed();
                break;
            case R.id.decode_succeeded:
                if (DEBUG) Log.d(TAG + ".handleMessage()", "ScanFragmentHandler receive msg 'succeed'. running:" + running);
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
                break;
            case R.id.decode_failed:
                if(DEBUG) Log.d(TAG, "decode_failed msg received. running:"+running);
                if(running){
                    fragment.changeState(ScanFragment.State.SCANNING);
                }
                break;
            default:
                Log.w(TAG+".handleMessage()", "ScanFragmentHandler received unknown msg.");
                break;
        }
    }

    public void pauseDecode(){
        setRunning(false);
        Message pauseMessage = Message.obtain(fragment.getDecodeHandler(), R.id.decode_pause);
        pauseMessage.sendToTarget();

        removeMessages(R.id.decode_succeeded);
        removeMessages(R.id.decode_failed);
    }

    public void resumeDecode(){
        setRunning(true);
        if(fragment.getDecodeHandler() != null){
            Message resumeMessage = Message.obtain(fragment.getDecodeHandler(), R.id.decode_resume);
            resumeMessage.sendToTarget();
        }
    }

    /**
     * This method will be called when QRScanActivity enter onPause() state.
     * Kills the DecodeThread and do stuff.
     */
    public void onPause(){
        if (DEBUG) Log.d(TAG, "onPause");
        Message quit = Message.obtain(fragment.getDecodeHandler(), R.id.quit);
        quit.sendToTarget();

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
