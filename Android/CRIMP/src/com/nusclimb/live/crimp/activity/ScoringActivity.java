package com.nusclimb.live.crimp.activity;

import com.nusclimb.live.crimp.CrimpApplication;
import com.nusclimb.live.crimp.Helper;
import com.nusclimb.live.crimp.QueueObject;
import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.json.RoundInfoMap;
import com.nusclimb.live.crimp.json.Score;
import com.nusclimb.live.crimp.request.ClimberInfoRequest;
import com.nusclimb.live.crimp.request.ScoreRequest;
import com.nusclimb.live.crimp.service.CrimpService;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

/**
 * Third activity of CRIMP.
 * 
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 *
 */
public class ScoringActivity extends Activity{
	private static final String TAG = ScoringActivity.class.getSimpleName();
	
	private String infoRequestCacheKey;		// CacheKey for request. Double as 
											// a flag to indicate requesting
											// for info.
	private String scoreRequestCacheKey;	// CacheKey for request. Double as
											// a flag to indicate requesting
											// for score.
	
	private EditText scoreEdit;
	private String routeJudge, round, route, climberId, climberName, oldScore;
	private SpiceManager spiceManager = new SpiceManager(
			CrimpService.class);
	
	/*=========================================================================
	 * Inner class
	 *=======================================================================*/
	/**
	 * RequestListener for Climber info. Updating of climber info is not 
	 * performed here. Update climber info UI after score request is complete.
	 * 
	 * @author Lin Weizhi (ecc.weizhi@gmail.com)
	 *
	 */
	private class ClimberInfoRequestListener implements RequestListener<RoundInfoMap> {		
		@Override
		public void onRequestFailure(SpiceException e) {
			infoRequestCacheKey = null;
			
			updateInfoAndScoreUI();
			
			Log.w(TAG, "ClimberInfoRequestListener request fail.");
		}

		@Override
		public void onRequestSuccess(RoundInfoMap result) {
			infoRequestCacheKey = null;
	    	 
			climberName = result.get(climberId);
	    	 
			updateInfoAndScoreUI();
		}
	}
	
	/**
	 * RequestListener for score request. Updates both score and climber info
	 * UI when request is completed.
	 * 
	 * @author Lin Weizhi (ecc.weizhi@gmail.com)
	 *
	 */
	private class ScoreRequestListener implements RequestListener<Score> {
		@Override
		public void onRequestFailure(SpiceException e) {
			scoreRequestCacheKey = null;
			
			oldScore = null;
			
			updateInfoAndScoreUI();
						
			Log.w(TAG, "ScoreRequestListener request fail.");
		}

		@Override
		public void onRequestSuccess(Score result) {
			scoreRequestCacheKey = null;
	    	 
			oldScore = result.getC_score();
	    	
			updateInfoAndScoreUI();
		}
	}
	
	/**
	 * Update info and score UI only if downloads are completed.
	 */
	private void updateInfoAndScoreUI(){
		// Download for both info and score completed. Stop progress bar, update UI.
		if( (infoRequestCacheKey == null) && (scoreRequestCacheKey == null) ){
			ScoringActivity.this.setProgressBarIndeterminateVisibility(false);
						
			EditText nameEdit = (EditText) findViewById(R.id.scoring_climber_name_edit);
			if(climberName == null){
				nameEdit.setText(getText(R.string.UI_unavailable));
			}
			else{
				nameEdit.setText(climberName);
			}
						
			EditText scoreHistoryEdit = (EditText) findViewById(R.id.scoring_score_history_edit);
			if(oldScore == null){
				scoreHistoryEdit.setText(getText(R.string.UI_unavailable));
			}
			else{
				scoreHistoryEdit.setText(oldScore);
			}
		}
	}
	
	/*=========================================================================
	 * Activity lifecycle methods
	 *=======================================================================*/
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "ScoringActivity onCreate.");
		
		// We want as long as this window is visible to the user, 
		// keep the device's screen turned on and bright. 
		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS); 
		
		setContentView(R.layout.activity_scoring);
		
		// Get stuff from Intent
		String packageName = getString(R.string.package_name);
		Intent intent = getIntent();
		routeJudge = intent.getStringExtra(packageName + getString(R.string.intent_username));
		round = intent.getStringExtra(packageName + getString(R.string.intent_round));
		route = intent.getStringExtra(packageName + getString(R.string.intent_route));
		climberId = intent.getStringExtra(packageName + getString(R.string.intent_climber_id));
		climberName = intent.getStringExtra(packageName + getString(R.string.intent_climber_name));
		
		// Update UI with info
		this.setTitle(round + " " + route);
		EditText climberIdView = (EditText) findViewById(R.id.scoring_climber_id_edit);
		climberIdView.setText(climberId);
		if(climberName != null){
			EditText climberNameView = (EditText) findViewById(R.id.scoring_climber_name_edit);
			climberNameView.setText(climberName);
			Log.d(TAG, "Climber name was send along in intent. UI updated.");
		}
		else{
			Log.d(TAG, "Climber name was not send along in intent.");
		}
	}
	
	@Override
	protected void onStart(){
		super.onStart();
		Log.d(TAG, "ScoringActivity onStart.");
		
		spiceManager.start(this);
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		Log.d(TAG, "ScoringActivity onResume.");
		
		// Check UI
		if(climberName == null){
			refreshName();
		}
		refreshScore();
	}
	
	@Override
	protected void onPause(){
		if(infoRequestCacheKey != null){
			Log.d(TAG, "onPause. Cancelling info request: "+infoRequestCacheKey);
			spiceManager.cancel(RoundInfoMap.class, infoRequestCacheKey);
			infoRequestCacheKey = null;
		}
		
		if(scoreRequestCacheKey != null){
			Log.d(TAG, "onPause. Cancelling score request: "+scoreRequestCacheKey);
			spiceManager.cancel(RoundInfoMap.class, scoreRequestCacheKey);
			scoreRequestCacheKey = null;
		}
		
		Log.d(TAG, "ScoringActivity onPause.");
		super.onPause();
	}
	
	@Override
	protected void onStop(){
		spiceManager.shouldStop();
		
		Log.d(TAG, "ScoringActivity onStop.");
		super.onStop();
	}
	
	@Override
	protected void onDestroy(){
		Log.d(TAG, "ScoringActivity onDestroy.");
		super.onDestroy();
	}
	
	
	
	/*=========================================================================
	 * Overriden methods
	 *=======================================================================*/
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
			// Respond to the action bar's Up/Home button
	    	case android.R.id.home:
	    		NavUtils.navigateUpFromSameTask(this);
		        return true;
	    	case R.id.refresh:
	    		// Cancel previous download...
	    		if(infoRequestCacheKey != null){
	    			Log.d(TAG, "Refresh clicked. Cancelling info request: "+infoRequestCacheKey);
	    			spiceManager.cancel(RoundInfoMap.class, infoRequestCacheKey);
	    			infoRequestCacheKey = null;
	    		}
	    		
	    		if(scoreRequestCacheKey != null){
	    			Log.d(TAG, "Refresh clicked. Cancelling score request: "+scoreRequestCacheKey);
	    			spiceManager.cancel(RoundInfoMap.class, scoreRequestCacheKey);
	    			scoreRequestCacheKey = null;
	    		}
	    		
	    		// ...then start new download.
	    		if(climberName == null){
	    			refreshName();
	    		}
	    		refreshScore();
	    		
		        return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.scoring, menu);
	    return true;
	}
	
	
	
	/*=========================================================================
	 * Button on click methods
	 *=======================================================================*/
	private void refreshScore(){
		setProgressBarIndeterminateVisibility(true);

        ScoreRequest request = new ScoreRequest(climberId, round, route);
        
        scoreRequestCacheKey = request.createCacheKey();
        
        spiceManager.execute(request, scoreRequestCacheKey, 
        		DurationInMillis.ALWAYS_EXPIRED, 
        		new ScoreRequestListener());
	}
	
	private void refreshName(){
		setProgressBarIndeterminateVisibility(true);

        ClimberInfoRequest request = new ClimberInfoRequest(round);
        
        infoRequestCacheKey = request.createCacheKey();
        
        spiceManager.execute(request, infoRequestCacheKey, 
        		DurationInMillis.ALWAYS_EXPIRED, 
        		new ClimberInfoRequestListener());
	}
	
	public void plusOne(View view){
		if(scoreEdit == null){
			scoreEdit = (EditText) findViewById(R.id.scoring_score_current_edit);
		}
		scoreEdit.append("1");
	}
	
	public void bonus(View view){
		if(scoreEdit == null){
			scoreEdit = (EditText) findViewById(R.id.scoring_score_current_edit);
		}
		scoreEdit.append("B");
	}
	
	public void top(View view){
		if(scoreEdit == null){
			scoreEdit = (EditText) findViewById(R.id.scoring_score_current_edit);
		}
		scoreEdit.append("T");
	}
	
	public void backspace(View view){
		if(scoreEdit == null){
			scoreEdit = (EditText) findViewById(R.id.scoring_score_current_edit);
		}
		if(scoreEdit.getText().length() > 0){
			scoreEdit.getText().delete(scoreEdit.getText().length()-1, scoreEdit.getText().length());
		}
	}
	
	public void submit(View view){
		// Convert to server alias.
		String serverAliasRound = Helper.toServerRound(round);
		String serverAliasRoute = Helper.parseRoute(route);
		String r_id = serverAliasRound + serverAliasRoute;
		
		// Find the view for current score
		if(scoreEdit == null){
			scoreEdit = (EditText) findViewById(R.id.scoring_score_current_edit);
		}
		
		// Make QueueObject
		QueueObject mQueueObject = new QueueObject(routeJudge, getString(R.string.net_password_debug), 
				r_id, climberId, scoreEdit.getText().toString(), CrimpService.nextRequestId());
		
		
		// Add to a queue of QueueObject request.
		((CrimpApplication)getApplicationContext()).addRequest(mQueueObject);
		
		// Navigate up from this activity.
		NavUtils.navigateUpFromSameTask(this);
	}
}
