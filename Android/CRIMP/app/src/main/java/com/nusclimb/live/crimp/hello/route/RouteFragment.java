package com.nusclimb.live.crimp.hello.route;

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

import com.nusclimb.live.crimp.CrimpApplication2;
import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.common.Action;
import com.nusclimb.live.crimp.common.dao.User;
import com.nusclimb.live.crimp.common.event.RequestFailed;
import com.nusclimb.live.crimp.common.event.RequestSucceed;
import com.nusclimb.live.crimp.hello.HintableArrayAdapter;
import com.nusclimb.live.crimp.network.model.CategoriesJs;
import com.nusclimb.live.crimp.network.model.CategoryJs;
import com.nusclimb.live.crimp.network.model.ReportJs;
import com.nusclimb.live.crimp.network.model.RouteJs;
import com.nusclimb.live.crimp.servicehelper.ServiceHelper;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import timber.log.Timber;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class RouteFragment extends Fragment {
    public static final String TAG = "RouteFragment";
    public static final boolean DEBUG = true;

    public static final String ARGS_POSITION = "INT_POSITION";
    public static final String ARGS_TITLE = "STRING_TITLE";

    private RouteFragmentInterface mParent;

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
        // TODO set arguments
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

        mCategoryAdapter = new HintableArrayAdapter(context, android.R.layout.simple_spinner_item,
                "this is a category hint");
        mCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mRouteAdapter = new HintableArrayAdapter(context, android.R.layout.simple_spinner_item,
                "this is a route hint");
        mRouteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_route, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        mSwipeLayout = (SwipeRefreshLayout)view.findViewById(R.id.swipe_layout);
        mLoadWheel = (ProgressBar)view.findViewById(R.id.route_wheel_progressbar);
        mHelloText = (TextView)view.findViewById(R.id.route_hello_text);
        mCategorySpinner = (Spinner)view.findViewById(R.id.route_category_spinner);
        mRouteSpinner = (Spinner)view.findViewById(R.id.route_route_spinner);
        mRouteNextButton = (Button)view.findViewById(R.id.route_next_button);

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

        mCategorySpinner.setAdapter(mCategoryAdapter);
        mRouteSpinner.setAdapter(mRouteAdapter);
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
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        // Get stuff from activity
        String userName = mParent.getUser().getName();
        String greeting = String.format(getString(R.string.route_fragment_greeting), userName);
        mHelloText.setText(greeting);
    }

    @Override
    public void onStart(){
        super.onStart();
        CrimpApplication2.getBusInstance().register(this);

        if(mParent.getCategoriesJs()==null || mParent.getCategoriesJs().getCategories().size()<=0){
            // If we do not have categories info, we cannot show anything and therefore must show
            // refresh
            Timber.d("onStart. We don't have categories.");
            showDefaultNoCategories();

            // Whether we actually request categories info depends on if there is already one
            // in-flight
            if(mCategoriesTxId == null){
                Timber.d("onStart. We don't have mCategoriesTxId");
                mCategoriesTxId = ServiceHelper.getCategories(getActivity(), mCategoriesTxId);
            }
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

            showDefaultHasCategories();
            mCategorySpinner.setSelection(mParent.getCategoryPosition());
            if(mParent.getCategoryPosition() != 0){
                Timber.d("set enable true on routespinner");
                mRouteSpinner.setEnabled(true);
                mRouteSpinner.setSelection(mParent.getRoutePosition());
            }

            if(mParent.getRoutePosition()!=0 &&
                    (mParent.getCategoryPosition() != mParent.getCommittedCategoryPosition() ||
                            mParent.getRoutePosition() != mParent.getCommittedRoutePosition())){
                mRouteNextButton.setEnabled(true);
            }
        }

    }

    @Override
    public void onStop(){
        CrimpApplication2.getBusInstance().unregister(this);
        super.onStop();
    }

    //TODO REMOVE INJECTION
    private static CategoriesJs injectCategoriesJs(){
        RouteJs route1a = new RouteJs();
        route1a.setRouteName("route 1a");
        route1a.setRouteId(1);
        RouteJs route1b = new RouteJs();
        route1b.setRouteName("route 1b");
        route1b.setRouteId(2);
        RouteJs route2a = new RouteJs();
        route2a.setRouteName("route 2a");
        route2a.setRouteId(3);
        RouteJs route2b = new RouteJs();
        route2b.setRouteName("route 2b");
        route2b.setRouteId(4);

        CategoryJs category1 = new CategoryJs();
        category1.setCategoryName("category 1");
        category1.setCategoryId(1);
        ArrayList<RouteJs> cat1Route = new ArrayList<>();
        cat1Route.add(route1a);
        cat1Route.add(route1b);
        category1.setRoutes(cat1Route);

        CategoryJs category2 = new CategoryJs();
        category2.setCategoryName("category 2");
        category2.setCategoryId(2);
        ArrayList<RouteJs> cat2Route = new ArrayList<>();
        cat2Route.add(route2a);
        cat2Route.add(route2b);
        category2.setRoutes(cat2Route);

        ArrayList<CategoryJs> categoryList = new ArrayList<>();
        categoryList.add(category1);
        categoryList.add(category2);

        CategoriesJs categoriesJs = new CategoriesJs();
        categoriesJs.setCategories(categoryList);

        return categoriesJs;
    }

    //TODO REMOVE INJECTION
    private static ReportJs injectReportJs(){
        ReportJs reportJs = new ReportJs();
        reportJs.setFbUserId("stubUserId");
        reportJs.setUserName("stubUserName");
        reportJs.setCategoryId(1);
        reportJs.setRouteId(1);

        return reportJs;
    }

    @Subscribe
    public void requestSucceedReceived(RequestSucceed event) {
        Timber.d("Received RequestSucceed %s", event.txId);

        if(event.txId.equals(mCategoriesTxId)){
            // TODO: React to the event somehow! REMEMBER TO CLEAR THE TXID
            mParent.setCategoriesJs(CrimpApplication2.getLocalModel()
                    .fetch(mCategoriesTxId.toString(), CategoriesJs.class));

            //TODO REMOVE INJECTION
            mParent.setCategoriesJs(injectCategoriesJs());

            ArrayList<String> categoryNames = new ArrayList<>();
            for(CategoryJs c:mParent.getCategoriesJs().getCategories()){
                categoryNames.add(c.getCategoryName());
            }

            mCategoryAdapter.clear();
            mCategoryAdapter.addAll(categoryNames);
            mCategorySpinner.setEnabled(true);
            mSwipeLayout.setRefreshing(false);
        }
        else if(event.txId.equals(mReportTxId)){
            // TODO: React to the event somehow! REMEMBER TO CLEAR THE TXID
            ReportJs reportJs = CrimpApplication2.getLocalModel()
                    .fetch(mReportTxId.toString(), ReportJs.class);

            //TODO REMOVE INJECTION
            reportJs = injectReportJs();

            if(reportJs.getFbUserId().equals(mParent.getUser().getUserId())){
                // Succeed
                Timber.d("Report in is successful");
                mHelloText.setVisibility(View.VISIBLE);
                mCategorySpinner.setVisibility(View.VISIBLE);
                mRouteSpinner.setVisibility(View.VISIBLE);
                mRouteNextButton.setVisibility(View.VISIBLE);
                mRouteNextButton.setEnabled(false);
                mLoadWheel.setVisibility(View.GONE);

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
                        mReportTxId = ServiceHelper.reportIn(getActivity(), mReportTxId,
                                mParent.getUser().getUserId(), mParent.getUser().getAccessToken(),
                                mParent.getUser().getSequentialToken(), category.getCategoryId(),
                                route.getRouteId(), true);
                    }
                }, new Action() {
                    @Override
                    public void act() {
                        mHelloText.setVisibility(View.VISIBLE);
                        mCategorySpinner.setVisibility(View.VISIBLE);
                        mRouteSpinner.setVisibility(View.VISIBLE);
                        mRouteNextButton.setVisibility(View.VISIBLE);
                        mRouteNextButton.setEnabled(true);
                        mLoadWheel.setVisibility(View.GONE);
                    }
                }, reportJs.getUserName(), category.getCategoryName(), route.getRouteName());
                dialog.show();
            }
        }
    }

    @Subscribe
    public void requestFailedReceived(RequestFailed event){
        Timber.d("Received RequestFailed %s", event.txId);
        mSwipeLayout.setRefreshing(false);
    }

    private void showDefaultNoCategories(){
        Timber.d("showDefaultNoCategories");
        mSwipeLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeLayout.setRefreshing(true);
            }
        });
        mLoadWheel.setVisibility(View.GONE);
        mHelloText.setVisibility(View.VISIBLE);
        mCategorySpinner.setEnabled(false);
        mParent.setCategoryPosition(0);
        mParent.setCommittedCategoryPosition(0);
        mCategorySpinner.setSelection(mParent.getCategoryPosition());
        mCategorySpinner.setVisibility(View.VISIBLE);
        mRouteSpinner.setEnabled(false);
        mParent.setRoutePosition(0);
        mParent.setCommittedRoutePosition(0);
        mRouteSpinner.setSelection(mParent.getRoutePosition());
        mRouteSpinner.setVisibility(View.VISIBLE);
        mRouteNextButton.setEnabled(false);
        mRouteNextButton.setVisibility(View.VISIBLE);
    }

    private void showDefaultHasCategories(){
        Timber.d("showDefaultHasCategories");
        mSwipeLayout.setRefreshing(false);
        mLoadWheel.setVisibility(View.GONE);
        mHelloText.setVisibility(View.VISIBLE);
        mCategorySpinner.setEnabled(true);
        mCategorySpinner.setVisibility(View.VISIBLE);
        mRouteSpinner.setEnabled(false);
        mRouteSpinner.setVisibility(View.VISIBLE);
        mRouteNextButton.setEnabled(false);
        mRouteNextButton.setVisibility(View.VISIBLE);
    }

    private void onCategoryChosen(int position){
        Timber.d("onCategoryChosen(%d)", position);

        // Retrieve a list of route name based on selected category
        mParent.setCategoryPosition(position);
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
        mRouteSpinner.setEnabled(true);
        mParent.setRoutePosition(0);
        mRouteSpinner.setSelection(0);
        mRouteNextButton.setEnabled(false);
    }

    private void onRouteChosen(int position){
        Timber.d("onRouteChosen(%d)", position);
        mParent.setRoutePosition(position);

        if(mParent.getCommittedCategoryPosition() == mParent.getCategoryPosition() &&
                mParent.getCommittedRoutePosition() == mParent.getRoutePosition()){
            mRouteNextButton.setEnabled(false);
        }
        else{
            mRouteNextButton.setEnabled(true);
        }
    }

    private void onClickNext(){
        Timber.d("onClickNext");
        mHelloText.setVisibility(View.GONE);
        mCategorySpinner.setVisibility(View.GONE);
        mRouteSpinner.setVisibility(View.GONE);
        mRouteNextButton.setEnabled(false);
        mRouteNextButton.setVisibility(View.GONE);
        mLoadWheel.setVisibility(View.VISIBLE);

        // Send request
        CategoryJs chosenCategory =
                mParent.getCategoriesJs().getCategories().get(mParent.getCategoryPosition()-1);
        mReportTxId = ServiceHelper.reportIn(getActivity(), mReportTxId,
                mParent.getUser().getUserId(), mParent.getUser().getAccessToken(),
                mParent.getUser().getSequentialToken(), chosenCategory.getCategoryId(),
                chosenCategory.getRoutes().get(mParent.getRoutePosition()-1).getRouteId(), false);
    }

    private void onSwipe(){
        Timber.d("onSwipe");
        mSwipeLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeLayout.setRefreshing(true);
            }
        });
        mCategorySpinner.setEnabled(false);
        mParent.setCategoryPosition(0);
        mParent.setCommittedCategoryPosition(0);
        mCategorySpinner.setSelection(mParent.getCategoryPosition());
        mRouteSpinner.setEnabled(false);
        mParent.setRoutePosition(0);
        mParent.setCommittedRoutePosition(0);
        mRouteSpinner.setSelection(mParent.getRoutePosition());
        mRouteNextButton.setEnabled(false);
        mParent.setCategoriesJs(null);
        mParent.setCanDisplay(new boolean[]{true, false, false});

        mCategoriesTxId = ServiceHelper.getCategories(getActivity(), mCategoriesTxId);
    }

    public interface RouteFragmentInterface{
        User getUser();
        void setCategoriesJs(CategoriesJs categories);
        CategoriesJs getCategoriesJs();
        void setCategoryPosition(int categoryPosition);
        int getCategoryPosition();
        void setRoutePosition(int routePosition);
        int getRoutePosition();
        void setCommittedCategoryPosition(int categoryPosition);
        int getCommittedCategoryPosition();
        void setCommittedRoutePosition(int routePosition);
        int getCommittedRoutePosition();
        void goToScanTab();
        void setCanDisplay(boolean[] canDisplay);
    }

}
