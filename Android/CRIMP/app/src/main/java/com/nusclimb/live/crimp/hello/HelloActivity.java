package com.nusclimb.live.crimp.hello;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.Window;
import android.view.WindowManager;

import com.nusclimb.live.crimp.CrimpApplication2;
import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.common.dao.Climber;
import com.nusclimb.live.crimp.common.dao.User;
import com.nusclimb.live.crimp.common.event.SwipeTo;
import com.nusclimb.live.crimp.hello.route.RouteFragment;
import com.nusclimb.live.crimp.hello.scan.ScanFragment;
import com.nusclimb.live.crimp.network.model.CategoriesJs;
import com.squareup.otto.Produce;

public class HelloActivity extends AppCompatActivity implements
        RouteFragment.RouteFragmentInterface,
        ScanFragment.ScanFragmentInterface{
    private static final String TAG = "HelloActivity";
    private static final boolean DEBUG = true;

    public static final String SAVE_USER = "save_user";
    public static final String SAVE_CATEGORIES = "save_categories";
    public static final String SAVE_CATEGORY_INDEX = "save_category_index";
    public static final String SAVE_ROUTE_INDEX = "save_route_index";
    public static final String SAVE_STAGE = "save_stage";
    public static final String SAVE_COMMITTED_CATEGORY = "save_committed_category";
    public static final String SAVE_COMMITTED_ROUTE = "save_committed_route";
    public static final String SAVE_CAN_DISPLAY = "save_can_display";
    public static final String SAVE_IS_SCANNING = "save_is_scanning";

    // All the info
    private User mUser;
    private CategoriesJs mCategories;
    private Climber mClimber;
    private boolean[] mCanDisplay;

    // Route fragment info
    private int mCategoryPosition;
    private int mRoutePosition;
    private int mCommittedCategoryPosition;
    private int mCommittedRoutePosition;

    // Scan fragment info
    private boolean mIsScanning;

    // Views
    private AppBarLayout mAppBar;
    private Toolbar mToolbar;
    private TabLayout mTabLayout;
    private HelloViewPager mPager;

    private HelloFragmentAdapter mFragmentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        // We want as long as this window is visible to the user,
        // keep the device's screen turned on and bright.
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_hello);

        /* Note to self: This activity UI has only 4 components:
         * 1) mAppBar: Wrap up mToolbar and mTabLayout to provide many of the features of material
         * designs app bar concept, namely scrolling gestures.
         *
         * 2) mToolbar: A bar with title.
         *
         * 3) mTabLayout: A bar with tabs
         *
         * 4) mPager: ViewPager showing fragments.
         */
        // Find reference to views
        mAppBar = (AppBarLayout) findViewById(R.id.app_bar_layout);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mPager = (HelloViewPager) findViewById(R.id.pager);

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
        setSupportActionBar(mToolbar);

        // Load/instantiate data we already have.
        if(savedInstanceState == null){
            mUser = (User)getIntent().getSerializableExtra(SAVE_USER);
            mCanDisplay = new boolean[]{true, false, false};
            mIsScanning = true;
        }
        else{
            mUser = (User)savedInstanceState.getSerializable(SAVE_USER);
            mCategories = (CategoriesJs) savedInstanceState.getSerializable(SAVE_CATEGORIES);
            mCategoryPosition = savedInstanceState.getInt(SAVE_CATEGORY_INDEX, 0);
            mRoutePosition = savedInstanceState.getInt(SAVE_ROUTE_INDEX, 0);
            mCommittedCategoryPosition = savedInstanceState.getInt(SAVE_COMMITTED_CATEGORY, 0);
            mCommittedRoutePosition = savedInstanceState.getInt(SAVE_COMMITTED_ROUTE, 0);
            mCanDisplay = savedInstanceState.getBooleanArray(SAVE_CAN_DISPLAY);
            if(mCanDisplay == null){
                mCanDisplay = new boolean[]{true, false, false};
            }
            mIsScanning = savedInstanceState.getBoolean(SAVE_IS_SCANNING, true);
        }

        // prepare view pager
        mFragmentAdapter = new HelloFragmentAdapter(getSupportFragmentManager());
        for(int i=0; i<mCanDisplay.length; i++){
            mFragmentAdapter.getCanDisplay()[i] = mCanDisplay[i];
        }

        mTabLayout.addTab(mTabLayout.newTab().setText(mFragmentAdapter.getPageTitle(0)));
        mTabLayout.addTab(mTabLayout.newTab().setText(mFragmentAdapter.getPageTitle(1)));
        mTabLayout.addTab(mTabLayout.newTab().setText(mFragmentAdapter.getPageTitle(2)));
        mTabLayout.setOnTabSelectedListener(new HelloOnTabSelectedListener(mPager, mTabLayout));

        mPager.setAdapter(mFragmentAdapter);
        mPager.addOnPageChangeListener(new HelloPageChangeListener(mTabLayout));
    }

    @Override
    protected void onStart(){
        super.onStart();
        CrimpApplication2.getBusInstance().register(this);
    }

    @Override
    protected void onStop(){
        CrimpApplication2.getBusInstance().unregister(this);
        super.onStop();
    }

    @Produce
    public SwipeTo produceCurrentTab() {
        return new SwipeTo(mPager.getCurrentItem());
    }

    @Override
    protected void onSaveInstanceState (Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putSerializable(SAVE_USER, mUser);
        outState.putSerializable(SAVE_CATEGORIES, mCategories);
        outState.putInt(SAVE_CATEGORY_INDEX, mCategoryPosition);
        outState.putInt(SAVE_ROUTE_INDEX, mRoutePosition);
        outState.putInt(SAVE_COMMITTED_CATEGORY, mCommittedCategoryPosition);
        outState.putInt(SAVE_COMMITTED_ROUTE, mCommittedRoutePosition);
        outState.putBooleanArray(SAVE_CAN_DISPLAY, mCanDisplay);
        outState.putBoolean(SAVE_IS_SCANNING, mIsScanning);
    }

    /*
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
    */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_hello, menu);
        return true;
    }

    @Override
    public User getUser(){
        return mUser;
    }

    @Override
    public void setCategoriesJs(CategoriesJs categories) {
        mCategories = categories;
    }

    @Override
    public CategoriesJs getCategoriesJs() {
        return mCategories;
    }

    @Override
    public void setCategoryPosition(int categoryPosition) {
        mCategoryPosition = categoryPosition;
    }

    @Override
    public int getCategoryPosition() {
        return mCategoryPosition;
    }

    @Override
    public void setRoutePosition(int routePosition) {
        mRoutePosition = routePosition;
    }

    @Override
    public int getRoutePosition() {
        return mRoutePosition;
    }

    @Override
    public void setCommittedCategoryPosition(int categoryPosition) {
        mCommittedCategoryPosition = categoryPosition;
    }

    @Override
    public int getCommittedCategoryPosition() {
        return mCommittedCategoryPosition;
    }

    @Override
    public void setCommittedRoutePosition(int routePosition) {
        mCommittedRoutePosition = routePosition;
    }

    @Override
    public int getCommittedRoutePosition() {
        return mCommittedRoutePosition;
    }

    @Override
    public void goToScanTab() {
        mCommittedCategoryPosition = mCategoryPosition;
        mCommittedRoutePosition = mRoutePosition;

        mCanDisplay[1] = true;
        mFragmentAdapter.getCanDisplay()[1] = true;

        mPager.setCurrentItem(1);
    }

    @Override
    public void setCanDisplay(boolean[] canDisplay){
        this.mCanDisplay = canDisplay;
        for(int i=0; i<mCanDisplay.length; i++){
            mFragmentAdapter.getCanDisplay()[i] = mCanDisplay[i];
        }
    }

    @Override
    public void setIsScanning(boolean isScanning){
        mIsScanning = isScanning;
    }

    @Override
    public boolean getIsScanning(){
        return mIsScanning;
    }

    /*
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
                String url = getString(R.string.crimp_base_url)+getString(R.string.helpme_api);
                HelpMeRequest mHelpMeRequest = new HelpMeRequest(getmUser().getUserId(),
                        getmUser().getAuthToken(), getmUser().getCategoryId(),
                        getmUser().getRouteId(), url);
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
    */

/*
    @Override
    public void collapseToolBar(){
        AppBarLayout mAppBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);
        if(mAppBarLayout != null)
            mAppBarLayout.setExpanded(false);
    }
    */


}
