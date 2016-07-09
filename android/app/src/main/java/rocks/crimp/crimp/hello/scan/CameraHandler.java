package rocks.crimp.crimp.hello.scan;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.view.Display;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

import rocks.crimp.crimp.CrimpApplication;
import rocks.crimp.crimp.common.event.CameraAcquired;
import timber.log.Timber;

/**
 * This class deals directly with {@link Camera} object.
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class CameraHandler extends Handler {
    public static final int ACQUIRE_CAMERA = 1;
    public static final int INITIALIZE_CAMERA_PARAMS = 2;
    public static final int START_PREVIEW = 3;
    public static final int START_SCAN = 4;
    public static final int STOP_PREVIEW = 5;
    public static final int RELEASE_CAMERA = 6;
    public static final int QUIT_THREAD = 7;

    private volatile HandlerThread mHandlerThread;
    private boolean mIsReleased;
    private boolean mIsScanning;

    /**
     * Camera instance that this CameraManager is managing.
     */
    private volatile Camera mCamera;

    /**
     * We are tracking camera id manually because there is no way to get camera id from Camera
     * instance.
     */
    private int mCameraId;

    /**
     * The preview size chosen from list of preview size available for our camera instance that
     * best matches our display surface size.
     */
    private Camera.Size mBestPreviewSize;
    private int mPxSurfaceExpectedHeight;

    public CameraHandler (Looper looper, HandlerThread handlerThread){
        super(looper);
        mHandlerThread = handlerThread;
    }

    public void setIsScanning(boolean isScanning){
        mIsScanning = isScanning;
    }

    public boolean cameraIsReleased(){
        return mIsReleased;
    }

    @Override
    public void handleMessage(Message msg){
        switch(msg.what){
            case ACQUIRE_CAMERA:
                Timber.v("Received ACQUIRE_CAMERA: mCamera: %s", mCamera);
                if(mCamera == null){
                    mCamera = acquireCamera();
                    if(mCamera != null){
                        mIsReleased = false;
                    }
                }
                break;

            case INITIALIZE_CAMERA_PARAMS:
                Timber.v("Received INITIALIZE_CAMERA_PARAMS");
                Parameters params;
                if(msg.obj!=null && msg.obj instanceof Parameters){
                    params = (Parameters) msg.obj;
                }
                else{
                    throw new IllegalArgumentException("Parameters for camera initialization not provided.");
                }

                if(mCamera != null){
                    initializeCameraParams(mCamera, mCameraId, params.targetWidth,
                            params.displayRotation);
                }
                else{
                    throw new NullPointerException("Camera is null. Did you call acquireCamera()?");
                }
                break;

            case START_PREVIEW:
                Timber.v("Received START_PREVIEW");
                SurfaceView surface;
                if(msg.obj != null && msg.obj instanceof SurfaceView){
                    surface = (SurfaceView) msg.obj;
                }
                else{
                    throw new IllegalArgumentException("Need to provide a valid SurfaceView to start preview.");
                }

                if(mCamera != null){
                    startPreview(mCamera, surface);
                }
                else{
                    throw new NullPointerException("Camera is null. Did you call acquireCamera()?");
                }
                break;

            case START_SCAN:
                Timber.v("Received START_SCAN");
                Camera.PreviewCallback callback;
                if(msg.obj != null && msg.obj instanceof Camera.PreviewCallback){
                    callback = (Camera.PreviewCallback) msg.obj;
                }
                else{
                    throw new IllegalArgumentException("Need to provide a valid PreviewCallback to start scan.");
                }

                if(mIsScanning){
                    // already scanning. no-op
                }
                else{
                    if(mCamera == null){
                        throw new NullPointerException("Camera is null. Did you call acquireCamera()?");
                    }

                    startScan(mCamera, callback);
                }

                break;

            case STOP_PREVIEW:
                Timber.d("Received STOP_PREVIEW. mCamera: %s", mCamera);
                if(mCamera != null){
                    mCamera.stopPreview();
                }
                break;

            case RELEASE_CAMERA:
                Timber.d("Received RELEASE_CAMERA. mCamera: %s", mCamera);

                if(mCamera != null){
                    Timber.d("Camera releasing, mIsScanning: %b, mIsReleased: %b",
                            mIsScanning, mIsReleased);
                    mCamera.release();
                    mCamera = null;
                    mIsReleased = true;
                }
                break;

            case QUIT_THREAD:
                Timber.d("Received QUIT_THREAD");
                mHandlerThread.quit();
                mHandlerThread = null;
                break;

            default:
                Timber.d("Unknown message");
        }
    }

    /**
     * Safe method to acquire the first back facing camera resource. Update mCameraId to the opened
     * camera id if camera is successfully opened.
     *
     * @return Camera resource or null if camera is unavailable.
     */
    private Camera acquireCamera(){
        Camera c = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int cameraCount = Camera.getNumberOfCameras();

        // We loop through every camera hardware and find the first back-facing camera.
        int cId = -1;
        for(int i=0; i<cameraCount; i++){
            Camera.getCameraInfo(i, cameraInfo);
            if(cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK){
                cId = i;
            }
        }

        if(cId != -1){
            try {
                c = Camera.open(cId); // attempt to get a Camera instance
                Timber.d("Camera instance acquired");
            }
            catch (Exception e){
                Timber.e(e, "GetCameraInstance failed. Camera is not available (in use or does not exist");
            }
        }
        else{
            Timber.e("Failed to find a back facing camera");
        }

        if(c != null){
            mCameraId = cId;
        }
        return c;
    }

    /**
     * Perform rotation of camera by 90 degree, find ideal preview
     * size base on target resolution and set autofocus.
     *
     * @param camera camera to initialize
     * @param cameraId id of camera to initialize
     * @param targetWidth target width of our ideal SurfaceView
     * @param displayRotation rotation of the screen from its "natural" orientation
     * @see Display#getRotation()
     */
    private void initializeCameraParams(@NonNull Camera camera, int cameraId,
                                        int targetWidth, int displayRotation){

        // We want to find how much we need to rotate the camera picture clockwise by in degree.
        // This is piece of code is provided by offical android api documentation.
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);
        int angleToRotateClockwise = (cameraInfo.orientation - displayRotation + 360) % 360;
        camera.setDisplayOrientation(angleToRotateClockwise);

        // We want to find out which preview size to use. The ideal preview size is the largest
        // resolution.
        Camera.Parameters param = camera.getParameters();
        List<Camera.Size> supportedSize = param.getSupportedPreviewSizes();
        mBestPreviewSize = supportedSize.get(0);
        switch(angleToRotateClockwise){
            case 0:     // deliberate fall through
            case 180:
                // The ideal preview size is the one with longest min(height, width).
                for (Camera.Size s : supportedSize) {
                    if(Math.min(s.height, s.width)
                            >= Math.min(mBestPreviewSize.height, mBestPreviewSize.width)){

                        // We will scale the s.width to screen width and therefore need to
                        // calculate what is the scaled height.
                        mPxSurfaceExpectedHeight = targetWidth * s.height / s.width;
                        mBestPreviewSize = s;
                    }
                }
                break;
            case 90:    // deliberate fall through
            case 270:
                // The ideal preview size is the one with longest min(height, width).
                // Since we rotated by 90/270 degrees, we will use the preview width as height
                // and vice versa.
                for (Camera.Size s : supportedSize) {
                    if(Math.min(s.height, s.width)
                            >= Math.min(mBestPreviewSize.height, mBestPreviewSize.width)){

                        // We will scale the s.width to screen width and therefore need to
                        // calculate what is the scaled height.
                        mPxSurfaceExpectedHeight = targetWidth * s.width / s.height;
                        mBestPreviewSize = s;
                    }
                }
                break;
            default:
                throw new IllegalArgumentException("Camera image angle to rotate is not a multiple of 90 degree");
        }

        param.setPreviewSize(mBestPreviewSize.width, mBestPreviewSize.height);
        Timber.v("Chosen size: H%d x W%d", mBestPreviewSize.height, mBestPreviewSize.width);

        // Set autofocus if possible.
        List<String> focusModes = param.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            param.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }

        // Set preview format
        param.setPreviewFormat(ImageFormat.NV21);

        camera.setParameters(param);
        Timber.v("Camera parameter set");
        CrimpApplication.getBusInstance()
                .post(new CameraAcquired(targetWidth, mPxSurfaceExpectedHeight));

        if(angleToRotateClockwise==0 || angleToRotateClockwise==180){
            Timber.v("---------- Camera information ----------\n" +
                    "Rotated camera clockwise: %ddegree\n" +
                    "Chosen resolution: H%dpx, W%dpx\n" +
                    "Chosen aspect ratio (H/W): %f\n" +
                    "Expected height of surface: %dpx",
                    angleToRotateClockwise, mBestPreviewSize.height, mBestPreviewSize.width,
                    ((float)mBestPreviewSize.height)/(float)mBestPreviewSize.width,
                    mPxSurfaceExpectedHeight);
        }
        else{
            Timber.v("---------- Camera information ----------\n" +
                            "Rotated camera clockwise: %ddegree\n" +
                            "Chosen resolution: H%dpx, W%dpx\n" +
                            "Chosen aspect ratio (W/H): %f\n" +
                            "Expected height of surface: %dpx",
                    angleToRotateClockwise, mBestPreviewSize.height, mBestPreviewSize.width,
                    ((float)mBestPreviewSize.width)/(float)mBestPreviewSize.height,
                    mPxSurfaceExpectedHeight);
        }
    }

    /**
     * Method to start previewing and display camera input onto surfaceView.
     *
     * @param camera Camera to start preview
     * @param surfaceView SurfaceView to output our preview frames to
     */
    private void startPreview(@NonNull Camera camera, @NonNull SurfaceView surfaceView){
        try {
            camera.setPreviewDisplay(surfaceView.getHolder());
            camera.startPreview();
        } catch (IOException e) {
            Timber.e(e, "IOExeption when attempting to setPreviewDisplay");
        }
    }

    /**
     * Method to start scanning.
     */
    private void startScan(@NonNull Camera camera, @NonNull Camera.PreviewCallback callback){
        mIsScanning = true;
        camera.setOneShotPreviewCallback(callback);
    }

    public static class Parameters{
        public int targetWidth;
        //public float targetAspectRatio;
        public int displayRotation;

        public Parameters(int targetWidth, int displayRotation){
            this.targetWidth = targetWidth;
            //this.targetAspectRatio = targetAspectRatio;
            this.displayRotation = displayRotation;
        }
    }
}
