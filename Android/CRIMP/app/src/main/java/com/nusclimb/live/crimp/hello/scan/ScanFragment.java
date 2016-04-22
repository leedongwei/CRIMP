package com.nusclimb.live.crimp.hello.scan;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nusclimb.live.crimp.R;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class ScanFragment extends Fragment {
    public static final String TAG = "ScanFragment";
    public static final boolean DEBUG = true;

    public static final String ARGS_POSITION = "INT_POSITION";
    public static final String ARGS_TITLE = "STRING_TITLE";

    private FrameLayout mPreviewFrame;
    private View mTransparentView;
    private RelativeLayout mInputLayout;
    private EditText mCategoryIdText;
    private ImageButton mFlashButton;
    private Button mRescanButton;
    private EditText mClimberIdText;
    private EditText mClimberNameText;
    private Button mScanNextButton;


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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_scan, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        mPreviewFrame = (FrameLayout) view.findViewById(R.id.scan_frame);
        mTransparentView = view.findViewById(R.id.scan_transparent);
        mInputLayout = (RelativeLayout) view.findViewById(R.id.scan_form);
        mCategoryIdText = (EditText)view.findViewById(R.id.scan_category_id_edit);
        mFlashButton = (ImageButton)view.findViewById(R.id.scan_flash_button);
        mRescanButton = (Button)view.findViewById(R.id.scan_rescan_button);
        mClimberIdText = (EditText)view.findViewById(R.id.scan_climber_id_edit);
        mClimberNameText = (EditText)view.findViewById(R.id.scan_climber_name_edit);
        mScanNextButton = (Button)view.findViewById(R.id.scan_next_button);
    }
}
