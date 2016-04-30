package rocks.crimp.crimp.hello.scan;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.squareup.otto.Subscribe;

import rocks.crimp.crimp.CrimpApplication;
import rocks.crimp.crimp.R;
import rocks.crimp.crimp.common.event.SwipeTo;
import timber.log.Timber;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class ScanFragment extends Fragment implements SurfaceHolder.Callback{
    public static final String TAG = "ScanFragment";
    public static final boolean DEBUG = true;

    public static final String ARGS_POSITION = "INT_POSITION";
    public static final String ARGS_TITLE = "STRING_TITLE";

    private ScanFragmentInterface mParent;

    private SurfaceView mPreviewFrame;
    private RelativeLayout mInputLayout;
    private EditText mCategoryIdText;
    private ImageButton mFlashButton;
    private Button mRescanButton;
    private EditText mClimberIdText;
    private EditText mClimberNameText;
    private Button mScanNextButton;

    private ScanFragmentHandler mMainHandler;
    private DecodeThread mDecodeThread;
    private CrimpCameraManager mCameraManager;
    private int mDisplayRotation;
    private int mTargetWidth;
    private float mAspectRatio;
    private int mPosition;
    private boolean mIsSurfaceReady;

    private boolean mIsShowing;
    private boolean mIsOnResume;

    public static ScanFragment newInstance(int position, String title){
        ScanFragment f = new ScanFragment();
        // TODO set arguments
        Bundle args = new Bundle();
        args.putInt(ARGS_POSITION, position);
        args.putString(ARGS_TITLE, title);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mMainHandler = new ScanFragmentHandler(this);
        mCameraManager = new CrimpCameraManager();

        mPosition = getArguments().getInt(ARGS_POSITION);
    }

    @SuppressLint("BinaryOperationInTimber")
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mParent = (ScanFragmentInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement ScanFragmentInterface");
        }

        // We need 3 things: 1) screen width(px), 2)ideal aspect ratio of SurfaceVIew,
        // 3)display rotation
        // Context.getResources().getDisplayMetrics() gives the resolution of the screen without
        // screen decorations (i.e. Navigation bar) in pixels.
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        mTargetWidth = displayMetrics.widthPixels;
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        final int dpTabLayoutHeight = 48;
        // Remaining height is the height of the space available after subtract away TabLayout.
        final int dpRemainingHeight = (int) (dpHeight - dpTabLayoutHeight);
        mAspectRatio = dpRemainingHeight / dpWidth;

        WindowManager windowManager = (WindowManager)context
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.fragment_scan, container, false);

        mPreviewFrame = (SurfaceView) rootView.findViewById(R.id.scan_frame);
        mInputLayout = (RelativeLayout) rootView.findViewById(R.id.scan_form);
        mCategoryIdText = (EditText)rootView.findViewById(R.id.scan_category_id_edit);
        mFlashButton = (ImageButton)rootView.findViewById(R.id.scan_flash_button);
        mRescanButton = (Button)rootView.findViewById(R.id.scan_rescan_button);
        mClimberIdText = (EditText)rootView.findViewById(R.id.scan_climber_id_edit);
        mClimberNameText = (EditText)rootView.findViewById(R.id.scan_climber_name_edit);
        mScanNextButton = (Button)rootView.findViewById(R.id.scan_next_button);

        mPreviewFrame.getHolder().addCallback(this);

        return rootView;
    }

    @Override
    public void onStart(){
        super.onStart();
        CrimpApplication.getBusInstance().register(this);

        mDecodeThread = new DecodeThread(mMainHandler);
        if(mDecodeThread.getState() == Thread.State.NEW) {
            mDecodeThread.start();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        mIsOnResume = true;

        onStateChangeCheckSurface();
    }

    @Override
    public void onPause(){
        mIsOnResume = false;

        onStateChangeCheckSurface();
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

    @Subscribe
    public void onReceivedSwipeTo(SwipeTo event){
        Timber.d("onReceivedSwipeTo: %d", event.position);
        mIsShowing = (event.position == mPosition);
        onStateChangeCheckSurface();
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
    private void onStateChangeCheckSurface(){
        if(mIsOnResume && mIsShowing && mParent.getIsScanning()){
            mCameraManager.acquireCamera(mTargetWidth, mAspectRatio, mDisplayRotation);

            if(!mIsSurfaceReady){
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mPreviewFrame.getLayoutParams();
                params.height = mCameraManager.getPxScreenHeight();
                params.width = mTargetWidth;
                mPreviewFrame.setLayoutParams(params);
            }

            //TODO MIGHT WANT TO REMOVE THIS
            Message message = mDecodeThread.getHandler().obtainMessage();
            message.what = DecodeHandler.INITIALIZE;
            message.obj = "<BA2015>";
            message.arg1 = mTargetWidth;
            message.arg2 = mCameraManager.getPxScreenHeight();
            message.sendToTarget();

            mCameraManager.startPreview(mPreviewFrame.getHolder());

            mCameraManager.startScan(mDecodeThread);
        }
        else{
            mCameraManager.stopPreview();
            mCameraManager.releaseCamera();
        }
    }

    public interface ScanFragmentInterface{
        void setIsScanning(boolean isScanning);
        boolean getIsScanning();
    }
}
