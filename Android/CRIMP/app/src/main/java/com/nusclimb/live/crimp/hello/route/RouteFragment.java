package com.nusclimb.live.crimp.hello.route;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.nusclimb.live.crimp.CrimpApplication2;
import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.common.Action;
import com.nusclimb.live.crimp.common.event.RequestFailed;
import com.nusclimb.live.crimp.common.event.RequestSucceed;
import com.nusclimb.live.crimp.hello.HintableArrayAdapter;
import com.nusclimb.live.crimp.network.model.CategoriesJs;
import com.nusclimb.live.crimp.network.model.CategoryJs;
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
public class RouteFragment extends Fragment implements View.OnClickListener{
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
    private TextView mRouteReplaceText;
    private LinearLayout mYesNoGroup;
    private Button mYesButton;
    private Button mNoButton;

    private HintableArrayAdapter mCategoryAdapter;
    private HintableArrayAdapter mRouteAdapter;

    private int mCategoryIndex;
    private int mRouteIndex;
    private CategoriesJs mCategories;
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
        mRouteReplaceText = (TextView)view.findViewById(R.id.route_replace_text);
        mYesNoGroup = (LinearLayout)view.findViewById(R.id.route_yes_no_viewgroup);
        mYesButton = (Button)view.findViewById(R.id.route_yes_button);
        mNoButton = (Button)view.findViewById(R.id.route_no_button);

        mRouteNextButton.setOnClickListener(this);
        mYesButton.setOnClickListener(this);
        mNoButton.setOnClickListener(this);

        RefreshListener refreshListener = new RefreshListener(new Action() {
            @Override
            public void act() {
                Timber.d("Initiated refresh gesture");
                onShowRefresh();
                mCategoriesTxId = ServiceHelper.getCategories(getActivity(), mCategoriesTxId);
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
                    // We subtract one from position to account for hint at index zero.
                    onCategoryChosen(position-1);
                }
            }
        }, null);
        SpinnerListener routeListener = new SpinnerListener(new Action() {
            @Override
            public void act() {
                Timber.d("routeSpinner selected %d", mRouteSpinner.getSelectedItemPosition());
                int position = mRouteSpinner.getSelectedItemPosition();
                if(position != 0){
                    // We subtract one from position to account for hint at index zero.
                    onRouteChosen(position-1);
                }
            }
        }, null);
        mCategorySpinner.setOnItemSelectedListener(categoryListener);
        mRouteSpinner.setOnItemSelectedListener(routeListener);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        String userName = "user";
        String greeting = String.format(getString(R.string.route_fragment_greeting), userName);
        mHelloText.setText(greeting);
    }

    @Override
    public void onStart(){
        super.onStart();
        CrimpApplication2.getBusInstance().register(this);

        if(mCategories==null || mCategories.getCategories().size()<=0){
            // If we do not have categories info, we cannot show anything and therefore must show
            // refresh
            Timber.d("onStart. We don't have categories.");
            onShowRefresh();

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
            for(CategoryJs c:mCategories.getCategories()){
                categoryNames.add(c.getCategoryName());
            }

            mCategoryAdapter.clear();
            mCategoryAdapter.addAll(categoryNames);
            onShowPickCategory();
            mCategorySpinner.setSelection(mCategoryIndex);
        }

    }

    @Override
    public void onStop(){
        CrimpApplication2.getBusInstance().unregister(this);
        super.onStop();
    }

    @Subscribe
    public void requestSucceedReceived(RequestSucceed event) {
        Timber.d("Received RequestSucceed %s", event.txId);

        if(event.txId.equals(mCategoriesTxId)){
            // TODO: React to the event somehow! REMEMBER TO CLEAR THE TXID
            mCategories = CrimpApplication2.getLocalModel()
                    .fetch(mCategoriesTxId.toString(), CategoriesJs.class);

            //TODO REMOVE INJECTION
            RouteJs route1a = new RouteJs();
            route1a.setRouteName("route 1a");
            RouteJs route1b = new RouteJs();
            route1b.setRouteName("route 1b");
            RouteJs route2a = new RouteJs();
            route2a.setRouteName("route 2a");
            RouteJs route2b = new RouteJs();
            route2b.setRouteName("route 2b");

            CategoryJs category1 = new CategoryJs();
            category1.setCategoryName("category 1");
            ArrayList<RouteJs> cat1Route = new ArrayList<>();
            cat1Route.add(route1a);
            cat1Route.add(route1b);
            category1.setRoutes(cat1Route);

            CategoryJs category2 = new CategoryJs();
            category2.setCategoryName("category 2");
            ArrayList<RouteJs> cat2Route = new ArrayList<>();
            cat2Route.add(route2a);
            cat2Route.add(route2b);
            category2.setRoutes(cat2Route);

            ArrayList<CategoryJs> categoryList = new ArrayList<>();
            categoryList.add(category1);
            categoryList.add(category2);

            mCategories = new CategoriesJs();  //TODO INJECTION
            mCategories.setCategories(categoryList);

            ArrayList<String> categoryNames = new ArrayList<>();
            for(CategoryJs c:mCategories.getCategories()){
                categoryNames.add(c.getCategoryName());
            }

            mCategoryAdapter.clear();
            mCategoryAdapter.addAll(categoryNames);
            mCategorySpinner.setEnabled(true);
            mSwipeLayout.setRefreshing(false);
        }
        else{

        }

    }

    @Subscribe
    public void requestFailedReceived(RequestFailed event){
        Timber.d("Received RequestFailed %s", event.txId);
        mSwipeLayout.setRefreshing(false);
    }

    private void onCategoryChosen(int index){
        // Retrieve a list of route name based on selected category
        CategoryJs categoryJs = mCategories.getCategories().get(index);
        List<RouteJs> routeJsList = categoryJs.getRoutes();
        List<String> routeNameList = new ArrayList<>();
        for(RouteJs routeJs:routeJsList){
            routeNameList.add(routeJs.getRouteName());
        }

        // Populate route adapter
        mRouteAdapter.clear();
        mRouteAdapter.addAll(routeNameList);
        mRouteSpinner.setEnabled(true);
        mRouteSpinner.setSelection(mRouteIndex);
    }

    private void onRouteChosen(int index){
        mRouteNextButton.setEnabled(true);
    }

    private void onShowRefresh(){
        Timber.d("onShowRefresh");
        mSwipeLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeLayout.setRefreshing(true);
            }
        });
        mCategorySpinner.setEnabled(false);
        mCategorySpinner.setSelection(0);
        mRouteSpinner.setEnabled(false);
        mRouteSpinner.setSelection(0);
        mRouteNextButton.setEnabled(false);
    }

    private void onShowPickCategory(){
        mSwipeLayout.setRefreshing(false);
        mCategorySpinner.setEnabled(true);
        mRouteSpinner.setEnabled(false);
        mRouteNextButton.setEnabled(false);
    }

    private void onClickNext(){
        mHelloText.setVisibility(View.GONE);
        mCategorySpinner.setVisibility(View.GONE);
        mRouteSpinner.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.route_next_button:
                onClickNext();
                break;
            case R.id.route_yes_button:
                break;
            case R.id.route_no_button:
                break;
        }
    }


    public interface RouteFragmentInterface{

    }

}
