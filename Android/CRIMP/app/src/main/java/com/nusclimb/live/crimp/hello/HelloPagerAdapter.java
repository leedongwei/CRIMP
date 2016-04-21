package com.nusclimb.live.crimp.hello;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.nusclimb.live.crimp.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import timber.log.Timber;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class HelloPagerAdapter extends PagerAdapter {
    private static final String TAG = "HelloPagerAdapter";
    private static final boolean DEBUG = true;

    private static final int COUNT = 3;

    private ArrayList<WeakReference<View>> viewArray = new ArrayList<>();
    private boolean[] canDisplay;
    private Context mContext;

    public HelloPagerAdapter(Context context) {
        super();
        this.mContext = context;
        canDisplay = new boolean[COUNT];
        for(int i=0; i<COUNT; i++){
            canDisplay[i] = true;
        }
        for(int i=0; i<COUNT; i++){
            viewArray.add(null);
        }
    }

    public boolean[] getCanDisplay(){
        return canDisplay;
    }

    @Nullable
    public WeakReference<View> getView(int position){
        if(position>=0 && position<viewArray.size()){
            return viewArray.get(position);
        }

        return null;
    }

    @Override
    public Object instantiateItem (ViewGroup container, int position){
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View layout;

        switch(position){
            case 0:
                layout = inflater.inflate(R.layout.layout_route, container, false);
                RouteViewHolder routeHolder = new RouteViewHolder();
                routeHolder.swipeLayout = (SwipeRefreshLayout)layout.findViewById(R.id.swipe_layout);
                routeHolder.loadWheel = (ProgressBar)layout.findViewById(R.id.route_wheel_progressbar);
                routeHolder.statusText = (TextView)layout.findViewById(R.id.route_request_status_text);
                routeHolder.retryButton = (Button)layout.findViewById(R.id.route_retry_button);
                routeHolder.helloText = (TextView)layout.findViewById(R.id.route_hello_text);
                routeHolder.categorySpinner = (Spinner)layout.findViewById(R.id.route_category_spinner);
                routeHolder.routeSpinner = (Spinner)layout.findViewById(R.id.route_route_spinner);
                routeHolder.routeNextButton = (Button)layout.findViewById(R.id.route_next_button);
                routeHolder.routeReplaceText = (TextView)layout.findViewById(R.id.route_replace_text);
                routeHolder.yesNoGroup = (LinearLayout)layout.findViewById(R.id.route_yes_no_viewgroup);
                layout.setTag(routeHolder);
                break;

            case 1:
                layout = inflater.inflate(R.layout.layout_scan, container, false);
                ScanViewHolder scanHolder = new ScanViewHolder();
                scanHolder.previewFrame = (FrameLayout)layout.findViewById(R.id.scan_frame);
                scanHolder.transparentView = layout.findViewById(R.id.scan_transparent);
                scanHolder.inputLayout = (RelativeLayout)layout.findViewById(R.id.scan_form);
                scanHolder.categoryIdText = (EditText)layout.findViewById(R.id.scan_category_id_edit);
                scanHolder.flashButton = (ImageButton)layout.findViewById(R.id.scan_flash_button);
                scanHolder.rescanButton = (Button)layout.findViewById(R.id.scan_rescan_button);
                scanHolder.climberIdText = (EditText)layout.findViewById(R.id.scan_climber_id_edit);
                scanHolder.climberNameText = (EditText)layout.findViewById(R.id.scan_climber_name_edit);
                scanHolder.scanNextButton = (Button)layout.findViewById(R.id.scan_next_button);
                layout.setTag(scanHolder);
                break;

            case 2:
                layout = inflater.inflate(R.layout.layout_score, container, false);
                ScoreViewHolder scoreHolder = new ScoreViewHolder();
                scoreHolder.categoryText = (TextView)layout.findViewById(R.id.score_category_text);
                scoreHolder.routeText = (TextView)layout.findViewById(R.id.score_route_text);
                scoreHolder.climberIdText = (EditText)layout.findViewById(R.id.score_climberId_edit);
                scoreHolder.climberNameText = (EditText)layout.findViewById(R.id.score_climberName_edit);
                scoreHolder.accumulatedText = (EditText)layout.findViewById(R.id.score_accumulated_edit);
                scoreHolder.currentText = (EditText)layout.findViewById(R.id.score_current_edit);
                scoreHolder.scoreModuleLayout = (ViewStub)layout.findViewById(R.id.score_score_fragment);
                scoreHolder.submitButton = (Button)layout.findViewById(R.id.score_submit_button);
                layout.setTag(scoreHolder);
                break;

            default:
                layout = null;
        }

        container.addView(layout);
        viewArray.set(position, new WeakReference<>(layout));

        Timber.d("instantiated page %d", position);
        return layout;
    }

    @Override
    public void destroyItem (ViewGroup container, int position, Object object){
        container.removeView((View)object);
    }

    @Override
    public int getCount() {
        return COUNT;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(int position){
        switch(position) {
            case 0: return "Route";
            case 1: return "Scan";
            case 2: return "Score";
            default: return null;
        }
    }

    public static class RouteViewHolder{
        public SwipeRefreshLayout swipeLayout;
        public ProgressBar loadWheel;
        public TextView statusText;
        public Button retryButton;
        public TextView helloText;
        public Spinner categorySpinner;
        public Spinner routeSpinner;
        public Button routeNextButton;
        public TextView routeReplaceText;
        public LinearLayout yesNoGroup;
    }

    public static class ScanViewHolder{
        public FrameLayout previewFrame;
        public View transparentView;
        public RelativeLayout inputLayout;
        public EditText categoryIdText;
        public ImageButton flashButton;
        public Button rescanButton;
        public EditText climberIdText;
        public EditText climberNameText;
        public Button scanNextButton;
    }

    public static class ScoreViewHolder{
        public TextView categoryText;
        public TextView routeText;
        public EditText climberIdText;
        public EditText climberNameText;
        public EditText accumulatedText;
        public EditText currentText;
        public ViewStub scoreModuleLayout;
        public Button submitButton;
    }
}
