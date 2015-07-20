package com.nusclimb.live.crimp.hello;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.nusclimb.live.crimp.CrimpFragmentPagerAdapter;
import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.common.BusProvider;
import com.nusclimb.live.crimp.common.busevent.InRouteTab;
import com.nusclimb.live.crimp.common.busevent.InScanTab;
import com.nusclimb.live.crimp.common.busevent.InScoreTab;
import com.nusclimb.live.crimp.common.busevent.RouteFinish;
import com.nusclimb.live.crimp.common.busevent.RouteNotFinish;
import com.nusclimb.live.crimp.common.busevent.RouteOnPause;
import com.nusclimb.live.crimp.common.busevent.RouteOnResume;
import com.nusclimb.live.crimp.common.busevent.ScanFinish;
import com.nusclimb.live.crimp.common.busevent.ScanNotFinish;
import com.nusclimb.live.crimp.common.busevent.ScanOnPause;
import com.nusclimb.live.crimp.common.busevent.ScanOnResume;
import com.nusclimb.live.crimp.common.busevent.ScoreFinish;
import com.nusclimb.live.crimp.common.busevent.ScoreOnPause;
import com.nusclimb.live.crimp.common.busevent.ScoreOnResume;
import com.nusclimb.live.crimp.common.busevent.StartScan;
import com.nusclimb.live.crimp.login.LoginActivity;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class HelloActivity extends ActionBarActivity implements ActionBar.TabListener{
    private final String TAG = HelloActivity.class.getSimpleName();

    // For doing tab manipulation
    private ActionBar mActionBar;
    private ViewPager mViewPager;
    private CrimpFragmentPagerAdapter mCrimpFragmentPagerAdapter;
    private Handler activityHandler;

    // Fragment State. For use with Bus.
    private boolean isRouteOnResume = false;
    private boolean isScanOnResume = false;
    private boolean isScoreOnResume = false;
    private int currentTab = 0;
    private int tabCount = 1;

    // Info from fragments
    private String xUserId;                         // From LoginActivity
    private String xAuthToken;                      // From LoginActivity
    private String[] categoryIdListAsArray;         // From LoginActivity
    private String[] categoryNameListAsArray;       // From LoginActivity
    private int[] categoryRouteCountListAsArray;    // From LoginActivity
    private String routeId;                         // From RouteFragment
    private String climberId;                       // From ScanFragment
    private String climberName;                     // From ScanFragment (optional)



    public String getxUserId(){
        return xUserId;
    }

    public String getxAuthToken(){
        return xAuthToken;
    }

    public String getRouteId(){
        return routeId;
    }

    public String getClimberId(){
        return climberId;
    }

    public String getClimberName(){
        return climberName;
    }

    public List<SpinnerItem> getCategoryList(){
        List<SpinnerItem> categorySpinnerItemList = new ArrayList<SpinnerItem>();
        for(int i=0; i<categoryIdListAsArray.length; i++){
            categorySpinnerItemList.add(new CategorySpinnerItem(categoryNameListAsArray[i],
                    categoryIdListAsArray[i], categoryRouteCountListAsArray[i], false));
        }

        return categorySpinnerItemList;
    }



    /*=========================================================================
   * Bus methods
   *=======================================================================*/
    @Subscribe
    public void onReceiveRouteNotFinish(RouteNotFinish event){
        Log.d(TAG+".onReceiveRouteFinish()", "Received RouteNotFinish event. Set tab count to 1.");

        tabCount = 1;
        mCrimpFragmentPagerAdapter.set_count(tabCount);
    }

    @Subscribe
    public void onReceiveRouteFinish(RouteFinish event){
        Log.d(TAG+".onReceiveRouteFinish()", "Received RouteFinish event. Set tab count to 2. Select tab 1. routeId="+event.getRouteId());

        routeId = event.getRouteId();
        tabCount = 2;
        currentTab = 1;
        mCrimpFragmentPagerAdapter.set_count(tabCount);
        mActionBar.setSelectedNavigationItem(currentTab);
    }

    @Subscribe
    public void onReceiveScanNotFinish(ScanNotFinish event){
        if(tabCount<=2){
            Log.d(TAG + ".onReceiveScanNotFinish()", "Received ScanNotFinish event. Tab count unchanged.");
        }
        else {
            Log.d(TAG + ".onReceiveScanNotFinish()", "Received ScanNotFinish event. Set tab count to 2.");
            tabCount = 2;
            mCrimpFragmentPagerAdapter.set_count(tabCount);
        }
    }

    @Subscribe
    public void onReceiveScanFinish(ScanFinish event){
        Log.d(TAG+".onReceiveScanFinish()", "Received ScanFinish event. Set tab count to 3. Select tab 2. cid = "+event.getClimberId()+"; cname = "+event.getClimberName());
        climberId = event.getClimberId();
        climberName = event.getClimberName();

        tabCount = 3;
        currentTab = 2;
        mCrimpFragmentPagerAdapter.set_count(tabCount);
        mActionBar.setSelectedNavigationItem(currentTab);
    }

    @Subscribe
    public void onReceiveScoreFinish(ScoreFinish event){
        Log.d(TAG+".onReceiveScoreFinish()", "Received ScoreFinish event.");

        climberId = null;
        climberName = null;

        tabCount = 2;
        currentTab = 1;
        mActionBar.setSelectedNavigationItem(currentTab);
        mCrimpFragmentPagerAdapter.set_count(tabCount);
    }

    @Subscribe
    public void onReceiveRouteOnResume(RouteOnResume event){
        isRouteOnResume = true;

        if(isRouteOnResume && isScanOnResume && isScoreOnResume){

            mCrimpFragmentPagerAdapter.set_count(tabCount);
            mActionBar.setSelectedNavigationItem(currentTab);
            Log.d(TAG + ".onReceiveRouteOnResume()", "Received RouteOnResume event. Set tab count to " + tabCount);
        }
        else{
            Log.d(TAG + ".onReceiveRouteOnResume()", "Received RouteOnResume event.");
        }
    }

    @Subscribe
    public void onReceivedRouteOnPause(RouteOnPause event){
        isRouteOnResume = false;
    }



    @Subscribe
    public void onReceiveScanOnResume(ScanOnResume event){
        Log.d(TAG + ".onReceiveScanOnResume", "Received ScanOnResume event.");

        isScanOnResume = true;
        if(tabCount>= 2){
            Log.d(TAG + ".onReceiveScanOnResume", "Post StartScan event.");
            BusProvider.getInstance().post(new StartScan());
        }

        if(isRouteOnResume && isScanOnResume && isScoreOnResume){
            Log.d(TAG + ".onReceiveScanOnResume", "Set tab count to "+tabCount);
            mCrimpFragmentPagerAdapter.set_count(tabCount);
            mActionBar.setSelectedNavigationItem(currentTab);
        }
    }

    @Subscribe
    public void onReceivedScanOnPause(ScanOnPause event){
        isScanOnResume = false;
    }

    @Subscribe
    public void onReceiveScoreOnResume(ScoreOnResume event){
        isScoreOnResume = true;

        if(isRouteOnResume && isScanOnResume && isScoreOnResume){
            Log.d(TAG + ".onReceiveScoreOnResume()", "Received ScoreOnResume event. Set tab count to "+tabCount);
            mCrimpFragmentPagerAdapter.set_count(tabCount);
            mActionBar.setSelectedNavigationItem(currentTab);
        }
        else{
            Log.d(TAG + ".onReceiveScoreOnResume()", "Received ScoreOnResume event.");
        }
    }

    @Subscribe
    public void onReceiveScoreOnPause(ScoreOnPause event){
        isScoreOnResume = false;
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

        activityHandler = new Handler();

        // Instantiate object for tab manipulation
        mActionBar = getSupportActionBar();
        mCrimpFragmentPagerAdapter = new CrimpFragmentPagerAdapter(getSupportFragmentManager(), this);
        mViewPager = (ViewPager) findViewById(R.id.pager);

        // Specify that we will be displaying tabs in the action bar.
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
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

        mViewPager.setAdapter(mCrimpFragmentPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            private final String TAG = ViewPager.SimpleOnPageChangeListener.class.getSimpleName();

            @Override
            public void onPageSelected(int position) {
                // When swiping between different app sections, select the corresponding tab.
                // We can also use ActionBar.Tab#select() to do this if we have a reference to the
                // Tab.
                Log.d(TAG + ".onPageSelected", "called with position = " + position);
                currentTab = position;
                mActionBar.setSelectedNavigationItem(currentTab);
            }
        });
        mViewPager.setOffscreenPageLimit(2);

        if(savedInstanceState == null){
            // Newly created activity
            xUserId = getIntent().getExtras().getString(getString(R.string.bundle_x_user_id));
            xAuthToken = getIntent().getExtras().getString(getString(R.string.bundle_x_auth_token));
            categoryIdListAsArray = getIntent().getExtras().getStringArray(getString(R.string.bundle_category_id_list));
            categoryNameListAsArray = getIntent().getExtras().getStringArray(getString(R.string.bundle_category_name_list));
            categoryRouteCountListAsArray = getIntent().getExtras().getIntArray(getString(R.string.bundle_category_route_count_list));

            Log.d(TAG + ".onCreate()", "Newly created.\n"+
                    "xUserId="+xUserId+"\n"+
                    "xAuthToken="+xAuthToken+"\n"+
                    "categoryIdListAsArray="+ categoryIdListAsArray.length +"\n"+
                    "categoryNameListAsArray="+categoryNameListAsArray.length+"\n"+
                    "categoryRouteCountListAsArray="+categoryRouteCountListAsArray.length);
        }
        else{
            // Restored activity
            tabCount = savedInstanceState.getInt(getString(R.string.bundle_tab_count));
            currentTab = savedInstanceState.getInt(getString(R.string.bundle_current_tab));

            xUserId = savedInstanceState.getString(getString(R.string.bundle_x_user_id));
            xAuthToken = savedInstanceState.getString(getString(R.string.bundle_x_auth_token));
            categoryIdListAsArray = savedInstanceState.getStringArray(getString(R.string.bundle_category_id_list));
            categoryNameListAsArray = savedInstanceState.getStringArray(getString(R.string.bundle_category_name_list));
            categoryRouteCountListAsArray = savedInstanceState.getIntArray(getString(R.string.bundle_category_route_count_list));

            if(tabCount == 2){
                routeId = savedInstanceState.getString(getString(R.string.bundle_route_id));
            }
            else if(tabCount == 3){
                climberId = savedInstanceState.getString(getString(R.string.bundle_climber_id));
                climberName = savedInstanceState.getString(getString(R.string.bundle_climber_name));
            }

            String temp = "Restored.";
            temp = temp + "\nxUserId=" + xUserId;
            temp = temp + "\nxAuthToken" + xAuthToken;
            temp = temp + "\ncategoryIdListAsArray=" + categoryIdListAsArray.length;
            temp = temp + "\ncategoryNameListAsArray=" + categoryNameListAsArray.length;
            temp = temp + "\ncategoryRouteCountListAsArray=" + categoryRouteCountListAsArray.length;
            if(tabCount == 2){
                temp = temp + "\nrouteId=" + routeId;
            }
            else if(tabCount == 3){
                temp = temp + "\nclimberId=" + climberId;
                temp = temp + "\nclimberName=" + climberName;
            }
            temp = temp + "\ntabCount=" + tabCount;
            temp = temp + "\ncurrentTab=" + currentTab;


            Log.d(TAG+".onCreate()", temp);
        }
    }

    @Override
    protected void onStart(){
        super.onStart();
        Log.v(TAG + ".onStart()", "start");
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

    @Override
    protected void onSaveInstanceState (Bundle outState){
        super.onSaveInstanceState(outState);

        outState.putString(getString(R.string.bundle_x_user_id), xUserId);
        outState.putString(getString(R.string.bundle_x_auth_token), xAuthToken);
        outState.putStringArray(getString(R.string.bundle_category_id_list), categoryIdListAsArray);
        outState.putStringArray(getString(R.string.bundle_category_name_list), categoryNameListAsArray);
        outState.putIntArray(getString(R.string.bundle_category_route_count_list), categoryRouteCountListAsArray);
        outState.putInt(getString(R.string.bundle_tab_count), tabCount);
        outState.putInt(getString(R.string.bundle_current_tab), currentTab);

        if(tabCount == 2)
            outState.putString(getString(R.string.bundle_route_id), routeId);
        if(tabCount == 3) {
            outState.putString(getString(R.string.bundle_climber_id), climberId);
            outState.putString(getString(R.string.bundle_climber_name), climberName);
        }

        String temp = "xUserId=" + xUserId;
        temp = temp + "\nxAuthToken" + xAuthToken;
        temp = temp + "\ncategoryIdListAsArray=" + categoryIdListAsArray.length;
        temp = temp + "\ncategoryNameListAsArray=" + categoryNameListAsArray.length;
        temp = temp + "\ncategoryRouteCountListAsArray=" + categoryRouteCountListAsArray.length;
        if(tabCount == 2){
            temp = temp + "\nrouteId=" + routeId;
        }
        else if(tabCount == 3){
            temp = temp + "\nclimberId=" + climberId;
            temp = temp + "\nclimberName=" + climberName;
        }
        temp = temp + "\ntabCount=" + tabCount;
        temp = temp + "\ncurrentTab=" + currentTab;

        Log.d(TAG + ".onSavedInstanceState()", temp);
    }



    /*=========================================================================
     * ActionBar.TabListener interface methods
     *=======================================================================*/
    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        if(tab.getPosition() >= tabCount) {
            Log.d(TAG+".onTabSelected()", "selected "+tab.getPosition()+" >= count "+tabCount);

            final int currentTabPosition = currentTab;

            activityHandler.postAtFrontOfQueue(new Runnable() {
                @Override
                public void run() {
                    getSupportActionBar().setSelectedNavigationItem(
                            currentTabPosition);
                }
            });
        }
        else {
            Log.d(TAG+".onTabSelected()", "selected "+tab.getPosition()+" < count "+tabCount);
            // When the given tab is selected, switch to the corresponding page in the ViewPager.
            currentTab = tab.getPosition();
            mViewPager.setCurrentItem(currentTab);

            switch(currentTab){
                case 0:
                    Log.d(TAG+".onTabSelected()", "Posted InRouteTab to bus.");
                    BusProvider.getInstance().post(new InRouteTab());
                    break;
                case 1:
                    Log.d(TAG+".onTabSelected()", "Posted InScanTab("+routeId+") to bus.");
                    BusProvider.getInstance().post(new InScanTab(routeId));
                    break;
                case 2:
                    Log.d(TAG+".onTabSelected()", "Posted InScoreTab to bus.");
                    BusProvider.getInstance().post(new InScoreTab());
                    break;
                default:
                    break;
            }
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
        Log.v(TAG + ".routeNext()", "Button clicked.");
        RouteFragment rf = (RouteFragment) getFirstMatchingFragment(RouteFragment.class);
        rf.next();
    }

    public void routeYes(View view){
        Log.v(TAG+".routeYes()", "Button clicked.");
        RouteFragment rf = (RouteFragment) getFirstMatchingFragment(RouteFragment.class);
        rf.yes();
    }

    public void routeNo(View view){
        Log.v(TAG+".routeNo()", "Button clicked.");
        RouteFragment rf = (RouteFragment) getFirstMatchingFragment(RouteFragment.class);
        rf.no();
    }

    public void scanRescan(View view){
        Log.v(TAG+".scanRescan()", "Button clicked.");
        ScanFragment sf = (ScanFragment) getFirstMatchingFragment(ScanFragment.class);
        sf.rescan();
    }

    public void scanFlash(View view){
        Log.v(TAG+".scanFlash()", "Button clicked.");
        ScanFragment sf = (ScanFragment) getFirstMatchingFragment(ScanFragment.class);
        sf.toggleFlash();
    }

    public void scanNext(View view){
        Log.v(TAG + ".scanNext()", "Button clicked.");
        ScanFragment sf = (ScanFragment) getFirstMatchingFragment(ScanFragment.class);
        sf.next();
    }

    public void scorePlusOne(View view){
        Log.v(TAG+".scorePlusOne()", "Button clicked.");
        // Get instance of Vibrator from current Context
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 50 milliseconds
        v.vibrate(50);
        ScoreFragment sf = (ScoreFragment) getFirstMatchingFragment(ScoreFragment.class);
        sf.plusOne();
    }

    public void scoreBonus(View view){
        Log.v(TAG+".scorePlusOne()", "Button clicked.");
        // Get instance of Vibrator from current Context
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 50 milliseconds
        v.vibrate(50);
        ScoreFragment sf = (ScoreFragment) getFirstMatchingFragment(ScoreFragment.class);
        sf.bonus();
    }

    public void scoreTop(View view){
        Log.v(TAG + ".scorePlusOne()", "Button clicked.");
        // Get instance of Vibrator from current Context
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 50 milliseconds
        v.vibrate(50);
        ScoreFragment sf = (ScoreFragment) getFirstMatchingFragment(ScoreFragment.class);
        sf.top();
    }

    public void scoreBackspace(View view){
        Log.v(TAG + ".scorePlusOne()", "Button clicked.");
        // Get instance of Vibrator from current Context
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 50 milliseconds
        v.vibrate(50);
        ScoreFragment sf = (ScoreFragment) getFirstMatchingFragment(ScoreFragment.class);
        sf.backspace();
    }

    public void scoreSubmit(View view){
        Log.v(TAG + ".scoreSubmit()", "Button clicked.");
        ScoreFragment sf = (ScoreFragment) getFirstMatchingFragment(ScoreFragment.class);
        sf.submit();
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

    @Override
    public void onBackPressed()
    {
        switch(currentTab){
            case 0:
                super.onBackPressed();
                break;
            case 1:
                currentTab = 0;
                mActionBar.setSelectedNavigationItem(currentTab);
                break;
            case 2:
                // TODO prompt user
                currentTab = 1;
                mActionBar.setSelectedNavigationItem(currentTab);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_hello, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case R.id.action_logout:
                LoginManager.getInstance().logOut();
                Intent mIntent = new Intent(getApplicationContext(), LoginActivity.class);
                finish();
                startActivity(mIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}