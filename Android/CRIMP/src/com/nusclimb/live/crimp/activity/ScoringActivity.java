package com.nusclimb.live.crimp.activity;

import com.nusclimb.live.crimp.Helper;
import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.json.RoundInfoMap;
import com.nusclimb.live.crimp.json.Score;
import com.nusclimb.live.crimp.json.SessionUpload;
import com.nusclimb.live.crimp.request.ClimberInfoRequest;
import com.nusclimb.live.crimp.request.ScoreRequest;
import com.nusclimb.live.crimp.request.UploadRequest;
import com.nusclimb.live.crimp.retry.CrimpRetryPolicy;
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
	
	private String infoRequestCacheKey;
	private String scoreRequestCacheKey;
	
	private EditText scoreEdit;
	private String routeJudge, round, route, climberId, climberName;
	private SpiceManager spiceManager = new SpiceManager(
			CrimpService.class);
	
	/*=========================================================================
	 * Inner class
	 *=======================================================================*/
	private class ClimberInfoRequestListener implements RequestListener<RoundInfoMap> {
		@Override
		public void onRequestFailure(SpiceException e) {
			infoRequestCacheKey = null;
			
			//Update UI
			EditText nameEdit = (EditText) findViewById(R.id.scoring_climber_name_edit);
			nameEdit.setText(getText(R.string.UI_unavailable));
		}

	     @Override
	     public void onRequestSuccess(RoundInfoMap result) {
	    	 infoRequestCacheKey = null;
	    	 
	    	 climberName = result.get(climberId);
	    	 
	    	 // Update UI.
	    	 EditText nameEdit = (EditText) findViewById(R.id.scoring_climber_name_edit);
	    	 if(climberName == null){
	    		 Log.w(TAG, "Climber name request returns null.");
	    		 nameEdit.setText(getText(R.string.UI_unavailable));
	    	 }
	    	 else{
	    		 Log.i(TAG, "Climber name is "+climberName);
	    		 nameEdit.setText(climberName);
	    	 }
	     }
	}
	
	private class ScoreRequestListener implements RequestListener<Score> {
		@Override
		public void onRequestFailure(SpiceException e) {
			scoreRequestCacheKey = null;
			
			EditText scoreHistoryEdit = (EditText) findViewById(R.id.scoring_score_history_edit);
			scoreHistoryEdit.setText(getText(R.string.UI_unavailable));
			ScoringActivity.this.setProgressBarIndeterminateVisibility(false);
		}

	     @Override
	     public void onRequestSuccess(Score result) {
	    	 scoreRequestCacheKey = null;
	    	 
	    	 String score = result.getC_score();
	    	 
	    	 //Update UI
	    	 EditText scoreHistoryEdit = (EditText) findViewById(R.id.scoring_score_history_edit);
	    	 if(score == null){
	    		 Log.w(TAG, "Climber score request returns null.");
	    		 scoreHistoryEdit.setText(getText(R.string.UI_unavailable));
	    	 }
	    	 else{
	    		 Log.i(TAG, "Climber score request returns "+score);
	    		 scoreHistoryEdit.setText(score);
	    	 }
	         ScoringActivity.this.setProgressBarIndeterminateVisibility(false);
	     }
	}
	
	private class UploadRequestListener implements RequestListener<Object> {
		@Override
		public void onRequestFailure(SpiceException e) {
			Log.e(TAG, "Upload failed.");
		}

	     @Override
	     public void onRequestSuccess(Object result) {
	    	 Log.i(TAG, "Upload succeed.");
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
			spiceManager.cancel(RoundInfoMap.class, infoRequestCacheKey);
			infoRequestCacheKey = null;
		}
		
		if(scoreRequestCacheKey != null){
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
	    			spiceManager.cancel(RoundInfoMap.class, infoRequestCacheKey);
	    			infoRequestCacheKey = null;
	    		}
	    		
	    		if(scoreRequestCacheKey != null){
	    			spiceManager.cancel(RoundInfoMap.class, scoreRequestCacheKey);
	    			scoreRequestCacheKey = null;
	    		}
	    		
	    		// ,,,then start new download.
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
	public void refreshScore(){
		setProgressBarIndeterminateVisibility(true);

        ScoreRequest request = new ScoreRequest(climberId, round, route);
        
        scoreRequestCacheKey = request.createCacheKey();
        
        //TODO Maybe should use cache?
        spiceManager.execute(request, scoreRequestCacheKey, 
        		DurationInMillis.ALWAYS_EXPIRED, 
        		new ScoreRequestListener());
	}
	
	public void refreshName(){
		setProgressBarIndeterminateVisibility(true);

        ClimberInfoRequest request = new ClimberInfoRequest(round);
        
        infoRequestCacheKey = request.createCacheKey();
        
        //TODO Maybe should use cache?
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
		
		// Get old score.
		EditText scoreHistoryEdit = (EditText) findViewById(R.id.scoring_score_history_edit);
		String oldScore = scoreHistoryEdit.getText().toString();
		
		// Prepare POJO
		if(scoreEdit == null){
			scoreEdit = (EditText) findViewById(R.id.scoring_score_current_edit);
		}
		SessionUpload uploadPOJO = new SessionUpload();
		if(oldScore.length() == 0){
			uploadPOJO.setAll_current(routeJudge, getString(R.string.net_password_debug), 
					r_id, climberId, scoreEdit.getText().toString());
		}
		else{
			uploadPOJO.setAll_old_current(routeJudge, getString(R.string.net_password_debug), 
					r_id, climberId, oldScore, scoreEdit.getText().toString());
		}
		
		Log.v(TAG, "Upload json: "+uploadPOJO.toString());
		
		UploadRequest request = new UploadRequest(uploadPOJO, CrimpService.nextRequestId());
		request.setRetryPolicy(new CrimpRetryPolicy());
		
		String lastRequestCacheKey = request.createCacheKey();
		
		//TODO Maybe should use cache?
		spiceManager.execute(request, /*lastRequestCacheKey, 
        		DurationInMillis.ALWAYS_EXPIRED,*/ 
        		new UploadRequestListener());
		
		// Quit this activity.
		NavUtils.navigateUpFromSameTask(this);
	}
	
	/*
	public void test1(View view){
		// Convert to server alias.
		String serverAliasRound = Helper.toServerRound(round);
		String serverAliasRoute = Helper.parseRoute(route);
		String r_id = serverAliasRound + serverAliasRoute;
		
		// Get old score.
		EditText scoreHistoryEdit = (EditText) findViewById(R.id.scoring_score_history_edit);
		String oldScore = scoreHistoryEdit.getText().toString();
		
		// Prepare POJO
		if(scoreEdit == null){
			scoreEdit = (EditText) findViewById(R.id.scoring_score_current_edit);
		}
		SessionUpload uploadPOJO0 = new SessionUpload();
		SessionUpload uploadPOJO1 = new SessionUpload();
		SessionUpload uploadPOJO2 = new SessionUpload();
		SessionUpload uploadPOJO3 = new SessionUpload();
		SessionUpload uploadPOJO4 = new SessionUpload();
		SessionUpload uploadPOJO5 = new SessionUpload();
		SessionUpload uploadPOJO6 = new SessionUpload();
		SessionUpload uploadPOJO7 = new SessionUpload();
		SessionUpload uploadPOJO8 = new SessionUpload();
		SessionUpload uploadPOJO9 = new SessionUpload();
		
		uploadPOJO0.setAll_current(routeJudge, getString(R.string.net_password_debug), 
				r_id, climberId, "0");
		
		uploadPOJO1.setAll_current(routeJudge, getString(R.string.net_password_debug), 
				r_id, climberId, "1");
		
		uploadPOJO2.setAll_current(routeJudge, getString(R.string.net_password_debug), 
				r_id, climberId, "2");
		
		uploadPOJO3.setAll_current(routeJudge, getString(R.string.net_password_debug), 
				r_id, climberId, "3");
		
		uploadPOJO4.setAll_current(routeJudge, getString(R.string.net_password_debug), 
				r_id, climberId, "4");
		
		uploadPOJO5.setAll_current(routeJudge, getString(R.string.net_password_debug), 
				r_id, climberId, "5");
		
		uploadPOJO6.setAll_current(routeJudge, getString(R.string.net_password_debug), 
				r_id, climberId, "6");
		
		uploadPOJO7.setAll_current(routeJudge, getString(R.string.net_password_debug), 
				r_id, climberId, "7");
		
		uploadPOJO8.setAll_current(routeJudge, getString(R.string.net_password_debug), 
				r_id, climberId, "8");
		
		uploadPOJO9.setAll_current(routeJudge, getString(R.string.net_password_debug), 
				r_id, climberId, "9");
		
		//Log.v(TAG, "Upload json: "+uploadPOJO.toString());
		
		UploadRequest request0 = new UploadRequest(uploadPOJO0, CrimpService.nextRequestId());
		request0.setRetryPolicy(new CrimpRetryPolicy());
		
		UploadRequest request1 = new UploadRequest(uploadPOJO1, CrimpService.nextRequestId());
		request1.setRetryPolicy(new CrimpRetryPolicy());
		
		UploadRequest request2 = new UploadRequest(uploadPOJO2, CrimpService.nextRequestId());
		request2.setRetryPolicy(new CrimpRetryPolicy());
		
		UploadRequest request3 = new UploadRequest(uploadPOJO3, CrimpService.nextRequestId());
		request3.setRetryPolicy(new CrimpRetryPolicy());
		
		UploadRequest request4 = new UploadRequest(uploadPOJO4, CrimpService.nextRequestId());
		request4.setRetryPolicy(new CrimpRetryPolicy());
		
		UploadRequest request5 = new UploadRequest(uploadPOJO5, CrimpService.nextRequestId());
		request5.setRetryPolicy(new CrimpRetryPolicy());
		
		UploadRequest request6 = new UploadRequest(uploadPOJO6, CrimpService.nextRequestId());
		request6.setRetryPolicy(new CrimpRetryPolicy());
		
		UploadRequest request7 = new UploadRequest(uploadPOJO7, CrimpService.nextRequestId());
		request7.setRetryPolicy(new CrimpRetryPolicy());
		
		UploadRequest request8 = new UploadRequest(uploadPOJO8, CrimpService.nextRequestId());
		request8.setRetryPolicy(new CrimpRetryPolicy());
		
		UploadRequest request9 = new UploadRequest(uploadPOJO9, CrimpService.nextRequestId());
		request9.setRetryPolicy(new CrimpRetryPolicy());
		
		//String lastRequestCacheKey = request.createCacheKey();
		
		//TODO Maybe should use cache?
		spiceManager.execute(request0, request0.createCacheKey(), DurationInMillis.ALWAYS_EXPIRED, new UploadRequestListener());
		spiceManager.execute(request1, request1.createCacheKey(), DurationInMillis.ALWAYS_EXPIRED, new UploadRequestListener());
		spiceManager.execute(request2, request2.createCacheKey(), DurationInMillis.ALWAYS_EXPIRED, new UploadRequestListener());
		spiceManager.execute(request3, request3.createCacheKey(), DurationInMillis.ALWAYS_EXPIRED, new UploadRequestListener());
		spiceManager.execute(request4, request4.createCacheKey(), DurationInMillis.ALWAYS_EXPIRED, new UploadRequestListener());
		spiceManager.execute(request5, request5.createCacheKey(), DurationInMillis.ALWAYS_EXPIRED, new UploadRequestListener());
		spiceManager.execute(request6, request6.createCacheKey(), DurationInMillis.ALWAYS_EXPIRED, new UploadRequestListener());
		spiceManager.execute(request7, request7.createCacheKey(), DurationInMillis.ALWAYS_EXPIRED, new UploadRequestListener());
		spiceManager.execute(request8, request8.createCacheKey(), DurationInMillis.ALWAYS_EXPIRED, new UploadRequestListener());
		spiceManager.execute(request9, request9.createCacheKey(), DurationInMillis.ALWAYS_EXPIRED, new UploadRequestListener());
		
		
		// Quit this activity.
		NavUtils.navigateUpFromSameTask(this);
		
	}
	
	public void test2(View view){
		// Convert to server alias.
		String serverAliasRound = Helper.toServerRound(round);
		String serverAliasRoute = Helper.parseRoute(route);
		String r_id = serverAliasRound + serverAliasRoute;
		
		// Get old score.
		EditText scoreHistoryEdit = (EditText) findViewById(R.id.scoring_score_history_edit);
		String oldScore = scoreHistoryEdit.getText().toString();
		
		// Prepare POJO
		if(scoreEdit == null){
			scoreEdit = (EditText) findViewById(R.id.scoring_score_current_edit);
		}
		SessionUpload uploadPOJOA = new SessionUpload();
		SessionUpload uploadPOJOB = new SessionUpload();
		SessionUpload uploadPOJOC = new SessionUpload();
		
		uploadPOJOA.setAll_current(routeJudge, getString(R.string.net_password_debug), 
				r_id, climberId, "A");
		
		uploadPOJOB.setAll_current(routeJudge, getString(R.string.net_password_debug), 
				r_id, climberId, "B");
		
		uploadPOJOC.setAll_current(routeJudge, getString(R.string.net_password_debug), 
				r_id, climberId, "C");
		
		
		//Log.v(TAG, "Upload json: "+uploadPOJO.toString());
		
		UploadRequest requestA = new UploadRequest(uploadPOJOA, CrimpService.nextRequestId());
		requestA.setRetryPolicy(new CrimpRetryPolicy());
		
		UploadRequest requestB = new UploadRequest(uploadPOJOB, CrimpService.nextRequestId());
		requestB.setRetryPolicy(new CrimpRetryPolicy());
		
		UploadRequest requestC = new UploadRequest(uploadPOJOC, CrimpService.nextRequestId());
		requestC.setRetryPolicy(new CrimpRetryPolicy());
		
		
		//String lastRequestCacheKey = request.createCacheKey();
		
		//TODO Maybe should use cache?
		spiceManager.execute(requestA, requestA.createCacheKey(), DurationInMillis.ALWAYS_EXPIRED, new UploadRequestListener());
		spiceManager.execute(requestB, requestB.createCacheKey(), DurationInMillis.ALWAYS_EXPIRED, new UploadRequestListener());
		spiceManager.execute(requestC, requestC.createCacheKey(), DurationInMillis.ALWAYS_EXPIRED, new UploadRequestListener());
		
		
		// Quit this activity.
		NavUtils.navigateUpFromSameTask(this);
	}
	
	public void test3(View view){
		// Convert to server alias.
		String serverAliasRound = Helper.toServerRound(round);
		String serverAliasRoute = Helper.parseRoute(route);
		String r_id = serverAliasRound + serverAliasRoute;
		
		// Get old score.
		EditText scoreHistoryEdit = (EditText) findViewById(R.id.scoring_score_history_edit);
		String oldScore = scoreHistoryEdit.getText().toString();
		
		// Prepare POJO
		if(scoreEdit == null){
			scoreEdit = (EditText) findViewById(R.id.scoring_score_current_edit);
		}
		SessionUpload uploadPOJOD = new SessionUpload();
		SessionUpload uploadPOJOE = new SessionUpload();
		SessionUpload uploadPOJOF = new SessionUpload();
		
		uploadPOJOD.setAll_current(routeJudge, getString(R.string.net_password_debug), 
				r_id, climberId, "D");
		
		uploadPOJOE.setAll_current(routeJudge, getString(R.string.net_password_debug), 
				r_id, climberId, "E");
		
		uploadPOJOF.setAll_current(routeJudge, getString(R.string.net_password_debug), 
				r_id, climberId, "F");
		
		
		//Log.v(TAG, "Upload json: "+uploadPOJO.toString());
		
		UploadRequest requestD = new UploadRequest(uploadPOJOD, CrimpService.nextRequestId());
		requestD.setRetryPolicy(new CrimpRetryPolicy());
		
		UploadRequest requestE = new UploadRequest(uploadPOJOE, CrimpService.nextRequestId());
		requestE.setRetryPolicy(new CrimpRetryPolicy());
		
		UploadRequest requestF = new UploadRequest(uploadPOJOF, CrimpService.nextRequestId());
		requestF.setRetryPolicy(new CrimpRetryPolicy());
		
		
		//String lastRequestCacheKey = request.createCacheKey();
		
		//TODO Maybe should use cache?
		spiceManager.execute(requestD, requestD.createCacheKey(), DurationInMillis.ALWAYS_EXPIRED, new UploadRequestListener());
		spiceManager.execute(requestE, requestE.createCacheKey(), DurationInMillis.ALWAYS_EXPIRED, new UploadRequestListener());
		spiceManager.execute(requestF, requestF.createCacheKey(), DurationInMillis.ALWAYS_EXPIRED, new UploadRequestListener());
		
		
		// Quit this activity.
		NavUtils.navigateUpFromSameTask(this);
	}
	
	public void test4(View view){
		// Convert to server alias.
		String serverAliasRound = Helper.toServerRound(round);
		String serverAliasRoute = Helper.parseRoute(route);
		String r_id = serverAliasRound + serverAliasRoute;
		
		// Get old score.
		EditText scoreHistoryEdit = (EditText) findViewById(R.id.scoring_score_history_edit);
		String oldScore = scoreHistoryEdit.getText().toString();
		
		// Prepare POJO
		if(scoreEdit == null){
			scoreEdit = (EditText) findViewById(R.id.scoring_score_current_edit);
		}
		SessionUpload uploadPOJOG = new SessionUpload();
		SessionUpload uploadPOJOH = new SessionUpload();
		SessionUpload uploadPOJOI = new SessionUpload();
		
		uploadPOJOG.setAll_current(routeJudge, getString(R.string.net_password_debug), 
				r_id, climberId, "G");
		
		uploadPOJOH.setAll_current(routeJudge, getString(R.string.net_password_debug), 
				r_id, climberId, "H");
		
		uploadPOJOI.setAll_current(routeJudge, getString(R.string.net_password_debug), 
				r_id, climberId, "I");
		
		//Log.v(TAG, "Upload json: "+uploadPOJO.toString());
		
		UploadRequest requestG = new UploadRequest(uploadPOJOG, CrimpService.nextRequestId());
		requestG.setRetryPolicy(new CrimpRetryPolicy());
		
		UploadRequest requestH = new UploadRequest(uploadPOJOH, CrimpService.nextRequestId());
		requestH.setRetryPolicy(new CrimpRetryPolicy());
		
		UploadRequest requestI = new UploadRequest(uploadPOJOI, CrimpService.nextRequestId());
		requestI.setRetryPolicy(new CrimpRetryPolicy());
		
		
		//String lastRequestCacheKey = request.createCacheKey();
		
		//TODO Maybe should use cache?
		spiceManager.execute(requestG, requestG.createCacheKey(), DurationInMillis.ALWAYS_EXPIRED, new UploadRequestListener());
		spiceManager.execute(requestH, requestH.createCacheKey(), DurationInMillis.ALWAYS_EXPIRED, new UploadRequestListener());
		spiceManager.execute(requestI, requestI.createCacheKey(), DurationInMillis.ALWAYS_EXPIRED, new UploadRequestListener());
		
		
		// Quit this activity.
		NavUtils.navigateUpFromSameTask(this);
	}
	*/
}
