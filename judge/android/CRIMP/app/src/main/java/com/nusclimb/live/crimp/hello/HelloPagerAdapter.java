package com.nusclimb.live.crimp.hello;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.nusclimb.live.crimp.R;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class HelloPagerAdapter extends PagerAdapter {
    private static final String TAG = "HelloPagerAdapter";
    private static final boolean DEBUG = true;

    private static final int COUNT = 3;

    private boolean[] canDisplay;
    private Context mContext;
    private ArrayAdapter<String> categoryAdapter;
    private ArrayAdapter<String> routeAdapter;

    public HelloPagerAdapter(Context context) {
        super();
        this.mContext = context;
        canDisplay = new boolean[COUNT];
        for(int i=0; i<COUNT; i++){
            canDisplay[i] = true;
        }
    }

    public boolean[] getCanDisplay(){
        return canDisplay;
    }

    @Override
    public Object instantiateItem (ViewGroup container, int position){
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View layout;

        switch(position){
            case 0:
                layout = inflater.inflate(R.layout.layout_route, container, false);
                break;

            case 1:
                layout = inflater.inflate(R.layout.layout_scan, container, false);
                break;

            default:
                layout = inflater.inflate(R.layout.layout_score, container, false);
        }

        container.addView(layout);

        return layout;
    }

    @Override
    public void destroyItem (ViewGroup container, int position, Object object){
        if(DEBUG) Log.d(TAG, "destroy "+position);
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
        public Button nextButton;
        public TextView routeReplaceText;
        public LinearLayout yesNoGroup;
    }

    public static class ScanViewHolder{

    }

    public static class ScoreViewHolder{

    }
}
