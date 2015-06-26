package com.nusclimb.live.crimp.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.common.json.LoginResponse;
import com.nusclimb.live.crimp.common.spicerequest.LoginRequest;
import com.nusclimb.live.crimp.hello.HelloActivity;
import com.nusclimb.live.crimp.service.CrimpService;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Login activity of CRIMP.
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class LoginActivity extends Activity {
    private final String TAG = LoginActivity.class.getSimpleName();

    // Various state of activity.
    private enum LoginState {
        NOT_LOGIN,          // Not login to facebook. Not login to CRIMP server.
        IN_FACEBOOK,        // In facebook login activity.
        FACEBOOK_OK,        // Successfully login to facebook.
        IN_VERIFYING,       // Trying to login to CRIMP server.
        VERIFIED_OK,        // CRIMP server reply ok.
        VERIFIED_NOT_OK,    // CRIMP server reject user.
        VERIFIED_FAILED     // No response or unknown response from CRIMP server.
    }

    // Stuff for communicating with CRIMP server
    private String loginRequestCacheKey;
    private SpiceManager spiceManager = new SpiceManager(
            CrimpService.class);
    private String xUserId;
    private String xAuthToken;
    private ArrayList<String> roles;

    // Activity state
    private LoginState mState = LoginState.NOT_LOGIN;

    // UI references.
    private View mViewVerifying;
    private View mViewLoadingWheel;
    private View mViewResponseText;
    private View mViewCancelButton;
    private View mViewRetryButton;
    private LoginButton mViewLoginButton;

    // Facebook references.
    private CallbackManager callbackManager;

    /*=========================================================================
     * Inner class
     *=======================================================================*/
    /**
     * RequestListener for receiving response of login request.
     *
     * @author Lin Weizhi (ecc.weizhi@gmail.com)
     */
    private class LoginRequestListener implements RequestListener<LoginResponse> {
        @Override
        public void onRequestFailure(SpiceException e) {
            Log.d(TAG, "LoginRequestListener request fail.");

            loginRequestCacheKey = null;

            //TODO uncomment this
            forceSuccess();
            /*
            if(mState == LoginState.IN_VERIFYING)
                changeState(LoginState.VERIFIED_FAILED);
            */
        }

        @Override
        public void onRequestSuccess(LoginResponse result) {
            Log.d(TAG, "LoginRequestListener request succeed.");

            loginRequestCacheKey = null;
            xUserId = result.getxUserId();
            xAuthToken = result.getxAuthToken();
            roles = result.getRoles();

            if(mState == LoginState.IN_VERIFYING)
                changeState(LoginState.VERIFIED_OK);
        }
    }

    //TODO remove this before production
    private void forceSuccess(){
        xUserId = "testUserId";
        xAuthToken = "testAuthToken";
        String[] rolesArray = {"testRole"};
        roles = new ArrayList<String>(Arrays.asList(rolesArray));

        if(mState == LoginState.IN_VERIFYING)
            changeState(LoginState.VERIFIED_OK);
    }



    /*=========================================================================
     * UI methods
     *=======================================================================*/
    private void showFacebookButton(boolean show){
        mViewLoginButton.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showVerifyingView(boolean show){
        mViewVerifying.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showLoadingWheel(boolean show){
        mViewLoadingWheel.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    private void showResponseText(boolean show){
        mViewResponseText.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showResponseText(int textResource){
        ((TextView) mViewResponseText).setText(textResource);
        mViewResponseText.setVisibility(View.VISIBLE);
    }

    private void showCancelButton(boolean show){
        mViewCancelButton.setVisibility(show ? View.VISIBLE : View.GONE);
        mViewRetryButton.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    /**
     * Method to control which UI element is visible at different state.
     */
    private void updateUI(){
        Log.d(TAG, "Update UI. mState:" + mState);
        switch (mState) {
            case NOT_LOGIN:
                showFacebookButton(true);
                showVerifyingView(false);
                break;
            case IN_FACEBOOK:
                break;
            case FACEBOOK_OK:
                break;
            case IN_VERIFYING:
                showFacebookButton(false);
                showLoadingWheel(true);
                showResponseText(R.string.login_activity_login_wait);
                showCancelButton(true);
                showVerifyingView(true);
                break;
            case VERIFIED_OK:
                break;
            case VERIFIED_NOT_OK:
                //TODO toast message and UI stuff. Incomplete.
                Context context = getApplicationContext();
                CharSequence text = getString(R.string.login_activity_login_fail);
                Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
                toast.show();
                break;
            case VERIFIED_FAILED:
                showFacebookButton(false);
                showLoadingWheel(false);
                showResponseText(R.string.login_activity_login_error);
                showCancelButton(false);
                showVerifyingView(true);
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "In onActivityResult(). mState: " + mState + "; resultcode: " + resultCode + "; ok is " + RESULT_OK);

        if(resultCode == RESULT_OK)
            changeState(LoginState.FACEBOOK_OK);
    }

    /**
     * Method to control what is performed at different state.
     */
    private void doVerification(){
        Log.d(TAG, "Attempts to do verification. mState:" + mState);
        switch (mState){
            case NOT_LOGIN:
                break;
            case IN_FACEBOOK:
                break;
            case FACEBOOK_OK:
                changeState(LoginState.IN_VERIFYING);
                break;
            case IN_VERIFYING:
                AccessToken currentAccessToken = AccessToken.getCurrentAccessToken();
                //TODO change this when going production
                //LoginRequest request = new LoginRequest(currentAccessToken.getToken(), currentAccessToken.getExpires().toString());
                LoginRequest request = new LoginRequest("testAccessToken", "testExpire");
                loginRequestCacheKey = request.createCacheKey();
                spiceManager.execute(request, loginRequestCacheKey,
                        DurationInMillis.ALWAYS_EXPIRED,
                        new LoginRequestListener());
                break;
            case VERIFIED_OK:
                launchHelloActivity();
                break;
            case VERIFIED_NOT_OK:
                //TODO
                break;
            case VERIFIED_FAILED:
                break;
        }
    }

    /**
     * Set {@code mState} to {@code state}. Changes to {@code mState} must
     * go through this method. Perform a call to {@code updateUI()} before
     * calling {@code doVerification()}.
     *
     * @param state Login state to set {@code mState} to.
     */
    private void changeState(LoginState state){
        Log.d(TAG, "Change state: "+mState+"->"+state);

        mState = state;
        updateUI();
        doVerification();
    }

    /**
     * This method is called when user clicked on cancel button. Log out from
     * facebook (if user is not already logged out).
     *
     * @param view
     */
    public void retry(View view) {
        LoginManager.getInstance().logOut();
        changeState(LoginState.NOT_LOGIN);
    }

    public void cancel(View view){
        if(loginRequestCacheKey!=null) {
            Log.d(TAG, "Pressed cancel. mState:" + mState+". Cancel request: "+loginRequestCacheKey);
        }
        else{
            Log.d(TAG, "Pressed cancel. mState:" + mState);
        }
        if(loginRequestCacheKey != null){
            spiceManager.cancel(LoginResponse.class, loginRequestCacheKey);
            loginRequestCacheKey = null;
        }
        LoginManager.getInstance().logOut();

        changeState(LoginState.NOT_LOGIN);
    }

    private void launchHelloActivity(){
        Log.d(TAG, "Launching hello activity. mState: "+mState+ "; xUserId: "+xUserId+"; xAuthToken: "+xAuthToken);
        Intent intent = new Intent(getApplicationContext(), HelloActivity.class);
        intent.putExtra(getString(R.string.package_name) + getString(R.string.login_activity_xuserid), xUserId);
        intent.putExtra(getString(R.string.package_name) + getString(R.string.login_activity_xauthtoken), xAuthToken);
        intent.putStringArrayListExtra(getString(R.string.package_name) + getString(R.string.login_activity_role), roles);
        startActivity(intent);
    }



    /*=========================================================================
     * Activity lifecycle methods
     *=======================================================================*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);

        Log.d(TAG, "In onCreate(). mState:"+mState);

        // Assign views to references
        mViewVerifying = findViewById(R.id.verifying_view);
        mViewLoginButton = (LoginButton) findViewById(R.id.login_button);
        mViewLoadingWheel = findViewById(R.id.loading_wheel);
        mViewResponseText = findViewById(R.id.response_text);
        mViewCancelButton = findViewById(R.id.login_cancel_button);
        mViewRetryButton = findViewById(R.id.login_retry_button);

        // Setting up Facebook login button stuff.
        callbackManager = CallbackManager.Factory.create();
        mViewLoginButton.setReadPermissions("public_profile");
        mViewLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "In FacebookCallback. Facebook login succeeded.");
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "In FacebookCallback. Facebook login cancelled.");
            }

            @Override
            public void onError(FacebookException exception) {
                Log.e(TAG, "In FacebookCallback. Facebook login error.");
            }
        });
    }

    @Override
    protected void onStart(){
        super.onStart();
        Log.d(TAG, "In onStart(). mState:" + mState);

        spiceManager.start(this);

        // Application could have restarted. Checking if we are login on facebook
        // and update mState accordingly.
        if(mState == LoginState.NOT_LOGIN){
            if(AccessToken.getCurrentAccessToken()!=null){
                changeState(LoginState.FACEBOOK_OK);
            }
        }
    }

    @Override
    protected void onRestart(){
        super.onRestart();
        Log.d(TAG, "In onRestart(). mState:" + mState);
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.d(TAG, "In onResume(). mState:" + mState);

        changeState(mState);
    }

    @Override
    protected void onPause(){
        super.onPause();

        if(loginRequestCacheKey != null) {
            Log.d(TAG, "In onPause(). mState: " + mState+". Cancelling loginRequest: "+loginRequestCacheKey);
        }
        else{
            Log.d(TAG, "In onPause(). mState: " + mState);
        }

        if(loginRequestCacheKey != null){
            spiceManager.cancel(LoginResponse.class, loginRequestCacheKey);
            loginRequestCacheKey = null;
        }
    }

    @Override
    protected void onStop(){
        super.onStop();
        Log.d(TAG, "In onStop().mState:" + mState);
        spiceManager.shouldStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "In onDestroy(). mState:" + mState);
    }
}

