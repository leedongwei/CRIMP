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
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import rocks.crimp.crimp.CrimpApplication;
import rocks.crimp.crimp.R;
import rocks.crimp.crimp.common.event.SwipeTo;
import rocks.crimp.crimp.hello.HelloActivity;
import rocks.crimp.crimp.hello.score.scoremodule.BonusTwoModule;
import rocks.crimp.crimp.hello.score.scoremodule.ScoreModule;
import rocks.crimp.crimp.hello.score.scoremodule.TopBonusModule;
import rocks.crimp.crimp.network.model.CategoriesJs;
import rocks.crimp.crimp.network.model.CategoryJs;
import timber.log.Timber;

public class ScoreFragment extends Fragment implements View.OnClickListener,
        ScoreModule.ScoreModuleInterface{
    public static final String ARGS_POSITION = "INT_POSITION";
    public static final String ARGS_TITLE = "STRING_TITLE";

    private TextView mCategoryText;
    private TextView mRouteText;
    private EditText mClimberIdText;
    private EditText mClimberNameText;
    private EditText mAccumulatedText;
    private EditText mCurrentText;
    private ViewStub mScoreModuleLayout;
    private View mInflatedScoreModule;
    private Button mSubmitButton;

    private ScoreFragmentInterface mParent;
    private int mPosition;
    private ScoreModule mScoreModule;

    public static ScoreFragment newInstance(int position, String title){
        ScoreFragment f = new ScoreFragment();
        // TODO set arguments
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_score, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        mCategoryText = (TextView)view.findViewById(R.id.score_category_text);
        mRouteText = (TextView)view.findViewById(R.id.score_route_text);
        mClimberIdText = (EditText)view.findViewById(R.id.score_climberId_edit);
        mClimberNameText = (EditText)view.findViewById(R.id.score_climberName_edit);
        mAccumulatedText = (EditText)view.findViewById(R.id.score_accumulated_edit);
        mCurrentText = (EditText)view.findViewById(R.id.score_current_edit);
        mScoreModuleLayout = (ViewStub)view.findViewById(R.id.score_score_fragment);
        mSubmitButton = (Button)view.findViewById(R.id.score_submit_button);

        mSubmitButton.setOnClickListener(this);
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

        // Category info can be obtained as early as onStart
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
            scoreType = categoryJs.getRoutes().get(routePosition-1).getScoreType();
        }
        else {
            throw new RuntimeException("Unable to find out category Scan tab");
        }

        // update UI with category name, route name and score module
        mCategoryText.setText(categoryName);
        mRouteText.setText(routeName);
        switch(scoreType){
            case "top_bonus":
                mScoreModuleLayout.setLayoutResource(R.layout.fragment_top_bonus_scoring);
                mInflatedScoreModule = mScoreModuleLayout.inflate();
                mScoreModule = new TopBonusModule(mInflatedScoreModule, getActivity(), this);
                break;
            case "bonus_2":
                mScoreModuleLayout.setLayoutResource(R.layout.fragment_top_bonus2_scoring);
                mInflatedScoreModule = mScoreModuleLayout.inflate();
                mScoreModule = new BonusTwoModule(mInflatedScoreModule, getActivity(), this);
                break;
            default:
                throw new RuntimeException("unknown score type: "+scoreType);
        }

        mScoreModule.notifyScore(mAccumulatedText.getText().toString()
                + mCurrentText.getText().toString());
    }

    @Override
    public void onStop(){
        CrimpApplication.getBusInstance().unregister(this);
        super.onStop();
    }

    @Subscribe
    public void onReceivedSwipeTo(SwipeTo event){
        Timber.d("onReceivedSwipeTo: %d", event.position);
        if (event.position == mPosition){
            String markerId = CrimpApplication.getAppState()
                    .getString(CrimpApplication.MARKER_ID, "");
            String climberName = CrimpApplication.getAppState()
                    .getString(CrimpApplication.CLIMBER_NAME, "");

            mClimberIdText.setText(markerId);
            mClimberNameText.setText(climberName);
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.score_submit_button:
                Toast toast = Toast.makeText(getActivity(), "STUB!", Toast.LENGTH_SHORT);
                toast.show();
                break;
        }
    }

    @Override
    public void append(String s) {
        mCurrentText.append(s);

        mScoreModule.notifyScore(mAccumulatedText.getText().toString()
                + mCurrentText.getText().toString());
    }

    @Override
    public void backspace() {
        String currentScore = mCurrentText.getText().toString();
        if(currentScore.length()>0){
            currentScore = currentScore.substring(0, currentScore.length()-1);
            mCurrentText.setText(currentScore);
        }

        mScoreModule.notifyScore(mAccumulatedText.getText().toString()
                + mCurrentText.getText().toString());
    }

    public interface ScoreFragmentInterface{
        CategoriesJs getCategoriesJs();
    }
}
