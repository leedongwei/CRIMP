package com.nusclimb.live.crimp.qr;

import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.hello.ScanFragment;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;

public class QRScanHandler extends Handler{
    private static final String TAG = QRScanHandler.class.getSimpleName();

    private ScanFragment fragment;
    private boolean running;

    public QRScanHandler(ScanFragment fragment){
        this.fragment = fragment;
        Log.d(TAG, "QRScanHandler constructed");
    }

    public void setRunning(boolean running){
        this.running = running;
    }

    @Override
    public void handleMessage(Message message) {
        if(!running){
            Log.d(TAG+".handleMessage()", "QRScanHandler received msg but not running.");
            return;
        }
        switch (message.what) {
            case R.id.decode_succeeded:
                Log.d(TAG+".handleMessage()", "QRScanHandler receive msg 'succeed'.");
                fragment.getCameraManager().stopPreview();
                fragment.setState(R.id.decode_succeeded);
                String result = (String) message.obj;
                fragment.updateStatusView(result);

                // Get instance of Vibrator from current Context
                Vibrator v = (Vibrator) fragment.getActivity().getSystemService(Context.VIBRATOR_SERVICE);

                // Vibrate for 300 milliseconds
                v.vibrate(100);

                //TODO should release camera?
                break;
            case R.id.decode_failed:
                //TODO maybe need check and start preview.
                if(fragment.getCameraManager().isPreviewing()){
                    fragment.getCameraManager().startScan();
                }
                else{
                    Log.w(TAG+".handleMessage()", "QRScanHandler received decode fail msg. Start scan failed due to not previewing." );
                }
                break;
            default:
                Log.w(TAG+".handleMessage()", "QRScanHandler received unknown msg.");
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
