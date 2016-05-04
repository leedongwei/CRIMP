package rocks.crimp.crimp.hello;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

import rocks.crimp.crimp.CrimpApplication;
import rocks.crimp.crimp.R;
import rocks.crimp.crimp.common.Action;
import rocks.crimp.crimp.common.Climber;
import rocks.crimp.crimp.common.User;
import rocks.crimp.crimp.common.event.SwipeTo;
import rocks.crimp.crimp.hello.route.RouteFragment;
import rocks.crimp.crimp.hello.scan.ScanFragment;
import rocks.crimp.crimp.hello.score.ScoreFragment;
import rocks.crimp.crimp.login.LoginActivity;
import rocks.crimp.crimp.network.model.CategoriesJs;
import rocks.crimp.crimp.persistence.LocalModelImpl;

public class HelloActivity extends AppCompatActivity implements
        RouteFragment.RouteFragmentInterface,
        ScanFragment.ScanFragmentInterface,
        ScoreFragment.ScoreFragmentInterface{
    public static final String SAVE_FB_USER_ID = "fb_user_id";
    public static final String SAVE_FB_ACCESS_TOKEN = "fb_access_token";
    public static final String SAVE_FB_USER_NAME = "fb_user_name";
    public static final String SAVE_SEQUENTIAL_TOKEN = "sequential_token";

    public static final String SAVE_IMAGE = "save_image";

    // persisted info
    private CategoriesJs mCategories;

    // Scan fragment info
    private Bitmap mImage;

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
        if(savedInstanceState != null){
            mImage = savedInstanceState.getParcelable(SAVE_IMAGE);
        }
        mCategories = CrimpApplication.getLocalModel()
                .loadCategoriesAndCloseStream(LocalModelImpl.getInputStream(this));

        // prepare view pager
        mFragmentAdapter = new HelloFragmentAdapter(getSupportFragmentManager());
        mFragmentAdapter.setCanDisplay(CrimpApplication.getAppState()
                .getInt(CrimpApplication.CAN_DISPLAY, 0b001));

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
        CrimpApplication.getBusInstance().register(this);
    }

    @Override
    protected void onStop(){
        CrimpApplication.getBusInstance().unregister(this);
        super.onStop();
    }

    @Produce
    public SwipeTo produceCurrentTab() {
        return new SwipeTo(mPager.getCurrentItem());
    }

    @Override
    protected void onSaveInstanceState (Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putParcelable(SAVE_IMAGE, mImage);
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

    @Subscribe
    public void onReceivedSwipeTo(SwipeTo event){
        if(event.position == 1){
            if(mAppBar != null)
                mAppBar.setExpanded(false);
        }
    }

    @Override
    public void setCategoriesJs(CategoriesJs categories) {
        mCategories = categories;
        CrimpApplication.getLocalModel()
                .saveCategoriesAndCloseStream(LocalModelImpl.getOutputStream(this), mCategories);
    }

    @Override
    public CategoriesJs getCategoriesJs() {
        return mCategories;
    }

    @Override
    public void goToScanTab() {
        int canDisplay = CrimpApplication.getAppState()
                .getInt(CrimpApplication.CAN_DISPLAY, 0b001);
        canDisplay = canDisplay | 0b010;
        CrimpApplication.getAppState().edit()
                .putInt(CrimpApplication.CAN_DISPLAY, canDisplay).commit();
        mFragmentAdapter.setCanDisplay(canDisplay);

        mPager.setCurrentItem(1);
    }

    @Override
    public void setCanDisplay(int canDisplay){
        CrimpApplication.getAppState().edit()
                .putInt(CrimpApplication.CAN_DISPLAY, canDisplay).commit();
        mFragmentAdapter.setCanDisplay(canDisplay);
    }

    @Override
    public void setDecodedImage(Bitmap image) {
        mImage = image;
    }

    @Override
    public Bitmap getDecodedImage(){
        return mImage;
    }

    @Override
    public void goToScoreTab() {
        int canDisplay = CrimpApplication.getAppState().getInt(CrimpApplication.CAN_DISPLAY, 0b001);
        canDisplay = canDisplay | 0b100;
        CrimpApplication.getAppState().edit()
                .putInt(CrimpApplication.CAN_DISPLAY, canDisplay).commit();
        mFragmentAdapter.setCanDisplay(canDisplay);

        mPager.setCurrentItem(2);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case R.id.action_helpme:
                Toast toast = Toast.makeText(this,
                        "STUB! A ticket has been sent to the admins. Please wait for assistance.",
                        Toast.LENGTH_SHORT);
                toast.show();
                // TODO HELPME
                return true;
            case R.id.action_logout:
                LogoutDialog.create(this, new Action() {
                    @Override
                    public void act() {
                        LoginManager.getInstance().logOut();
                        CrimpApplication.getAppState().edit().clear().commit();
                        Intent intent = new Intent(HelloActivity.this, LoginActivity.class);
                        finish();
                        startActivity(intent);
                    }
                }, new Action() {
                    @Override
                    public void act() {
                        // Do nothing
                    }
                }).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
