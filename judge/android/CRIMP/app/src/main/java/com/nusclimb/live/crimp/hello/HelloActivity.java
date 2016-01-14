package com.nusclimb.live.crimp.hello;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.nusclimb.live.crimp.CrimpApplication;
import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.common.Categories;
import com.nusclimb.live.crimp.common.Climber;
import com.nusclimb.live.crimp.common.QueueObject;
import com.nusclimb.live.crimp.common.User;
import com.nusclimb.live.crimp.common.json.CategoriesResponseBody;
import com.nusclimb.live.crimp.common.json.HelpMeResponseBody;
import com.nusclimb.live.crimp.common.spicerequest.HelpMeRequest;
import com.nusclimb.live.crimp.hello.route.RouteFragment;
import com.nusclimb.live.crimp.hello.scan.ScanFragment;
import com.nusclimb.live.crimp.hello.score.ScoreFragment;
import com.nusclimb.live.crimp.login.LoginActivity;
import com.nusclimb.live.crimp.service.CrimpService;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

/**
 * This class is the main activity of Crimp. It contains several HelloActivityFragments. Navigating
 * between HelloActivityFragments are done by clicking on ActionBarTab or swiping on
 * HelloActivityViewPager. This class also contain User, Categories and Climber information. Any
 * HelloActivityFragments intending to use these information must always get it from this class.
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class HelloActivity extends AppCompatActivity implements RouteFragment.RouteFragmentToActivityMethods,
        ScanFragment.ScanFragmentToActivityMethods,
        ScoreFragment.ScoreFragmentToActivityMethods {
    private final String TAG = HelloActivity.class.getSimpleName();
    private final boolean DEBUG = false;

    private Bundle routeBundle = new Bundle();
    private Bundle scanBundle = new Bundle();
    private Bundle scoreBundle = new Bundle();
    private SpiceManager spiceManager = new SpiceManager(CrimpService.class);

    // All the info
    private User mUser;
    private Categories mCategories;
    private Climber mClimber;

    // For doing tab manipulation
    private TabLayout mTabLayout;
    private HelloActivityViewPager mViewPager;
    private HelloActivityFragmentPagerAdapter mFragmentPagerAdapter;
    private Handler activityHandler;
    private int prevPageIndex;
    private HelloActivityViewPagerListener mViewPagerListener;

    @NonNull
    private User getmUser(){
        if(mUser == null)
            mUser = new User();
        return mUser;
    }

    @NonNull
    private Categories getmCategories(){
        if(mCategories == null)
            mCategories = new Categories();
        return mCategories;
    }

    @NonNull
    private Climber getmClimber(){
        if(mClimber == null)
            mClimber = new Climber();
        return mClimber;
    }

    private TabLayout getmTabLayout(){
        if(mTabLayout == null)
            mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        return mTabLayout;
    }

    private HelloActivityViewPager getmViewPager(){
        if(mViewPager == null)
            mViewPager = (HelloActivityViewPager)findViewById(R.id.pager);
        return mViewPager;
    }

    @NonNull
    private HelloActivityFragmentPagerAdapter getmFragmentPagerAdapter(){
        if(mFragmentPagerAdapter == null)
            mFragmentPagerAdapter = new HelloActivityFragmentPagerAdapter(getSupportFragmentManager());
        return mFragmentPagerAdapter;
    }

    @NonNull
    private Handler getActivityHandler(){
        if(activityHandler == null)
            activityHandler = new Handler();
        return activityHandler;
    }

    @NonNull
    private HelloActivityViewPagerListener getmViewPagerListener(){
        if(mViewPagerListener == null)
            mViewPagerListener = new HelloActivityViewPagerListener();
        return mViewPagerListener;
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
        setContentView(R.layout.activity_hello);

        /* Note to self: ActionBar and ViewPager are separated. User can switch between
         * fragments by using either ActionBar or ViewPager. When ActionBar is used, we set
         * ViewPager to match the ActionBar and vice versa.
         *
         * ActionBar (the top bar) is just navigation "buttons" and does not have "content".
         *
         * ViewPager (the entire content area) has an adapter which contains a list of fragments.
         * When we swipe the ViewPager, the adapter will pull the correct fragment from its list
         * and display it.
         */
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        // ViewPager stuff
        getmViewPager().setAdapter(getmFragmentPagerAdapter());

        // TabLayout stuff
        getmTabLayout().setupWithViewPager(getmViewPager());
        getmTabLayout().setOnTabSelectedListener(new HelloActivityOnTabSelectedListener(getmViewPager()));

        if(savedInstanceState == null) {
            // Newly created activity therefore we can only get information from intent.
            getmUser().setFacebookAccessToken(getIntent().getStringExtra(getString(R.string.bundle_access_token)));
            getmUser().setUserName(getIntent().getStringExtra(getString(R.string.bundle_user_name)));
            getmUser().setUserId(getIntent().getStringExtra(getString(R.string.bundle_x_user_id)));
            getmUser().setAuthToken(getIntent().getStringExtra(getString(R.string.bundle_x_auth_token)));

            getmFragmentPagerAdapter().setCount(1);
            prevPageIndex = 0;
        }
        else{
            // Recreating activity. Restore all info from saved instance state.
            getmUser().setFacebookAccessToken(savedInstanceState.getString(getString(R.string.bundle_access_token)));
            getmUser().setUserName(savedInstanceState.getString(getString(R.string.bundle_user_name)));
            getmUser().setUserId(savedInstanceState.getString(getString(R.string.bundle_x_user_id)));
            getmUser().setAuthToken(savedInstanceState.getString(getString(R.string.bundle_x_auth_token)));
            getmUser().setCategoryId(savedInstanceState.getString(getString(R.string.bundle_category_id)));
            getmUser().setRouteId(savedInstanceState.getString(getString(R.string.bundle_route_id)));

            getmCategories().copy(savedInstanceState.getStringArrayList(getString(R.string.bundle_category_name_list)),
                    savedInstanceState.getStringArrayList(getString(R.string.bundle_category_id_list)),
                    savedInstanceState.getIntegerArrayList(getString(R.string.bundle_category_route_count_list)),
                    savedInstanceState.getStringArrayList(getString(R.string.bundle_route_name_list)),
                    savedInstanceState.getStringArrayList(getString(R.string.bundle_route_id_list)),
                    savedInstanceState.getStringArrayList(getString(R.string.bundle_route_score_list)),
                    savedInstanceState.getByteArray(getString(R.string.bundle_category_finalize_list)),
                    savedInstanceState.getStringArrayList(getString(R.string.bundle_category_start_list)),
                    savedInstanceState.getStringArrayList(getString(R.string.bundle_category_end_list)));

            getmClimber().setClimberId(savedInstanceState.getString(getString(R.string.bundle_climber_id)));
            getmClimber().setClimberName(savedInstanceState.getString(getString(R.string.bundle_climber_name)));
            getmClimber().setTotalScore(savedInstanceState.getString(getString(R.string.bundle_total_score)));

            routeBundle = savedInstanceState.getBundle(getString(R.string.bundle_route_bundle));
            scanBundle = savedInstanceState.getBundle(getString(R.string.bundle_scan_bundle));
            scoreBundle = savedInstanceState.getBundle(getString(R.string.bundle_score_bundle));

            getmFragmentPagerAdapter().setCount(savedInstanceState.getInt(getString(R.string.bundle_hello_fragment_count)));
            prevPageIndex = savedInstanceState.getInt(getString(R.string.bundle_hello_previous_index));
        }
    }

    @Override
    protected void onStart(){
        super.onStart();
        spiceManager.start(this);
        if (DEBUG) Log.d(TAG, "onStart");
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(DEBUG) Log.d(TAG, "onResume");
        getmViewPager().addOnPageChangeListener(getmViewPagerListener());
    }

    @Override
    protected void onPause(){
        getmViewPager().removeOnPageChangeListener(getmViewPagerListener());
        if (DEBUG) Log.d(TAG, "onPause currentItem:" + getmViewPager().getCurrentItem());
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState (Bundle outState){
        super.onSaveInstanceState(outState);

        outState.putString(getString(R.string.bundle_access_token), mUser.getFacebookAccessToken());
        outState.putString(getString(R.string.bundle_user_name), getmUser().getUserName());
        outState.putString(getString(R.string.bundle_x_user_id), getmUser().getUserId());
        outState.putString(getString(R.string.bundle_x_auth_token), getmUser().getAuthToken());
        outState.putString(getString(R.string.bundle_category_id), getmUser().getCategoryId());
        outState.putString(getString(R.string.bundle_route_id), getmUser().getRouteId());

        outState.putStringArrayList(getString(R.string.bundle_category_name_list), getmCategories().getCategoryNameList());
        outState.putStringArrayList(getString(R.string.bundle_category_id_list), getmCategories().getCategoryIdList());
        outState.putIntegerArrayList(getString(R.string.bundle_category_route_count_list), getmCategories().getCategoryRouteCountList());
        outState.putStringArrayList(getString(R.string.bundle_route_name_list), getmCategories().getRouteNameList());
        outState.putStringArrayList(getString(R.string.bundle_route_id_list), getmCategories().getRouteIdList());
        outState.putStringArrayList(getString(R.string.bundle_route_score_list), getmCategories().getRouteScoreList());
        outState.putByteArray(getString(R.string.bundle_category_finalize_list), getmCategories().getCategoryFinalizeArray());
        outState.putStringArrayList(getString(R.string.bundle_category_start_list), getmCategories().getCategoryStartList());
        outState.putStringArrayList(getString(R.string.bundle_category_end_list), getmCategories().getCategoryEndList());

        outState.putString(getString(R.string.bundle_climber_id), getmClimber().getClimberId());
        outState.putString(getString(R.string.bundle_climber_name), getmClimber().getClimberName());
        outState.putString(getString(R.string.bundle_total_score), getmClimber().getTotalScore());

        outState.putBundle(getString(R.string.bundle_route_bundle), routeBundle);
        outState.putBundle(getString(R.string.bundle_route_bundle), scanBundle);
        outState.putBundle(getString(R.string.bundle_route_bundle), scoreBundle);

        outState.putInt(getString(R.string.bundle_hello_fragment_count), getmFragmentPagerAdapter().getCount());
        outState.putInt(getString(R.string.bundle_hello_previous_index), prevPageIndex);

        if (DEBUG) Log.d(TAG, "HelloActivity onSaveInstanceState");
    }

    @Override
    protected void onStop(){
        spiceManager.shouldStop();
        if (DEBUG) Log.d(TAG, "onStop");

        mTabLayout = null;
        mViewPager = null;

        super.onStop();
    }

    /*=========================================================================
     * Other classes
     *=======================================================================*/
    private class HelpMeRequestListener implements RequestListener<HelpMeResponseBody> {
        private final String TAG = HelpMeRequestListener.class.getSimpleName();

        @Override
        public void onRequestFailure(SpiceException e) {
            Log.w(TAG+".onRequestFailure()", "fail");
        }

        @Override
        public void onRequestSuccess(HelpMeResponseBody result) {
            if (DEBUG) Log.i(TAG+".onRequestSuccess()", "success");
        }
    }

    private class HelloActivityViewPagerListener implements ViewPager.OnPageChangeListener{

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            // Note to self: when this method is called, the ViewPager.getCurrentItem() is already
            // returning the parameter position. Therefore we should be careful not to have a loop
            // and call ViewPager.setCurrentItem(int) again.

            if (DEBUG)
                Log.d(TAG, "onPageSelected(" + position + ") viewPagerCurrentItem:" + getmViewPager().getCurrentItem()
                    +" prev:"+prevPageIndex);

            // Do onNavigateAway before onNavigateTo
            switch(position){
                case 0:
                    if(getmFragmentPagerAdapter().getScanFragment()!=null && prevPageIndex==1)
                        getmFragmentPagerAdapter().getScanFragment().onNavigateAway();
                    if(getmFragmentPagerAdapter().getScoreFragment()!=null && prevPageIndex==2)
                        getmFragmentPagerAdapter().getScoreFragment().onNavigateAway();
                    if(getmFragmentPagerAdapter().getRouteFragment()!=null)
                        getmFragmentPagerAdapter().getRouteFragment().onNavigateTo();
                    break;
                case 1:
                    if(getmFragmentPagerAdapter().getRouteFragment()!=null && prevPageIndex==0)
                        getmFragmentPagerAdapter().getRouteFragment().onNavigateAway();
                    if(getmFragmentPagerAdapter().getScoreFragment()!=null && prevPageIndex==2)
                        getmFragmentPagerAdapter().getScoreFragment().onNavigateAway();
                    if(getmFragmentPagerAdapter().getScanFragment()!=null)
                        getmFragmentPagerAdapter().getScanFragment().onNavigateTo();
                    break;
                case 2:
                    if(getmFragmentPagerAdapter().getRouteFragment()!=null && prevPageIndex==0)
                        getmFragmentPagerAdapter().getRouteFragment().onNavigateAway();
                    if(getmFragmentPagerAdapter().getScanFragment()!=null && prevPageIndex==1)
                        getmFragmentPagerAdapter().getScanFragment().onNavigateAway();
                    if(getmFragmentPagerAdapter().getScoreFragment()!=null)
                        getmFragmentPagerAdapter().getScoreFragment().onNavigateTo();
                    break;
            }

            prevPageIndex = position;
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    private class HelloActivityOnTabSelectedListener implements TabLayout.OnTabSelectedListener {
        private final ViewPager mViewPager;

        public HelloActivityOnTabSelectedListener(ViewPager viewPager){
            mViewPager = viewPager;
        }

        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            if(mViewPager.getAdapter().getCount() <= tab.getPosition()){
                getActivityHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getmTabLayout().getTabAt(mViewPager.getCurrentItem()).select();
                    }
                }, 200);
            }
            else if(mViewPager.getCurrentItem() == 2 && tab.getPosition() != 2){
                final int selectedTabPosition = tab.getPosition();
                new AlertDialog.Builder(HelloActivity.this)
                        .setTitle("Navigate away from Score tab")
                        .setMessage("Current session score will be lost.")
                        .setPositiveButton("Navigate away", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Do stuff
                                navigateAwayFromScore(selectedTabPosition);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                getActivityHandler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        getmTabLayout().getTabAt(mViewPager.getCurrentItem()).select();
                                    }
                                }, 200);
                            }
                        })
                        .show();
            }
            else{
                mViewPager.setCurrentItem(tab.getPosition());
            }
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {

        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {

        }
    }

    /*=========================================================================
     * Other methods
     *=======================================================================*/
    /**
     * This method handles what needs to be done when navigating away from score tab.
     * This method enable swiping on viewpager, calls viewpager to set to the target
     * position, update tab colors, reinitialize Climber info and reinitialize scan tab.
     *
     * @param targetPosition the destination position to navigate to
     */
    private void navigateAwayFromScore(int targetPosition){
        getmViewPager().setIsAllowSwiping(true);
        getmViewPager().setCurrentItem(targetPosition);
        getmFragmentPagerAdapter().setCount(2);
    }

    @Override
    public void onSubmit(String currentScore){
        // Make QueueObject
        QueueObject mQueueObject = new QueueObject(getmUser().getUserId(),
                getmUser().getAuthToken(),
                getmUser().getCategoryId(),
                getmUser().getRouteId(),
                getmClimber().getClimberId(),
                currentScore,
                this);

        // Add to a queue of QueueObject request.
        ((CrimpApplication)getApplicationContext()).addRequest(mQueueObject);

        if(DEBUG) Log.d(TAG, "submit score: "+currentScore);
        navigateAwayFromScore(1);
    }

    @Override
    public void onBackPressed() {
        final int currentTab = getmViewPager().getCurrentItem();
        switch(currentTab){
            case 0:
                break;
            case 1:
                getmViewPager().setCurrentItem(currentTab - 1);
                break;
            case 2:
                new AlertDialog.Builder(this)
                        .setTitle("Navigate away from Score tab")
                        .setMessage("Current session score will be lost.")
                        .setPositiveButton("Navigate away", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Do stuff
                                navigateAwayFromScore(1);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing
                            }
                        })
                        .show();
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
            case R.id.action_helpme:
                Toast toast = Toast.makeText(this,
                        "A ticket has been sent to the admins. Please wait for assistance.",
                        Toast.LENGTH_SHORT);
                toast.show();
                HelpMeRequest mHelpMeRequest = new HelpMeRequest(getmUser().getUserId(),
                        getmUser().getAuthToken(), getmUser().getCategoryId(), getmUser().getRouteId(), this);
                spiceManager.execute(mHelpMeRequest, new HelpMeRequestListener());
                return true;
            case R.id.action_logout:
                new AlertDialog.Builder(this)
                        .setMessage("Logout?")
                        .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Do stuff
                                LoginManager.getInstance().logOut();
                                Intent mIntent = new Intent(getApplicationContext(), LoginActivity.class);
                                finish();
                                startActivity(mIntent);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void createAndSwitchToScanFragment() {
        getmFragmentPagerAdapter().setCount(2);
        getmViewPager().setCurrentItem(1);
    }

    @Override
    public void onSpinnerSelectionChange(){
        if (DEBUG) Log.d(TAG, "Spinner selection change when we are showing mViewPager:" + getmViewPager().getCurrentItem());
        if(getmViewPager().getCurrentItem()==0){
            getmFragmentPagerAdapter().setCount(1);

            getmUser().setCategoryId(null);
            getmUser().setRouteId(null);
        }
        else{
            Log.w(TAG, "Spinner selection change while not in route tab.");
        }
    }

    @Override
    @NonNull
    public User getUser(){
        return new User(getmUser());
    }

    @Override
    @NonNull
    public Categories getCategories(){
        return new Categories(getmCategories());
    }

    @Override
    public void onCategoryRouteSelected(String categoryId, String routeId){
        getmUser().setCategoryId(categoryId);
        getmUser().setRouteId(routeId);
    }

    @Override
    public void setCategories(@NonNull Categories categories){
        getmCategories().copy(categories);
    }

    @Override
    public void saveRouteInstance(Bundle bundle){
        routeBundle = bundle;
    }

    @Override
    public Bundle restoreRouteInstance(){
        return routeBundle;
    }

    @Override
    public void saveScanInstance(Bundle bundle){
        scanBundle = bundle;
    }

    @Override
    public Bundle restoreScanInstance(){
        return scanBundle;
    }

    @Override
    public void saveScoreInstance(Bundle bundle){
        scoreBundle = bundle;
    }

    @Override
    public Bundle restoreScoreInstance(){
        return scoreBundle;
    }

    @Override
    public void createAndSwitchToScoreFragment() {
        getmFragmentPagerAdapter().setCount(3);
        getmViewPager().setCurrentItem(2);
        getmViewPager().setIsAllowSwiping(false);
    }

    @Override
    public void updateActivityClimberInfo(String climberId, String climberName){
        getmClimber().setClimberId(climberId);
        getmClimber().setClimberName(climberName);
        getmClimber().setTotalScore(null);
    }

    @Override
    public String getCategoryId(){
        return getmUser().getCategoryId();
    }

    @Override
    public Climber getClimber(){
        return new Climber(getmClimber());
    }

    @Override
    public void updateClimberInfo(String climberName,String totalScore){
        getmClimber().setClimberName(climberName);
        getmClimber().setTotalScore(totalScore);
    }

    @Override
    public String getScoringType(){
        CategoriesResponseBody.Category c = getmCategories().findCategoryById(getmUser().getCategoryId());
        if(c!=null){
            CategoriesResponseBody.Category.Route r = c.findRouteById(getmUser().getRouteId());
            if(r != null)
                return r.getScore();
        }

        return null;
    }

    @Override
    public void collapseToolBar(){
        AppBarLayout mAppBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);
        if(mAppBarLayout != null)
            mAppBarLayout.setExpanded(false);
    }

    @Override
    public String[] getCategoryNameAndRouteName(){
        CategoriesResponseBody.Category category = getmCategories().findCategoryById(getmUser().getCategoryId());
        CategoriesResponseBody.Category.Route route = category.findRouteById(getmUser().getRouteId());

        String[] result = {category.getCategoryName(), route.getRouteName()};

        return result;
    }
}