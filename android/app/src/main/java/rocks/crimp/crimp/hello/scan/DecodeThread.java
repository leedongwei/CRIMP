package rocks.crimp.crimp.hello.scan;

import android.os.Handler;
import android.os.Looper;

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
    private DecodeHandler mDecodeHandler;

    @Override
    public void run(){
        Timber.d("DecodeThread begin running");
        Looper.prepare();
        mDecodeHandler = new DecodeHandler();
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