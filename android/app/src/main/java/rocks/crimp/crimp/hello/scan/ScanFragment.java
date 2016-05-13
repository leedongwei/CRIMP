package rocks.crimp.crimp.hello.scan;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Message;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import rocks.crimp.crimp.CrimpApplication;
import rocks.crimp.crimp.R;
import rocks.crimp.crimp.common.Action;
import rocks.crimp.crimp.common.event.DecodeFail;
import rocks.crimp.crimp.common.event.DecodeSucceed;
import rocks.crimp.crimp.common.event.SwipeTo;
import rocks.crimp.crimp.network.model.CategoriesJs;
import rocks.crimp.crimp.network.model.CategoryJs;
import rocks.crimp.crimp.service.ServiceHelper;
import timber.log.Timber;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class ScanFragment extends Fragment implements SurfaceHolder.Callback,
        View.OnClickListener{
    public static final String ARGS_POSITION = "INT_POSITION";
    public static final String ARGS_TITLE = "STRING_TITLE";
    public static final String MARKER_ID_PATTERN = ".{3}[0-9]{3}";
    public static final int MARKER_ID_DIGIT_START = 3;
    public static final int MARKER_ID_DIGIT_END = 6;

    private static final String SCREEN_WIDTH_PX = "screen_width_px";
    private static final String ASPECT_RATIO = "aspect_ratio";
    private static final String DISPLAY_ROTATION = "display rotation";

    private ScanFragmentInterface mParent;

    private SurfaceView mPreviewFrame;
    private RelativeLayout mInputLayout;
    private EditText mCategoryIdText;
    private ImageButton mFlashButton;
    private Button mRescanButton;
    private EditText mMarkerIdText;
    private EditText mClimberNameText;
    private Button mScanNextButton;
    private LinearLayout mMarkerLayout;
    private TextView mMarkerValid;
    private Button mClearButton;

    private DecodeThread mDecodeThread;
    private CrimpCameraManager mCameraManager;
    private int mDisplayRotation;
    private int mTargetWidth;
    private float mAspectRatio;
    private int mPosition;
    private boolean mIsSurfaceReady;

    private boolean mIsShowing;
    private boolean mIsOnResume;
    private boolean mIsScanning;

    public static ScanFragment newInstance(int position, String title){
        ScanFragment f = new ScanFragment();
        Bundle args = new Bundle();
        args.putInt(ARGS_POSITION, position);
        args.putString(ARGS_TITLE, title);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mParent = (ScanFragmentInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement ScanFragmentInterface");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mPosition = getArguments().getInt(ARGS_POSITION);

        mCameraManager = new CrimpCameraManager();

        // We need 3 things: 1) screen width(px), 2)ideal aspect ratio of SurfaceVIew,
        // 3)display rotation
        if(savedInstanceState != null){
            mTargetWidth = savedInstanceState.getInt(SCREEN_WIDTH_PX);
            mAspectRatio = savedInstanceState.getFloat(ASPECT_RATIO);
            mDisplayRotation = savedInstanceState.getInt(DISPLAY_ROTATION);
        }
        else{
            // Context.getResources().getDisplayMetrics() gives the resolution of the screen without
            // screen decorations (i.e. Navigation bar) in pixels.
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            mTargetWidth = displayMetrics.widthPixels;
            float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
            float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
            final int dpTabLayoutHeight = 48;
            // Remaining height is the height of the space available after subtract away TabLayout.
            final int dpRemainingHeight = (int) (dpHeight - dpTabLayoutHeight);
            mAspectRatio = dpRemainingHeight / dpWidth;

            WindowManager windowManager = (WindowManager)getActivity()
                    .getSystemService(Context.WINDOW_SERVICE);
            mDisplayRotation = windowManager.getDefaultDisplay().getRotation();
            Timber.d("----------Display information:----------\n" +
                            "Without decoration(px): W%dpx, H%dpx\n" +
                            "Without decoration(dp): W%fdp, H%fdp\n" +
                            "Logical density: %f, aspect ratio (after removing TabLayout): %f\n" +
                            "displayRotation: %d degree",
                    displayMetrics.widthPixels, displayMetrics.heightPixels, dpWidth, dpHeight,
                    displayMetrics.density, mAspectRatio, mDisplayRotation);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.fragment_scan, container, false);

        // Find references to Views
        mPreviewFrame = (SurfaceView) rootView.findViewById(R.id.scan_frame);
        mInputLayout = (RelativeLayout) rootView.findViewById(R.id.scan_form);
        mCategoryIdText = (EditText)rootView.findViewById(R.id.scan_category_id_edit);
        mFlashButton = (ImageButton)rootView.findViewById(R.id.scan_flash_button);
        mRescanButton = (Button)rootView.findViewById(R.id.scan_rescan_button);
        mMarkerIdText = (EditText)rootView.findViewById(R.id.scan_marker_id_edit);
        mClimberNameText = (EditText)rootView.findViewById(R.id.scan_climber_name_edit);
        mScanNextButton = (Button)rootView.findViewById(R.id.scan_next_button);
        mMarkerLayout = (LinearLayout) rootView.findViewById(R.id.scan_valid_marker_layout);
        mMarkerValid = (TextView) rootView.findViewById(R.id.scan_valid_marker_text);
        mClearButton = (Button) rootView.findViewById(R.id.scan_clear_button);

        mPreviewFrame.getHolder().addCallback(this);
        mScanNextButton.setOnClickListener(this);
        mRescanButton.setOnClickListener(this);
        mClearButton.setOnClickListener(this);
        mFlashButton.setOnClickListener(this);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mPreviewFrame.getLayoutParams();
        params.height = CrimpApplication.getAppState().getInt(CrimpApplication.IMAGE_HEIGHT, 0);
        mPreviewFrame.setLayoutParams(params);
        mMarkerIdText.addTextChangedListener(new MarkerIdTextWatcher(new Action() {
            @Override
            public void act() {
                // prepare button info
                int categoryPosition = CrimpApplication.getAppState()
                        .getInt(CrimpApplication.COMMITTED_CATEGORY, 0);
                CategoriesJs categoriesJs = mParent.getCategoriesJs();
                String categoryAcronym;
                if(categoriesJs != null && categoryPosition != 0) {
                    // minus one from categoryPosition because of hint in spinner adapter.
                    categoryAcronym = categoriesJs.getCategories().get(categoryPosition-1).getAcronym();
                }
                else {
                    throw new RuntimeException("Unable to find out category Scan tab");
                }
                String digits = mMarkerIdText.getText().toString();
                mMarkerValid.setText(categoryAcronym+digits);

                // hide mCategoryIdText, mMarkerIdText and soft keyboard. Show button
                InputMethodManager imm = (InputMethodManager)getActivity()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mMarkerIdText.getWindowToken(), 0);
                mCategoryIdText.setVisibility(View.GONE);
                mMarkerIdText.setVisibility(View.GONE);
                mMarkerLayout.setVisibility(View.VISIBLE);
            }
        }, new Action() {
            @Override
            public void act() {
                mScanNextButton.setEnabled(true);
            }
        }, new Action() {
            @Override
            public void act() {
                mScanNextButton.setEnabled(false);
            }
        }));

        return rootView;
    }

    @Override
    public void onStart(){
        super.onStart();
        CrimpApplication.getBusInstance().register(this);
        mDecodeThread = new DecodeThread();
        if(mDecodeThread.getState() == Thread.State.NEW) {
            mDecodeThread.start();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        mIsOnResume = true;
        onStateChangeCheckScanning();
    }

    @Override
    public void onPause(){
        mIsOnResume = false;
        onStateChangeCheckScanning();
        super.onPause();
    }

    @Override
    public void onStop(){
        CrimpApplication.getBusInstance().unregister(this);
        Message quitMessage = mDecodeThread.getHandler().obtainMessage();
        quitMessage.what = DecodeHandler.QUIT;
        quitMessage.sendToTarget();
        super.onStop();
    }

    @Override
    public void onClick(View view){
        switch(view.getId()){
            case R.id.scan_rescan_button: {
                // We only need to show dialog if user has enter stuff on Score tab.
                String currentScore = CrimpApplication.getAppState()
                        .getString(CrimpApplication.CURRENT_SCORE, null);
                if (currentScore == null || currentScore.length() == 0) {
                    rescan();
                    // We set SHOULD_SCAN to true. Check if we should start scanning.
                    onStateChangeCheckScanning();
                } else {
                    // Prepare stuff to use in NextDialog creation.
                    String markerId = CrimpApplication.getAppState().getString(CrimpApplication.MARKER_ID, null);
                    String climberName = CrimpApplication.getAppState().getString(CrimpApplication.CLIMBER_NAME, null);
                    int categoryPosition = CrimpApplication.getAppState()
                            .getInt(CrimpApplication.CATEGORY_POSITION, 0);
                    int routePosition = CrimpApplication.getAppState()
                            .getInt(CrimpApplication.ROUTE_POSITION, 0);
                    CategoryJs chosenCategory =
                            mParent.getCategoriesJs().getCategories().get(categoryPosition - 1);
                    String routeName = chosenCategory.getRoutes().get(routePosition - 1).getRouteName();

                    RescanDialog.create(getActivity(), new Action() {
                        @Override
                        public void act() {
                            rescan();
                            // We set SHOULD_SCAN to true. Check if we should start scanning.
                            onStateChangeCheckScanning();
                        }
                    }, new Action() {
                        @Override
                        public void act() {
                            // Do nothing
                        }
                    }, markerId, climberName, routeName);
                }
                break;
            }

            case R.id.scan_next_button: {
                int categoryPosition = CrimpApplication.getAppState()
                        .getInt(CrimpApplication.COMMITTED_CATEGORY, 0);
                CategoriesJs categoriesJs = mParent.getCategoriesJs();
                CategoryJs category;
                if (categoriesJs != null && categoryPosition != 0) {
                    // minus one from categoryPosition because of hint in spinner adapter.
                    category = categoriesJs.getCategories().get(categoryPosition - 1);
                } else {
                    throw new RuntimeException("Unable to find out category Scan tab");
                }

                String markerId = mMarkerValid.getText().toString();
                String climberName = mClimberNameText.getText().toString();
                CrimpApplication.getAppState().edit()
                        .putString(CrimpApplication.MARKER_ID, markerId)
                        .putString(CrimpApplication.CLIMBER_NAME, climberName)
                        .remove(CrimpApplication.ACCUMULATED_SCORE)
                        .remove(CrimpApplication.CURRENT_SCORE)
                        .commit();

                int routePosition = CrimpApplication.getAppState()
                        .getInt(CrimpApplication.ROUTE_POSITION, 0);
                String userId = CrimpApplication.getAppState()
                        .getString(CrimpApplication.FB_USER_ID, "user_id");
                String accessToken = CrimpApplication.getAppState()
                        .getString(CrimpApplication.FB_ACCESS_TOKEN, "token");
                long sequentialToken = CrimpApplication.getAppState()
                        .getLong(CrimpApplication.SEQUENTIAL_TOKEN, -1);
                long routeId = category.getRoutes().get(routePosition - 1).getRouteId();

                // We don't care about response so we are not keeping track of txId.
                ServiceHelper.clearActive(getActivity(), null, userId, accessToken, sequentialToken,
                        routeId);
                ServiceHelper.setActive(getActivity(), null, userId, accessToken, sequentialToken,
                        routeId, markerId);
                mParent.goToScoreTab();
                break;
            }

            case R.id.scan_clear_button: {
                // We only need to show dialog if user has enter stuff on Score tab.
                String currentScore = CrimpApplication.getAppState()
                        .getString(CrimpApplication.CURRENT_SCORE, null);
                if (currentScore == null || currentScore.length() == 0) {
                    rescan();
                    // We set SHOULD_SCAN to true. Check if we should start scanning.
                    onStateChangeCheckScanning();
                } else {
                    // Prepare stuff to use in NextDialog creation.
                    String markerId = CrimpApplication.getAppState().getString(CrimpApplication.MARKER_ID, null);
                    String climberName = CrimpApplication.getAppState().getString(CrimpApplication.CLIMBER_NAME, "");
                    int categoryPosition = CrimpApplication.getAppState()
                            .getInt(CrimpApplication.CATEGORY_POSITION, 0);
                    int routePosition = CrimpApplication.getAppState()
                            .getInt(CrimpApplication.ROUTE_POSITION, 0);
                    CategoryJs chosenCategory =
                            mParent.getCategoriesJs().getCategories().get(categoryPosition - 1);
                    String routeName = chosenCategory.getRoutes().get(routePosition - 1).getRouteName();

                    RemoveDialog.create(getActivity(), new Action() {
                        @Override
                        public void act() {
                            rescan();
                            // We set SHOULD_SCAN to true. Check if we should start scanning.
                            onStateChangeCheckScanning();
                        }
                    }, new Action() {
                        @Override
                        public void act() {
                            // Do nothing
                        }
                    }, markerId, climberName, routeName).show();
                }
                break;
            }

            case R.id.scan_flash_button:{
                Toast toast = Toast.makeText(getActivity(), "STUB!", Toast.LENGTH_SHORT);
                toast.show();
                break;
            }
        }
    }

    private void rescan(){
        // We want to disable access to scan tab.
        int canDisplay = CrimpApplication.getAppState().getInt(CrimpApplication.CAN_DISPLAY, 0b001);
        canDisplay = canDisplay & 0b011;
        mParent.setCanDisplay(canDisplay);

        mMarkerLayout.setVisibility(View.GONE);
        mMarkerIdText.setVisibility(View.VISIBLE);
        mCategoryIdText.setVisibility(View.VISIBLE);

        // Set textbox on scan tab to null
        mMarkerIdText.setText(null);
        mClimberNameText.setText(null);

        CrimpApplication.getAppState().edit()
                .putBoolean(CrimpApplication.SHOULD_SCAN, true)
                .remove(CrimpApplication.MARKER_ID)
                .remove(CrimpApplication.CLIMBER_NAME)
                .remove(CrimpApplication.MARKER_ID_TEMP)
                .commit();


    }

    private void showInfoPanel(){
        mMarkerLayout.setVisibility(View.GONE);
        mCategoryIdText.setVisibility(View.VISIBLE);
        mMarkerIdText.setVisibility(View.VISIBLE);

        int categoryPosition = CrimpApplication.getAppState()
                .getInt(CrimpApplication.COMMITTED_CATEGORY, 0);
        CategoriesJs categoriesJs = mParent.getCategoriesJs();
        String categoryAcronym;
        if(categoriesJs != null && categoryPosition != 0) {
            // minus one from categoryPosition because of hint in spinner adapter.
            categoryAcronym = categoriesJs.getCategories().get(categoryPosition-1).getAcronym();
        }
        else {
            throw new RuntimeException("Unable to find out category Scan tab");
        }
        mCategoryIdText.setText(categoryAcronym);

        String markerId = CrimpApplication.getAppState().getString(CrimpApplication.MARKER_ID_TEMP, null);
        mMarkerIdText.setText(markerId);

        if(markerId == null || markerId.length()==0){
            mClimberNameText.setText(null);
        }
        else{
            String climberName = CrimpApplication.getAppState().getString(CrimpApplication.CLIMBER_NAME, "");
            mClimberNameText.setText(climberName);
        }
    }

    @Subscribe
    public void onReceivedSwipeTo(SwipeTo event){
        Timber.d("onReceivedSwipeTo: %d", event.position);
        if(mMarkerIdText != null) {
            InputMethodManager imm = (InputMethodManager) getActivity()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mMarkerIdText.getWindowToken(), 0);
        }
        mIsShowing = (event.position == mPosition);
        if(mIsShowing){
            showInfoPanel();
        }
        onStateChangeCheckScanning();
    }

    @Subscribe
    public void onReceivedDecodeSucceed(DecodeSucceed event){
        Timber.d("Received decode succeed. mIsScanning: %b", mIsScanning);
        if(mIsScanning){
            mIsScanning = false;

            // Check result against category.
            int categoryPosition = CrimpApplication.getAppState()
                    .getInt(CrimpApplication.COMMITTED_CATEGORY, 0);
            CategoriesJs categoriesJs = mParent.getCategoriesJs();
            String categoryAcronym;
            if(categoriesJs != null && categoryPosition != 0) {
                // minus one from categoryPosition because of hint in spinner adapter.
                categoryAcronym = categoriesJs.getCategories().get(categoryPosition-1).getAcronym();
            }
            else {
                throw new RuntimeException("Unable to find out category Scan tab");
            }
            String regex = categoryAcronym+".++";
            boolean isValid = event.result.matches(regex);

            // We only vibrate and stop scan if the result is valid. Otherwise this is as good as
            // onReceivedDecodeFail().
            if(isValid){
                CrimpApplication.getAppState().edit()
                        .putBoolean(CrimpApplication.SHOULD_SCAN, false).commit();
                mRescanButton.setEnabled(true);

                // vibrate 100ms
                Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(100);

                String[] subStrings = event.result.split(categoryAcronym+"|;");
                mMarkerIdText.setText(subStrings[1]);
                mClimberNameText.setText(subStrings[2]);
                mParent.setDecodedImage(event.image);
            }

            onStateChangeCheckScanning();
        }
    }

    @Subscribe
    public void onReceivedDecodeFail(DecodeFail event){
        if(mIsScanning){
            mIsScanning = false;
            onStateChangeCheckScanning();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Timber.d("Surface created");
        mIsSurfaceReady = true;
        if(mCameraManager != null){
            mCameraManager.setIsSurfaceReady(true, holder);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Timber.d("Surface changed");
        boolean shouldScan = CrimpApplication.getAppState()
                .getBoolean(CrimpApplication.SHOULD_SCAN, true);
        if(!shouldScan){
            mRescanButton.setEnabled(true);
            Bitmap image = mParent.getDecodedImage();
            Timber.d("image: %s", image);
            if(image != null) {
                Canvas canvas = holder.lockCanvas();
                if (canvas != null) {
                    canvas.drawBitmap(image, 0, 0, null);
                    mPreviewFrame.getHolder().unlockCanvasAndPost(canvas); //finalize
                }
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Timber.d("Surface destroyed");
        mIsSurfaceReady = false;
        if(mCameraManager != null){
            mCameraManager.setIsSurfaceReady(false, holder);
        }
    }

    /**
     * This method is called when something happen that might affect whether we will be scanning.
     * Do a check start scan if necessary.
     */
    private void onStateChangeCheckScanning(){
        boolean shouldScan = CrimpApplication.getAppState()
                .getBoolean(CrimpApplication.SHOULD_SCAN, true);

        mRescanButton.setEnabled(!shouldScan);

        //Timber.d("mIsOnResume: %b, mIsShowing: %b, shouldScan: %b, mIsScanning: %b",
        //        mIsOnResume, mIsShowing, shouldScan, mIsScanning);

        if(mIsOnResume && mIsShowing && shouldScan && !mIsScanning){
            mCameraManager.acquireCamera(mTargetWidth, mAspectRatio, mDisplayRotation);

            if(!mIsSurfaceReady){
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mPreviewFrame.getLayoutParams();
                params.height = mCameraManager.getPxScreenHeight();
                params.width = mTargetWidth;
                mPreviewFrame.setLayoutParams(params);
            }

            mCameraManager.startPreview(mPreviewFrame.getHolder());

            mIsScanning = true;
            mCameraManager.startScan(mDecodeThread);
        }

        if(!mIsOnResume || !mIsShowing || !shouldScan){
            mCameraManager.stopPreview();
            mCameraManager.releaseCamera();
            mIsScanning = false;
        }
    }

    public interface ScanFragmentInterface{
        void setDecodedImage(Bitmap image);
        Bitmap getDecodedImage();
        CategoriesJs getCategoriesJs();
        void goToScoreTab();
        void setCanDisplay(int canDisplay);
    }
}
