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

import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.common.dao.Category;
import com.nusclimb.live.crimp.common.dao.Climber;
import com.nusclimb.live.crimp.common.dao.User;
import com.nusclimb.live.crimp.hello.route.RouteFragment;

import java.util.ArrayList;
import java.util.UUID;

public class HelloActivity extends AppCompatActivity implements
        RouteFragment.RouteFragmentInterface{
    private static final String TAG = "HelloActivity";
    private static final boolean DEBUG = true;

    public static final String SAVE_USER = "save_user";
    public static final String SAVE_CATEGORIES = "save_categories";
    public static final String SAVE_CATEGORIES_TXID = "save_categories_txid";

    private Bundle routeBundle;
    private Bundle scanBundle;
    private Bundle scoreBundle;

    // All the info
    private User mUser;
    private ArrayList<Category> mCategoryList;
    private Climber mClimber;

    // Views
    private AppBarLayout mAppBar;
    private Toolbar mToolbar;
    private TabLayout mTabLayout;
    private HelloViewPager mPager;

    private HelloFragmentAdapter mFragmentAdapter;

    private UUID categoriesTxId;

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
        }
        else{
            mUser = (User)savedInstanceState.getSerializable(SAVE_USER);
            mCategoryList = (ArrayList<Category>)savedInstanceState.getSerializable(SAVE_CATEGORIES);
        }

        // prepare view pager
        mFragmentAdapter = new HelloFragmentAdapter(getSupportFragmentManager());

        mTabLayout.addTab(mTabLayout.newTab().setText(mFragmentAdapter.getPageTitle(0)));
        mTabLayout.addTab(mTabLayout.newTab().setText(mFragmentAdapter.getPageTitle(1)));
        mTabLayout.addTab(mTabLayout.newTab().setText(mFragmentAdapter.getPageTitle(2)));
        mTabLayout.setOnTabSelectedListener(new HelloOnTabSelectedListener(mPager, mTabLayout));

        mPager.setAdapter(mFragmentAdapter);
        mPager.addOnPageChangeListener(new HelloPageChangeListener(mTabLayout));
    }

    @Override
    protected void onSaveInstanceState (Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putSerializable(SAVE_USER, mUser);
        outState.putSerializable(SAVE_CATEGORIES, mCategoryList);
    }




    /*
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
                AlertDialog alertDialog = new AlertDialog.Builder(HelloActivity.this)
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
                        .create();
                alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        getActivityHandler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                getmTabLayout().getTabAt(mViewPager.getCurrentItem()).select();
                            }
                        }, 200);
                    }
                });
                alertDialog.show();
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
    */

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
