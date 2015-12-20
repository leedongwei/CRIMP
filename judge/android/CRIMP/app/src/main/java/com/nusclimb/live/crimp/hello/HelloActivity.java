package com.nusclimb.live.crimp.hello;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.nusclimb.live.crimp.CrimpFragmentStatePagerAdapter;
import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.common.Categories;
import com.nusclimb.live.crimp.common.Climber;
import com.nusclimb.live.crimp.common.User;
import com.nusclimb.live.crimp.login.LoginActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class HelloActivity extends ActionBarActivity implements ActionBar.TabListener,
        RouteFragment.RouteFragmentToActivityMethods, ScanFragment.ScanFragmentToActivityMethods,
        ScoreFragment.ScoreFragmentToActivityMethods {
    private final String TAG = HelloActivity.class.getSimpleName();

    // All the info
    private User mUser = null;
    private Categories mCategories = null;
    private Climber mClimber = null;

    // For doing tab manipulation
    private ActionBar mActionBar;
    private CrimpViewPager mViewPager;
    private HintableArrayAdapter mHintableArrayAdapter;

    private CrimpFragmentStatePagerAdapter mCrimpFragmentStatePagerAdapter;
    private Handler activityHandler;






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

        // Prepare the initial fragment (RouteFragment) and add it to a list of fragments.
        List<CrimpFragment> fragList = new ArrayList<CrimpFragment>();
        RouteFragment rf = RouteFragment.newInstance(getIntent().getExtras());
        fragList.add(rf);

        /* Note to self: ActionBar and ViewPager are seperated. User can switch between
         * fragments by using either ActionBar or ViewPager. When ActionBar is used, we set
         * ViewPager to match the ActionBar and vice versa.
         *
         * ActionBar (the top bar) is just navigation "buttons" and does not have "content".
         *
         * ViewPager (the entire content area) has an adapter which contains a list of fragments.
         * When we swipe the ViewPager, the adapter will pull the correct fragment from its list
         * and display it.
         */

        activityHandler = new Handler();

        // Prepare ActionBar, create a routeTab for our RouteFragment.
        mCrimpFragmentStatePagerAdapter = new CrimpFragmentStatePagerAdapter(getSupportFragmentManager(), fragList);
        mViewPager = (CrimpViewPager)findViewById(R.id.pager);
        mActionBar = getSupportActionBar();
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        ActionBar.Tab routeTab = mActionBar.newTab()
                .setText(rf.getPageTitle())
                .setTabListener(this);
        mActionBar.addTab(routeTab);

        // Prepare ViewPager, attach adapter to ViewPager, back adapter with a list of fragment.
        mViewPager.setAdapter(mCrimpFragmentStatePagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            // This is THE listener for page swiping.
            private final String TAG = ViewPager.SimpleOnPageChangeListener.class.getSimpleName();

            @Override
            public void onPageScrollStateChanged(int state) {
                Log.v(TAG, "onPageScrollStateChanged(" + state + ")");
                super.onPageScrollStateChanged(state);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.v(TAG, "onPageScrolled(" + position + ", " + positionOffset + ", " + positionOffsetPixels + ")");
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                // When swiping between different app sections, select the corresponding tab.
                // We can also use ActionBar.Tab#select() to do this if we have a reference to the
                // Tab.
                Log.d(TAG, "onPageSelected(" + position + ") currentItem:"+mViewPager.getCurrentItem());
                if(mViewPager.getCurrentItem()==2 && position == 1){
                    //TODO
                    Log.d(TAG, "DONT ALLOW GOING TO SCAN FRAG");
                }
                else{
                    super.onPageSelected(position);
                    mActionBar.setSelectedNavigationItem(position);
                    Log.d(TAG, "ALLOWED GOING TO SCAN FRAG");
                }
            }
        });

        if(savedInstanceState == null) {
            // Newly created activity therefore we can only get information from intent.
            mUser = new User();
            mUser.setFacebookAccessToken(getIntent().getStringExtra(getString(R.string.bundle_access_token)));
            mUser.setUserName(getIntent().getStringExtra(getString(R.string.bundle_user_name)));
            mUser.setUserId(getIntent().getStringExtra(getString(R.string.bundle_x_user_id)));
            mUser.setAuthToken(getIntent().getStringExtra(getString(R.string.bundle_x_auth_token)));
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
            //mUser.setClimberId(savedInstanceState.getString(getString(R.string.bundle_climber_id)));

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
        }
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
        //outState.putString(getString(R.string.bundle_climber_id), mUser.getClimberId());

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
            // We skipped climberId because we already done that in User.
            outState.putString(getString(R.string.bundle_climber_name), mClimber.getClimberName());
            outState.putString(getString(R.string.bundle_total_score), mClimber.getTotalScore());
        }
    }



    /*=========================================================================
     * ActionBar.TabListener interface methods
     *=======================================================================*/
    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        if(mViewPager.getCurrentItem()!=2) {
            // We can always go back to earlier tab (earlier tab always exist).
            // We can go to a tab if it exist.
            int currentIndex = mViewPager.getCurrentItem();
            if (tab.getPosition() < currentIndex || tab.getPosition() < mCrimpFragmentStatePagerAdapter.getCount()) {
                mViewPager.setCurrentItem(tab.getPosition());
            }
        }
        else{
            activityHandler.postAtFrontOfQueue(new Runnable() {
                @Override
                public void run() {
                    getSupportActionBar().setSelectedNavigationItem(2);
                }
            });
        }

        if(mViewPager.getCurrentItem()==2){
            mViewPager.setIsAllowSwiping(false);
        }
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }



    /*=========================================================================
     * Other methods
     *=======================================================================*/


    @Override
    public void onBackPressed()
    {
        int currentTab = mViewPager.getCurrentItem();
        if(currentTab == 0){
            //super.onBackPressed();
        }
        else{
            mActionBar.setSelectedNavigationItem(currentTab-1);
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

    @Override
     public void createAndSwitchToScanFragment(User user, Categories categoriesInfo) {
        mUser = new User(user);
        mCategories = new Categories(categoriesInfo);

        //We try to switch to ScanFragment. Create ScanFragment if it does not already exist.
        if(mActionBar.getTabCount()<2){
            ScanFragment mScanFragment = ScanFragment.newInstance(user, categoriesInfo, this);

            ActionBar.Tab tab = mActionBar.newTab()
                    .setText(mScanFragment.getPageTitle())
                    .setTabListener(this);
            mActionBar.addTab(tab);

            mCrimpFragmentStatePagerAdapter.addFragment(mScanFragment);
        }
        mViewPager.setCurrentItem(1);
    }

    @Override
    public void createAndSwitchToScoreFragment(User user, Climber climber) {
        //TODO
        mUser = new User(user);
        mClimber = new Climber(climber);

        //We try to switch to ScoreFragment. Create ScoreFragment if it does not already exist.
        if(mActionBar.getTabCount()<3){
            ScoreFragment mScoreFragment = ScoreFragment.newInstance(user, mCategories, climber, this);

            ActionBar.Tab tab = mActionBar.newTab()
                    .setText(mScoreFragment.getPageTitle())
                    .setTabListener(this);
            mActionBar.addTab(tab);

            mCrimpFragmentStatePagerAdapter.addFragment(mScoreFragment);
        }
        mViewPager.setCurrentItem(2);
    }

    @Override
    public void destroyOtherTabButRoute(){
        if(mViewPager.getCurrentItem()==0) {
            Log.v(TAG, "currentTab: " + mViewPager.getCurrentItem() + "number of tab: " + mActionBar.getTabCount());
            for (int i = mActionBar.getTabCount(); i > mViewPager.getCurrentItem() + 1; i--) {
                removeFragment(i - 1);
            }
        }
    }

    @Override
    public void destroyOtherTabButScan(){
        if(mViewPager.getCurrentItem()==1) {
            Log.v(TAG, "currentTab: " + mViewPager.getCurrentItem() + "number of tab: " + mActionBar.getTabCount());
            for (int i = mActionBar.getTabCount(); i > mViewPager.getCurrentItem() + 1; i--) {
                removeFragment(i - 1);
            }
        }
    }

    private void removeFragment(int position){
        mCrimpFragmentStatePagerAdapter.removeFragment(position);
        mActionBar.removeTabAt(position);
    }

}