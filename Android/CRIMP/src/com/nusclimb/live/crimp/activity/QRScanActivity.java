package com.nusclimb.live.crimp.activity;

import com.nusclimb.live.crimp.Helper;
import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.scanner.CameraManager;
import com.nusclimb.live.crimp.scanner.DecodeHandler;
import com.nusclimb.live.crimp.view.PreviewView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Second Activity of CRIMP.
 * 
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 *
 */
public class QRScanActivity extends Activity {
	private final String TAG = QRScanActivity.class.getSimpleName();
	
	private final String STATE_ACTIVITY = "state_activity";
	private final String STATE_JUDGE = "state_judge";
	private final String STATE_ROUND = "state_round";
	private final String STATE_ROUTE = "state_route";
	
	private String routeJudge, round, route;
	private String climberId;
	private String climberName;
	
	private Point previewResolution;	// Size of the previewView
	private PreviewView previewView;
	private CameraManager cameraManager;
	private QRScanHandler handler;
	private int activityState;			// R.id.decode: previewView showing camera input. preview send
										// 		to DecodeThread to be decoded. Default entry state.
										// R.id.decode_failed: Transitive state. Happens when DecodeThread
										//		fails to find QRCode. Will quickly transit to decode state 
										// 		to attempt to scan/decode again.
										// R.id.decode_succeeded: Camera resource released. previewView
										//		stop previewing. DecodeThread killed. qrResult not null.
										// R.id.quit: Prepare to quit. Camera resource released.
										//		previewView stop previewing. DecodeThread killed.
	
	/*=========================================================================
	 * Activity lifecycle methods
	 *=======================================================================*/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "QRScanActivity onCreate.");
		
		// We want as long as this window is visible to the user, 
		// keep the device's screen turned on and bright. 
		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		previewResolution = calculatePreviewResolution();
		
		// Check for savedInstanceState
		if(savedInstanceState != null){
			// Restore state
			activityState = savedInstanceState.getInt(STATE_ACTIVITY);
			
			routeJudge = savedInstanceState.getString(STATE_JUDGE);
			round = savedInstanceState.getString(STATE_ROUND);
			route = savedInstanceState.getString(STATE_ROUTE);
			
			Log.d(TAG, "QRScanActivity recreating an instance.");
		}
		else{
			// Create a new instance
			activityState = R.id.decode;
			Log.d(TAG, "QRScanActivity creating a new instance.");
		}
		
		setContentView(R.layout.activity_qrscan);
		
		// Setting and instantiating stuff
		cameraManager = new CameraManager(this);
		
		// Read intent
		String packageName = getString(R.string.package_name);
		Intent intent = getIntent();
		routeJudge = intent.getStringExtra(packageName + getString(R.string.intent_username));
		round = intent.getStringExtra(packageName + getString(R.string.intent_round));
		route = intent.getStringExtra(packageName + getString(R.string.intent_route));
		
		// Update title.
		this.setTitle(round + " " + route);
		TextView climberRoundId =  (TextView) findViewById(R.id.QRScan_round_text);
		climberRoundId.setText(Helper.toIdRound(round));
		
		// Update buttons.
		if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)){
			Button flashButton = (Button) findViewById(R.id.QRScan_flash_button);
			flashButton.setEnabled(false);
		}
	}
	
	@Override
	protected void onStart(){
		super.onStart();
		Log.d(TAG, "QRScanActivity onStart.");
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		Log.d(TAG, "QRScanActivity onResume."); 
		
		handler = new QRScanHandler(this);
		handler.setRunning(true);
		
		// We need camera resource. We will acquire camera in onResume and release in onPause.
		// Check if we have camera resource first.
		if(!cameraManager.hasCamera()){
			// No Camera resource.
			Log.d(TAG, "In onResume() without camera resource. Will attempt to acquire camera.");
			
			boolean temp = cameraManager.acquireCamera(previewResolution);
			if(temp == false){
				// Error handling. Fail to get camera resource.
				Log.e(TAG, "Failed to get camera resource.");
			}
		}
		
		//TODO Not sure if we can assume camera acquired. Perform check for safety.
		if(cameraManager.hasCamera()){
			createPreviewAndArrange();
		}
	}
	
	@Override
	protected void onPause(){
		handler.setRunning(false);
	
		// Stop previewing and release camera. We will acquire camera in onResume.
		if(cameraManager != null){
			cameraManager.stopPreview();
			cameraManager.releaseCamera();
		}
		
		// TODO We will also need to kill DecodeThread.
		handler.onPause();

		Log.d(TAG, "QRScanActivity onPause.");
		super.onPause();
	}
	
	@Override
	protected void onStop(){
		Log.d(TAG, "QRScanActivity onStop.");
		super.onStop();
	}
	
	@Override
	protected void onDestroy(){
		Log.d(TAG, "QRScanActivity onDestroy.");
		super.onDestroy();
	}
	
	
	
	/*=========================================================================
	 * Getter/Setter methods
	 *=======================================================================*/
	public int getState(){
		return activityState;
	}
	
	public void setState(int state){
		activityState = state;
	}
	
	public QRScanHandler getHandler(){
		return handler;
	}
	
	public DecodeHandler getDecodeHandler(){
		return handler.getDecodeHandler();
	}
	
	public CameraManager getCameraManager(){
		return cameraManager;
	}
	
	
	
	/*=========================================================================
	 * Overriden methods
	 *=======================================================================*/
	@Override
	protected void onSaveInstanceState(Bundle outState){
		outState.putInt(STATE_ACTIVITY, activityState);
		
		outState.putString(STATE_JUDGE, routeJudge);
		outState.putString(STATE_ROUND, round);
		outState.putString(STATE_ROUTE, route);
		
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
			// Respond to the action bar's Up/Home button
	    	case android.R.id.home:
	    		NavUtils.navigateUpFromSameTask(this);
		        return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	
	
	/*=========================================================================
	 * Public methods
	 *=======================================================================*/
	/**
	 * This method takes in a result String, retrieve climber's id and name from
	 * result String, and update the UI.
	 * 
	 * @param result String containing climber's id and name.
	 */
	public void updateStatusView(String result){
		String[] climberInfo = result.split(";");
		climberId = climberInfo[0];
		climberName = climberInfo[1];
		
		EditText idEdit = (EditText) findViewById(R.id.QRScan_climber_id_edit);
		EditText nameEdit = (EditText) findViewById(R.id.QRScan_climber_name_edit);
		idEdit.setText(climberId, TextView.BufferType.EDITABLE);
		nameEdit.setText(climberName, TextView.BufferType.EDITABLE);
	}
	
	/**
	 * This method reset activity state to "decode" and restart scanning.
	 * No-op if previewView surface is not ready.
	 * 
	 * @param view Reference to the button that was clicked.
	 */
	public void rescan(View view){
		climberId = "";
		climberName = "";
		
		EditText idEdit = (EditText) findViewById(R.id.QRScan_climber_id_edit);
		EditText nameEdit = (EditText) findViewById(R.id.QRScan_climber_name_edit);
		idEdit.setText(climberId, TextView.BufferType.EDITABLE);
		nameEdit.setText(climberName, TextView.BufferType.EDITABLE);
		
		if(cameraManager.isSurfaceReady()){
			setState(R.id.decode);
			cameraManager.startPreview(previewView.getHolder());
			cameraManager.startScan();
		}
		else{
			Log.w(TAG, "Rescan button failed due to previewView surface not ready.");
		}
	}
	
	/**
	 * Method to run when "next" button is clicked. Perform creation of intent and
	 * starting scoringActivity.
	 * 
	 * @param view Reference to the button that was clicked.
	 */
	public void next(View view){
		EditText climberIdEdit = (EditText) findViewById(R.id.QRScan_climber_id_edit);
		climberId = climberIdEdit.getText().toString();
		
		if(climberId.length() == 0){
			// Do not allow user to get past this activity if
			// climber id is not entered.
			Context context = getApplicationContext();
			CharSequence text = "Climber ID is required!";
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		}
		else{			
			String packageName = getString(R.string.package_name);
			
			// Preparing to start ScoringActivity.
			Intent intent = new Intent(this, ScoringActivity.class);
			intent.putExtra(packageName + getString(R.string.intent_username) , routeJudge);
			intent.putExtra(packageName + getString(R.string.intent_round), round);
			intent.putExtra(packageName + getString(R.string.intent_route), route);
			intent.putExtra(packageName + getString(R.string.intent_climber_id), Helper.toIdRound(round) + climberId);
			if(climberName != null){
				intent.putExtra(packageName + getString(R.string.intent_climber_name), climberName);
			}
			
			startActivity(intent);
		}
	}
	
	/**
	 * Method to toggle the camera flash.
	 * 
	 * @param view Reference to the button that was clicked.
	 */
	public void toggleFlash(View view){
		getCameraManager().setFlash(!getCameraManager().isTorchOn());
	}
	
	
	
	/*=========================================================================
	 * Private methods
	 *=======================================================================*/
	/**
	 * This method calculate the size of preview surface view. We want the
	 * width of preview view to be the entire display width. Aspect ratio
	 * of the preview view is the inverse of device real resolution aspect ratio.
	 */
	@SuppressLint("NewApi")
	private Point calculatePreviewResolution() {
		WindowManager manager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
	    Display display = manager.getDefaultDisplay();
	    Point screenResolution = new Point();	//application display size without system decoration
	    Point realResolution = new Point();		//display size including system decoration
	    if(android.os.Build.VERSION.SDK_INT >= 13){
	    	display.getSize(screenResolution);
	    }
	    else{
	    	screenResolution.x = display.getWidth();
	    	screenResolution.y = display.getHeight();
	    }
	    
	    // display.getRealSize only work from api lvl 17 onward.
	    if(android.os.Build.VERSION.SDK_INT >= 17){
	    	Log.d(TAG, "Using Display.getRealSize().");
	    	display.getRealSize(realResolution);
	    }
	    else{
	    	//TODO not working as intended.
	    	Log.d(TAG, "Using Display.getMetrics().");
	    	DisplayMetrics displaymetrics = new DisplayMetrics();
	    	display.getMetrics(displaymetrics);
	    	realResolution.y = displaymetrics.heightPixels;
	    	realResolution.x = displaymetrics.widthPixels;
	    }
		
	    Point previewViewSize = new Point();
	    previewViewSize.x = screenResolution.x;
	    previewViewSize.y = (int) (screenResolution.x * ((double)realResolution.x/realResolution.y));
	    Log.v(TAG, "screenReso: X" + screenResolution.x + " x Y" + screenResolution.y + 
	    		"\nrealReso: X" + realResolution.x + " x Y" + realResolution.y + 
	    		"\npreviewReso: X" + previewViewSize.x + " x Y" + previewViewSize.y);
	    return previewViewSize;
	}

	/**
	 * This method create previewView and translate it. No-op if previewView
	 * already exist. Important: This method can only be called after camera 
	 * resource is acquired (e.g. starting from onResume).
	 */
	private void createPreviewAndArrange(){
		if(previewView == null){
			Camera.Size temp = cameraManager.getBestPreviewSize();
			
			// Find all view/viewgroup first.
			FrameLayout previewFrame = (FrameLayout) findViewById(R.id.QRScan_frame_viewgroup);
			
			previewView = new PreviewView(this);
			int height;
			if(temp.width > previewResolution.y){
				height = temp.width;
			}
			else{
				height = previewResolution.y;
			}
			FrameLayout.LayoutParams frameParams = 
					new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, height);
			previewView.setLayoutParams(frameParams);
			
			//previewFrame
			LinearLayout.LayoutParams linearParams = 
					(android.widget.LinearLayout.LayoutParams) previewFrame.getLayoutParams();
			linearParams.height = previewResolution.y;
			previewFrame.setLayoutParams(linearParams);
			previewFrame.addView(previewView);
			
			previewView.getHolder().addCallback(cameraManager);
			if(temp.width > previewResolution.y){
				previewView.setTranslationY(previewResolution.y - temp.width);
			}
		}
	}
	
}
