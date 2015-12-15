package com.nusclimb.live.crimp.hello;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
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
 * Created by Zhi on 7/8/2015.
 */
public class ScanFragment extends CrimpFragment {
    private final String TAG = ScanFragment.class.getSimpleName();

    private User mUser = null;
    private Climber mClimber = null;

    // Decoding stuff
    private DecodeThread mDecodeThread;
    private ScanFragmentHandler mScanFragmentHandler;

    // Camera stuff
    private CameraManager cameraManager;
    private Point previewResolution;	// Size of the previewView
    private PreviewView previewView;

    // UI references
    private FrameLayout mPreviewFrame;
    private EditText mRouteIdEdit;
    private EditText mClimberIdEdit;
    private Button mRescanButton;
    private Button mFlashButton;
    private EditText mClimberNameEdit;
    private Button mNextButton;

    private State mState;

    public static ScanFragment newInstance(User user, Categories categoriesInfo, Context context) {
        ScanFragment myFragment = new ScanFragment();

        Bundle args = new Bundle();
        if(user != null){
            args.putString(context.getString(R.string.bundle_x_user_id), user.getUserId());
            args.putString(context.getString(R.string.bundle_x_auth_token), user.getAuthToken());
            args.putString(context.getString(R.string.bundle_user_name), user.getUserName());
            args.putString(context.getString(R.string.bundle_access_token), user.getFacebookAccessToken());
        }

        if(categoriesInfo != null){
            ArrayList<String> categoryNameList = categoriesInfo.getCategoryNameList();
            ArrayList<String> categoryIdList = categoriesInfo.getCategoryIdList();
            ArrayList<Integer> categoryRouteCountList = categoriesInfo.getCategoryRouteCountList();
            ArrayList<String> routeNameList = categoriesInfo.getRouteNameList();
            ArrayList<String> routeIdList = categoriesInfo.getRouteIdList();
            ArrayList<String> routeScoreList = categoriesInfo.getRouteScoreList();
            byte[] categoryFinalizeArray = categoriesInfo.getCategoryFinalizeArray();
            ArrayList<String> categoryStartList = categoriesInfo.getCategoryStartList();
            ArrayList<String> categoryEndList = categoriesInfo.getCategoryEndList();

            args.putStringArrayList(context.getString(R.string.bundle_category_name_list), categoryNameList);
            args.putStringArrayList(context.getString(R.string.bundle_category_id_list),categoryIdList);
            args.putIntegerArrayList(context.getString(R.string.bundle_category_route_count_list), categoryRouteCountList);
            args.putStringArrayList(context.getString(R.string.bundle_route_name_list), routeNameList);
            args.putStringArrayList(context.getString(R.string.bundle_route_id_list), routeIdList);
            args.putStringArrayList(context.getString(R.string.bundle_route_score_list), routeScoreList);
            args.putByteArray(context.getString(R.string.bundle_category_finalize_list), categoryFinalizeArray);
            args.putStringArrayList(context.getString(R.string.bundle_category_start_list), categoryStartList);
            args.putStringArrayList(context.getString(R.string.bundle_category_end_list), categoryEndList);
        }
        myFragment.setArguments(args);

        return myFragment;
    }

    /*=========================================================================
     * Fragment lifecycle methods
     *=======================================================================*/
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        mScanFragmentHandler = new ScanFragmentHandler(this);

        Log.d(TAG + ".onCreate()", "created");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){

        View rootView = inflater.inflate(R.layout.fragment_scan, container, false);

        // Get UI references.
        mPreviewFrame = (FrameLayout) rootView.findViewById(R.id.scan_frame_viewgroup);
        mRouteIdEdit = (EditText) rootView.findViewById(R.id.scan_route_id_edit);
        mClimberIdEdit = (EditText) rootView.findViewById(R.id.scan_climber_id_edit);
        mRescanButton = (Button) rootView.findViewById(R.id.scan_rescan_button);
        mFlashButton = (Button) rootView.findViewById(R.id.scan_flash_button);
        mClimberNameEdit = (EditText) rootView.findViewById(R.id.scan_climber_name_edit);
        mNextButton = (Button) rootView.findViewById(R.id.scan_next_button);

        mFlashButton.setOnClickListener(this);
        mNextButton.setOnClickListener(this);
        mRescanButton.setOnClickListener(this);

        mClimberIdEdit.addTextChangedListener(new ClimberIdTextWatcher());

        // Update buttons.
        if(!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)){
            mFlashButton.setEnabled(false);
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        previewResolution = calculatePreviewResolution();

        if(savedInstanceState == null){
            // Initialize mState
            mState = State.SCANNING;
            Bundle args = getArguments();

            // Initialize mUser
            if(mUser == null)
                mUser = new User();
            mUser.setUserId(args.getString(getString(R.string.bundle_x_user_id)));
            mUser.setAuthToken(args.getString(getString(R.string.bundle_x_auth_token)));
            mUser.setUserName(args.getString(getString(R.string.bundle_user_name)));
            mUser.setFacebookAccessToken(args.getString(getString(R.string.bundle_access_token)));
            mUser.setCategoryId(args.getString(getString(R.string.bundle_category_id)));
            mUser.setRouteId(args.getString(getString(R.string.bundle_route_id)));
            //mUser.setClimberId(args.getString(getString(R.string.bundle_climber_id)));

            // Initialize mClimber
            if(mClimber == null)
                mClimber = new Climber();
            mClimber.setClimberId(args.getString(getString(R.string.bundle_climber_id)));
            mClimber.setClimberName(args.getString(getString(R.string.bundle_climber_name)));
            mClimber.setTotalScore(args.getString(getString(R.string.bundle_total_score)));
        }
        else{
            mState = State.toEnum(savedInstanceState.getInt(getString(R.string.bundle_scan_state)));

            // Initialize mUser
            if(mUser == null)
                mUser = new User();
            mUser.setUserId(savedInstanceState.getString(getString(R.string.bundle_x_user_id)));
            mUser.setAuthToken(savedInstanceState.getString(getString(R.string.bundle_x_auth_token)));
            mUser.setUserName(savedInstanceState.getString(getString(R.string.bundle_user_name)));
            mUser.setFacebookAccessToken(savedInstanceState.getString(getString(R.string.bundle_access_token)));
            mUser.setCategoryId(savedInstanceState.getString(getString(R.string.bundle_category_id)));
            mUser.setRouteId(savedInstanceState.getString(getString(R.string.bundle_route_id)));
            //mUser.setClimberId(savedInstanceState.getString(getString(R.string.bundle_climber_id)));mUser.setCategoryId(args.getString(getString(R.string.bundle_category_id)));

            // Initialize mClimber
            if(mClimber == null)
                mClimber = new Climber();
            mClimber.setClimberId(savedInstanceState.getString(getString(R.string.bundle_climber_id)));
            mClimber.setClimberName(savedInstanceState.getString(getString(R.string.bundle_climber_name)));
            mClimber.setTotalScore(savedInstanceState.getString(getString(R.string.bundle_total_score)));
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.v(TAG + ".onResume()", "resume");

        // Will always perform these in onResume().
        startThread();
        cameraManager = new CameraManager(getDecodeHandler());
        acquireCamera();
        acquirePreview();

        changeState(mState);
    }

    @Override
    public void onPause(){
        Log.d(TAG + ".onPause()", "pause");

        releaseCameraAndStopDecodeThread();
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mUser != null){
            outState.putString(getString(R.string.bundle_x_user_id), mUser.getUserId());
            outState.putString(getString(R.string.bundle_x_auth_token), mUser.getAuthToken());
            outState.putString(getString(R.string.bundle_user_name), mUser.getUserName());
            outState.putString(getString(R.string.bundle_access_token), mUser.getFacebookAccessToken());
            outState.putString(getString(R.string.bundle_category_id), mUser.getCategoryId());
            outState.putString(getString(R.string.bundle_route_id), mUser.getRouteId());
            //outState.putString(getString(R.string.bundle_climber_id), mUser.getClimberId());
        }

        if(mClimber != null){
            outState.putString(getString(R.string.bundle_climber_id), mClimber.getClimberId());
            outState.putString(getString(R.string.bundle_climber_name), mClimber.getClimberName());
            outState.putString(getString(R.string.bundle_total_score), mClimber.getTotalScore());
        }

        outState.putInt(getString(R.string.bundle_scan_state), mState.getValue());
    }



    public void updateClimberWithScanResult(String result){
        String[] climberInfo = result.split(";");
        mClimber.setClimberId(climberInfo[0]);
        mClimber.setClimberName(climberInfo[1]);
    }






    /**
     * Set {@code mState} to {@code state}. Changes to {@code mState} must
     * go through this method.
     *
     * @param state Hello state to set {@code mState} to.
     */
    public void changeState(State state) {
        Log.d(TAG + ".changeState()", mState + " -> " + state);

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
                mRouteIdEdit.setText(mUser.getRouteId());
                //Don't touch mClimberIdEdit. It is left as it is.
                //Don't touch mClimberNameEdit. It is left as it is.
                break;
            case NOT_SCANNING:
                mRescanButton.setEnabled(true);
                mRouteIdEdit.setText(mUser.getRouteId());
                mClimberIdEdit.setText(mClimber.getClimberId());
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
                cameraManager.startPreview(previewView.getHolder());
                cameraManager.startScan();
                break;
            case NOT_SCANNING:
                cameraManager.stopPreview();
                break;
            default:
                break;
        }
    }

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


    public CameraManager getCameraManager(){
        return cameraManager;
    }





    /*
    @Subscribe
    public void onReceiveClimberIdChange(ClimberIdChange event){
        Log.d(TAG + ".onReceiveClimberIdChange()", "Received ClimberIdChange event. Length = " + event.getIdLength());
        climberId = mClimberIdEdit.getText().toString();

        if(cNameImmune == false) {
            climberName = "";
            mClimberNameEdit.setText(climberName);
        }
        else{
            cNameImmune = false;
        }

        BusProvider.getInstance().post(new ScanNotFinish());

        if(event.getIdLength() == 3){
            mNextButton.setEnabled(true);
        }
        else{
            mNextButton.setEnabled(false);
        }
    }
    */


    private void startThread() {
        mDecodeThread = new DecodeThread(mScanFragmentHandler,
                getActivity().getString(R.string.qr_prefix),
                previewResolution);
        if(mDecodeThread.getState() == Thread.State.NEW)
            mDecodeThread.start();
        mScanFragmentHandler.setRunning(true);
    }

    private void acquireCamera(){
        // We need camera resource. We will acquire camera in onResume and release in onPause.
        // Check if we have camera resource first.
        if (!cameraManager.hasCamera()) {
            // No Camera resource.
            Log.d(TAG + ".acquireCamera()", "No camera resource. Will attempt to acquire camera.");

            boolean temp = cameraManager.acquireCamera(previewResolution);
            if (temp == false) {
                // Error handling. Fail to get camera resource.
                Log.e(TAG + ".acquireCamera()", "Failed to get camera resource.");
            }
        }
    }

    private void acquirePreview(){
        //TODO Not sure if we can assume camera acquired. Perform check for safety.
        if (cameraManager.hasCamera()) {
            Log.d(TAG + ".acquirePreview()", "hasCamera");
            createPreviewAndArrange();
        } else {
            Log.d(TAG + ".acquirePreview()", "no camera");
        }
    }

    private void startScan(){
        if (mState == State.SCANNING) {
            if (cameraManager.isSurfaceReady()) {
                cameraManager.startPreview(previewView.getHolder());
                cameraManager.startScan();
            }
        }
    }

    private void releaseCameraAndStopDecodeThread(){
        if(mScanFragmentHandler != null){
            mScanFragmentHandler.setRunning(false);
        }
        else{
            Log.e(TAG + ".releaseCameraAndStopDecodeThread()", "mScanFragmentHandler == null while trying to setRunning(false).");
        }

        if (cameraManager != null) {
            cameraManager.stopPreview();
            cameraManager.releaseCamera();
        }
        else{
            Log.e(TAG+".releaseCameraAndStopDecodeThread()", "cameraManager == null while trying to stopPreview() and releaseCameraAndStopDecodeThread()");
        }

        mScanFragmentHandler.onPause();
    }


    /**
     * This method reset activity state to "decode" and restart scanning.
     * No-op if previewView surface is not ready.
     */
    public void rescan(){
        mClimber.setClimberId(null);
        mClimber.setClimberName(null);
        mClimber.setTotalScore(null);

        changeState(State.SCANNING);
    }

    /**
     * Method to toggle the camera flash.
     */
    public void toggleFlash(){
        getCameraManager().setFlash(!getCameraManager().isTorchOn());
    }

    public void next(){

    }

    public Handler getDecodeHandler(){
        return mDecodeThread.getHandler();
    }

    public Thread getDecodeThread(){
        return mDecodeThread;
    }

    @Override
    public CharSequence getPageTitle() {
        return "Scan";
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.scan_rescan_button:
                break;
            case R.id.scan_flash_button:
                break;
            case R.id.scan_next_button:
                break;
        }
    }




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
}
