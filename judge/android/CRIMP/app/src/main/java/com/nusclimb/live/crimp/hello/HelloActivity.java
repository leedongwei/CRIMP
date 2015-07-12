package com.nusclimb.live.crimp.hello;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.facebook.FacebookSdk;
import com.nusclimb.live.crimp.CrimpFragmentPagerAdapter;
import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.common.BusProvider;
import com.nusclimb.live.crimp.common.busevent.RouteFinish;
import com.nusclimb.live.crimp.common.busevent.RouteNotFinish;
import com.nusclimb.live.crimp.common.busevent.ScanAcquireCamera;
import com.nusclimb.live.crimp.common.busevent.ScanOnPause;
import com.nusclimb.live.crimp.common.busevent.ScanOnResume;
import com.squareup.otto.Subscribe;

import java.util.List;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class HelloActivity extends ActionBarActivity implements ActionBar.TabListener{
    private final String TAG = HelloActivity.class.getSimpleName();

    // Information retrieved from intent.
    private Bundle mBundle;

    // For doing tab manipulation
    private ActionBar mActionBar;
    private ViewPager mViewPager;
    private CrimpFragmentPagerAdapter mCrimpFragmentPagerAdapter;
    private Handler mHandler;

    // Fragment State. For use with Bus.
    private boolean isScanOnResume = false;
    private boolean isScoreOnResume = false;
    private boolean isRouteFinish = false;
    private boolean isScanFinish = false;
    private boolean isScoreFinish = false;



    /*=========================================================================
     * Bus methods
     *=======================================================================*/
    @Subscribe
    public void onReceiveRouteNotFinish(RouteNotFinish event){
        Log.d(TAG+".onReceiveRouteFinish()", "Received RouteNotFinish event.");

        isRouteFinish = false;

        mCrimpFragmentPagerAdapter.set_count(1);
    }

    @Subscribe
    public void onReceiveRouteFinish(RouteFinish event){
        Log.d(TAG+".onReceiveRouteFinish()", "Received RouteFinish event.");

        isRouteFinish = true;

        mCrimpFragmentPagerAdapter.set_count(2);
        mActionBar.setSelectedNavigationItem(1);
    }

    @Subscribe
    public void onReceiveScanOnResume(ScanOnResume event){
        Log.d(TAG + ".onReceiveScanOnResume", "Received ScanOnResume event.");

        isScanOnResume = true;
        if(isRouteFinish){
            BusProvider.getInstance().post(new ScanAcquireCamera());
        }
    }

    @Subscribe
    public void onReceiveScanOnPause(ScanOnPause event){
        Log.d(TAG + ".onReceiveScanOnResume", "Received ScanOnPause event.");
        isScanOnResume = false;
    }



    /*=========================================================================
     * Lifecycle methods
     *=======================================================================*/
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        // We want as long as this window is visible to the user,
        // keep the device's screen turned on and bright.
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_hello);

        mHandler = new Handler();

        if(savedInstanceState == null){
            // Newly created activity
            Log.v(TAG+".onCreate()", "Newly created.");
        }
        else{
            // Restored activity
            Log.v(TAG+".onCreate()", "Restored.");
        }

        // Get intent info.
        // TODO might want to shift to the if-else check above
        mBundle = getIntent().getExtras();

        // Instantiate object for tab manipulation
        mActionBar = getSupportActionBar();
        // Specify that we will be displaying tabs in the action bar.
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        mCrimpFragmentPagerAdapter = new CrimpFragmentPagerAdapter(getSupportFragmentManager(), this);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mCrimpFragmentPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            private final String TAG = ViewPager.SimpleOnPageChangeListener.class.getSimpleName();

            @Override
            public void onPageSelected(int position) {
                // When swiping between different app sections, select the corresponding tab.
                // We can also use ActionBar.Tab#select() to do this if we have a reference to the
                // Tab.
                Log.d(TAG + ".onPageSelected", "called with position = " + position);
                mActionBar.setSelectedNavigationItem(position);
            }
        });

        // Add route, scan, score tab to action bar.
        ActionBar.Tab routeTab = mActionBar.newTab()
                .setText(mCrimpFragmentPagerAdapter.getPageTitle(0))
                .setTabListener(this);
        mActionBar.addTab(routeTab);

        ActionBar.Tab scanTab = mActionBar.newTab()
                .setText(mCrimpFragmentPagerAdapter.getPageTitle(1))
                .setTabListener(this);
        mActionBar.addTab(scanTab);

        ActionBar.Tab scoreTab = mActionBar.newTab()
                .setText(mCrimpFragmentPagerAdapter.getPageTitle(2))
                .setTabListener(this);
        mActionBar.addTab(scoreTab);

        // TODO
        mCrimpFragmentPagerAdapter.set_count(1);
    }

    @Override
    protected void onStart(){
        super.onStart();
        Log.v(TAG+".onStart()", "start");
    }

    @Override
    protected void onResume(){
        super.onResume();
        BusProvider.getInstance().register(this);
        Log.v(TAG + ".onResume()", "registered BusProvider");
    }

    @Override
    protected void onPause(){
        super.onPause();
        BusProvider.getInstance().unregister(this);
        Log.v(TAG + ".onPause()", "unregistered BusProvider");
    }

    @Override
    protected void onStop(){
        Log.v(TAG + ".onStop()", "stop");
        super.onStop();
    }



    /*=========================================================================
     * ActionBar.TabListener interface methods
     *=======================================================================*/
    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        Log.d(TAG + ".onTabUnselected()", "tab.position = " + tab.getPosition());
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        Log.d(TAG + ".onTabSelected()", "tab.position = " + tab.getPosition());

        if(tab.getPosition() >= mCrimpFragmentPagerAdapter.getCount()) {
            Log.d(TAG+".onTabSelected()", "selected "+tab.getPosition()+" >= count "+mCrimpFragmentPagerAdapter.getCount());

            final int currentTabPosition = mViewPager.getCurrentItem();

            mHandler.postAtFrontOfQueue(new Runnable() {
                @Override
                public void run() {
                    getSupportActionBar().setSelectedNavigationItem(
                            currentTabPosition);
                }
            });
        }
        else {
            Log.d(TAG+".onTabSelected()", "selected "+tab.getPosition()+" < count "+mCrimpFragmentPagerAdapter.getCount());
            // When the given tab is selected, switch to the corresponding page in the ViewPager.
            mViewPager.setCurrentItem(tab.getPosition());
        }
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        Log.d(TAG + ".onTabReselected()", "tab.position = " + tab.getPosition());
    }



    /*=========================================================================
     * Button onClick methods
     *=======================================================================*/
    public void routeNext(View view){
        Log.v(TAG + ".routeNext()", "Button clicked");
        RouteFragment rf = (RouteFragment) getFirstMatchingFragment(RouteFragment.class);
        if(rf==null)
            Log.d(TAG+".routeNext()", "CANT FIND RF");
        rf.next();
    }

    public void routeYes(View view){
        Log.v(TAG+".routeYes()", "Button clicked");
        RouteFragment rf = (RouteFragment) getFirstMatchingFragment(RouteFragment.class);
        rf.yes();
    }

    public void routeNo(View view){
        Log.v(TAG+".routeNo()", "Button clicked");
        RouteFragment rf = (RouteFragment) getFirstMatchingFragment(RouteFragment.class);
        rf.no();
    }

    public void scanRescan(View view){
        Log.v(TAG+".scanRescan()", "Button clicked");
        ScanFragment sf = (ScanFragment) getFirstMatchingFragment(ScanFragment.class);
        sf.rescan();
    }

    public void scanFlash(View view){
        Log.v(TAG+".scanFlash()", "Button clicked");
        ScanFragment sf = (ScanFragment) getFirstMatchingFragment(ScanFragment.class);
        sf.flash();
    }



    /*=========================================================================
     * Other methods
     *=======================================================================*/
    private Fragment getFirstMatchingFragment(Class clazz){
        List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
        for(Fragment fr: fragmentList){
            if( fr.getClass().getSimpleName().equals(clazz.getSimpleName())){
                return fr;
            }
        }
        return null;
    }

    /**
     * Getter for mBundle.
     *
     * @return mBundle. Contain information from loginActivity.
     */
    public Bundle getBundle(){
        return mBundle;
    }
}