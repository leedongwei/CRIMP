package rocks.crimp.crimp.tasklist;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.TextView;

import rocks.crimp.crimp.CrimpApplication;
import rocks.crimp.crimp.R;

public class TaskListActivity extends AppCompatActivity {
    private TextView mCounterText;
    private TextView mRouteText;
    private TextView mMarkerIdText;
    private TextView mScoreText;
    private Button mResumeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        
        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();
        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);


        mCounterText = (TextView) findViewById(R.id.tasklist_counter);
        mRouteText = (TextView) findViewById(R.id.tasklist_route_id);
        mMarkerIdText = (TextView) findViewById(R.id.tasklist_marker_id);
        mScoreText = (TextView) findViewById(R.id.tasklist_score);
        mResumeButton = (Button) findViewById(R.id.tasklist_resume);
    }

    @Override
    protected void onStart(){
        super.onStart();
        CrimpApplication.getBusInstance().register(this);

        int taskCount = CrimpApplication.getScoreHandler().getThreadTaskCount();
        mCounterText.setText(String.valueOf(taskCount));
    }

    @Override
    protected void onStop(){
        CrimpApplication.getBusInstance().unregister(this);
        super.onStop();
    }
}
