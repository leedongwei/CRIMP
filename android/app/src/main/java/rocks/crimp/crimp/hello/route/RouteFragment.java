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
import rocks.crimp.crimp.common.User;
import rocks.crimp.crimp.common.event.RequestFailed;
import rocks.crimp.crimp.common.event.RequestSucceed;
import rocks.crimp.crimp.hello.HelloActivity;
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
        String userName = CrimpApplication.getAppState()
                .getString(HelloActivity.SAVE_FB_USER_NAME, "User_name");
        String greeting = String.format(getString(R.string.route_fragment_greeting), userName);
        mHelloText.setText(greeting);
    }

    @Override
    public void onStart(){
        super.onStart();
        CrimpApplication.getBusInstance().register(this);

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
            int categoryPosition = CrimpApplication.getAppState()
                    .getInt(HelloActivity.SAVE_CATEGORY_INDEX, 0);
            int routePosition = CrimpApplication.getAppState()
                    .getInt(HelloActivity.SAVE_ROUTE_INDEX, 0);
            mCategorySpinner.setSelection(categoryPosition);
            if(categoryPosition != 0){
                Timber.d("set enable true on routespinner");
                mRouteSpinner.setEnabled(true);
                mRouteSpinner.setSelection(routePosition);
            }

            int committedCategoryPosition = CrimpApplication.getAppState()
                    .getInt(HelloActivity.SAVE_COMMITTED_CATEGORY, 0);
            int committedRoutePosition = CrimpApplication.getAppState()
                    .getInt(HelloActivity.SAVE_COMMITTED_ROUTE, 0);
            if(routePosition!=0 &&
                    (categoryPosition != committedCategoryPosition ||
                            routePosition != committedRoutePosition)){
                mRouteNextButton.setEnabled(true);
            }
        }

    }

    @Override
    public void onStop(){
        CrimpApplication.getBusInstance().unregister(this);
        super.onStop();
    }

    @Subscribe
    public void requestSucceedReceived(RequestSucceed event) {
        Timber.d("Received RequestSucceed %s", event.txId);

        if(event.txId.equals(mCategoriesTxId)){
            // TODO: React to the event somehow! REMEMBER TO CLEAR THE TXID
            mParent.setCategoriesJs(CrimpApplication.getLocalModel()
                    .fetch(mCategoriesTxId.toString(), CategoriesJs.class));

            mCategoriesTxId = null;

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
            ReportJs reportJs = CrimpApplication.getLocalModel()
                    .fetch(mReportTxId.toString(), ReportJs.class);

            mReportTxId = null;

            String userId = CrimpApplication.getAppState()
                    .getString(HelloActivity.SAVE_FB_USER_ID, "user_id");
            if(reportJs.getFbUserId().equals(userId)){
                // Succeed
                Timber.d("Report in is successful");
                mHelloText.setVisibility(View.VISIBLE);
                mCategorySpinner.setVisibility(View.VISIBLE);
                mRouteSpinner.setVisibility(View.VISIBLE);
                mRouteNextButton.setVisibility(View.VISIBLE);
                mRouteNextButton.setEnabled(false);
                mLoadWheel.setVisibility(View.GONE);

                int categoryIndex = CrimpApplication.getAppState().getInt(HelloActivity.SAVE_CATEGORY_INDEX, 0);
                int routeIndex = CrimpApplication.getAppState().getInt(HelloActivity.SAVE_ROUTE_INDEX, 0);
                CrimpApplication.getAppState().edit().putInt(HelloActivity.SAVE_COMMITTED_CATEGORY, categoryIndex)
                        .putInt(HelloActivity.SAVE_COMMITTED_ROUTE, routeIndex)
                        .commit();
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
                        String userId = CrimpApplication.getAppState()
                                .getString(HelloActivity.SAVE_FB_USER_ID, "user_id");
                        String accessToken = CrimpApplication.getAppState()
                                .getString(HelloActivity.SAVE_FB_ACCESS_TOKEN, "token");
                        long sequentialToken = CrimpApplication.getAppState()
                                .getLong(HelloActivity.SAVE_SEQUENTIAL_TOKEN, -1);
                        mReportTxId = ServiceHelper.reportIn(getActivity(), mReportTxId,
                                userId, accessToken, sequentialToken, category.getCategoryId(),
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
        CrimpApplication.getAppState().edit().putInt(HelloActivity.SAVE_CATEGORY_INDEX, 0)
                .putInt(HelloActivity.SAVE_COMMITTED_CATEGORY, 0)
                .putInt(HelloActivity.SAVE_ROUTE_INDEX, 0)
                .putInt(HelloActivity.SAVE_COMMITTED_ROUTE, 0)
                .commit();
        mCategorySpinner.setSelection(0);
        mCategorySpinner.setVisibility(View.VISIBLE);
        mRouteSpinner.setEnabled(false);
        mRouteSpinner.setSelection(0);
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

        int committedCategoryPosition = CrimpApplication.getAppState().getInt(HelloActivity.SAVE_COMMITTED_CATEGORY, 0);
        int commitedRoutePosition = CrimpApplication.getAppState().getInt(HelloActivity.SAVE_COMMITTED_ROUTE, 0);
        int routePosition;
        if(position == committedCategoryPosition){
            routePosition = commitedRoutePosition;
        }
        else{
            routePosition = 0;
        }
        mRouteSpinner.setSelection(routePosition);
        mRouteNextButton.setEnabled(false);

        CrimpApplication.getAppState().edit().putInt(HelloActivity.SAVE_CATEGORY_INDEX, position)
                .putInt(HelloActivity.SAVE_ROUTE_INDEX, routePosition)
                .commit();
    }

    private void onRouteChosen(int position){
        Timber.d("onRouteChosen(%d)", position);

        int committedCategoryPosition = CrimpApplication.getAppState()
                .getInt(HelloActivity.SAVE_COMMITTED_CATEGORY, 0);
        int categoryPosition = CrimpApplication.getAppState()
                .getInt(HelloActivity.SAVE_CATEGORY_INDEX, 0);
        int committedRoutePosition = CrimpApplication.getAppState()
                .getInt(HelloActivity.SAVE_COMMITTED_ROUTE, 0);
        if(committedCategoryPosition == categoryPosition &&
                committedRoutePosition == position){
            mRouteNextButton.setEnabled(false);
        }
        else{
            mRouteNextButton.setEnabled(true);
        }

        CrimpApplication.getAppState().edit().putInt(HelloActivity.SAVE_ROUTE_INDEX, position).commit();
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
        int categoryPosition = CrimpApplication.getAppState()
                .getInt(HelloActivity.SAVE_CATEGORY_INDEX, 0);
        int routePosition = CrimpApplication.getAppState()
                .getInt(HelloActivity.SAVE_ROUTE_INDEX, 0);
        String userId = CrimpApplication.getAppState().getString(HelloActivity.SAVE_FB_USER_ID, "user_id");
        String accessToken = CrimpApplication.getAppState().getString(HelloActivity.SAVE_FB_ACCESS_TOKEN, "token");
        long sequentialToken = CrimpApplication.getAppState().getLong(HelloActivity.SAVE_SEQUENTIAL_TOKEN, -1);
        CategoryJs chosenCategory =
                mParent.getCategoriesJs().getCategories().get(categoryPosition-1);
        mReportTxId = ServiceHelper.reportIn(getActivity(), mReportTxId, userId, accessToken,
                sequentialToken, chosenCategory.getCategoryId(),
                chosenCategory.getRoutes().get(routePosition-1).getRouteId(), false);
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
        CrimpApplication.getAppState().edit().putInt(HelloActivity.SAVE_CATEGORY_INDEX, 0)
                .putInt(HelloActivity.SAVE_COMMITTED_CATEGORY, 0)
                .putInt(HelloActivity.SAVE_ROUTE_INDEX, 0)
                .putInt(HelloActivity.SAVE_COMMITTED_ROUTE, 0)
                .commit();
        mCategorySpinner.setSelection(0);
        mRouteSpinner.setEnabled(false);
        mRouteSpinner.setSelection(0);
        mRouteNextButton.setEnabled(false);
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
