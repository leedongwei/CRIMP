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
import com.nusclimb.live.crimp.common.event.ResponseReceived;
import com.nusclimb.live.crimp.hello.HintableArrayAdapter;
import com.nusclimb.live.crimp.network.model.CategoriesJs;
import com.nusclimb.live.crimp.network.model.CategoryJs;
import com.nusclimb.live.crimp.network.model.RouteJs;
import com.nusclimb.live.crimp.servicehelper.ServiceHelper;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
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
    private TextView mStatusText;
    private Button mRetryButton;
    private TextView mHelloText;
    private Spinner mCategorySpinner;
    private Spinner mRouteSpinner;
    private Button mRouteNextButton;
    private TextView mRouteReplaceText;
    private LinearLayout mYesNoGroup;

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
        mStatusText = (TextView)view.findViewById(R.id.route_request_status_text);
        mRetryButton = (Button)view.findViewById(R.id.route_retry_button);
        mHelloText = (TextView)view.findViewById(R.id.route_hello_text);
        mCategorySpinner = (Spinner)view.findViewById(R.id.route_category_spinner);
        mRouteSpinner = (Spinner)view.findViewById(R.id.route_route_spinner);
        mRouteNextButton = (Button)view.findViewById(R.id.route_next_button);
        mRouteReplaceText = (TextView)view.findViewById(R.id.route_replace_text);
        mYesNoGroup = (LinearLayout)view.findViewById(R.id.route_yes_no_viewgroup);

        mCategorySpinner.setAdapter(mCategoryAdapter);
        mRouteSpinner.setAdapter(mRouteAdapter);
    }

    @Override
    public void onStart(){
        super.onStart();
        CrimpApplication2.getBusInstance().register(this);

        if(mCategoriesTxId == null && mCategoryAdapter.getCount() <= 1){
            mCategoriesTxId = ServiceHelper.getCategories(getActivity(), mCategoriesTxId);
        }
    }

    @Override
    public void onStop(){
        CrimpApplication2.getBusInstance().unregister(this);
        super.onStop();
    }

    @Subscribe
    public void RestResponseReceived(ResponseReceived event) {
        Timber.d("Received response %s", event.txId);
        if(event.txId.equals(mCategoriesTxId)){
            // TODO: React to the event somehow! REMEMBER TO CLEAR THE TXID
            CategoriesJs categories = CrimpApplication2.getLocalModel()
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

            categories = new CategoriesJs();  //TODO INJECTION
            categories.setCategories(categoryList);

            ArrayList<String> categoryNames = new ArrayList<>();
            for(CategoryJs c:categories.getCategories()){
                categoryNames.add(c.getCategoryName());
            }

            mCategoryAdapter.addAll(categoryNames);
        }
        else{

        }
    }

    public interface RouteFragmentInterface{

    }

}
