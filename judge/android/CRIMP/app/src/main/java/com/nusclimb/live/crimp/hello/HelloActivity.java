package com.nusclimb.live.crimp.hello;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Zhi on 7/6/2015.
 */
public class HelloActivity extends ActionBarActivity implements ActionBar.TabListener{
    private final String TAG = HelloActivity.class.getSimpleName();

    // Information retrieved from intent.
    private Bundle mBundle;
    private String xUserId;
    private String xAuthToken;
    private List<SpinnerItem> categorySpinnerItemList;

    // For doing tab manipulation
    private ActionBar mActionBar;
    private ViewPager mViewPager;
    private CrimpFragmentPagerAdapter mCrimpFragmentPagerAdapter;
    private Handler mHandler;

    // Tabs
    private ActionBar.Tab routeTab;
    private ActionBar.Tab scanTab;
    private ActionBar.Tab scoreTab;

    // RoboSpice stuff
    //private SpiceManager spiceManager = new SpiceManager(CrimpService.class);

    public Bundle getBundle(){
        return mBundle;
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

        if(savedInstanceState == null){
            // Newly created activity
            Log.d(TAG+".onCreate()", "Newly created.");
        }
        else{
            // Restored activity
            Log.d(TAG+".onCreate()", "Restored.");
        }

        // Get intent info.
        // TODO might want to shift to the if-else check above
        mBundle = getIntent().getExtras();  // TODO using bundle?
        xUserId = mBundle.getString(getString(R.string.package_name) +
                getString(R.string.bundle_x_user_id));
        xAuthToken = mBundle.getString(getString(R.string.package_name) +
                getString(R.string.bundle_x_auth_token));
        String[] categoryIdList = mBundle.getStringArray(getString(R.string.package_name) +
                getString(R.string.bundle_category_id_list));
        String[] categoryNameList = mBundle.getStringArray(getString(R.string.package_name) +
                getString(R.string.bundle_category_name_list));
        int[] categoryRouteCountList = mBundle.getIntArray(getString(R.string.package_name) +
                getString(R.string.bundle_category_route_count_list));

        // Combine category info into a list.
        categorySpinnerItemList = new ArrayList<SpinnerItem>();
        for (int i = 0; i < categoryIdList.length; i++) {
            categorySpinnerItemList.add(new CategorySpinnerItem(categoryNameList[i],
                    categoryIdList[i], categoryRouteCountList[i], false));
        }

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
                Log.d(TAG+".onPageSelected", "called with position = "+position);
                mActionBar.setSelectedNavigationItem(position);
            }
        });

        mHandler = new Handler(Looper.getMainLooper()){
            private final String TAG = Handler.class.getSimpleName();
            public void handleMessager(Message inputMessage){
                Log.d(TAG+".handleMessager", "HelloActivity handler receive message = "+inputMessage.what);
                switch(inputMessage.what){
                    // TODO implement message handling
                    default:
                        super.handleMessage(inputMessage);
                        break;
                }
            }
        };

        // Add route, scan, score tab to action bar.
        routeTab = mActionBar.newTab()
                .setText(mCrimpFragmentPagerAdapter.getPageTitle(0))
                .setTabListener(this);
        mActionBar.addTab(routeTab);

        scanTab = mActionBar.newTab()
                .setText(mCrimpFragmentPagerAdapter.getPageTitle(1))
                .setTabListener(this);
        mActionBar.addTab(scanTab);

        scoreTab = mActionBar.newTab()
                .setText(mCrimpFragmentPagerAdapter.getPageTitle(2))
                .setTabListener(this);
        mActionBar.addTab(scoreTab);
    }

    @Override
    protected void onStart(){
        super.onStart();
        Log.d(TAG + ".onStart()", "Starting spiceManager");
        //spiceManager.start(this);
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.d(TAG + ".onResume()", "");
    }

    @Override
    protected void onPause(){
        super.onPause();
        Log.d(TAG+".onPause()", "");
    }

    @Override
    protected void onStop(){
        super.onStop();
        Log.d(TAG + ".onStop()", "Stopping spiceManager.");
        //spiceManager.shouldStop();
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
        Log.d(TAG+".onTabSelected()", "tab.position = "+tab.getPosition());

        // When the given tab is selected, switch to the corresponding page in the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        Log.d(TAG + ".onTabReselected()", "tab.position = " + tab.getPosition());
    }

    /*=========================================================================
     * Button onClick methods
     *=======================================================================*/
    public void routeNext(View view){
        Log.d(TAG + ".routeNext()", "Button clicked");
        RouteFragment rf = (RouteFragment) getFirstMatchingFragment(RouteFragment.class);
        if(rf==null)
            Log.d(TAG+".routeNext()", "CANT FIND RF");
        rf.next(view);
    }

    public void routeYes(View view){
        Log.d(TAG+".routeYes()", "Button clicked");
        RouteFragment rf = (RouteFragment) getFirstMatchingFragment(RouteFragment.class);
        rf.yes(view);
    }

    public void routeNo(View view){
        Log.d(TAG+".routeNo()", "Button clicked");
        RouteFragment rf = (RouteFragment) getFirstMatchingFragment(RouteFragment.class);
        rf.no(view);
    }





    public Fragment getFirstMatchingFragment(Class clazz){
        List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
        for(Fragment fr: fragmentList){
            if( fr.getClass().getSimpleName().equals(clazz.getSimpleName())){
                return fr;
            }
        }
        return null;
    }

    public int getCurrentSelectedTab(){
        return mViewPager.getCurrentItem();
    }




}
