package com.nusclimb.live.crimp.hello.scan;

import android.hardware.Camera;
import android.os.Message;
import android.support.annotation.NonNull;
import android.view.Display;
import android.view.SurfaceHolder;

import com.nusclimb.live.crimp.R;

import java.io.IOException;
import java.util.List;

import timber.log.Timber;

/**
 * This class provides an interface for acquiring/releasing of camera 
 * resource and any operations pertaining to camera resource. 
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 *
 */
class CrimpCameraManager implements Camera.PreviewCallback{
    /**
     * Whether the surface to show preview is ready (between surfaceCreated and surfaceDestroyed).
     */
    private boolean mIsSurfaceReady;

    /**
     * We are tracking camera id manually because there is no way to get camera id from Camera
     * instance.
     */
    private int mCameraId;

    /**
     * Camera instance that this CameraManager is managing.
     */
    private Camera mCamera;

    /**
     * The thread that handles decoding of images capture by camera.
     */
    private DecodeThread mDecodeThread;

    /**
     * True: We are capturing and drawing preview frames to the screen.
     */
    private boolean mIsPreviewing;

    /**
     * True: Send preview image for decoding. False: Do not send preview image for decoding.
     */
    private boolean mIsScanning;

    /**
     * The preview size chosen from list of preview size available for our camera instance that
     * best matches our display surface size.
     */
    private Camera.Size mBestPreviewSize;

    /**
     * True: Camera torch is on.
     */
    private boolean mIsTorchOn;

    /**
     * True: there is a call to startPreview() but SurfaceView is not ready yet.
     */
    private boolean mStartPreviewPending = false;

    /**
     * True: there is a call to startScan() but somehow we are not able to start scan straight away.
     */
    private boolean mStartScanPending = false;

    private int mPxScreenHeight;

    /**
     * Method to acquire camera resource and initialize camera parameter.
     *
     * @param targetWidth ideal width for our SurfaceView
     * @param aspectRatio aspect ratio of our ideal SurfaceView
     * @param displayRotation rotation of the screen from its "natural" orientation
     * @return True: Camera acquired. False: Camera acquisition failed.
     * @see Display#getRotation()
     */
    public boolean acquireCamera(int targetWidth, float aspectRatio, int displayRotation){
        if(mCamera != null){
            return true;
        }

        mCamera = getCameraInstance();
        if(mCamera == null){
            Timber.d("Acquire camera failed");
            return false;
        }
        else{
            Timber.d("Acquire camera succeed");
            initCamera(targetWidth, aspectRatio, displayRotation);
            return true;
        }
    }

    /**
     * Method to release camera resource. No-op if we did not acquire camera
     * resource.
     */
    public void releaseCamera(){
        if (mCamera != null){
            mBestPreviewSize = null;
            mCameraId = -1;
            mDecodeThread = null;
            mIsPreviewing = false;
            mIsScanning = false;
            mIsTorchOn = false;
            mStartPreviewPending = false;
            mStartScanPending = false;
            mPxScreenHeight = 0;
            mCamera.release();
            mCamera = null;
        }

        Timber.d("Camera is released");
    }

    /**
     * Method to start previewing and display camera input onto surfaceView.
     *
     * @param holder Holder of surfaceView
     */
    public void startPreview(@NonNull SurfaceHolder holder){
        if(mCamera == null){
            throw new NullPointerException("Camera must be instantiated to start preview");
        }

        if(!mIsSurfaceReady){
            Timber.d("SurfaceHolder not ready to start preview.");
            mStartPreviewPending = true;
        }
        else {
            mStartPreviewPending = false;
            if (!mIsPreviewing) {
                try {
                    mCamera.setPreviewDisplay(holder);
                    mCamera.startPreview();
                    mIsPreviewing = true;
                    Timber.d("start previewing");
                    if(mStartScanPending){
                        startScan(mDecodeThread);
                    }
                } catch (IOException e) {
                    Timber.e(e, "IOE when attempting to setPreviewDisplay().");
                }
            }
        }
    }

    /**
     * Method to stop previewing. No-op if 1) camera resource not acquired
     * and/or 2) not previewing.
     */
    public void stopPreview(){
        mStartScanPending = false;
        mStartPreviewPending = false;
        if(mIsPreviewing && mCamera!=null ){
            mCamera.stopPreview();
            mIsPreviewing = false;
            Timber.d("Stop preview");
        }
    }

    /**
     * Method to start scanning.
     */
    public void startScan(DecodeThread decodeThread){
        if(mIsScanning){
            return;
        }

        if(decodeThread.getState()==Thread.State.NEW ||
                decodeThread.getState()==Thread.State.TERMINATED){
            throw new IllegalThreadStateException("DecodeThread must be running when starting scan");
        }

        if(decodeThread.getHandler() == null){
            throw new NullPointerException("DecodeThread must have a non null handler");
        }

        mDecodeThread = decodeThread;

        if(mIsPreviewing){
            mStartScanPending = false;
            mCamera.setOneShotPreviewCallback(this);
            mIsScanning = true;
        }
        else{
            mStartScanPending = true;
            Timber.d("Attempt to start scan fail due to preview not started yet.");
        }
    }

    /**
     * Set the camera flash.
     *
     * @param on Boolean flag to indicate whether to on flash.
     */
    public void setFlash(boolean on){
        if(mCamera != null){
            Camera.Parameters params = mCamera.getParameters();
            List<String> flashMode = params.getSupportedFlashModes();

            if(on){
                if(flashMode.contains(Camera.Parameters.FLASH_MODE_TORCH)){
                    params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    mCamera.setParameters(params);
                    mIsTorchOn = true;
                }
                else{
                    if(flashMode.contains(Camera.Parameters.FLASH_MODE_ON)){
                        params.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                        mCamera.setParameters(params);
                        mIsTorchOn = true;
                    }
                }
            }
            else{
                if(flashMode.contains(Camera.Parameters.FLASH_MODE_OFF)){
                    params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    mCamera.setParameters(params);
                    mIsTorchOn = false;
                }
            }
        }
    }

    /**
     * Perform rotation of camera by 90 degree, find ideal preview
     * size base on target resolution and aspect ratio and set autofocus.
     *
     * @param targetWidth target width of our ideal SurfaceView
     * @param targetAspectRatio aspect ratio of our ideal SurfaceView
     * @param displayRotation rotation of the screen from its "natural" orientation
     * @see Display#getRotation()
     */
    private void initCamera(int targetWidth, float targetAspectRatio, int displayRotation){
        if(mCamera != null){
            // make targetAspectRatio slightly bigger in case floating point operation screw us up.
            targetAspectRatio = targetAspectRatio + 0.0001f;

            /* We want to find how much we need to rotate the camera picture clockwise by in degree.
             * This is done by adding CameraInfo.orientation with Display.getOrientation. We also
             * further limit this degree to [0, 360);
             */
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(mCameraId, cameraInfo);
            int angleToRotateClockwise = cameraInfo.orientation + displayRotation;
            while(angleToRotateClockwise >= 360){
                angleToRotateClockwise = angleToRotateClockwise - 360;
            }
            mCamera.setDisplayOrientation(angleToRotateClockwise);

            /* We want to find out which preview size to use. The ideal preview size is the largest
             * resolution with aspect ratio less than the target aspect ratio.
             */
            Camera.Parameters param = mCamera.getParameters();
            List<Camera.Size> supportedSize = param.getSupportedPreviewSizes();
            mBestPreviewSize = supportedSize.get(0);
            int longestHeight = 0;
            switch(angleToRotateClockwise){
                case 0:     // deliberate fall through
                case 180:
                    // The ideal preview size is the one with longest height and aspect ratio less
                    // than target aspect ratio.
                    for (Camera.Size s : supportedSize) {
                        final float aspectRatio = ((float)s.height)/(float)s.width;
                        if(aspectRatio <= targetAspectRatio){
                            if(s.height > longestHeight){
                                longestHeight = s.height;
                                // We will scale the s.width to screen width and therefore need to
                                // calculate what is the scaled height.
                                mPxScreenHeight = targetWidth * s.height / s.width;
                                mBestPreviewSize = s;
                            }
                        }
                        Timber.d("Rotated: %ddegree, Resolution: H%dpx, W%dpx, Ratio: %f, target: %f",
                                angleToRotateClockwise, s.height, s.width, aspectRatio, targetAspectRatio);
                    }
                    break;
                case 90:    // deliberate fall through
                case 270:
                    // The ideal preview size is the one with longest height and aspect ratio less
                    // than target aspect ratio. Since we rotated by 90/270 degrees, we will use the
                    // preview width as height and vice versa.
                    for (Camera.Size s : supportedSize) {
                        final float aspectRatio = ((float)s.width)/(float)s.height;
                        if(aspectRatio <= targetAspectRatio){
                            if(s.width > longestHeight){
                                longestHeight = s.width;
                                // We will scale the s.height to screen width and therefore need to
                                // calculate what is the scaled height.
                                mPxScreenHeight = targetWidth * s.width / s.height;
                                mBestPreviewSize = s;
                            }
                        }
                        Timber.d("Rotated: %ddegree, Resolution: H%dpx, W%dpx, Ratio: %f, target: %f",
                                angleToRotateClockwise, s.height, s.width, aspectRatio, targetAspectRatio);
                    }
                    break;
                default:
                    Timber.d("Camera image angle to rotate is not a multiple of 90 degree");
            }
            param.setPreviewSize(mBestPreviewSize.width, mBestPreviewSize.height);
            Timber.d("Chosen size: H%d x W%d", mBestPreviewSize.height, mBestPreviewSize.width);

            // Set autofocus if possible.
            List<String> focusModes = param.getSupportedFocusModes();
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                param.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            }

            mCamera.setParameters(param);
        }
        else{
            throw new NullPointerException("initCamera must be called with a valid camera instance");
        }
    }

    /**
     * Safe method to acquire the first back facing camera resource.
     *
     * @return Camera resource.
     */
    private Camera getCameraInstance(){
        Camera c = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int cameraCount = Camera.getNumberOfCameras();

        // We loop through every camera hardware and find the first back-facing camera.
        int cameraId = -1;
        for(int i=0; i<cameraCount; i++){
            Camera.getCameraInfo(i, cameraInfo);
            if(cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK){
                cameraId = i;
            }
        }

        if(cameraId != -1){
            try {
                c = Camera.open(cameraId); // attempt to get a Camera instance
            }
            catch (Exception e){
                Timber.e("GetCameraInstance failed. Camera is not available (in use or does not exist");
            }
        }
        else{
            Timber.e("Failed to find a back facing camera");
        }

        if(c != null){
            mCameraId = cameraId;
        }

        return c; // returns null if camera is unavailable
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        mIsScanning = false;
        if(mDecodeThread != null && mDecodeThread.getHandler() != null){
            Message message = mDecodeThread.getHandler().obtainMessage(R.id.decode,
                    mBestPreviewSize.width, mBestPreviewSize.height, data);
            message.sendToTarget();
        }
        else{
            throw new NullPointerException("Got preview callback, but no handler available");
        }
    }

    public boolean hasCamera(){
        return mCamera != null;
    }

    public boolean isTorchOn(){
        return mIsTorchOn;
    }

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

}