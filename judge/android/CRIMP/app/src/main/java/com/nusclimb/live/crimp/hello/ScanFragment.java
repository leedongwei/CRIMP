package com.nusclimb.live.crimp.hello;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.common.BusProvider;
import com.nusclimb.live.crimp.common.busevent.RouteFinish;
import com.nusclimb.live.crimp.common.busevent.RouteNotFinish;
import com.nusclimb.live.crimp.common.busevent.ScanAcquireCamera;
import com.nusclimb.live.crimp.common.busevent.ScanOnResume;
import com.nusclimb.live.crimp.qr.CameraManager;
import com.nusclimb.live.crimp.qr.DecodeThread;
import com.nusclimb.live.crimp.qr.PreviewView;
import com.nusclimb.live.crimp.qr.ScanFragmentHandler;
import com.squareup.otto.Subscribe;

/**
 * Created by Zhi on 7/8/2015.
 */
public class ScanFragment extends Fragment {
    private final String TAG = ScanFragment.class.getSimpleName();

    // Information retrieved from intent.
    private String xUserId;
    private String xAuthToken;

    // Decoding stuff
    private DecodeThread mDecodeThread;
    private ScanFragmentHandler handler;

    // Camera stuff
    private CameraManager cameraManager;
    private Point previewResolution;	// Size of the previewView
    private PreviewView previewView;

    // Info from scanned QR code
    private String climberId;
    private String climberName;

    // UI references
    private FrameLayout mPreviewFrame;
    private EditText mCategoryIdEdit;
    private EditText mClimberIdEdit;
    private Button mRescanButton;
    private Button mFlashButton;
    private EditText mClimberNameEdit;
    private Button mNextButton;

    private boolean isAcquire = false;//todo

    private int activityState;	// R.id.decode: previewView showing camera input. preview send
                                //      to DecodeThread to be decoded. Default entry state.
                                // R.id.decode_failed: Transitive state. Happens when DecodeThread
                                //      fails to find QRCode. Will quickly transit to decode state
                                //      to attempt to scan/decode again.
                                // R.id.decode_succeeded: Camera resource released. previewView
                                //      stop previewing. DecodeThread killed. qrResult not null.
                                // R.id.quit: Prepare to quit. Camera resource released.
                                //      previewView stop previewing. DecodeThread killed.


    /*=========================================================================
     * Fragment lifecycle methods
     *=======================================================================*/
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        mDecodeThread = new DecodeThread(this);
        handler = new ScanFragmentHandler(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        Log.v(TAG+".onCreateView()", "createview");

        View rootView = inflater.inflate(R.layout.fragment_scan, container, false);

        activityState = R.id.decode;

        previewResolution = calculatePreviewResolution();
        cameraManager = new CameraManager(this);

        // Get initial info from arguments.
        Bundle args = getArguments();
        xUserId = args.getString(getString(R.string.package_name) +
                getString(R.string.bundle_x_user_id));
        xAuthToken = args.getString(getString(R.string.package_name) +
                getString(R.string.bundle_x_auth_token));

        // Get UI references.
        mPreviewFrame = (FrameLayout) rootView.findViewById(R.id.scan_frame_viewgroup);
        mCategoryIdEdit = (EditText) rootView.findViewById(R.id.scan_category_edit);
        mClimberIdEdit = (EditText) rootView.findViewById(R.id.scan_climber_id_edit);
        mRescanButton = (Button) rootView.findViewById(R.id.scan_rescan_button);
        mFlashButton = (Button) rootView.findViewById(R.id.scan_flash_button);
        mClimberNameEdit = (EditText) rootView.findViewById(R.id.scan_climber_name_edit);
        mNextButton = (Button) rootView.findViewById(R.id.scan_next_button);

        // Initialize UI
        mCategoryIdEdit.setText(getArguments().getString(getString(R.string.bundle_route_id)));    // TODO
        // Update buttons.
        if(!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)){
            mFlashButton.setEnabled(false);
        }

        return rootView;
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.v(TAG + ".onResume()", "resume");

        BusProvider.getInstance().register(this);
        BusProvider.getInstance().post(new ScanOnResume());
    }

    @Override
    public void onPause(){
        Log.v(TAG + ".onPause()", "pause");
        super.onPause();
        BusProvider.getInstance().unregister(this);
        releaseCamera();
    }


    /**
     * This method takes in a result String, retrieve climber's id and name from
     * result String, and update the UI.
     *
     * @param result String containing climber's id and name.
     */
    public void updateStatusView(String result){
        String[] climberInfo = result.split(";");
        climberId = climberInfo[0];
        climberName = climberInfo[1];

        mClimberIdEdit.setText(climberId, TextView.BufferType.EDITABLE);
        mClimberNameEdit.setText(climberName, TextView.BufferType.EDITABLE);
    }


    /**
     * This method calculate the size of preview surface view. We want the
     * width of preview view to be the entire display width. Aspect ratio
     * of the preview view is the inverse of device real resolution aspect ratio.
     */
    @SuppressLint("NewApi")
    private Point calculatePreviewResolution() {
        WindowManager manager = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point screenResolution = new Point();	//application display size without system decoration
        Point realResolution = new Point();		//display size including system decoration
        if(android.os.Build.VERSION.SDK_INT >= 13){
            display.getSize(screenResolution);
        }
        else{
            screenResolution.x = display.getWidth();
            screenResolution.y = display.getHeight();
        }

        // display.getRealSize only work from api lvl 17 onward.
        if(android.os.Build.VERSION.SDK_INT >= 17){
            Log.v(TAG, "Using Display.getRealSize().");
            display.getRealSize(realResolution);
        }
        else{
            //TODO not working as intended.
            Log.v(TAG, "Using Display.getMetrics().");
            DisplayMetrics displaymetrics = new DisplayMetrics();
            display.getMetrics(displaymetrics);
            realResolution.y = displaymetrics.heightPixels;
            realResolution.x = displaymetrics.widthPixels;
        }

        Point previewViewSize = new Point();
        previewViewSize.x = screenResolution.x;
        previewViewSize.y = (int) (screenResolution.x * ((double)realResolution.x/realResolution.y));
        Log.v(TAG, "screenReso: X" + screenResolution.x + " x Y" + screenResolution.y +
                "\nrealReso: X" + realResolution.x + " x Y" + realResolution.y +
                "\npreviewReso: X" + previewViewSize.x + " x Y" + previewViewSize.y);
        return previewViewSize;
    }

    /**
     * This method create previewView and translate it. No-op if previewView
     * already exist. Important: This method can only be called after camera
     * resource is acquired (e.g. starting from onResume).
     */
    private void createPreviewAndArrange(){
        if(previewView == null){
            Camera.Size temp = cameraManager.getBestPreviewSize();

            previewView = new PreviewView(getActivity());
            int height;
            if(temp.width > previewResolution.y){
                height = temp.width;
            }
            else{
                height = previewResolution.y;
            }
            FrameLayout.LayoutParams frameParams =
                    new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, height);
            previewView.setLayoutParams(frameParams);

            //previewFrame
            LinearLayout.LayoutParams linearParams =
                    (android.widget.LinearLayout.LayoutParams) mPreviewFrame.getLayoutParams();
            linearParams.height = previewResolution.y;
            mPreviewFrame.setLayoutParams(linearParams);
            mPreviewFrame.addView(previewView);

            previewView.getHolder().addCallback(cameraManager);
            if(temp.width > previewResolution.y){
                previewView.setTranslationY(previewResolution.y - temp.width);
            }
        }
    }





    /*=========================================================================
     * Getter/Setter methods
     *=======================================================================*/
    public int getState(){
        return activityState;
    }

    public void setState(int state){
        activityState = state;
    }

    public ScanFragmentHandler getHandler(){
        return handler;
    }

    public CameraManager getCameraManager(){
        return cameraManager;
    }



    @Subscribe
    public void onReceiveScanAcquireCamera(ScanAcquireCamera event){
        Log.d(TAG + ".onReceiveRouteFinish()", "Received ScanAcquireCamera event.");
        acquireCamera();
    }


    @Subscribe
    public void onReceiveRouteNotFinish(RouteNotFinish event){
        Log.d(TAG + ".onReceiveRouteNotFinish()", "Received RouteNotFinish event.");
        releaseCamera();
    }

    @Subscribe
    public void onReceiveRouteFinish(RouteFinish event){
        Log.d(TAG + ".onReceiveRouteNotFinish()", "Received RouteFinish event.");
        acquireCamera();
    }

    // TODO
    private void acquireCamera(){
        if(isAcquire == false) {
            isAcquire = true;


            mDecodeThread.start();


            handler.setRunning(true);

            // We need camera resource. We will acquire camera in onResume and release in onPause.
            // Check if we have camera resource first.
            if (!cameraManager.hasCamera()) {
                // No Camera resource.
                Log.d(TAG + ".onResume()", "No camera resource. Will attempt to acquire camera.");

                boolean temp = cameraManager.acquireCamera(previewResolution);
                if (temp == false) {
                    // Error handling. Fail to get camera resource.
                    Log.e(TAG + ".onResume()", "Failed to get camera resource.");
                }
            }

            //TODO Not sure if we can assume camera acquired. Perform check for safety.
            if (cameraManager.hasCamera()) {
                Log.d(TAG + ".onResume()", "hasCamera");
                createPreviewAndArrange();
            } else {
                Log.d(TAG + ".onResume()", "no camera");
            }

            if (getState() == R.id.decode) {
                if (cameraManager.isSurfaceReady()) {
                    Log.d(TAG + ".onResume()", "here");
                    setState(R.id.decode);
                    cameraManager.startPreview(previewView.getHolder());
                    cameraManager.startScan();
                } else {
                    Log.d(TAG + ".onResume()", "there");
                }
            }
        }
    }

    private void releaseCamera(){
        if(isAcquire) {
            handler.setRunning(false);

            // Stop previewing and release camera. We will acquire camera in onResume.
            if (cameraManager != null) {
                cameraManager.stopPreview();
                cameraManager.releaseCamera();
            }

            // TODO We will also need to kill DecodeThread.
            handler.onPause();

            isAcquire = false;
        }
    }

    public Handler getDecodeHandler(){
        return mDecodeThread.getHandler();
    }

    public Thread getDecodeThread(){
        return mDecodeThread;
    }

}
