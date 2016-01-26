package com.nusclimb.live.crimp.hello.scan;

import android.graphics.Point;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.SurfaceHolder;

import com.nusclimb.live.crimp.R;

import java.io.IOException;
import java.util.List;

/**
 * This class provides an interface for acquiring/releasing of camera 
 * resource and any operations pertaining to camera resource. 
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 *
 */
class CameraManager implements Camera.PreviewCallback{
    private static final String TAG = CameraManager.class.getSimpleName();
    private final boolean DEBUG = false;

    private Camera camera;
    private Handler mDecodeHandler;
    private boolean isPreviewing;	    // Whether we are displaying camera input on previewView.
    private boolean isScanning;		    // Whether we are sending new camera input to decode.
    private boolean isTorchOn;

    /*=========================================================================
     * Setter/Getter/Check state methods
     *=======================================================================*/
    public boolean hasCamera(){
        return camera != null;
    }

    public Camera.Size getPreviewSize(){
        if(camera != null)
            return camera.getParameters().getPreviewSize();
        else return null;
    }

    public boolean isTorchOn(){
        return isTorchOn;
    }
	
	/*=========================================================================
	 * Public methods
	 *=======================================================================*/
    /**
     * Method to acquire camera resource and initialize camera parameter.
     *
     * @param targetResolution Ideal resolution for our previewView.
     * @return True: Camera acquired. False: Camera acquisition failed.
     */
    public boolean acquireCamera(@NonNull Point targetResolution){
        if (camera != null) {
            return false;
        }

        camera = getCameraInstance();
        if(camera == null){
            Log.w(TAG, "Acquire camera fail.");
            return false;
        }
        else{
            Log.i(TAG, "Acquire camera succeed");
            initCamera(targetResolution);
            return true;
        }
    }

    /**
     * Method to release camera resource. No-op if we did not acquire camera
     * resource.
     */
    public void releaseCamera(){
        if (camera != null){
            isScanning = false;
            isPreviewing = false;
            isTorchOn = false;
            camera.release();
            camera = null;
        }

        if (DEBUG) Log.d(TAG, "Camera is released.");
    }

    /**
     * Method to start previewing and display camera input onto surfaceView.
     * No-op if 1) camera resource not acquired and/or 2) already previewing
     *
     * @param holder Holder of surfaceView. Must be fully initialize and ready.
     */
    public void startPreview(@NonNull SurfaceHolder holder){
        if (hasCamera() && !isPreviewing) {
            try {
                camera.setPreviewDisplay(holder);
                camera.startPreview();
                isPreviewing = true;
                Log.i(TAG, "start previewing.");
            } catch (IOException e) {
                Log.e(TAG, "IOE when attempting to setPreviewDisplay().");
            }
        }
    }

    /**
     * Method to stop previewing. No-op if 1) camera resource not acquired
     * and/or 2) not previewing.
     */
    public void stopPreview(){
        if(isPreviewing && hasCamera() ){
            camera.stopPreview();
            isPreviewing = false;
            Log.i(TAG, "Camera preview stopped.");
        }
    }

    /**
     * Method to start scanning. No-op if not previewing.
     */
    public void startScan(@NonNull Handler decodeHandler){
        mDecodeHandler = decodeHandler;
        if(isPreviewing){
            camera.setOneShotPreviewCallback(this);
            isScanning = true;
        }
        else{
            Log.w(TAG, "Attempt to start scan fail due to preview not started yet.");
        }
    }

    /**
     * Set the camera flash.
     *
     * @param on Boolean flag to indicate whether to on flash.
     */
    public void setFlash(boolean on){
        if(hasCamera()){
            Camera.Parameters params = camera.getParameters();
            List<String> flashMode = params.getSupportedFlashModes();

            if(on){
                if(flashMode.contains(Camera.Parameters.FLASH_MODE_TORCH)){
                    params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    camera.setParameters(params);
                    isTorchOn = true;
                }
                else{
                    if(flashMode.contains(Camera.Parameters.FLASH_MODE_ON)){
                        params.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                        camera.setParameters(params);
                        isTorchOn = true;
                    }
                }
            }
            else{
                if(flashMode.contains(Camera.Parameters.FLASH_MODE_OFF)){
                    params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    camera.setParameters(params);
                    isTorchOn = false;
                }
            }
        }
    }

  	
  	
	/*=========================================================================
	 * Private methods
	 *=======================================================================*/
    /**
     * Perform rotation of camera by 90 degree, find available preview
     * size with height closest to targetResolution's width, and set autofocus.
     *
     * @param targetResolution target resolution of previewView.
     */
    private void initCamera(Point targetResolution){
        if(camera != null){
            // Rotate camera by 90 degrees.
            camera.setDisplayOrientation(90);

            Camera.Parameters param = camera.getParameters();
            List<Camera.Size> supportedSize = param.getSupportedPreviewSizes();

            // Find available preview size with height closest to
            // targetResolution's width.
            double smallestDiff = 100000; // Some arbitrary huge number.
            Camera.Size bestPreviewSize = supportedSize.get(0);
            for(Camera.Size s: supportedSize ){
                if(s.width <= targetResolution.y){
                    double diff = Math.abs(s.height - targetResolution.x);
                    if(diff < smallestDiff){
                        smallestDiff = diff;
                        bestPreviewSize = s;
                    }
                }
            }

            param.setPreviewSize(bestPreviewSize.width, bestPreviewSize.height);
            Log.v(TAG, "Chosen size: H"+bestPreviewSize.height+" x W"+bestPreviewSize.width);

            // Set autofocus if possible.
            List<String> focusModes = param.getSupportedFocusModes();
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO))
            {
                param.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                Log.i(TAG, "Camera set focus mode succeed.");
            }

            camera.setParameters(param);
        }
        else{
            throw new NullPointerException("initCamera must be called with a valid camera instance");
        }
    }

    /**
     * Safe method to acquire camera resource.
     *
     * @return Camera resource.
     */
    private Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            Log.w(TAG, "GetCameraInstance failed. Camera is not available (in use or does not exist");
        }
        return c; // returns null if camera is unavailable
    }



    /*=========================================================================
     * Camera.PreviewCallback methods
     *=======================================================================*/
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        // Got preview data. Need to send over to ScanFragmentHandler.
        if (mDecodeHandler != null) {
            if(!hasCamera() || !isPreviewing || !isScanning){
                //do nothing
            }
            else {
                Message message = mDecodeHandler.obtainMessage(R.id.decode,
                        camera.getParameters().getPreviewSize().width,
                        camera.getParameters().getPreviewSize().height, data);
                message.sendToTarget();
            }
        }
        else {
            throw new NullPointerException("Got preview callback, but no handler available");
        }

        // We only send 1 frame to decode. Stop scanning and wait for decode result.
        isScanning = false;
    }
}