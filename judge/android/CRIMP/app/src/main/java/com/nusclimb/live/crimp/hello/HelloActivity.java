package com.nusclimb.live.crimp.hello;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
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
public class HelloActivity extends AppCompatActivity implements ActionBar.TabListener,
        RouteFragment.RouteFragmentToActivityMethods, ScanFragment.ScanFragmentToActivityMethods,
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
    private ActionBar mActionBar;
    private HelloActivityViewPager mViewPager;
    private TextView mRouteTabTextView;
    private TextView mScanTabTextView;
    private TextView mScoreTabTextView;
    private CrimpFragmentPagerAdapter mCrimpFragmentPagerAdapter;
    private Handler activityHandler;
    private int prevPageIndex;



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
        if(activityHandler == null)
            activityHandler = new Handler();

        // Prepare ActionBar, create a routeTab for our RouteFragment.
        if(mCrimpFragmentPagerAdapter == null)
            mCrimpFragmentPagerAdapter = new CrimpFragmentPagerAdapter(getSupportFragmentManager());

        mViewPager = (HelloActivityViewPager)findViewById(R.id.pager);
        mActionBar = getSupportActionBar();
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        mRouteTabTextView = (TextView)getLayoutInflater().inflate(R.layout.routetab, null);
        mScanTabTextView = (TextView)getLayoutInflater().inflate(R.layout.scantab, null);
        mScoreTabTextView = (TextView)getLayoutInflater().inflate(R.layout.scoretab, null);
        ActionBar.Tab routeTab = mActionBar.newTab()
                .setTabListener(this)
                .setCustomView(mRouteTabTextView);
        mActionBar.addTab(routeTab);
        ActionBar.Tab scanTab = mActionBar.newTab()
                .setTabListener(this)
                .setCustomView(mScanTabTextView);
        mActionBar.addTab(scanTab);
        ActionBar.Tab scoreTab = mActionBar.newTab()
                .setTabListener(this)
                .setCustomView(mScoreTabTextView);
        mActionBar.addTab(scoreTab);

        // Prepare ViewPager, attach adapter to ViewPager.
        mViewPager.setAdapter(mCrimpFragmentPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            // This is THE listener for page swiping. Navigating between fragments is always
            // done by first calling ViewPager.setCurrentItem(int). Calling
            // ViewPager.setCurrentItem(int) will trigger a onPageSelected(int) call in ViewPager's
            // listener. ActionBar tab selection is then done during the callback.
            private final String TAG = ViewPager.SimpleOnPageChangeListener.class.getSimpleName();

            @Override
            public void onPageScrollStateChanged(int state) {
                /*
                if(DEBUG) {
                    String stateString;
                    switch (state) {
                        case ViewPager.SCROLL_STATE_DRAGGING:
                            stateString = "ScrollStateDragging";
                            break;
                        case ViewPager.SCROLL_STATE_IDLE:
                            stateString = "ScrollStateIdle";
                            break;
                        case ViewPager.SCROLL_STATE_SETTLING:
                            stateString = "ScrollStateSettling";
                            break;
                        default:
                            stateString = null;
                    }
                    Log.d(TAG, "onPageScrollStateChanged(" + stateString + ")");
                }
                */
                super.onPageScrollStateChanged(state);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //if (DEBUG) Log.d(TAG, "onPageScrolled(" + position + ", " + positionOffset + ", " + positionOffsetPixels + ")");
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                // Note to self: when this method is called, the ViewPager.getCurrentItem() is already
                // returning the parameter position. Therefore we should be careful not to have a loop
                // and call ViewPager.setCurrentItem(int) again.

                if (DEBUG) Log.d(TAG, "onPageSelected(" + position + ") viewPagerCurrentItem:" + mViewPager.getCurrentItem()
                        +" prev:"+prevPageIndex);
                super.onPageSelected(position);

                // If the actionbar tab selection is the same as ViewPager's selection, we do nothing. It is possible
                // for actionbar tab to already be on the correct position (such as when user navigate by pressing tab instead
                // of swiping).
                if(mActionBar.getSelectedNavigationIndex() != position)
                    mActionBar.setSelectedNavigationItem(position);

                // Do onNavigateAway before onNavigateTo
                switch(position){
                    case 0:
                        if(mCrimpFragmentPagerAdapter.getScanFragment()!=null && prevPageIndex==1)
                            mCrimpFragmentPagerAdapter.getScanFragment().onNavigateAway();
                        if(mCrimpFragmentPagerAdapter.getScoreFragment()!=null && prevPageIndex==2)
                            mCrimpFragmentPagerAdapter.getScoreFragment().onNavigateAway();
                        if(mCrimpFragmentPagerAdapter.getRouteFragment()!=null)
                            mCrimpFragmentPagerAdapter.getRouteFragment().onNavigateTo();
                        break;
                    case 1:
                        if(mCrimpFragmentPagerAdapter.getRouteFragment()!=null && prevPageIndex==0)
                            mCrimpFragmentPagerAdapter.getRouteFragment().onNavigateAway();
                        if(mCrimpFragmentPagerAdapter.getScoreFragment()!=null && prevPageIndex==2)
                            mCrimpFragmentPagerAdapter.getScoreFragment().onNavigateAway();
                        if(mCrimpFragmentPagerAdapter.getScanFragment()!=null)
                            mCrimpFragmentPagerAdapter.getScanFragment().onNavigateTo();
                        break;
                    case 2:
                        if(mCrimpFragmentPagerAdapter.getRouteFragment()!=null && prevPageIndex==0)
                            mCrimpFragmentPagerAdapter.getRouteFragment().onNavigateAway();
                        if(mCrimpFragmentPagerAdapter.getScanFragment()!=null && prevPageIndex==1)
                            mCrimpFragmentPagerAdapter.getScanFragment().onNavigateAway();
                        if(mCrimpFragmentPagerAdapter.getScoreFragment()!=null)
                            mCrimpFragmentPagerAdapter.getScoreFragment().onNavigateTo();
                        break;
                }

                prevPageIndex = position;
            }
        });

        if(savedInstanceState == null) {
            // Newly created activity therefore we can only get information from intent.
            mUser = new User();
            mUser.setFacebookAccessToken(getIntent().getStringExtra(getString(R.string.bundle_access_token)));
            mUser.setUserName(getIntent().getStringExtra(getString(R.string.bundle_user_name)));
            mUser.setUserId(getIntent().getStringExtra(getString(R.string.bundle_x_user_id)));
            mUser.setAuthToken(getIntent().getStringExtra(getString(R.string.bundle_x_auth_token)));

            mCategories = new Categories();
            mClimber = new Climber();
        }
        else{
            // Recreating activity. Restore all info from saved instance state.
            mUser = new User();
            mUser.setFacebookAccessToken(savedInstanceState.getString(getString(R.string.bundle_access_token)));
            mUser.setUserName(savedInstanceState.getString(getString(R.string.bundle_user_name)));
            mUser.setUserId(savedInstanceState.getString(getString(R.string.bundle_x_user_id)));
            mUser.setAuthToken(savedInstanceState.getString(getString(R.string.bundle_x_auth_token)));
            mUser.setCategoryId(savedInstanceState.getString(getString(R.string.bundle_category_id)));
            mUser.setRouteId(savedInstanceState.getString(getString(R.string.bundle_route_id)));

            mCategories = new Categories(savedInstanceState.getStringArrayList(getString(R.string.bundle_category_name_list)),
                    savedInstanceState.getStringArrayList(getString(R.string.bundle_category_id_list)),
                    savedInstanceState.getIntegerArrayList(getString(R.string.bundle_category_route_count_list)),
                    savedInstanceState.getStringArrayList(getString(R.string.bundle_route_name_list)),
                    savedInstanceState.getStringArrayList(getString(R.string.bundle_route_id_list)),
                    savedInstanceState.getStringArrayList(getString(R.string.bundle_route_score_list)),
                    savedInstanceState.getByteArray(getString(R.string.bundle_category_finalize_list)),
                    savedInstanceState.getStringArrayList(getString(R.string.bundle_category_start_list)),
                    savedInstanceState.getStringArrayList(getString(R.string.bundle_category_end_list)));

            mClimber = new Climber();
            mClimber.setClimberId(savedInstanceState.getString(getString(R.string.bundle_climber_id)));
            mClimber.setClimberName(savedInstanceState.getString(getString(R.string.bundle_climber_name)));
            mClimber.setTotalScore(savedInstanceState.getString(getString(R.string.bundle_total_score)));

            routeBundle = savedInstanceState.getBundle(getString(R.string.bundle_route_bundle));
            scanBundle = savedInstanceState.getBundle(getString(R.string.bundle_scan_bundle));
            scoreBundle = savedInstanceState.getBundle(getString(R.string.bundle_score_bundle));
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
    }

    @Override
    protected void onPause(){
        if (DEBUG) Log.d(TAG, "onPause currentItem:" + mViewPager.getCurrentItem());
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState (Bundle outState){
        super.onSaveInstanceState(outState);

        outState.putString(getString(R.string.bundle_access_token), mUser.getFacebookAccessToken());
        outState.putString(getString(R.string.bundle_user_name), mUser.getUserName());
        outState.putString(getString(R.string.bundle_x_user_id), mUser.getUserId());
        outState.putString(getString(R.string.bundle_x_auth_token), mUser.getAuthToken());
        outState.putString(getString(R.string.bundle_category_id), mUser.getCategoryId());
        outState.putString(getString(R.string.bundle_route_id), mUser.getRouteId());

        if(mCategories != null){
            outState.putStringArrayList(getString(R.string.bundle_category_name_list), mCategories.getCategoryNameList());
            outState.putStringArrayList(getString(R.string.bundle_category_id_list), mCategories.getCategoryIdList());
            outState.putIntegerArrayList(getString(R.string.bundle_category_route_count_list), mCategories.getCategoryRouteCountList());
            outState.putStringArrayList(getString(R.string.bundle_route_name_list), mCategories.getRouteNameList());
            outState.putStringArrayList(getString(R.string.bundle_route_id_list), mCategories.getRouteIdList());
            outState.putStringArrayList(getString(R.string.bundle_route_score_list), mCategories.getRouteScoreList());
            outState.putByteArray(getString(R.string.bundle_category_finalize_list), mCategories.getCategoryFinalizeArray());
            outState.putStringArrayList(getString(R.string.bundle_category_start_list), mCategories.getCategoryStartList());
            outState.putStringArrayList(getString(R.string.bundle_category_end_list), mCategories.getCategoryEndList());
        }

        if(mClimber != null){
            outState.putString(getString(R.string.bundle_climber_id), mClimber.getClimberId());
            outState.putString(getString(R.string.bundle_climber_name), mClimber.getClimberName());
            outState.putString(getString(R.string.bundle_total_score), mClimber.getTotalScore());
        }

        outState.putBundle(getString(R.string.bundle_route_bundle), routeBundle);
        outState.putBundle(getString(R.string.bundle_route_bundle), scanBundle);
        outState.putBundle(getString(R.string.bundle_route_bundle), scoreBundle);

        if (DEBUG) Log.d(TAG, "HelloActivity onSaveInstanceState");
    }

    @Override
    protected void onStop(){
        spiceManager.shouldStop();
        if (DEBUG) Log.d(TAG, "onStop");
        super.onStop();
    }



    /*=========================================================================
     * ActionBar.TabListener interface methods
     *=======================================================================*/
    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {}

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // Note to self: When this method is called, the correct tab is already being selected (i.e.
        // tab.getPosition() == ActionBar.getSelectedNavigationIndex() ). We should be careful not
        // to create a loop and call ActionBar.setSelectedNavigationIndex() again.
        // This method will not be called if select back the same tab.

        // In this method, a change to tab selection has already occur. We want to update the viewPager
        // page to reflect this change if possible. Therefore we do nothing if
        // 1) selectedTab is invalid (i.e. selectedTabPosition >= numberOfFragment)
        // 2) viewPager page is already correct (i.e. viewPagerCurrentIndex == selectedTabPosition)

        final int selectedTabPosition = tab.getPosition();
        final int currentTabPosition = mActionBar.getSelectedNavigationIndex();
        final int viewPagerCurrentIndex = mViewPager.getCurrentItem();
        int numberOfFragment = mCrimpFragmentPagerAdapter.getCount();

        if (DEBUG) Log.d(TAG, "onTabSelected. SelectedTab:"+selectedTabPosition+
                " currentTab:"+currentTabPosition+
                " viewPagerCurrent:"+viewPagerCurrentIndex+
                " adapterCount:"+numberOfFragment);

        // case 1
        if(selectedTabPosition >= numberOfFragment){
            activityHandler.postAtFrontOfQueue(new Runnable() {
                @Override
                public void run() {
                    getSupportActionBar().setSelectedNavigationItem(viewPagerCurrentIndex);
                }
            });
            return;
        }

        // case 2
        if(viewPagerCurrentIndex == selectedTabPosition)
            return;

        // Do work here
        if(viewPagerCurrentIndex == 2){
            activityHandler.postAtFrontOfQueue(new Runnable() {
                @Override
                public void run() {
                    getSupportActionBar().setSelectedNavigationItem(viewPagerCurrentIndex);
                }
            });

            if(viewPagerCurrentIndex!=tab.getPosition()){
                new AlertDialog.Builder(this)
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

                            }
                        })
                        .show();
            }
        }
        else
            mViewPager.setCurrentItem(selectedTabPosition);
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {}



    /*=========================================================================
     * SpiceRequest Listener
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
        mViewPager.setIsAllowSwiping(true);
        mViewPager.setCurrentItem(targetPosition);
        mCrimpFragmentPagerAdapter.setCount(2);
        mRouteTabTextView.setTextColor(getResources().getColor(R.color.white));
        mScanTabTextView.setTextColor(getResources().getColor(R.color.white));
        mScoreTabTextView.setTextColor(getResources().getColor(R.color.whiteWithTransparency));
        mClimber = new Climber();
    }

    @Override
    public void onSubmit(String currentScore){
        // Make QueueObject
        QueueObject mQueueObject = new QueueObject(mUser.getUserId(),
                mUser.getAuthToken(),
                mUser.getCategoryId(),
                mUser.getRouteId(),
                mClimber.getClimberId(),
                currentScore,
                this);

        // Add to a queue of QueueObject request.
        ((CrimpApplication)getApplicationContext()).addRequest(mQueueObject);

        if(DEBUG) Log.d(TAG, "submit score: "+currentScore);
        navigateAwayFromScore(1);
    }

    @Override
    public void onBackPressed() {
        final int currentTab = mViewPager.getCurrentItem();
        switch(currentTab){
            case 0:
                break;
            case 1:
                mViewPager.setCurrentItem(currentTab - 1);
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
                HelpMeRequest mHelpMeRequest = new HelpMeRequest(mUser.getUserId(),
                        mUser.getAuthToken(), mUser.getCategoryId(), mUser.getRouteId(), this);
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
        mCrimpFragmentPagerAdapter.setCount(2);
        mRouteTabTextView.setTextColor(getResources().getColor(R.color.white));
        mScanTabTextView.setTextColor(getResources().getColor(R.color.white));
        mScoreTabTextView.setTextColor(getResources().getColor(R.color.whiteWithTransparency));
        mViewPager.setCurrentItem(1);
    }

    @Override
    public void onSpinnerSelectionChange(){
        if (DEBUG) Log.d(TAG, "Spinner selection change when we are showing mViewPager:" + mViewPager.getCurrentItem());
        if(mViewPager.getCurrentItem()==0){
            mCrimpFragmentPagerAdapter.setCount(1);

            mRouteTabTextView.setTextColor(getResources().getColor(R.color.white));
            mScanTabTextView.setTextColor(getResources().getColor(R.color.whiteWithTransparency));
            mScoreTabTextView.setTextColor(getResources().getColor(R.color.whiteWithTransparency));

            mUser.setCategoryId(null);
            mUser.setRouteId(null);
            mClimber = new Climber();
        }
        else{
            Log.w(TAG, "Spinner selection change while not in route tab.");
        }
    }

    @Override
    public User getUser(){
        return new User(mUser);
    }

    @Override
    public Categories getCategories(){
        return new Categories(mCategories);
    }

    @Override
    public void onCategoryRouteSelected(String categoryId, String routeId){
        mUser.setCategoryId(categoryId);
        mUser.setRouteId(routeId);

        mClimber = new Climber();
    }

    @Override
    public void setCategories(Categories categories){
        mCategories = new Categories(categories);
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
    public void resetClimber(){
        mClimber = new Climber();
    }

    @Override
    public void createAndSwitchToScoreFragment() {
        mCrimpFragmentPagerAdapter.setCount(3);
        mRouteTabTextView.setTextColor(getResources().getColor(R.color.whiteWithTransparency));
        mScanTabTextView.setTextColor(getResources().getColor(R.color.whiteWithTransparency));
        mScoreTabTextView.setTextColor(getResources().getColor(R.color.white));
        mViewPager.setCurrentItem(2);
        mViewPager.setIsAllowSwiping(false);
    }

    @Override
    public void updateActivityClimberInfo(String climberId, String climberName){
        mClimber.setClimberId(climberId);
        mClimber.setClimberName(climberName);
        mClimber.setTotalScore(null);
    }

    @Override
    public String getCategoryId(){
        return mUser.getCategoryId();
    }

    @Override
    public Climber getClimber(){
        return new Climber(mClimber);
    }

    @Override
    public void updateClimberInfo(String climberName,String totalScore){
        mClimber.setClimberName(climberName);
        mClimber.setTotalScore(totalScore);
    }

    @Override
    public String getScoringType(){
        CategoriesResponseBody.Category c = mCategories.findCategoryById(mUser.getCategoryId());
        if(c!=null){
            CategoriesResponseBody.Category.Route r = c.findRouteById(mUser.getRouteId());
            if(r != null)
                return r.getScore();
        }

        return null;
    }
}