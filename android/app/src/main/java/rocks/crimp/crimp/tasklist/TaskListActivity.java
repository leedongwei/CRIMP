package rocks.crimp.crimp.tasklist;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import rocks.crimp.crimp.CrimpApplication;
import rocks.crimp.crimp.R;
import rocks.crimp.crimp.common.event.CurrentUploadTask;

public class TaskListActivity extends AppCompatActivity implements View.OnClickListener{
    private TextView mCounter;
    private CardView mCardView;
    private TextView mCategoryRoute;
    private TextView mMarkerIdText;
    private TextView mScoreText;
    private TextView mStatus;
    private FloatingActionButton mResumeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.tasklist_toolbar);
        setSupportActionBar(myToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();
        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        mCounter = (TextView) findViewById(R.id.tasklist_counter);
        mCardView = (CardView) findViewById(R.id.tasklist_card);
        mCategoryRoute = (TextView) findViewById(R.id.tasklist_category_route_name);
        mMarkerIdText = (TextView) findViewById(R.id.tasklist_marker_id);
        mScoreText = (TextView) findViewById(R.id.tasklist_score);
        mStatus = (TextView) findViewById(R.id.tasklist_status);
        mResumeButton = (FloatingActionButton) findViewById(R.id.tasklist_fab);

        mCardView.setOnClickListener(this);
        mResumeButton.setOnClickListener(this);
    }

    @Override
    protected void onStart(){
        super.onStart();
        CrimpApplication.getBusInstance().register(this);
    }

    @Subscribe
    public void receivedCurrentUploadTask(CurrentUploadTask event){
        if(event.taskCountLeft == 0){
            mCounter.setText(R.string.tasklist_activity_counter_zero);
            mCardView.setVisibility(View.GONE);
        }
        else{
            String counterText = String.format(getString(R.string.tasklist_activity_counter), event.taskCountLeft);
            mCounter.setText(counterText);
            String categoryRouteText = String.format(getString(R.string.tasklist_activity_category_route),
                    event.currentTask.getMetaBean().getCategoryName(),
                    event.currentTask.getMetaBean().getRouteName());
            mCategoryRoute.setText(categoryRouteText);
            String climberText = String.format(getString(R.string.tasklist_activity_climber),
                    event.currentTask.getPathBean().getMarkerId());
            mMarkerIdText.setText(climberText);
            String scoreText = String.format(getString(R.string.tasklist_activity_score),
                    event.currentTask.getRequestBodyJs().getScoreString());
            mScoreText.setText(scoreText);
            mStatus.setText(event.status);
            mCardView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onStop(){
        CrimpApplication.getBusInstance().unregister(this);
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        Toast toast = Toast.makeText(this, "STUB", Toast.LENGTH_SHORT);
        switch(v.getId()){
            case R.id.tasklist_card:
                toast.show();
                break;
            case R.id.tasklist_fab:
                toast.show();
                break;
            default:
        }
    }
}
