package com.nusclimb.live.crimp.hello.scan;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.hello.HelloActivityFragment;

/**
 * Fragment for scanning QR code to get climber id and name. Activity containing this Fragment must
 * implement ScanFragmentToActivityMethods interface to allow this fragment to communicate with the attached
 * Activity and possibly other Fragments. Information from the Activity is passed to this Fragment
 * through arguments.
 *
 * For QR code scanning, we need these 5 classes: ScanFragment, ScanFragmentHandler, DecodeThread,
 * DecodeHandler and CameraManager. We go through ScanFragmentHandler to communicate with ScanFragment
 * and go through DecodeHandler to communicate with DecodeThread. CameraManager requires a DecodeHandler
 * instance. DecodeHandler can only be instantiated by DecodeThread. DecodeThread is started by ScanFragment.
 *
 * Therefore the flow for setting up is as follows:
 * ScanFragment     ScanFragmentHandler     DecodeThread     DecodeHandler     CameraManager
 *     |
 *     |---construct--->|
 *     |<---------------|
 *     |                |
 *     | ---------------------start------------->|
 *     |                |                        |----construct-->|
 *     |                |                        |<---------------|
 *     |                |<-inform decodehandler--|                |
 *     |                |          ready         |                |
 *     |<---------------|                        |                |
 *     |                |                        |                |
 *     |----------------------------------------------------------------construct-->|
 *     |<---------------------------------------------------------------------------|
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class ScanFragment extends HelloActivityFragment implements SurfaceHolder.Callback, ClimberIdTextWatcher.ToFragmentInteraction {
    private final String TAG = ScanFragment.class.getSimpleName();
    private final boolean DEBUG = true;

    public enum State{
        SCANNING(0),
        NOT_SCANNING(1);

        private final int value;

        State(int value){
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static State toEnum(int i){
            switch(i){
                case 0:
                    return SCANNING;
                case 1:
                    return NOT_SCANNING;
                default:
                    return null;
            }
        }
    }

    private ScanFragmentToActivityMethods mToActivityMethod;    //This is how we will communicate with
                                                                //Hello Activity.
    private State mState;

    // Decoding stuff
    private DecodeThread mDecodeThread;
    private ScanFragmentHandler mScanFragmentHandler;

    // Camera stuff
    private CameraManager cameraManager;
    private PreviewView previewSurface;
    private Point screenResolution;

    // UI references
    private FrameLayout mPreviewFrame;
    private EditText mCategoryIdEdit;
    private EditText mClimberIdEdit;
    private Button mRescanButton;
    private ImageButton mFlashButton;
    private EditText mClimberNameEdit;
    private Button mNextButton;

    private TextWatcher mTextWatcher;

    private CameraManager getCameraManager(){
        if(cameraManager == null)
            cameraManager = new CameraManager();
        return cameraManager;
    }

    // can only be called after onActivityCreated()
    private Point getScreenResolution(){
        if(screenResolution == null) {
            WindowManager manager = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
            Display display = manager.getDefaultDisplay();
            screenResolution = new Point();    //application display size without system decoration
            if (android.os.Build.VERSION.SDK_INT >= 13) {
                display.getSize(screenResolution);
            } else {
                screenResolution.x = display.getWidth();
                screenResolution.y = display.getHeight();
            }
        }

        return screenResolution;
    }

    private PreviewView getPreviewSurface(){
        if(previewSurface == null)
            previewSurface = new PreviewView(getActivity());
        return previewSurface;
    }

    private FrameLayout getmPreviewFrame(){
        if(mPreviewFrame == null)
            mPreviewFrame = (FrameLayout) getView().findViewById(R.id.scan_frame);
        return mPreviewFrame;
    }

    private EditText getmCategoryIdEdit(){
        if(mCategoryIdEdit == null)
            mCategoryIdEdit = (EditText) getView().findViewById(R.id.scan_category_id_edit);
        return mCategoryIdEdit;
    }

    private EditText getmClimberIdEdit(){
        if(mClimberIdEdit == null)
            mClimberIdEdit = (EditText) getView().findViewById(R.id.scan_climber_id_edit);
        return mClimberIdEdit;
    }

    private Button getmRescanButton(){
        if(mRescanButton == null)
            mRescanButton = (Button)getView().findViewById(R.id.scan_rescan_button);
        return mRescanButton;
    }

    private ImageButton getmFlashButton(){
        if(mFlashButton == null)
            mFlashButton = (ImageButton) getView().findViewById(R.id.scan_flash_button);
        return mFlashButton;
    }

    private EditText getmClimberNameEdit(){
        if(mClimberNameEdit == null)
            mClimberNameEdit = (EditText) getView().findViewById(R.id.scan_climber_name_edit);
        return mClimberNameEdit;
    }

    private Button getmNextButton(){
        if(mNextButton == null)
            mNextButton = (Button)getView().findViewById(R.id.scan_next_button);
        return mNextButton;
    }

    private TextWatcher getmTextWatcher(){
        if(mTextWatcher == null)
            mTextWatcher = new ClimberIdTextWatcher(this);
        return mTextWatcher;
    }

    public static ScanFragment newInstance() {
        return new ScanFragment();
    }

    /**
     * Update mClimber with climber id and climber name.
     *
     * @param climberId climber id
     * @param climberName climber name
     */
    public void updateClimberWithScanResult(String climberId, String climberName){
        getmClimberIdEdit().setText(climberId);
        getmClimberNameEdit().setText(climberName);
    }

    /**
     * This method initialize cameraManager, acquire camera and create a preview view.
     * This is called when DecodeHandler has been constructed.
     */
    public void onReceiveDecodeHandlerConstructed(){
        if (DEBUG) Log.d(TAG, "onReceiveDecodeHandlerConstructed");
        getCameraManager().setDecodeHandler(getDecodeHandler());
        acquireCamera();
        createSurfaceAndAttach();
    }

    public Handler getDecodeHandler(){
        return mDecodeThread.getHandler();
    }

    public Thread getDecodeThread(){
        return mDecodeThread;
    }



    /*=========================================================================
     * Fragment lifecycle methods
     *=======================================================================*/
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mToActivityMethod = (ScanFragmentToActivityMethods) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ScanFragmentToActivityMethods");
        }

        if (DEBUG) Log.d(TAG, "ScanFragment onAttach");
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mScanFragmentHandler = new ScanFragmentHandler(this);

        if (DEBUG) Log.d(TAG, "ScanFragment onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.fragment_scan, container, false);


        // Update buttons.
        if(!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)){
            getmFlashButton().setEnabled(false);
        }

        if (DEBUG) Log.d(TAG, "ScanFragment onCreateView");
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        // add all the listeners for view.
        getmFlashButton().setOnClickListener(this);
        getmNextButton().setOnClickListener(this);
        getmRescanButton().setOnClickListener(this);
        getmClimberIdEdit().addTextChangedListener(getmTextWatcher());

        Bundle mySaveInstanceState = mToActivityMethod.restoreScanInstance();
        mState = State.toEnum(mySaveInstanceState.getInt(getString(R.string.bundle_scan_state), State.SCANNING.getValue()));

        if (DEBUG) Log.d(TAG, "ScanFragment onActivityCreate. mState: "+mState);
    }

    @Override
    public void onStart(){
        super.onStart();
    }

    @Override
    public void onResume(){
        super.onResume();
        if (DEBUG) Log.d(TAG, "ScanFragment onResume");

        getmClimberIdEdit().addTextChangedListener(getmTextWatcher());

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int overlayHeight = 192 * metrics.densityDpi / 160;
        int transparentHeight = getScreenResolution().y - overlayHeight;
        Point transparentResolution = new Point(getScreenResolution().x, transparentHeight);

        startThread(transparentResolution);

        Bundle myBundle = mToActivityMethod.restoreScanInstance();
        String climberIdEditString = myBundle.getString(getString(R.string.bundle_climber_id));
        String climberNameEditString = myBundle.getString(getString(R.string.bundle_climber_name));
        State state = State.toEnum(myBundle.getInt(getString(R.string.bundle_scan_state), State.SCANNING.getValue()));

        getmClimberIdEdit().setText(climberIdEditString);
        getmClimberNameEdit().setText(climberNameEditString);
        mState = state; //TODO
    }

    @Override
    public void onPause(){
        if (DEBUG) Log.d(TAG, "ScanFragment onPause");

        getmClimberIdEdit().removeTextChangedListener(getmTextWatcher());

        Bundle myOutState = new Bundle();
        myOutState.putInt(getString(R.string.bundle_scan_state), mState.getValue());
        myOutState.putString(getString(R.string.bundle_climber_id), getmClimberIdEdit().getText().toString());
        myOutState.putString(getString(R.string.bundle_climber_name), getmClimberNameEdit().getText().toString());
        mToActivityMethod.saveScanInstance(myOutState);

        releaseCameraAndStopDecodeThread();
        getPreviewSurface().getHolder().removeCallback(this);
        getmPreviewFrame().removeView(getPreviewSurface());
        super.onPause();
    }

    @Override
    public void onStop(){
        mPreviewFrame = null;
        mCategoryIdEdit = null;
        mClimberIdEdit = null;
        mRescanButton = null;
        mFlashButton = null;
        mClimberNameEdit = null;
        mNextButton = null;
        previewSurface = null;
        mTextWatcher = null;        // just in case fragment gets destroyed
        screenResolution = null;    // resolution might change when we stop the app
        super.onStop();
    }

    @Override
    public void onDetach() {
        if (DEBUG) Log.d(TAG, "ScanFragment onDetach");
        mToActivityMethod = null;
        super.onDetach();
    }



    /*=========================================================================
     * Main flow methods.
     *=======================================================================*/
    /**
     * Set {@code mState} to {@code state}. Changes to {@code mState} must
     * go through this method.
     *
     * @param state Hello state to set {@code mState} to.
     */
    public void changeState(State state) {
        //if(DEBUG) Log.d(TAG, "Change state: " + mState + " -> " + state);

        mState = state;
        updateUI();
        doWork();
    }

    /**
     * Method to control which UI element is visible at different state.
     */
    private void updateUI(){
        switch (mState){
            case SCANNING:
                getmRescanButton().setEnabled(false);
                getmCategoryIdEdit().setText(mToActivityMethod.getCategoryId());
                // Don't touch mClimberIdEdit.
                // Don't touch mClimberNameEdit. It is left as it is.
                break;
            case NOT_SCANNING:
                getmRescanButton().setEnabled(true);
                getmCategoryIdEdit().setText(mToActivityMethod.getCategoryId());
                // Don't touch mClimberNameEdit. It is left as it is.
                break;
            default:
                break;
        }
    }

    /**
     * Method to control what is performed at different state.
     */
    private void doWork(){
        switch (mState){
            case SCANNING:
                mScanFragmentHandler.resumeDecode();
                getCameraManager().startPreview(getPreviewSurface().getHolder());
                getCameraManager().startScan();
                break;
            case NOT_SCANNING:
                mScanFragmentHandler.pauseDecode();
                getCameraManager().stopPreview();
                break;
            default:
                break;
        }
    }



    /*=========================================================================
     * Interface methods
     *=======================================================================*/
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //surface is only ready after surfaceChanged is called.
        if(DEBUG) Log.d(TAG, "Preview view surface created.");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        getCameraManager().stopPreview();
        if(DEBUG) Log.d(TAG, "Preview view surface destroyed.");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        if(DEBUG) Log.d(TAG, "Preview view surface changed.");
        changeState(mState);
    }

    @Override
    public void onNavigateAway(){
        if (DEBUG) Log.d(TAG, "NavigateAway. mState:" +mState);
        String climberIdEditString = getmClimberIdEdit().getText().toString();
        String climberNameEditString = getmClimberNameEdit().getText().toString();

        Bundle myBundle = new Bundle();
        myBundle.putString(getString(R.string.bundle_climber_id), climberIdEditString);
        myBundle.putString(getString(R.string.bundle_climber_name), climberNameEditString);
        myBundle.putInt(getString(R.string.bundle_scan_state), mState.getValue());

        mToActivityMethod.saveScanInstance(myBundle);

        changeState(State.NOT_SCANNING);
    }

    @Override
    public void onNavigateTo(){
        Bundle myBundle = mToActivityMethod.restoreScanInstance();
        String climberIdEditString = myBundle.getString(getString(R.string.bundle_climber_id));
        String climberNameEditString = myBundle.getString(getString(R.string.bundle_climber_name));
        State state = State.toEnum(myBundle.getInt(getString(R.string.bundle_scan_state), State.SCANNING.getValue()));

        if (DEBUG) Log.d(TAG, "NavigateTo. state:"+state);

        getmClimberIdEdit().setText(climberIdEditString);
        getmClimberNameEdit().setText(climberNameEditString);

        if(climberIdEditString == null || climberIdEditString.length()==0){
            state = State.SCANNING;
        }

        mToActivityMethod.collapseToolBar();
        changeState(state);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.scan_rescan_button:
                if(DEBUG) Log.d(TAG, "clicked rescan");
                rescan();
                break;
            case R.id.scan_flash_button:
                if(DEBUG) Log.d(TAG, "clicked flash");
                toggleFlash();
                break;
            case R.id.scan_next_button:
                if(DEBUG) Log.d(TAG, "clicked next");
                next();
                break;
        }
    }

    @Override
    public void onClimberIdEditTextChange(String climberId){
        getmClimberNameEdit().setText(null);
        getmNextButton().setEnabled(climberId.length() == 3);
    }



    /*=========================================================================
     * Private methods
     *=======================================================================*/
    /**
     * This method calculate the size of preview surface view. We want the
     * width of preview view to be the entire display width. Aspect ratio
     * of the preview view is the inverse of device real resolution aspect ratio.
     *
     * Need to be attached to activity.
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
            if (DEBUG) Log.v(TAG, "Using Display.getRealSize().");
            display.getRealSize(realResolution);
        }
        else{
            //TODO not working as intended.
            if (DEBUG) Log.v(TAG, "Using Display.getMetrics().");
            DisplayMetrics displaymetrics = new DisplayMetrics();
            display.getMetrics(displaymetrics);
            realResolution.y = displaymetrics.heightPixels;
            realResolution.x = displaymetrics.widthPixels;
        }

        Point previewViewSize = new Point();
        previewViewSize.x = screenResolution.x;
        previewViewSize.y = (int) (screenResolution.x * ((double)realResolution.x/realResolution.y));
        if (DEBUG) Log.v(TAG, "screenReso: X" + screenResolution.x + " x Y" + screenResolution.y +
                "\nrealReso: X" + realResolution.x + " x Y" + realResolution.y +
                "\npreviewReso: X" + previewViewSize.x + " x Y" + previewViewSize.y);
        return previewViewSize;
    }



    /**
     * This method create previewSurface and translate it. No-op if previewSurface
     * already exist. Important: This method can only be called after we have CameraManager
     * and camera resource is acquired.
     */
    private void createSurfaceAndAttach(){
        if (DEBUG) Log.d(TAG, "createSurfaceAndAttach");
        Camera.Size temp = getCameraManager().getPreviewSize();

        DisplayMetrics metrics = getResources().getDisplayMetrics();

        int overlayHeight = 192 * metrics.densityDpi / 160;
        int transparentHeight = getScreenResolution().y - overlayHeight;
        int height = temp.width* getScreenResolution().x/temp.height;

        // transparent view
        View transparentView = getView().findViewById(R.id.scan_transparent);
        ViewGroup.LayoutParams transparentLayoutParam = transparentView.getLayoutParams();
        transparentLayoutParam.height = transparentHeight;
        transparentView.setLayoutParams(transparentLayoutParam);

        // attaching previewSurface to our frame
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(getScreenResolution().x, height);
        getPreviewSurface().setLayoutParams(layoutParams);
        getmPreviewFrame().addView(getPreviewSurface());
        getPreviewSurface().getHolder().addCallback(this);
    }

    /**
     * This method start the decode thread.
     */
    private void startThread(Point transparentResolution) {
        if (DEBUG) Log.d(TAG, "startThread");
        mDecodeThread = new DecodeThread(mScanFragmentHandler,
                getActivity().getString(R.string.qr_prefix),
                transparentResolution);
        if(mDecodeThread.getState() == Thread.State.NEW)
            mDecodeThread.start();
        mScanFragmentHandler.setRunning(true);
    }

    /**
     * Clean up method to release camera resource and stop decode thread.
     */
    private void releaseCameraAndStopDecodeThread(){
        if(DEBUG) Log.d(TAG, "releaseCameraAndStopDecodeThread");
        if(mScanFragmentHandler != null){
            mScanFragmentHandler.setRunning(false);
        }
        else{
            Log.e(TAG + ".releaseCameraAndStopDecodeThread()",
                    "mScanFragmentHandler == null while trying to setRunning(false).");
        }

        getCameraManager().stopPreview();
        getCameraManager().releaseCamera();

        mScanFragmentHandler.onPause();
    }

    /**
     * This method tries to acquire camera resource using camera manager.
     */
    private void acquireCamera(){
        // We need camera resource. We will acquire camera in onResume and release in onPause.
        // Check if we have camera resource first.
        if (!getCameraManager().hasCamera()) {

            DisplayMetrics metrics = getResources().getDisplayMetrics();
            Point targetResolution = new Point(screenResolution.x, screenResolution.y - (48*metrics.densityDpi/160));
            boolean temp = getCameraManager().acquireCamera(targetResolution);
            if (!temp) {
                // Error handling. Fail to get camera resource.
                Log.e(TAG, "Failed to get camera resource.");
            }
            else{
                Log.i(TAG, "Camera resource acquired.");
            }
        }
    }

    /**
     * This method reset mClimber, clear mClimberIdEdit and restart scanning.
     */
    private void rescan(){
        if(DEBUG) Log.d(TAG, "rescan");
        mToActivityMethod.updateActivityClimberInfo(null, null);
        getmClimberIdEdit().setText(null);
        changeState(State.SCANNING);
    }

    /**
     * Method to toggle the camera flash.
     */
    private void toggleFlash(){
        getCameraManager().setFlash(!getCameraManager().isTorchOn());
    }

    /**
     * This method is called when next button is pressed.
     */
    private void next(){
        if(DEBUG) Log.d(TAG, "next");
        getCameraManager().setFlash(false);
        getmNextButton().setEnabled(false);
        mToActivityMethod.updateActivityClimberInfo(getmClimberIdEdit().getText().toString(),
                getmClimberNameEdit().getText().toString());
        getmClimberIdEdit().setText(null);
        getmClimberNameEdit().setText(null);
        changeState(State.NOT_SCANNING);
        mToActivityMethod.createAndSwitchToScoreFragment();
    }



    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface ScanFragmentToActivityMethods {
        void createAndSwitchToScoreFragment();
        void updateActivityClimberInfo(String climberId, String climberName);
        String getCategoryId();
        void saveScanInstance(Bundle bundle);
        Bundle restoreScanInstance();
        void collapseToolBar();
    }
}
