package rocks.crimp.crimp.hello.score;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import java.util.UUID;

import rocks.crimp.crimp.CrimpApplication;
import rocks.crimp.crimp.R;
import rocks.crimp.crimp.common.Action;
import rocks.crimp.crimp.common.event.RequestFailed;
import rocks.crimp.crimp.common.event.RequestSucceed;
import rocks.crimp.crimp.common.event.SwipeTo;
import rocks.crimp.crimp.hello.score.scoremodule.BonusTwoModule;
import rocks.crimp.crimp.hello.score.scoremodule.PointsModule;
import rocks.crimp.crimp.hello.score.scoremodule.ScoreModule;
import rocks.crimp.crimp.hello.score.scoremodule.TopB2B1Module;
import rocks.crimp.crimp.hello.score.scoremodule.TopBonusModule;
import rocks.crimp.crimp.network.model.CategoriesJs;
import rocks.crimp.crimp.network.model.CategoryJs;
import rocks.crimp.crimp.network.model.ClimberScoreJs;
import rocks.crimp.crimp.network.model.GetScoreJs;
import rocks.crimp.crimp.service.ServiceHelper;
import timber.log.Timber;

public class ScoreFragment extends Fragment implements View.OnClickListener,
        ScoreModule.ScoreModuleInterface{
    public static final String RULES_IFSC_TOP_BONUS = "ifsc-top-bonus";
    public static final String RULES_TOP_B1_B2 = "top-bonus1-bonus2";
    public static final String RULES_POINTS = "points";
    public static final String ARGS_POSITION = "INT_POSITION";
    public static final String ARGS_TITLE = "STRING_TITLE";
    private static final String GET_SCORE_TXID = "get_score_txid";

    private TextView mCategoryText;
    private TextView mRouteText;
    private EditText mClimberIdText;
    private EditText mClimberNameText;
    private EditText mAccumulatedText;
    private EditText mCurrentText;
    private ViewStub mScoreModuleLayout;
    private View mInflatedScoreModule;
    private Button mSubmitButton;
    private ProgressBar mInfoProgressBar;
    private ImageView mCloseButton;

    private UUID mGetScoreTxId;
    private ScoreFragmentInterface mParent;
    private int mPosition;
    private ScoreModule mScoreModule;

    public static ScoreFragment newInstance(int position, String title){
        ScoreFragment f = new ScoreFragment();
        Bundle args = new Bundle();
        args.putInt(ARGS_POSITION, position);
        args.putString(ARGS_TITLE, title);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        mPosition = getArguments().getInt(ARGS_POSITION);

        if(savedInstanceState != null){
            mGetScoreTxId = (UUID) savedInstanceState.getSerializable(GET_SCORE_TXID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.fragment_score, container, false);

        mCategoryText = (TextView)rootView.findViewById(R.id.score_category_text);
        mRouteText = (TextView)rootView.findViewById(R.id.score_route_text);
        mClimberIdText = (EditText)rootView.findViewById(R.id.score_climberId_edit);
        mClimberNameText = (EditText)rootView.findViewById(R.id.score_climberName_edit);
        mAccumulatedText = (EditText)rootView.findViewById(R.id.score_accumulated_edit);
        mCurrentText = (EditText)rootView.findViewById(R.id.score_current_edit);
        mScoreModuleLayout = (ViewStub)rootView.findViewById(R.id.score_score_fragment);
        mSubmitButton = (Button)rootView.findViewById(R.id.score_submit_button);
        mInfoProgressBar = (ProgressBar)rootView.findViewById(R.id.score_info_progressbar);
        mCloseButton = (ImageView) rootView.findViewById(R.id.score_close_button);

        mCloseButton.setOnClickListener(this);
        mSubmitButton.setOnClickListener(this);

        int categoryPosition = CrimpApplication.getAppState()
                .getInt(CrimpApplication.COMMITTED_CATEGORY, 0);
        int routePosition = CrimpApplication.getAppState()
                .getInt(CrimpApplication.COMMITTED_ROUTE, 0);
        CategoriesJs categoriesJs = mParent.getCategoriesJs();
        String categoryName;
        String routeName;
        String scoreType;
        if(categoriesJs != null && categoryPosition != 0) {
            // minus one from categoryPosition because of hint in spinner adapter.
            CategoryJs categoryJs = categoriesJs.getCategories().get(categoryPosition-1);
            categoryName = categoryJs.getCategoryName();
            // minus one from routePosition because of hint in spinner adapter.
            routeName = categoryJs.getRoutes().get(routePosition-1).getRouteName();
            scoreType = categoryJs.getRoutes().get(routePosition-1).getScoreRules();
        }
        else {
            throw new RuntimeException("Unable to find out category Score tab");
        }

        // update UI with category name, route name and score module
        mCategoryText.setText(categoryName);
        mRouteText.setText(routeName);
        inflateScoreModule(scoreType);

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mParent = (ScoreFragmentInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement ScoreFragmentInterface");
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        CrimpApplication.getBusInstance().register(this);

        mScoreModule.notifyScore(mAccumulatedText.getText().toString()
                + mCurrentText.getText().toString());
    }

    @Override
    public void onStop(){
        CrimpApplication.getBusInstance().unregister(this);
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);

        outState.putSerializable(GET_SCORE_TXID, mGetScoreTxId);
    }

    private void inflateScoreModule(String scoreType){
        String type[] = scoreType.split("__");
        switch(type[0]){
            case "ifsc-top-bonus":
                if(mScoreModuleLayout != null){
                    mScoreModuleLayout.setLayoutResource(R.layout.fragment_top_bonus_scoring);
                    mInflatedScoreModule = mScoreModuleLayout.inflate();
                    mScoreModuleLayout = null;
                }
                mScoreModule = new TopBonusModule(mInflatedScoreModule, getActivity(), this);
                break;
            case "points":
                if(mScoreModuleLayout != null){
                    mScoreModuleLayout.setLayoutResource(R.layout.fragment_points_scoring);
                    mInflatedScoreModule = mScoreModuleLayout.inflate();
                    mScoreModuleLayout = null;
                }
                mScoreModule = new PointsModule(mInflatedScoreModule, getActivity(), this,
                        Integer.parseInt(type[1]));
                break;
            case "top-bonus1-bonus2":
                if(mScoreModuleLayout != null){
                    mScoreModuleLayout.setLayoutResource(R.layout.fragment_top_b1_b2_scoring);
                    mInflatedScoreModule = mScoreModuleLayout.inflate();
                    mScoreModuleLayout = null;
                }
                mScoreModule = new TopB2B1Module(mInflatedScoreModule, getActivity(), this);
                break;
            default:
                throw new RuntimeException("unknown score type: "+scoreType);
        }
    }

    private void doClose(){
        // close score tab
        mGetScoreTxId = null;
        CrimpApplication.getAppState().edit()
                .remove(CrimpApplication.MARKER_ID)
                .remove(CrimpApplication.CLIMBER_NAME)
                .remove(CrimpApplication.SHOULD_SCAN)
                .remove(CrimpApplication.CURRENT_SCORE)
                .remove(CrimpApplication.ACCUMULATED_SCORE)
                .apply();
        mScoreModule.notifyScore("");
        mParent.goBackToScanTab();
    }

    @Subscribe
    public void requestSucceedReceived(RequestSucceed event) {
        if(event.txId.equals(mGetScoreTxId)){
            Timber.d("Get score request successful. TxId: %s", event.txId);
            mGetScoreTxId = null;
            mInfoProgressBar.setVisibility(View.GONE);
            GetScoreJs response = (GetScoreJs) event.value;

            // some assertion
            if(response.getClimberScores().size() != 1){
                Timber.e("GetScore did not return score for 1 climber only");
                return;
            }
            if(response.getClimberScores().get(0).getScores().size() != 1){
                Timber.e("GetScore did not return score for 1 route only");
                return;
            }

            String markerId = CrimpApplication.getAppState()
                    .getString(CrimpApplication.MARKER_ID, "");
            ClimberScoreJs climberScoreJs = response.getClimberScoreByMarkerId(markerId);
            if(climberScoreJs == null){
                throw new NullPointerException("Can't find climber score for "+markerId);
            }
            String climberName = climberScoreJs.getClimberName();
            String accumulatedScore = climberScoreJs.getScores().get(0).getScore();
            mClimberNameText.setText(climberName);
            mAccumulatedText.setText(accumulatedScore);
            mScoreModule.notifyScore(accumulatedScore + mCurrentText.getText());

            CrimpApplication.getAppState().edit()
                    .putString(CrimpApplication.CLIMBER_NAME, climberName)
                    .putString(CrimpApplication.ACCUMULATED_SCORE, accumulatedScore)
                    .commit();
        }
    }

    @Subscribe
    public void requestFailedReceived(RequestFailed event){
        if(event.txId.equals(mGetScoreTxId)){
            Timber.e("Get score request fail. TxId: %s", event.txId);
            mGetScoreTxId = null;
            mInfoProgressBar.setVisibility(View.GONE);
            //TODO handle fail
        }
    }

    @Subscribe
    public void onReceivedSwipeTo(SwipeTo event){
        Timber.d("onReceivedSwipeTo: %d", event.position);
        if (event.position == mPosition){
            // Get info
            String markerId = CrimpApplication.getAppState()
                    .getString(CrimpApplication.MARKER_ID, null);
            String climberName = CrimpApplication.getAppState()
                    .getString(CrimpApplication.CLIMBER_NAME, "");
            String accumulatedScore = CrimpApplication.getAppState()
                    .getString(CrimpApplication.ACCUMULATED_SCORE, null);
            String currentScore = CrimpApplication.getAppState()
                    .getString(CrimpApplication.CURRENT_SCORE, null);

            // Show on screen
            mClimberIdText.setText(markerId);
            mClimberNameText.setText(climberName);
            mAccumulatedText.setText(accumulatedScore);
            mCurrentText.setText(currentScore);
            if(mCurrentText.getText()!=null && mCurrentText.getText().length()>0){
                mSubmitButton.setEnabled(true);
            }
            else{
                mSubmitButton.setEnabled(false);
            }

            String xUserId = CrimpApplication.getAppState()
                    .getString(CrimpApplication.X_USER_ID, null);
            String xAuthToken = CrimpApplication.getAppState()
                    .getString(CrimpApplication.X_AUTH_TOKEN, null);

            if(accumulatedScore == null){
                mInfoProgressBar.setVisibility(View.VISIBLE);

                // find route id
                int categoryPosition = CrimpApplication.getAppState()
                        .getInt(CrimpApplication.COMMITTED_CATEGORY, 0);
                int routePosition = CrimpApplication.getAppState()
                        .getInt(CrimpApplication.COMMITTED_ROUTE, 0);
                CategoryJs chosenCategory =
                        mParent.getCategoriesJs().getCategories().get(categoryPosition - 1);
                String routeId = chosenCategory.getRoutes().get(routePosition - 1).getRouteId();

                mGetScoreTxId = ServiceHelper.getScore(getActivity(), mGetScoreTxId, null, null,
                        routeId, markerId, xUserId, xAuthToken);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.score_close_button:
                String currentScore = CrimpApplication.getAppState()
                        .getString(CrimpApplication.CURRENT_SCORE, null);
                if(currentScore!=null && currentScore.length()>0){
                    CloseDialog.create(getActivity(), new Action() {
                        @Override
                        public void act() {
                            // close score tab
                            doClose();
                        }
                    }, null).show();
                }
                else{
                    // close score tab
                    doClose();
                }
                break;

            case R.id.score_submit_button:
                SubmitDialog.create(getActivity(), new Action() {
                    @Override
                    public void act() {
                        // submit score
                        int categoryPosition = CrimpApplication.getAppState()
                                .getInt(CrimpApplication.COMMITTED_CATEGORY, 0);
                        int routePosition = CrimpApplication.getAppState()
                                .getInt(CrimpApplication.COMMITTED_ROUTE, 0);
                        CategoryJs chosenCategory =
                                mParent.getCategoriesJs().getCategories().get(categoryPosition - 1);
                        String chosenCategoryName = chosenCategory.getCategoryName();
                        String routeId = chosenCategory.getRoutes().get(routePosition - 1).getRouteId();
                        String chosenRouteName = chosenCategory.getRoutes().get(routePosition - 1).getRouteName();
                        String markerId = CrimpApplication.getAppState()
                                .getString(CrimpApplication.MARKER_ID, null);
                        String xUserId = CrimpApplication.getAppState()
                                .getString(CrimpApplication.X_USER_ID, null);
                        String xAuthToken = CrimpApplication.getAppState()
                                .getString(CrimpApplication.X_AUTH_TOKEN, null);
                        String currentScore = mCurrentText.getText().toString();

                        // some assertion
                        if(currentScore.length() <= 0){
                            throw new IllegalStateException("current score is null");
                        }
                        if(markerId == null){
                            throw new IllegalStateException("marker id is null");
                        }
                        if(xUserId == null){
                            throw new IllegalStateException("xUserId is null");
                        }
                        if(xAuthToken == null){
                            throw new IllegalStateException("xAuthToken is null");
                        }

                        ServiceHelper.postScore(getActivity(), null, routeId, markerId, xUserId, xAuthToken,
                                currentScore, chosenCategoryName, chosenRouteName);

                        mGetScoreTxId = null;
                        CrimpApplication.getAppState().edit()
                                .remove(CrimpApplication.MARKER_ID)
                                .remove(CrimpApplication.CLIMBER_NAME)
                                .remove(CrimpApplication.SHOULD_SCAN)
                                .remove(CrimpApplication.CURRENT_SCORE)
                                .remove(CrimpApplication.ACCUMULATED_SCORE)
                                .apply();
                        mScoreModule.notifyScore("");
                        mParent.goBackToScanTab();
                    }
                }, null).show();

                break;
        }
    }

    @Override
    public void append(String s) {
        mCurrentText.append(s);
        String currentScore = mCurrentText.getText().toString();
        CrimpApplication.getAppState().edit()
                .putString(CrimpApplication.CURRENT_SCORE, currentScore)
                .commit();

        mScoreModule.notifyScore(mAccumulatedText.getText().toString()
                + mCurrentText.getText().toString());

        mSubmitButton.setEnabled(true);
    }

    @Override
    public void backspace() {
        String currentScore = mCurrentText.getText().toString();
        if(currentScore.length()>0){
            currentScore = currentScore.substring(0, currentScore.length()-1);
            mCurrentText.setText(currentScore);
        }
        CrimpApplication.getAppState().edit()
                .putString(CrimpApplication.CURRENT_SCORE, currentScore)
                .commit();

        mScoreModule.notifyScore(mAccumulatedText.getText().toString()
                + mCurrentText.getText().toString());

        if(mCurrentText.getText()!=null && mCurrentText.getText().length()>0){
            mSubmitButton.setEnabled(true);
        }
        else{
            mSubmitButton.setEnabled(false);
        }
    }

    public interface ScoreFragmentInterface{
        CategoriesJs getCategoriesJs();
        void goBackToScanTab();
    }
}
