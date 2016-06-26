package rocks.crimp.crimp.tasklist;

import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
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
import rocks.crimp.crimp.common.Action;
import rocks.crimp.crimp.common.event.CurrentUploadTask;
import rocks.crimp.crimp.service.ScoreHandler;

public class TaskListActivity extends AppCompatActivity implements View.OnClickListener,
        View.OnLongClickListener{
    private TextView mCounter;
    private CardView mCardView;
    private TextView mCategoryRoute;
    private TextView mMarkerIdText;
    private TextView mScoreText;
    private TextView mStatus;
    private FloatingActionButton mResumeButton;
    private boolean mHasError = false;

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

        mCardView.setOnLongClickListener(this);
        mResumeButton.setOnClickListener(this);
    }

    @Override
    protected void onStart(){
        super.onStart();
        CrimpApplication.getBusInstance().register(this);
    }

    @Subscribe
    public void receivedCurrentUploadTask(CurrentUploadTask event){
        if(event.status == CurrentUploadTask.DROP_TASK){
            return;
        }

        if(CrimpApplication.getUploadTaskCount() == 0){
            mCounter.setText(R.string.tasklist_activity_counter_zero);
            mCardView.setVisibility(View.GONE);
            mResumeButton.setVisibility(View.GONE);
            mHasError = false;
        }
        else{
            String counterText = String.format(getString(R.string.tasklist_activity_counter),
                    CrimpApplication.getUploadTaskCount());
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

            switch(event.status){
                case CurrentUploadTask.IDLE:
                    mStatus.setText(R.string.tasklist_activity_status_idle);
                    mResumeButton.setVisibility(View.GONE);
                    mHasError = false;
                    break;
                case CurrentUploadTask.UPLOADING:
                    mStatus.setText(R.string.tasklist_activity_status_uploading);
                    mResumeButton.setVisibility(View.GONE);
                    mHasError = false;
                    break;
                case CurrentUploadTask.ERROR_HTTP_STATUS:
                    String httpStatusMsg = String.format(
                            getString(R.string.tasklist_activity_status_error_http),
                            event.httpStatusCode, event.httpMessage);
                    mStatus.setText(httpStatusMsg);
                    mResumeButton.setVisibility(View.VISIBLE);
                    mHasError = true;
                    break;
                case CurrentUploadTask.ERROR_EXCEPTION:
                    // some assertion
                    if(event.exception == null){
                        throw new IllegalStateException("exception is null");
                    }
                    String exceptionMsg = String.format(
                            getString(R.string.tasklist_activity_status_error_exception),
                            event.exception.getLocalizedMessage());
                    mStatus.setText(exceptionMsg);
                    mResumeButton.setVisibility(View.VISIBLE);
                    mHasError = true;
                    break;
                case CurrentUploadTask.ERROR_NO_NETWORK:
                    mStatus.setText(R.string.tasklist_activity_status_error_no_network);
                    mResumeButton.setVisibility(View.VISIBLE);
                    mHasError = true;
                    break;
                case CurrentUploadTask.DROP_TASK:
                    break;
                default:
                    mStatus.setText(null);
                    mResumeButton.setVisibility(View.GONE);
                    mHasError = false;
            }
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
        switch(v.getId()){
            case R.id.tasklist_fab:
                CrimpApplication.getScoreHandler()
                        .obtainMessage(ScoreHandler.RESUME_UPLOAD)
                        .sendToTarget();
                break;
            default:
        }
    }

    @Override
    public boolean onLongClick(View v) {
        switch(v.getId()){
            case R.id.tasklist_card:
                if(mHasError) {
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(100);

                    DeleteTaskDialog.create(this, new Action() {
                        @Override
                        public void act() {
                            CrimpApplication.getScoreHandler()
                                    .obtainMessage(ScoreHandler.DROP_TASK)
                                    .sendToTarget();
                        }
                    }, new Action() {
                        @Override
                        public void act() {
                            // No-op
                        }
                    }).show();
                }
                return true;

            default:
        }
        return false;
    }
}
