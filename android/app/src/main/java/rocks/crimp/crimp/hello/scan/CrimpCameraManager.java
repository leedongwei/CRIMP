package rocks.crimp.crimp.hello.scan;

import android.hardware.Camera;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.view.Display;
import android.view.SurfaceView;

import timber.log.Timber;

/**
 * This class provides an interface for acquiring/releasing of camera 
 * resource and any operations pertaining to camera resource. 
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 *
 */
class CrimpCameraManager implements Camera.PreviewCallback{
    private static CrimpCameraManager mInstance;
    private volatile HandlerThread mCameraHandlerThread;
    private CameraHandler mCameraHandler;

    /**
     * The thread that handles decoding of images capture by camera.
     */
    private DecodeThread mDecodeThread;

    private CrimpCameraManager(){
    }

    public static CrimpCameraManager getInstance(){
        if(mInstance == null){
            mInstance = new CrimpCameraManager();
        }

        return mInstance;
    }

    private void createAndStartThread(){
        mCameraHandlerThread = new HandlerThread("Camera thread");
        mCameraHandlerThread.start();
        Looper cameraThreadLooper = mCameraHandlerThread.getLooper();
        mCameraHandler = new CameraHandler(cameraThreadLooper, mCameraHandlerThread);
    }

    /**
     * Method to acquire camera resource and initialize camera parameter.
     *
     * @param targetWidth ideal width for our SurfaceView
     * @param aspectRatio aspect ratio of our ideal SurfaceView
     * @param displayRotation rotation of the screen from its "natural" orientation
     * @see Display#getRotation()
     */
    public void acquireCamera(int targetWidth, float aspectRatio, int displayRotation){
        if(mCameraHandlerThread==null){
            createAndStartThread();
        }

        if(mCameraHandlerThread.getState() == Thread.State.NEW ||
                mCameraHandlerThread.getState() == Thread.State.TERMINATED){
            throw new IllegalThreadStateException("mCameraHandlerThread must be running to acquire camera");
        }

        CameraHandler.Parameters params =
                new CameraHandler.Parameters(targetWidth, aspectRatio, displayRotation);
        mCameraHandler.obtainMessage(CameraHandler.ACQUIRE_CAMERA).sendToTarget();
        mCameraHandler.obtainMessage(CameraHandler.INITIALIZE_CAMERA_PARAMS, params).sendToTarget();
    }

    /**
     * Method to start previewing and display camera input onto surfaceView.
     *
     * @param holder Holder of surfaceView
     */
    public void startPreview(@NonNull SurfaceView surfaceView){
        if(mCameraHandlerThread==null){
            createAndStartThread();
        }

        if(mCameraHandlerThread.getState() == Thread.State.NEW ||
                mCameraHandlerThread.getState() == Thread.State.TERMINATED){
            throw new IllegalThreadStateException("mCameraHandlerThread must be running to acquire camera");
        }

        mCameraHandler.obtainMessage(CameraHandler.START_PREVIEW, surfaceView).sendToTarget();
    }

    /**
     * Method to start scanning.
     */
    public void startScan(@NonNull DecodeThread decodeThread){
        if(mCameraHandlerThread==null){
            createAndStartThread();
        }

        if(mCameraHandlerThread.getState() == Thread.State.NEW ||
                mCameraHandlerThread.getState() == Thread.State.TERMINATED){
            throw new IllegalThreadStateException("mCameraHandlerThread must be running to acquire camera");
        }

        mDecodeThread = decodeThread;
        mCameraHandler.obtainMessage(CameraHandler.START_SCAN, this).sendToTarget();
    }

    /**
     * Method to stop previewing. No-op if 1) camera resource not acquired
     * and/or 2) not previewing.
     */
    public void stopPreview(){
        if(mCameraHandlerThread==null){
            createAndStartThread();
        }

        if(mCameraHandlerThread.getState() == Thread.State.NEW ||
                mCameraHandlerThread.getState() == Thread.State.TERMINATED){
            throw new IllegalThreadStateException("mCameraHandlerThread must be running to stop preview");
        }

        mCameraHandler.obtainMessage(CameraHandler.STOP_PREVIEW, this).sendToTarget();
    }

    /**
     * Method to release camera resource. No-op if we did not acquire camera
     * resource.
     */
    public void releaseCamera(){
        if(mCameraHandlerThread==null){
            createAndStartThread();
        }

        if(mCameraHandlerThread.getState() == Thread.State.NEW ||
                mCameraHandlerThread.getState() == Thread.State.TERMINATED){
            throw new IllegalThreadStateException("mCameraHandlerThread must be running to acquire camera");
        }

        mCameraHandler.obtainMessage(CameraHandler.RELEASE_CAMERA).sendToTarget();
    }

    public void onPauseQuit() throws InterruptedException {
        if(mCameraHandlerThread==null){
            createAndStartThread();
        }

        if(mCameraHandlerThread.getState() == Thread.State.NEW ||
                mCameraHandlerThread.getState() == Thread.State.TERMINATED){
            throw new IllegalThreadStateException("mCameraHandlerThread must be running to acquire camera");
        }

        mCameraHandler.obtainMessage(CameraHandler.QUIT_THREAD).sendToTarget();

        mCameraHandlerThread.join();
        mCameraHandlerThread = null;
        mCameraHandler = null;
        mInstance = null;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Timber.v("onPreviewFrame");

        /* The byte array data passed in here is not affected by setDisplayOrientation(). The size
         * of the byte array data depends on the size in Camera.Parameters.setPreviewSize().
         */
        if(mDecodeThread != null && mDecodeThread.getHandler() != null){
            mDecodeThread.getHandler().obtainMessage(DecodeHandler.DECODE,
                    camera.getParameters().getPreviewSize().width,
                    camera.getParameters().getPreviewSize().height, data).sendToTarget();
            mCameraHandler.setIsScanning(false);
            mCameraHandlerThread.interrupt();
        }
        else{
            throw new NullPointerException("Got preview callback, but no handler available");
        }
    }

    /*

    public void setIsSurfaceReady(boolean isReady, SurfaceHolder holder){
        Timber.d("setIsSurfaceReady: %b, startPreviewPending: %b", isReady, mStartPreviewPending);
        mIsSurfaceReady = isReady;
        if(mStartPreviewPending){
            startPreview(holder);
        }
    }

    public int getPxScreenHeight(){
        return mPxScreenHeight;
    }
*/
}