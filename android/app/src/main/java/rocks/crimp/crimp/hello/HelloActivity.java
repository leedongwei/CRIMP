package rocks.crimp.crimp.hello;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

import java.util.List;
import java.util.UUID;

import rocks.crimp.crimp.CrimpApplication;
import rocks.crimp.crimp.R;
import rocks.crimp.crimp.common.Action;
import rocks.crimp.crimp.common.event.RequestFailed;
import rocks.crimp.crimp.common.event.RequestSucceed;
import rocks.crimp.crimp.common.event.SwipeTo;
import rocks.crimp.crimp.hello.route.RouteFragment;
import rocks.crimp.crimp.hello.scan.ScanFragment;
import rocks.crimp.crimp.hello.score.ScoreFragment;
import rocks.crimp.crimp.login.LoginActivity;
import rocks.crimp.crimp.network.model.CategoriesJs;
import rocks.crimp.crimp.network.model.CategoryJs;
import rocks.crimp.crimp.network.model.RouteJs;
import rocks.crimp.crimp.persistence.LocalModelImpl;
import rocks.crimp.crimp.service.ServiceHelper;
import rocks.crimp.crimp.tasklist.TaskListActivity;
import timber.log.Timber;

public class HelloActivity extends AppCompatActivity implements
        RouteFragment.RouteFragmentInterface,
        ScanFragment.ScanFragmentInterface,
        ScoreFragment.ScoreFragmentInterface{
    public static final String SAVE_IMAGE = "save_image";
    public static final String SAVE_PAGER_SELECTED = "save_pager_selected";
    private static final String SAVE_LOGOUT_TXID = "save_logout_txid";
    private static final String SAVE_HELPME_TXID = "save_helpme_txid";

    private UUID mLogoutTxId;
    private UUID mHelpMeTxId;

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
        mFragmentAdapter = new HelloFragmentAdapter(getSupportFragmentManager(), mTabLayout);

        // Tab and custom tab stuff
        mTabLayout.addTab(mTabLayout.newTab().setText(mFragmentAdapter.getPageTitle(0)));
        mTabLayout.addTab(mTabLayout.newTab().setText(mFragmentAdapter.getPageTitle(1)));
        mTabLayout.addTab(mTabLayout.newTab().setText(mFragmentAdapter.getPageTitle(2)));
        mTabLayout.setOnTabSelectedListener(new HelloOnTabSelectedListener(mPager, mTabLayout));
        TabLayout.Tab scoreTab = mTabLayout.getTabAt(2);
        scoreTab.setCustomView(R.layout.view_custom_tab);
        TabViewHolder holder = new TabViewHolder();
        holder.tabTitle = (TextView)scoreTab.getCustomView().findViewById(android.R.id.text1);
        holder.tabBadge = (ImageView)scoreTab.getCustomView().findViewById(R.id.badge);
        scoreTab.getCustomView().setTag(holder);

        holder.tabBadge.setImageResource(R.drawable.ic_report_white_24dp);
        if(mTabLayout.getSelectedTabPosition() == HelloFragmentAdapter.SCORE_TAB_POSITION){
            scoreTab.getCustomView().setSelected(true);
        }

        // Initial setup for badge visibility
        // Find out if we have currentScore in score tab
        String currentScore = CrimpApplication.getAppState()
                .getString(CrimpApplication.CURRENT_SCORE, null);
        if(currentScore != null && currentScore.length()>0 ){
            // We have currentScore. Set badge visibility base on which tab we are in.
            if(mTabLayout.getSelectedTabPosition() != HelloFragmentAdapter.SCORE_TAB_POSITION){
                if(holder.tabBadge!=null){
                    holder.tabBadge.setVisibility(View.VISIBLE);
                }
            }
            else{
                if(holder.tabBadge!=null){
                    holder.tabBadge.setVisibility(View.INVISIBLE);
                }
            }
        }
        else{
            // We have no currentScore. No need to show badge.
            if(holder.tabBadge!=null){
                holder.tabBadge.setVisibility(View.INVISIBLE);
            }
        }

        int canDisplay = CrimpApplication.getAppState().getInt(CrimpApplication.CAN_DISPLAY, 0b001);
        mFragmentAdapter.setCanDisplay(canDisplay);
        mPager.setAdapter(mFragmentAdapter);
        mPager.addOnPageChangeListener(new HelloPageChangeListener(mTabLayout));

        if(savedInstanceState != null){
            final int currentItem = savedInstanceState.getInt(SAVE_PAGER_SELECTED, 0);
            if( ((1<<currentItem) & canDisplay) == 0 ){
                throw new IllegalStateException("selectedTab "+currentItem+" cannot be displayed");
            }

            mPager.setCurrentItem(currentItem);
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        CrimpApplication.getBusInstance().register(this);
    }

    @Override
    protected void onPause(){
        CrimpApplication.getBusInstance().unregister(this);
        super.onPause();
    }

    @Produce
    public SwipeTo produceCurrentTab() {
        return new SwipeTo(mPager.getCurrentItem());
    }

    @Subscribe
    public void requestSucceedReceived(RequestSucceed event) {
        if(event.txId.equals(mLogoutTxId)){
            Timber.d("Received RequestSucceed for Logout: %s", event.txId);
            mLogoutTxId = null;

            // Logout facebook and wipe data
            LoginManager.getInstance().logOut();
            mCategories = null;
            mImage = null;
            CrimpApplication.getAppState().edit().clear().apply();
            Intent intent = new Intent(HelloActivity.this, LoginActivity.class);
            finish();
            startActivity(intent);
        }
        else if(event.txId.equals(mHelpMeTxId)){
            Timber.d("Received RequestSucceed for HelpMe: %s", event.txId);
            mHelpMeTxId = null;

            //TODO handle received helpme response
        }
    }

    @Subscribe
    public void requestFailedReceived(RequestFailed event) {
        if(event.txId.equals(mLogoutTxId)){
            Timber.d("Received RequestFailed for Logout: %s", event.txId);
            mLogoutTxId = null;

            // We don't care that logout fail.
            // Logout facebook and wipe data
            LoginManager.getInstance().logOut();
            mCategories = null;
            mImage = null;
            CrimpApplication.getAppState().edit().clear().apply();
            Intent intent = new Intent(HelloActivity.this, LoginActivity.class);
            finish();
            startActivity(intent);
        }
        else if(event.txId.equals(mHelpMeTxId)){
            Timber.d("Received RequestFailed for HelpMe: %s", event.txId);
            mHelpMeTxId = null;

            //TODO handle received helpme response
        }
    }

    @Override
    protected void onSaveInstanceState (Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putParcelable(SAVE_IMAGE, mImage);
        outState.putInt(SAVE_PAGER_SELECTED, mPager.getCurrentItem());
        Timber.d("onSaveInstanceState %d", mPager.getCurrentItem());
    }

    @Override
    public void onBackPressed() {
        final int currentTab = mPager.getCurrentItem();
        int canDisplay = CrimpApplication.getAppState().getInt(CrimpApplication.CAN_DISPLAY, 0b001);

        if(currentTab == 0){
            super.onBackPressed();
        }
        else{
            for(int i=currentTab-1; i>=0; i--){
                int bitMask = 1 << i;
                if((canDisplay & bitMask) != 0){
                    mPager.setCurrentItem(i);
                    break;
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_hello, menu);
        return true;
    }

    @Override
    public void setAppBarExpanded(boolean expanded){
        if(mAppBar != null)
            mAppBar.setExpanded(expanded);
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
        CrimpApplication.getAppState().edit()
                .putInt(CrimpApplication.IMAGE_HEIGHT, mImage.getHeight())
                .commit();
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
    public void goBackToScanTab() {
        int canDisplay = CrimpApplication.getAppState().getInt(CrimpApplication.CAN_DISPLAY, 0b001);
        canDisplay = canDisplay & 0b011;
        CrimpApplication.getAppState().edit()
                .putInt(CrimpApplication.CAN_DISPLAY, canDisplay).commit();
        mFragmentAdapter.setCanDisplay(canDisplay);

        mPager.setCurrentItem(1);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case R.id.action_helpme: {
                Toast toast = Toast.makeText(this,
                        "A ticket has been sent to the admins. Please wait for assistance.",
                        Toast.LENGTH_SHORT);
                toast.show();

                String xUserId = CrimpApplication.getAppState().getString(CrimpApplication.X_USER_ID, null);
                String xAuthToken = CrimpApplication.getAppState().getString(CrimpApplication.X_AUTH_TOKEN, null);
                int categoryPosition = CrimpApplication.getAppState()
                        .getInt(CrimpApplication.COMMITTED_CATEGORY, 0);
                int routePosition = CrimpApplication.getAppState()
                        .getInt(CrimpApplication.COMMITTED_ROUTE, 0);
                if (categoryPosition == 0 || routePosition == 0) {
                    ServiceHelper.requestHelp(this, null, xUserId, xAuthToken, null);
                } else {
                    List<CategoryJs> categoryJsList = mCategories != null ? mCategories.getCategories() : null;
                    CategoryJs chosenCategory = categoryJsList != null ? categoryJsList.get(categoryPosition - 1) : null;
                    List<RouteJs> routeJsList = chosenCategory != null ? chosenCategory.getRoutes() : null;
                    RouteJs chosenRoute = routeJsList != null ? routeJsList.get(routePosition - 1) : null;
                    String routeId = chosenRoute != null ? chosenRoute.getRouteId() : null;

                    ServiceHelper.requestHelp(this, null, xUserId, xAuthToken, routeId);
                }
                return true;
            }

            case R.id.action_task_list:
                Intent intent = new Intent(this, TaskListActivity.class);
                startActivity(intent);
                return true;

            case R.id.action_logout:
                // We only need to show dialog if user has enter stuff on Score tab.
                String currentScore = CrimpApplication.getAppState()
                        .getString(CrimpApplication.CURRENT_SCORE, null);
                if (currentScore == null || currentScore.length() == 0) {
                    LogoutDialog.create(this, new Action() {
                        @Override
                        public void act() {
                            // Do logout
                            String xUserId = CrimpApplication.getAppState().getString(CrimpApplication.X_USER_ID, null);
                            String xAuthToken = CrimpApplication.getAppState().getString(CrimpApplication.X_AUTH_TOKEN, null);
                            if(xUserId == null){
                                throw new NullPointerException("X-User-Id is null");
                            }
                            if(xAuthToken == null){
                                throw new NullPointerException("X-Auth-Token is null");
                            }
                            doLogout(xUserId, xAuthToken);
                        }
                    }, new Action() {
                        @Override
                        public void act() {
                            // Do nothing
                        }
                    }).show();
                } else {
                    // Prepare stuff to use in LogoutDialog creation.
                    String markerId = CrimpApplication.getAppState().getString(CrimpApplication.MARKER_ID, null);
                    String climberName = CrimpApplication.getAppState().getString(CrimpApplication.CLIMBER_NAME, "");
                    int categoryPosition = CrimpApplication.getAppState()
                            .getInt(CrimpApplication.COMMITTED_CATEGORY, 0);
                    int routePosition = CrimpApplication.getAppState()
                            .getInt(CrimpApplication.COMMITTED_ROUTE, 0);
                    CategoryJs chosenCategory =
                            mCategories.getCategories().get(categoryPosition - 1);
                    String routeName = chosenCategory.getRoutes().get(routePosition - 1).getRouteName();

                    LogoutDialog.create(this, new Action() {
                        @Override
                        public void act() {
                            // Do logout
                            String xUserId = CrimpApplication.getAppState().getString(CrimpApplication.X_USER_ID, null);
                            String xAuthToken = CrimpApplication.getAppState().getString(CrimpApplication.X_AUTH_TOKEN, null);
                            if(xUserId == null){
                                throw new NullPointerException("X-User-Id is null");
                            }
                            if(xAuthToken == null){
                                throw new NullPointerException("X-Auth-Token is null");
                            }
                            doLogout(xUserId, xAuthToken);
                        }
                    }, new Action() {
                        @Override
                        public void act() {
                            // Do nothing
                        }
                    }, markerId, climberName, routeName).show();
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void animateBadge(){
        TabLayout.Tab scoreTab = mTabLayout.getTabAt(HelloFragmentAdapter.SCORE_TAB_POSITION);
        View customView = scoreTab!=null ? scoreTab.getCustomView() : null;
        TabViewHolder holder = customView!=null ? (TabViewHolder)customView.getTag() : null;
        ImageView badge = holder!=null ? holder.tabBadge : null;

        Animation shakeAndJump = AnimationUtils.loadAnimation(this, R.anim.badge_animate);

        if(badge != null){
            badge.startAnimation(shakeAndJump);
        }
    }

    private void doLogout(@NonNull String xUserId, @NonNull String mXAuthToken){
        mLogoutTxId = ServiceHelper.logout(this, mLogoutTxId, xUserId, mXAuthToken);
    }

}
