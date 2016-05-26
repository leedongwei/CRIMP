package rocks.crimp.crimp.hello.route;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import rocks.crimp.crimp.CrimpApplication;
import rocks.crimp.crimp.R;
import rocks.crimp.crimp.common.Action;
import rocks.crimp.crimp.common.Helper;
import rocks.crimp.crimp.common.event.RequestFailed;
import rocks.crimp.crimp.common.event.RequestSucceed;
import rocks.crimp.crimp.network.model.CategoriesJs;
import rocks.crimp.crimp.network.model.CategoryJs;
import rocks.crimp.crimp.network.model.ReportJs;
import rocks.crimp.crimp.network.model.RouteJs;
import rocks.crimp.crimp.service.ServiceHelper;
import timber.log.Timber;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class RouteFragment extends Fragment {
    public static final String ARGS_POSITION = "INT_POSITION";
    public static final String ARGS_TITLE = "STRING_TITLE";
    private static final String CATEGORIES_TXID = "categories_txid";
    private static final String REPORT_TXID = "report_txid";

    private RouteFragmentInterface mParent;

    // View references
    private SwipeRefreshLayout mSwipeLayout;
    private ProgressBar mLoadWheel;
    private TextView mHelloText;
    private Spinner mCategorySpinner;
    private Spinner mRouteSpinner;
    private Button mRouteNextButton;

    private HintableArrayAdapter mCategoryAdapter;
    private HintableArrayAdapter mRouteAdapter;

    private UUID mCategoriesTxId;
    private UUID mReportTxId;

    public static RouteFragment newInstance(int position, String title){
        RouteFragment f = new RouteFragment();
        Bundle args = new Bundle();
        args.putInt(ARGS_POSITION, position);
        args.putString(ARGS_TITLE, title);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mParent = (RouteFragmentInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement RouteFragmentInterface");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null){
            mCategoriesTxId = (UUID) savedInstanceState.getSerializable(CATEGORIES_TXID);
            mReportTxId = (UUID) savedInstanceState.getSerializable(REPORT_TXID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_route, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        // Find references to views
        mSwipeLayout = (SwipeRefreshLayout)view.findViewById(R.id.swipe_layout);
        mLoadWheel = (ProgressBar)view.findViewById(R.id.route_wheel_progressbar);
        mHelloText = (TextView)view.findViewById(R.id.route_hello_text);
        mCategorySpinner = (Spinner)view.findViewById(R.id.route_category_spinner);
        mRouteSpinner = (Spinner)view.findViewById(R.id.route_route_spinner);
        mRouteNextButton = (Button)view.findViewById(R.id.route_next_button);

        // Set properties for views
        mCategorySpinner.setEnabled(false);
        mRouteSpinner.setEnabled(false);
        mRouteNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickNext();
            }
        });
        RefreshListener refreshListener = new RefreshListener(new Action() {
            @Override
            public void act() {
                Timber.d("Initiated refresh gesture");
                onSwipe();
            }
        });
        mSwipeLayout.setOnRefreshListener(refreshListener);

        // Set hello text
        String userName = CrimpApplication.getAppState()
                .getString(CrimpApplication.FB_USER_NAME, null);
        String greeting = String.format(getString(R.string.route_fragment_greeting), userName);
        mHelloText.setText(greeting);

        // Create and attached adapters for spinners
        mCategoryAdapter = new HintableArrayAdapter(getActivity(), android.R.layout.simple_spinner_item,
                getString(R.string.route_fragment_category_hint));
        mCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mRouteAdapter = new HintableArrayAdapter(getActivity(), android.R.layout.simple_spinner_item,
                getString(R.string.route_fragment_route_hint));
        mRouteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCategorySpinner.setAdapter(mCategoryAdapter);
        mRouteSpinner.setAdapter(mRouteAdapter);

        // Adding listeners to spinners
        SpinnerListener categoryListener = new SpinnerListener(new Action() {
            @Override
            public void act() {
                Timber.d("categorySpinner selected %d", mCategorySpinner.getSelectedItemPosition());
                int position = mCategorySpinner.getSelectedItemPosition();
                if(position != 0){
                    onCategoryChosen(position);
                }
            }
        }, null);
        SpinnerListener routeListener = new SpinnerListener(new Action() {
            @Override
            public void act() {
                Timber.d("routeSpinner selected %d", mRouteSpinner.getSelectedItemPosition());
                int position = mRouteSpinner.getSelectedItemPosition();
                if(position != 0){
                    onRouteChosen(position);
                }
            }
        }, null);
        mCategorySpinner.setOnItemSelectedListener(categoryListener);
        mRouteSpinner.setOnItemSelectedListener(routeListener);
    }

    @Override
    public void onStart(){
        super.onStart();
        CrimpApplication.getBusInstance().register(this);

        if(mParent.getCategoriesJs()==null || mParent.getCategoriesJs().getCategories().size()<=0){
            // If we do not have categories info, we cannot show anything and therefore must show
            // refresh
            Timber.d("onStart. We don't have categories.");
            refresh();
        }
        else{
            // We have categories but the screen could be showing spinner or replacement dialog. We
            // set the spinner data and selection do not modify the screen.

            ArrayList<String> categoryNames = new ArrayList<>();
            for(CategoryJs c:mParent.getCategoriesJs().getCategories()){
                categoryNames.add(c.getCategoryName());
            }
            mCategoryAdapter.clear();
            mCategoryAdapter.addAll(categoryNames);
            String currentScore = CrimpApplication.getAppState().getString(CrimpApplication.CURRENT_SCORE, null);
            if(currentScore!=null && currentScore.length()>0){
                mCategorySpinner.setEnabled(false);
            }
            else{
                mCategorySpinner.setEnabled(true);
            }


            int categoryPosition = CrimpApplication.getAppState()
                    .getInt(CrimpApplication.CATEGORY_POSITION, 0);
            int routePosition = CrimpApplication.getAppState()
                    .getInt(CrimpApplication.ROUTE_POSITION, 0);
            mCategorySpinner.setSelection(categoryPosition);
            mRouteSpinner.setSelection(routePosition);
        }

    }

    @Override
    public void onStop(){
        CrimpApplication.getBusInstance().unregister(this);
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putSerializable(CATEGORIES_TXID, mCategoriesTxId);
        outState.putSerializable(REPORT_TXID, mReportTxId);
    }

    @Subscribe
    public void requestSucceedReceived(RequestSucceed event) {
        Timber.d("Received RequestSucceed %s", event.txId);

        if(event.txId.equals(mCategoriesTxId)){
            mCategoriesTxId = null;
            CategoriesJs response = (CategoriesJs) event.value;
            mParent.setCategoriesJs(response);

            ArrayList<String> categoryNames = new ArrayList<>();
            for(CategoryJs c:mParent.getCategoriesJs().getCategories()){
                categoryNames.add(c.getCategoryName());
            }

            mCategoryAdapter.clear();
            mCategoryAdapter.addAll(categoryNames);
            String currentScore = CrimpApplication.getAppState().getString(CrimpApplication.CURRENT_SCORE, null);
            if(currentScore!=null && currentScore.length()>0){
                mCategorySpinner.setEnabled(false);
            }
            else{
                mCategorySpinner.setEnabled(true);
            }
            mSwipeLayout.setRefreshing(false);
        }
        else if(event.txId.equals(mReportTxId)){
            mReportTxId = null;
            ReportJs reportJs = (ReportJs) event.value;

            String xUserId = CrimpApplication.getAppState().getString(CrimpApplication.X_USER_ID, null);
            if(reportJs.getxUserId().equals(xUserId)){
                // Succeed
                Timber.d("Report in is successful");
                showHasCategories();
                mSwipeLayout.setEnabled(true);


                int categoryIndex = CrimpApplication.getAppState()
                        .getInt(CrimpApplication.CATEGORY_POSITION, 0);
                int routeIndex = CrimpApplication.getAppState()
                        .getInt(CrimpApplication.ROUTE_POSITION, 0);
                // assert stuff
                if(categoryIndex == 0){
                    throw new IllegalStateException("categoryIndex is 0");
                }
                if(routeIndex == 0){
                    throw new IllegalStateException("routeIndex is 0");
                }
                CrimpApplication.getAppState().edit()
                        .putInt(CrimpApplication.COMMITTED_CATEGORY, categoryIndex)
                        .putInt(CrimpApplication.COMMITTED_ROUTE, routeIndex)
                        .apply();
                mParent.goToScanTab();
            }
            else{
                Timber.d("Report in requires replace");

                final CategoryJs category = mParent.getCategoriesJs()
                        .getCategoryById(reportJs.getCategoryId());
                final RouteJs route = category.getRouteById(reportJs.getRouteId());

                AlertDialog dialog = ReplaceDialog.create(getActivity(), new Action() {
                    @Override
                    public void act() {
                        // Send request
                        String xUserId = CrimpApplication.getAppState()
                                .getString(CrimpApplication.X_USER_ID, null);
                        String xAuthToken = CrimpApplication.getAppState()
                                .getString(CrimpApplication.X_AUTH_TOKEN, null);

                        mReportTxId = ServiceHelper.reportIn(getActivity(), mReportTxId,
                                xUserId, xAuthToken, category.getCategoryId(),
                                route.getRouteId(), true);
                    }
                }, new Action() {
                    @Override
                    public void act() {
                        showHasCategories();
                        mRouteNextButton.setEnabled(true);
                        mSwipeLayout.setEnabled(true);
                    }
                }, reportJs.getUserName(), category.getCategoryName(), route.getRouteName());
                dialog.show();
            }
        }
    }

    @Subscribe
    public void requestFailedReceived(RequestFailed event){
        Timber.d("Received RequestFailed %s", event.txId);

        if(event.txId.equals(mCategoriesTxId)){
            mCategoriesTxId = null;
            mSwipeLayout.setRefreshing(false);

            // TODO handle fail
        }
        else if(event.txId.equals(mReportTxId)){
            mReportTxId = null;
            mSwipeLayout.setEnabled(true);
            showHasCategories();

            // TODO handle fail
        }
    }

    private void showNoCategories(){
        mLoadWheel.setVisibility(View.GONE);
        mHelloText.setVisibility(View.VISIBLE);
        mCategorySpinner.setVisibility(View.VISIBLE);
        mCategorySpinner.setEnabled(false);
        mCategorySpinner.setSelection(0);
        mRouteSpinner.setVisibility(View.VISIBLE);
        mRouteSpinner.setEnabled(false);
        mRouteSpinner.setSelection(0);
        mRouteNextButton.setVisibility(View.VISIBLE);
        mRouteNextButton.setEnabled(false);
    }

    private void showHasCategories(){
        Timber.d("showDefaultHasCategories");
        mSwipeLayout.setRefreshing(false);

        mLoadWheel.setVisibility(View.GONE);
        mHelloText.setVisibility(View.VISIBLE);

        String currentScore = CrimpApplication.getAppState().getString(CrimpApplication.CURRENT_SCORE, null);
        if(currentScore != null && currentScore.length()>0){
            mCategorySpinner.setEnabled(false);
        }
        else{
            mCategorySpinner.setEnabled(true);
        }
        int categoryPosition = CrimpApplication.getAppState()
                .getInt(CrimpApplication.CATEGORY_POSITION, 0);
        mCategorySpinner.setSelection(categoryPosition);
        mCategorySpinner.setVisibility(View.VISIBLE);
        int routePosition = CrimpApplication.getAppState()
                .getInt(CrimpApplication.ROUTE_POSITION, 0);
        mRouteSpinner.setSelection(routePosition);
        mRouteSpinner.setVisibility(View.VISIBLE);
        mRouteNextButton.setEnabled(false);
        mRouteNextButton.setVisibility(View.VISIBLE);
    }

    private void showReplaceLoading(){
        mHelloText.setVisibility(View.GONE);
        mCategorySpinner.setVisibility(View.GONE);
        mRouteSpinner.setVisibility(View.GONE);
        mRouteNextButton.setVisibility(View.GONE);
        mLoadWheel.setVisibility(View.VISIBLE);
    }

    private void onCategoryChosen(int position){
        Timber.d("onCategoryChosen(%d)", position);

        // Retrieve a list of route name based on selected category
        // minus one to account for hint in adapter.
        CategoryJs categoryJs = mParent.getCategoriesJs().getCategories().get(position-1);
        List<RouteJs> routeJsList = categoryJs.getRoutes();
        List<String> routeNameList = new ArrayList<>();
        for(RouteJs routeJs:routeJsList){
            routeNameList.add(routeJs.getRouteName());
        }

        // Populate route adapter
        mRouteAdapter.clear();
        mRouteAdapter.addAll(routeNameList);
        String currentScore = CrimpApplication.getAppState().getString(CrimpApplication.CURRENT_SCORE, null);
        if(currentScore != null && currentScore.length()>0){
            mRouteSpinner.setEnabled(false);
        }
        else{
            mRouteSpinner.setEnabled(true);
        }

        int committedCategoryPosition = CrimpApplication.getAppState()
                .getInt(CrimpApplication.COMMITTED_CATEGORY, 0);
        int commitedRoutePosition = CrimpApplication.getAppState()
                .getInt(CrimpApplication.COMMITTED_ROUTE, 0);
        int routePosition;
        if(position == committedCategoryPosition){
            routePosition = commitedRoutePosition;
        }
        else{
            routePosition = 0;
        }
        mRouteSpinner.setSelection(routePosition);
        mRouteNextButton.setEnabled(false);

        CrimpApplication.getAppState().edit()
                .putInt(CrimpApplication.CATEGORY_POSITION, position)
                .putInt(CrimpApplication.ROUTE_POSITION, routePosition)
                .apply();
    }

    private void onRouteChosen(int position){
        Timber.d("onRouteChosen(%d)", position);

        int committedCategoryPosition = CrimpApplication.getAppState()
                .getInt(CrimpApplication.COMMITTED_CATEGORY, -1);
        int categoryPosition = CrimpApplication.getAppState()
                .getInt(CrimpApplication.CATEGORY_POSITION, 0);
        int committedRoutePosition = CrimpApplication.getAppState()
                .getInt(CrimpApplication.COMMITTED_ROUTE, -1);

        if(committedCategoryPosition == categoryPosition &&
                committedRoutePosition == position){
            mRouteNextButton.setEnabled(false);
        }
        else{
            mRouteNextButton.setEnabled(true);
        }

        CrimpApplication.getAppState().edit()
                .putInt(CrimpApplication.ROUTE_POSITION, position).apply();
    }

    private void onClickNext(){
        Timber.d("onClickNext");
        String currentScore = CrimpApplication.getAppState()
                .getString(CrimpApplication.CURRENT_SCORE, null);
        if(currentScore != null && currentScore.length() > 0){
            // Prepare stuff to use in NextDialog creation.
            String markerId = CrimpApplication.getAppState().getString(CrimpApplication.MARKER_ID, null);
            String climberName = CrimpApplication.getAppState().getString(CrimpApplication.CLIMBER_NAME, null);
            int categoryPosition = CrimpApplication.getAppState()
                    .getInt(CrimpApplication.CATEGORY_POSITION, 0);
            int routePosition = CrimpApplication.getAppState()
                    .getInt(CrimpApplication.ROUTE_POSITION, 0);
            CategoryJs chosenCategory =
                    mParent.getCategoriesJs().getCategories().get(categoryPosition-1);
            String routeName = chosenCategory.getRoutes().get(routePosition-1).getRouteName();

            // Create and show NextDialog
            NextDialog.create(getActivity(), new Action() {
                @Override
                public void act() {
                    // do proceed
                    CrimpApplication.getAppState().edit()
                            .remove(CrimpApplication.MARKER_ID)
                            .remove(CrimpApplication.CLIMBER_NAME)
                            .remove(CrimpApplication.SHOULD_SCAN)
                            .remove(CrimpApplication.CURRENT_SCORE)
                            .remove(CrimpApplication.ACCUMULATED_SCORE)
                            .remove(CrimpApplication.COMMITTED_CATEGORY)
                            .remove(CrimpApplication.COMMITTED_ROUTE)
                            .apply();
                    mParent.setCanDisplay(0b001);
                    doNextButton();
                }
            }, new Action() {
                @Override
                public void act() {
                    // Do nothing
                }
            }, markerId, climberName, routeName).show();
        }
        else{
            doNextButton();
        }
    }

    private void doNextButton(){
        mRouteNextButton.setEnabled(false);
        mSwipeLayout.setEnabled(false);

        showReplaceLoading();

        // Send request
        int categoryPosition = CrimpApplication.getAppState()
                .getInt(CrimpApplication.CATEGORY_POSITION, 0);
        int routePosition = CrimpApplication.getAppState()
                .getInt(CrimpApplication.ROUTE_POSITION, 0);
        String xUserId = CrimpApplication.getAppState()
                .getString(CrimpApplication.X_USER_ID, null);
        String xAuthToken = CrimpApplication.getAppState()
                .getString(CrimpApplication.X_AUTH_TOKEN, null);

        // assert stuff
        if(categoryPosition == 0){
            throw new IllegalStateException("Category position cannot be 0");
        }
        if(routePosition == 0){
            throw new IllegalStateException("Route position cannot be 0");
        }
        if(xUserId == null){
            throw new IllegalStateException("xUserId is null");
        }
        if(xAuthToken == null){
            throw new IllegalStateException("xAuthToken is null");
        }

        CategoryJs chosenCategory =
                mParent.getCategoriesJs().getCategories().get(categoryPosition-1);

        mReportTxId = ServiceHelper.reportIn(getActivity(), mReportTxId, xUserId, xAuthToken,
                chosenCategory.getCategoryId(),
                chosenCategory.getRoutes().get(routePosition-1).getRouteId(), false);
    }

    /**
     * Called when user initiate a swipe refresh gesture. Determine whether an alert dialog is
     * needed before calling refresh().
     */
    private void onSwipe(){
        Timber.d("onSwipe");

        // We only need to show dialog if user has enter stuff on Score tab.
        String currentScore = CrimpApplication.getAppState()
                .getString(CrimpApplication.CURRENT_SCORE, null);
        if(currentScore == null || currentScore.length() == 0){
            refresh();
        }
        else{
            // Prepare stuff to use in RefreshDialog creation.
            String markerId = CrimpApplication.getAppState().getString(CrimpApplication.MARKER_ID, null);
            String climberName = CrimpApplication.getAppState().getString(CrimpApplication.CLIMBER_NAME, null);
            int categoryPosition = CrimpApplication.getAppState()
                    .getInt(CrimpApplication.CATEGORY_POSITION, 0);
            int routePosition = CrimpApplication.getAppState()
                    .getInt(CrimpApplication.ROUTE_POSITION, 0);
            CategoryJs chosenCategory =
                    mParent.getCategoriesJs().getCategories().get(categoryPosition-1);
            String routeName = chosenCategory.getRoutes().get(routePosition-1).getRouteName();

            // Create refresh dialog
            RefreshDialog.create(getActivity(), new Action() {
                @Override
                public void act() {
                    refresh();
                }
            }, new Action() {
                @Override
                public void act() {
                    mSwipeLayout.setRefreshing(false);
                }
            }, markerId, climberName, routeName).show();
        }
    }

    /**
     * Method to handle when SwipeRefreshLayout is refreshing. Does the following things:
     * 1) Show a refresh loading wheel
     * 2) Reset some AppState
     * 3) Show no-categories UI
     * 4) Reset stuff in parent activity
     * 5) Initiate request for categories
     */
    private void refresh(){
        mSwipeLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeLayout.setRefreshing(true);
            }
        });
        CrimpApplication.getAppState().edit()
                .remove(CrimpApplication.MARKER_ID)
                .remove(CrimpApplication.CLIMBER_NAME)
                .remove(CrimpApplication.SHOULD_SCAN)
                .remove(CrimpApplication.CURRENT_SCORE)
                .remove(CrimpApplication.ACCUMULATED_SCORE)
                .remove(CrimpApplication.CATEGORY_POSITION)
                .remove(CrimpApplication.COMMITTED_CATEGORY)
                .remove(CrimpApplication.ROUTE_POSITION)
                .remove(CrimpApplication.COMMITTED_ROUTE)
                .commit();

        showNoCategories();

        mParent.setCategoriesJs(null);
        mParent.setCanDisplay(0b001);

        mCategoriesTxId = ServiceHelper.getCategories(getActivity(), mCategoriesTxId);
    }

    public interface RouteFragmentInterface{
        void setCategoriesJs(CategoriesJs categories);
        CategoriesJs getCategoriesJs();
        void goToScanTab();
        void setCanDisplay(int canDisplay);
    }
}