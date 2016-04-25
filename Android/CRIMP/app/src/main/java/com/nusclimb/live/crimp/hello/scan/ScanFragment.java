package com.nusclimb.live.crimp.hello.scan;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.Display;
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

import com.nusclimb.live.crimp.CrimpApplication2;
import com.nusclimb.live.crimp.R;

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
    private View mTransparentView;
    private RelativeLayout mInputLayout;
    private EditText mCategoryIdText;
    private ImageButton mFlashButton;
    private Button mRescanButton;
    private EditText mClimberIdText;
    private EditText mClimberNameText;
    private Button mScanNextButton;

    private ScanFragmentHandler mScanFragmentHandler;
    private DecodeThread mDecodeThread;
    private CrimpCameraManager mCameraManager;


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
        mScanFragmentHandler = new ScanFragmentHandler(this);
        mCameraManager = new CrimpCameraManager();
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

        WindowManager windowManager = (WindowManager)getActivity()
                .getSystemService(Context.WINDOW_SERVICE);
        int rotation = windowManager.getDefaultDisplay().getRotation();
        mCameraManager.setDisplayRotation(rotation);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.fragment_scan, container, false);

        mPreviewFrame = (SurfaceView) rootView.findViewById(R.id.scan_frame);
        mTransparentView = rootView.findViewById(R.id.scan_transparent);
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
    public void onViewCreated(View view, Bundle savedInstanceState){

    }

    @Override
    public void onStart(){
        super.onStart();
        CrimpApplication2.getBusInstance().register(this);

        mDecodeThread = new DecodeThread(mScanFragmentHandler);
        mCameraManager.setDecodeThread(mDecodeThread);

        WindowManager manager = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point screenResolution = new Point();	//application display size without system decoration
        screenResolution.x = display.getWidth();
        screenResolution.y = display.getHeight();

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int overlayHeight = 192 * metrics.densityDpi / 160;
        int transparentHeight = screenResolution.y - overlayHeight;
        Point transparentResolution = new Point(screenResolution.x, transparentHeight);
    }

    @Override
    public void onStop(){
        CrimpApplication2.getBusInstance().unregister(this);
        super.onStop();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Timber.d("Surface created");
        if(mCameraManager != null){
            mCameraManager.setIsSurfaceReady(true);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Timber.d("Surface changed");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Timber.d("Surface destroyed");
        if(mCameraManager != null){
            mCameraManager.setIsSurfaceReady(false);
        }
    }

    public interface ScanFragmentInterface{

    }
}
