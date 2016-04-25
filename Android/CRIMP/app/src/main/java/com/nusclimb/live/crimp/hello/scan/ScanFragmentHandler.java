package com.nusclimb.live.crimp.hello.scan;

import com.nusclimb.live.crimp.R;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;

class ScanFragmentHandler extends Handler{
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
        if(!running){
            return;
        }
        switch (message.what) {
            case R.id.decode_succeeded:
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
            case R.id.decode_failed:
                /*
                if(running){
                    fragment.changeState(ScanFragment.State.SCANNING);
                }
                */
                break;
            default:
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
        Message quit = Message.obtain(fragment.getDecodeHandler(), R.id.quit);
        quit.sendToTarget();

        try {
            // Wait at most half a second; should be enough time, and onPause() will timeout quickly
            fragment.getDecodeThread().join(500L);
        } catch (InterruptedException e) {
            // continue
        }

        // Be absolutely sure we don't send any queued up messages
        removeMessages(R.id.decode_succeeded);
        removeMessages(R.id.decode_failed);
    }
}
