package com.nusclimb.live.crimp.login;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.common.json.RoundInfoMap;
import com.nusclimb.live.crimp.common.json.Session;
import com.nusclimb.live.crimp.common.spicerequest.LoginRequest;
import com.nusclimb.live.crimp.hello.HelloActivity;
import com.nusclimb.live.crimp.service.CrimpService;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

public class LoginActivity extends Activity {
    private enum LoginState {
        NOT_LOGIN, FACEBOOK, VERIFYING, VERIFIED_OK, VERIFIED_NOT_OK, FAILED
    }

    private final String TAG = LoginActivity.class.getSimpleName();
    private String loginRequestCacheKey;
    private SpiceManager spiceManager = new SpiceManager(
            CrimpService.class);
    private String sessionToken;
    private LoginState mState = LoginState.NOT_LOGIN;

    // UI references.
    private View mVerifyingView;
    private View mLoadingWheel;
    private View mResponseText;
    private View mCancelButton;
    private View mRetryButton;

    // Facebook references.
    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private ProfileTracker profileTracker;

    private class LoginRequestListener implements RequestListener<Session> {
        @Override
        public void onRequestFailure(SpiceException e) {
            Log.d(TAG, "LoginRequestListener request fail.");

            loginRequestCacheKey = null;
            if(mState == LoginState.VERIFYING) {
                mState = LoginState.FAILED;
                updateUI();
            }
        }

        @Override
        public void onRequestSuccess(Session result) {
            Log.d(TAG, "LoginRequestListener request succeed.");

            loginRequestCacheKey = null;
            sessionToken = result.getSessionToken();

            if(mState == LoginState.VERIFYING) {
                mState = LoginState.VERIFIED_OK;
                launchHelloActivity();
                //updateUI();
            }
        }
    }




    // Activity lifecycle methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);

        Log.d(TAG, "In onCreate().");

        // Assign views to references
        mVerifyingView = findViewById(R.id.verifying_view);
        loginButton = (LoginButton) findViewById(R.id.login_button);
        mLoadingWheel = findViewById(R.id.loading_wheel);
        mResponseText = findViewById(R.id.response_text);
        mCancelButton = findViewById(R.id.login_cancel_button);
        mRetryButton = findViewById(R.id.login_retry_button);

        // Setting up Facebook login button stuff.
        callbackManager = CallbackManager.Factory.create();
        loginButton.setReadPermissions("public_profile");
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "Facebook login succeeded.");
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "Facebook login cancelled.");
            }

            @Override
            public void onError(FacebookException exception) {
                Log.e(TAG, "Facebook login error.");
            }
        });

        // Setting up Facebook ProfileTracker stuff.
        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                String oldProfileString, currentProfileString, logMessage;
                if(oldProfile == null)
                    oldProfileString = "null";
                else
                    oldProfileString = oldProfile.getName();

                if(currentProfile == null) {
                    currentProfileString = "null";
                    logMessage = "Profile changed. ["+oldProfileString+
                            "] -> ["+currentProfileString+"]";

                    Log.d(TAG, logMessage);
                }
                else {
                    currentProfileString = currentProfile.getName();
                    logMessage = "Profile changed. ["+oldProfileString+
                            "] -> ["+currentProfileString+"]";

                    Log.d(TAG, logMessage);

                    doVerification();
                }
            }
        };
    }

    @Override
    protected void onStart(){
        super.onStart();
        Log.d(TAG, "In onStart().");

        spiceManager.start(this);
    }

    @Override
    protected void onRestart(){
        super.onRestart();
        Log.d(TAG, "In onRestart().");
    }

    @Override
    protected void onResume(){
        super.onResume();

        Profile mProfile = Profile.getCurrentProfile();
        if (mProfile!=null){
            Log.d(TAG, "In onResume(). Profile: " + mProfile.getName());
        }
        else{
            Log.d(TAG, "In onResume(). Profile: null");
        }

        doVerification();
    }

    @Override
    protected void onPause(){
        super.onPause();
        Log.d(TAG, "In onPause().");

        if(loginRequestCacheKey != null){
            Log.d(TAG, "onPause. Cancelling loginRequest: "+loginRequestCacheKey);
            spiceManager.cancel(Session.class, loginRequestCacheKey);
            loginRequestCacheKey = null;
        }

        if(mState == LoginState.VERIFYING){
            mState = LoginState.FACEBOOK;
        }
    }

    @Override
    protected void onStop(){
        super.onStop();
        Log.d(TAG, "In onStop().");
        spiceManager.shouldStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        profileTracker.stopTracking();
        Log.d(TAG, "In onDestroy(). Stopped profile tracking.");
    }

    // TODO for facebook login. no idea.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

        mState = LoginState.FACEBOOK;
        Log.d(TAG, "resultcode: " + resultCode + "; ok is " + RESULT_OK);
    }

    private void doVerification(){
        switch (mState){
            case NOT_LOGIN:
                Log.d(TAG, "Attempts to do verification. Not logged in. No action was done.");
                updateUI();
                break;
            case FACEBOOK:
                Log.d(TAG, "Attempts to do verification. Facebook was logged in.");
                mState = LoginState.VERIFYING;
                updateUI();

                LoginRequest request = new LoginRequest(AccessToken.getCurrentAccessToken().getToken());
                loginRequestCacheKey = request.createCacheKey();
                spiceManager.execute(request, loginRequestCacheKey,
                        DurationInMillis.ALWAYS_EXPIRED,
                        new LoginRequestListener());

                break;
            case VERIFYING:
                Log.d(TAG, "Attempt to do verification. Already verifying.");
                updateUI();
                break;
            case VERIFIED_OK:
                Log.d(TAG, "Attempt to do verification. Verified ok.");
                updateUI();
                break;
            case VERIFIED_NOT_OK:
                Log.d(TAG, "Attempt to do verification. Verified not ok.");
                updateUI();
                break;
            case FAILED:
                Log.d(TAG, "Attempt to do verification. Failed.");
                updateUI();
                break;
        }
    }

    private void updateUI(){
        switch (mState) {
            case NOT_LOGIN:
                Log.d(TAG, "Update UI. Not logged in.");
                showFacebookButton(true);
                showVerifyingView(false);
                break;
            case FACEBOOK:
                Log.d(TAG, "Update UI. Facebook was logged in.");
                showFacebookButton(false);
                showVerifyingView(false);
                break;
            case VERIFYING:
                Log.d(TAG, "Update UI. Already verifying.");
                showFacebookButton(false);
                showLoadingWheel(true);
                showResponseText(R.string.login_activity_login_wait);
                showCancelButton(true);
                showVerifyingView(true);
                break;
            case VERIFIED_OK:
                Log.d(TAG, "Update UI. Verified ok.");
                showFacebookButton(false);
                showLoadingWheel(false);
                showResponseText(R.string.login_activity_login_wait);
                showCancelButton(true);
                showVerifyingView(false);
                break;
            case VERIFIED_NOT_OK:
                Log.d(TAG, "Update UI. Verified not ok.");
                showFacebookButton(false);
                showLoadingWheel(false);
                showResponseText(R.string.login_activity_login_fail);
                showCancelButton(false);
                showVerifyingView(true);
                break;
            case FAILED:
                Log.d(TAG, "Update UI. Failed.");
                showFacebookButton(false);
                showLoadingWheel(false);
                showResponseText(R.string.login_activity_login_error);
                showCancelButton(false);
                showVerifyingView(true);
                break;
        }
    }

    private void showFacebookButton(boolean show){
        loginButton.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showVerifyingView(boolean show){
        mVerifyingView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showLoadingWheel(boolean show){
        mLoadingWheel.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    private void showResponseText(boolean show){
        mResponseText.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showResponseText(int textResource){
        ((TextView)mResponseText).setText(textResource);
        mResponseText.setVisibility(View.VISIBLE);
    }

    private void showCancelButton(boolean show){
        mCancelButton.setVisibility(show ? View.VISIBLE : View.GONE);
        mRetryButton.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    public void retry(View view) {
        LoginManager.getInstance().logOut();
        mState = LoginState.NOT_LOGIN;

        updateUI();
    }

    public void cancel(View view){
        if(loginRequestCacheKey != null){
            Log.d(TAG, "Pressed cancel. Cancelling loginRequest: "+loginRequestCacheKey);
            spiceManager.cancel(Session.class, loginRequestCacheKey);
            loginRequestCacheKey = null;
        }
        LoginManager.getInstance().logOut();
        mState = LoginState.NOT_LOGIN;

        updateUI();
    }

    private void launchHelloActivity(){
        Log.d(TAG, "Launching hello activity.");
        Intent intent = new Intent(getApplicationContext(), HelloActivity.class);
        startActivity(intent);
    }

}

