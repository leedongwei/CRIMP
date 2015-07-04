package com.nusclimb.live.crimp.qr;

import java.io.IOException;
import java.util.List;

import com.google.zxing.PlanarYUVLuminanceSource;
import com.nusclimb.live.crimp.R;

import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;

/**
 * This class provides an interface for acquiring/releasing of camera 
 * resource and any operations pertaining to camera resource. 
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 *
 */
public class CameraManager implements Camera.PreviewCallback, SurfaceHolder.Callback{
    private static final String TAG = CameraManager.class.getSimpleName();

    private boolean _isSurfaceReady;			// Whether the surface to show preview is ready (between
                                                // surfaceCreated and surfaceDestroyed).
    private Camera camera;
    private QRScanActivity activity;
    private boolean _isPreviewing;			    // Whether we are displaying camera input on previewView.
    private boolean _isScanning;			    // Whether we are sending new camera input to decode.
    private Camera.Size bestPreviewSize;
    private Point previewViewResolution;
    private boolean _isTorchOn;

    public CameraManager(QRScanActivity activity){
        this.activity = activity;
        _isScanning = false;
        _isPreviewing = false;
        bestPreviewSize = null;

        Log.d(TAG, "CameraManager is constructed!");
    }



    /*=========================================================================
     * Setter/Getter/Check state methods
     *=======================================================================*/
    public boolean isScanning(){
        return this._isScanning;
    }

    public boolean isPreviewing(){
        return this._isPreviewing;
    }

    public boolean hasCamera(){
        if(camera == null)
            return false;
        return true;
    }

    public Camera.Size getBestPreviewSize(){
        return bestPreviewSize;
    }

    public boolean isSurfaceReady(){
        return _isSurfaceReady;
    }

    public boolean isTorchOn(){
        return _isTorchOn;
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
    public boolean acquireCamera(Point targetResolution){
        previewViewResolution = targetResolution;
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
            camera.release();
            camera = null;
        }

        Log.d(TAG, "Camera is released.");
    }

    /**
     * Method to start previewing and display camera input onto surfaceView.
     * No-op if 1) camera resource not acquired and/or 2) already previewing
     * and/or 3) surface not ready.
     *
     * @param holder Holder of surfaceView
     */
    public void startPreview(SurfaceHolder holder){
        if(hasCamera() && !_isPreviewing && _isSurfaceReady){
            try {
                camera.setPreviewDisplay(holder);
                camera.startPreview();
                _isPreviewing = true;
                Log.i(TAG, "start previewing.");
            } catch (IOException e) {
                Log.e(TAG, "IOE when attempting to setPreviewDisplay().");
            }

        }
        else{
            Log.w(TAG, "Attempt to start preview fail. hasCamera=" + hasCamera() +
                    " isPreviewing=" + _isPreviewing +
                    "isSurfaceReady=" + _isSurfaceReady);
        }
    }

    /**
     * Method to stop previewing. No-op if 1) camera resource not acquired
     * and/or 2) not previewing.
     */
    public void stopPreview(){
        if(_isPreviewing && hasCamera() ){
            camera.stopPreview();
            _isPreviewing = false;
            Log.i(TAG, "Camera preview stopped.");
        }
    }

    /**
     * Method to start scanning. No-op if not previewing.
     */
    public void startScan(){
        if(_isPreviewing){
            camera.setOneShotPreviewCallback(this);
            _isScanning = true;
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
                    _isTorchOn = true;
                }
                else{
                    if(flashMode.contains(Camera.Parameters.FLASH_MODE_ON)){
                        params.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                        camera.setParameters(params);
                        _isTorchOn = true;
                    }
                }
            }
            else{
                if(flashMode.contains(Camera.Parameters.FLASH_MODE_OFF)){
                    params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    camera.setParameters(params);
                    _isTorchOn = false;
                }
            }
        }
    }

    /**
     * A factory method to build the appropriate LuminanceSource object based on the format
     * of the preview buffers, as described by Camera.Parameters.
     *
     * @param data A preview frame.
     * @param width The width of the image.
     * @param height The height of the image.
     * @return A PlanarYUVLuminanceSource instance.
     */
    public PlanarYUVLuminanceSource buildLuminanceSource(byte[] data, int width, int height) {
        int left, right, top, bottom;
        top = 0;
        bottom = height;
        right = width;
        if(width >= previewViewResolution.y){
            left = width - previewViewResolution.y;
        }
        else{
            left = 0;
        }
        Rect rect = new Rect(left, top, right, bottom);

        // Go ahead and assume it's YUV rather than die.
        return new PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top,
                rect.width(), rect.height(), false);
    }
  	
  	
	/*=========================================================================
	 * Private methods
	 *=======================================================================*/
    /**
     * Perform rotation of camera by 90 degree, find available preview
     * size with height closest to targetResolution's width, and set autofocus.
     * No-op if camera resource not acquired.
     *
     * @param targetResolution Resolution of previewView.
     */
    private void initCamera(Point targetResolution){
        if(camera != null){
            // Rotate camera by 90 degrees.
            camera.setDisplayOrientation(90);

            Camera.Parameters param = camera.getParameters();
            List<Camera.Size> supportedSize = param.getSupportedPreviewSizes();

            String temp = "Available preview size: ";
            for(Camera.Size s: supportedSize ){
                temp = temp + s.height + "x" + s.width + ";\n";
            }
            Log.v(TAG, temp);

            // Find available preview size with height closest to
            // targetResolution's width.
            double smallestDiff = 100000; // Some arbitrary huge number.
            bestPreviewSize = supportedSize.get(0);
            for(Camera.Size s: supportedSize ){
                double diff = Math.abs(s.height - targetResolution.x);
                if(diff < smallestDiff){
                    smallestDiff = diff;
                    bestPreviewSize = s;
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
            Log.w(TAG, "Called initCamera without camera instance. No-op.");
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
        // Got preview data. Need to send over to QRScanHandler.
        DecodeHandler handler = activity.getDecodeHandler();
        if (handler != null) {
            Message message = handler.obtainMessage(R.id.decode, bestPreviewSize.width, bestPreviewSize.height, data);
            message.sendToTarget();
        }
        else {
            Log.w(TAG, "Got preview callback, but no handler available");
        }

        // We only send 1 frame to decode. Stop scanning and wait for decode result.
        _isScanning = false;
    }



    /*=========================================================================
     * SurfaceHolder.Callback methods
     *=======================================================================*/
    public void surfaceCreated(SurfaceHolder holder) {
        _isSurfaceReady = true;

        Log.d(TAG, "Preview view surface created.");
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        _isSurfaceReady = false;

        Log.d(TAG, "Preview view surface destroyed.");

        stopPreview();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        Log.d(TAG, "Preview view surface changed.");

        if (holder.getSurface() == null){
            Log.w(TAG, "Preview view surface changed but surface is missing?");
            return;
        }

        // Stop camera preview, make changes, start camera preview again. 
        if(isPreviewing() && hasCamera()){
            stopPreview();
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // Only start scanning if activity state is "decode"
        if(activity.getState() == R.id.decode && hasCamera()){
            startPreview(holder);
            startScan();
        }
    }
}