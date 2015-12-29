package com.nusclimb.live.crimp.hello;

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
import android.widget.LinearLayout;

import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.common.Categories;
import com.nusclimb.live.crimp.common.Climber;
import com.nusclimb.live.crimp.common.User;
import com.nusclimb.live.crimp.qr.CameraManager;
import com.nusclimb.live.crimp.qr.DecodeThread;
import com.nusclimb.live.crimp.qr.PreviewView;
import com.nusclimb.live.crimp.qr.ScanFragmentHandler;

import java.util.ArrayList;

/**
 * Fragment for scanning QR code to get climber id and name. Activity containing this Fragment must
 * implement ScanFragmentToActivityMethods interface to allow this fragment to communicate with the attached
 * Activity and possibly other Fragments. Information from the Activity is passed to this Fragment
 * through arguments.
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class ScanFragment extends CrimpFragment implements SurfaceHolder.Callback, ClimberIdTextWatcher.ToFragmentInteraction {
    private final String TAG = ScanFragment.class.getSimpleName();

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
                    return SCANNING;
                default:
                    return null;
            }
        }
    }

    private ScanFragmentToActivityMethods mToActivityMethod;   //This is how we will communicate with
                                                                //Hello Activity.
    private State mState;

    // Decoding stuff
    private DecodeThread mDecodeThread;
    private ScanFragmentHandler mScanFragmentHandler;

    // Camera stuff
    private CameraManager cameraManager;
    private Point previewResolution;	// Size of the previewSurface
    private PreviewView previewSurface;

    // UI references
    private FrameLayout mPreviewFrame;
    private EditText mCategoryIdEdit;
    private EditText mClimberIdEdit;
    private Button mRescanButton;
    private Button mFlashButton;
    private EditText mClimberNameEdit;
    private Button mNextButton;

    private TextWatcher mTextWatcher;

    public static ScanFragment newInstance() {
        ScanFragment myFragment = new ScanFragment();

        return myFragment;
    }

    /**
     * Update mClimber with climber id and climber name.
     *
     * @param climberId climber id
     * @param climberName climber name
     */
    public void updateClimberWithScanResult(String climberId, String climberName){
        mToActivityMethod.updateActivityClimberInfo(climberId, climberName);
    }

    /**
     * This method initialize cameraManager, acquire camera and create a preview view.
     * This is called when DecodeHandler has been constructed.
     */
    public void onReceiveDecodeHandlerConstructed(){
        cameraManager = new CameraManager(getDecodeHandler());
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

        Log.d(TAG, "ScanFragment onAttach");
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mScanFragmentHandler = new ScanFragmentHandler(this);

        Log.d(TAG, "ScanFragment onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.fragment_scan, container, false);

        // Get UI references.
        mPreviewFrame = (FrameLayout) rootView.findViewById(R.id.scan_frame_viewgroup);
        mCategoryIdEdit = (EditText) rootView.findViewById(R.id.scan_category_id_edit);
        mClimberIdEdit = (EditText) rootView.findViewById(R.id.scan_climber_id_edit);
        mRescanButton = (Button) rootView.findViewById(R.id.scan_rescan_button);
        mFlashButton = (Button) rootView.findViewById(R.id.scan_flash_button);
        mClimberNameEdit = (EditText) rootView.findViewById(R.id.scan_climber_name_edit);
        mNextButton = (Button) rootView.findViewById(R.id.scan_next_button);

        mFlashButton.setOnClickListener(this);
        mNextButton.setOnClickListener(this);
        mRescanButton.setOnClickListener(this);

        mTextWatcher = new ClimberIdTextWatcher(this);
        mClimberIdEdit.addTextChangedListener(mTextWatcher);

        // Update buttons.
        if(!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)){
            mFlashButton.setEnabled(false);
        }

        Log.d(TAG, "ScanFragment onCreateView");
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        previewResolution = calculatePreviewResolution();

        if(savedInstanceState == null){
            // Initialize mState
            mState = State.SCANNING;
        }
        else{
            mState = State.toEnum(savedInstanceState.getInt(getString(R.string.bundle_scan_state)));
        }

        Log.d(TAG, "ScanFragment onActivityCreate");
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.d(TAG, "ScanFragment onResume");
        startThread();
    }

    @Override
    public void onPause(){
        Log.d(TAG, "ScanFragment onPause");

        releaseCameraAndStopDecodeThread();
        removePreviewView();
        cameraManager = null;
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(getString(R.string.bundle_scan_state), mState.getValue());

        Log.d(TAG, "ScanFragment onSaveInstance");
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "ScanFragment onDetach");
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
        //Log.d(TAG, "Change state: " + mState + " -> " + state);

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
                mRescanButton.setEnabled(false);
                mCategoryIdEdit.setText(mToActivityMethod.getCategoryId());
                //Don't touch mClimberIdEdit. It is left as it is.
                //Don't touch mClimberNameEdit. It is left as it is.
                break;
            case NOT_SCANNING:
                mRescanButton.setEnabled(true);
                mCategoryIdEdit.setText(mToActivityMethod.getCategoryId());
                mClimberIdEdit.setText(mToActivityMethod.getClimberId());
                //Don't touch mClimberNameEdit. It is left as it is.
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
                cameraManager.startPreview(previewSurface.getHolder());
                cameraManager.startScan();
                break;
            case NOT_SCANNING:
                cameraManager.stopPreview();
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
        cameraManager.set_isSurfaceReady(false);
        Log.d(TAG, "Preview view surface created.");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if(cameraManager != null){
            cameraManager.set_isSurfaceReady(false);
            cameraManager.stopPreview();
        }
        Log.d(TAG, "Preview view surface destroyed.");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        Log.d(TAG, "Preview view surface changed.");
        cameraManager.set_isSurfaceReady(true);
        changeState(mState);
    }

    @Override
    public CharSequence getPageTitle() {
        return "Scan";
    }

    @Override
    public void restart(){
        mClimberIdEdit.removeTextChangedListener(mTextWatcher);
        mClimberIdEdit.setText(null);
        mClimberNameEdit.setText(null);
        mTextWatcher = new ClimberIdTextWatcher(this);
        mClimberIdEdit.addTextChangedListener(mTextWatcher);
        changeState(State.SCANNING);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.scan_rescan_button:
                Log.d(TAG, "clicked rescan");
                rescan();
                break;
            case R.id.scan_flash_button:
                Log.d(TAG, "clicked flash");
                toggleFlash();
                break;
            case R.id.scan_next_button:
                Log.d(TAG, "clicked next");
                next();
                break;
        }
    }

    @Override
    public void updateClimberName(){
        String displayedId = mClimberIdEdit.getText().toString();
        if(displayedId.equals(mToActivityMethod.getClimberId())){
        }
        else{
            mToActivityMethod.updateActivityClimberInfo(displayedId, null);
        }
        mClimberNameEdit.setText(mToActivityMethod.getClimberName());
    }

    @Override
    public void updateNextButton(boolean enable){
        mNextButton.setEnabled(enable);
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
     * This method create previewSurface and translate it. No-op if previewSurface
     * already exist. Important: This method can only be called after we have CameraManager
     * and camera resource is acquired.
     */
    private void createSurfaceAndAttach(){
        if(previewSurface == null){
            Camera.Size temp = cameraManager.getBestPreviewSize();

            previewSurface = new PreviewView(getActivity());
            int height;
            if(temp.width > previewResolution.y){
                height = temp.width;
            }
            else{
                height = previewResolution.y;
            }
            FrameLayout.LayoutParams frameParams =
                    new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, height);
            previewSurface.setLayoutParams(frameParams);

            //previewFrame
            LinearLayout.LayoutParams linearParams =
                    (android.widget.LinearLayout.LayoutParams) mPreviewFrame.getLayoutParams();
            linearParams.height = previewResolution.y;
            mPreviewFrame.setLayoutParams(linearParams);
            mPreviewFrame.addView(previewSurface);

            previewSurface.getHolder().addCallback(this);
            if(temp.width > previewResolution.y){
                previewSurface.setTranslationY(previewResolution.y - temp.width);
            }
        }
    }

    /**
     * This method is use to remove previewView completely when the activity goes onPause().
     */
    private void removePreviewView(){
        if(previewSurface != null){
            previewSurface.getHolder().removeCallback(this);
            mPreviewFrame.removeView(previewSurface);
            previewSurface = null;
        }
    }

    /**
     * This method start the decode thread.
     */
    private void startThread() {
        mDecodeThread = new DecodeThread(mScanFragmentHandler,
                getActivity().getString(R.string.qr_prefix),
                previewResolution);
        if(mDecodeThread.getState() == Thread.State.NEW)
            mDecodeThread.start();
        mScanFragmentHandler.setRunning(true);
    }

    /**
     * Clean up method to release camera resource and stop decode thread.
     */
    private void releaseCameraAndStopDecodeThread(){
        if(mScanFragmentHandler != null){
            mScanFragmentHandler.setRunning(false);
        }
        else{
            Log.e(TAG + ".releaseCameraAndStopDecodeThread()",
                    "mScanFragmentHandler == null while trying to setRunning(false).");
        }

        if (cameraManager != null) {
            cameraManager.stopPreview();
            cameraManager.releaseCamera();
        }
        else{
            Log.e(TAG+".releaseCameraAndStopDecodeThread()",
                    "cameraManager == null while trying to stopPreview() and releaseCameraAndStopDecodeThread()");
        }

        mScanFragmentHandler.onPause();
    }

    /**
     * This method tries to acquire camera resource using camera manager.
     */
    private void acquireCamera(){
        // We need camera resource. We will acquire camera in onResume and release in onPause.
        // Check if we have camera resource first.
        if (!cameraManager.hasCamera()) {
            boolean temp = cameraManager.acquireCamera(previewResolution);
            if (temp == false) {
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
        mToActivityMethod.updateActivityClimberInfo(null, null);

        mClimberIdEdit.setText(null);

        changeState(State.SCANNING);
    }

    /**
     * Method to toggle the camera flash.
     */
    private void toggleFlash(){
        cameraManager.setFlash(!cameraManager.isTorchOn());
    }

    /**
     * This method is called when next button is pressed.
     */
    private void next(){
        mToActivityMethod.updateActivityClimberInfo(mClimberIdEdit.getText().toString(), mClimberNameEdit.getText().toString());
        changeState(State.NOT_SCANNING);
        //releaseCameraAndStopDecodeThread();
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
        String getClimberId();
        String getClimberName();
    }
}
