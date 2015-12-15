package com.nusclimb.live.crimp.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
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
import com.nusclimb.live.crimp.common.User;
import com.nusclimb.live.crimp.common.json.LoginResponseBody;
import com.nusclimb.live.crimp.common.spicerequest.LoginRequest;
import com.nusclimb.live.crimp.hello.HelloActivity;
import com.nusclimb.live.crimp.service.CrimpService;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

/**
 * Login activity of CRIMP. This activity will handle login to facebook
 * and CRIMP server. There are 4 states corresponding to stages in the
 * login process.
 *
 * NOT_LOGIN(0):        Not login to facebook. Not login to CRIMP server.
 * IN_VERIFYING(1):     Trying to login to CRIMP server. After successfully return from
 *                      facebook login, a request is immediately send to CRIMP server. This
 *                      state waits for reply from CRIMP server.
 * VERIFIED_OK(2):      CRIMP server reply ok.
 * VERIFIED_FAILED(3):  No response or unknown response from CRIMP server.
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class LoginActivity extends Activity {
    private final String TAG = LoginActivity.class.getSimpleName();

    // Various state of activity.
    private enum State {
        NOT_LOGIN(0),               // Not login to facebook. Not login to CRIMP server.
        IN_VERIFYING(1),            // Trying to login to CRIMP server.
        VERIFIED_OK(2),             // CRIMP server reply ok.
        VERIFIED_FAILED(3);         // No response or unknown response from CRIMP server.

        private final int value;

        State(int value){
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static State toEnum(int i){
            switch(i){
                case 0:
                    return NOT_LOGIN;
                case 1:
                    return IN_VERIFYING;
                case 2:
                    return VERIFIED_OK;
                case 3:
                    return VERIFIED_FAILED;
                default:
                    return null;
            }
        }
    }

    private SpiceManager spiceManager = new SpiceManager(CrimpService.class);
    private User mUser = null;
    private State mState;

    // UI references.
    private View mViewVerifying;
    private View mViewLoadingWheel;
    private View mViewResponseText;
    private View mViewCancelButton;
    private View mViewRetryButton;
    private LoginButton mViewLoginButton;

    // Facebook references.
    private CallbackManager callbackManager;
    AccessTokenTracker mAccessTokenTracker;
    ProfileTracker mProfileTracker;

    /*=========================================================================
     * Inner class
     *=======================================================================*/
    /**
     * RequestListener for receiving response of login request.
     *
     * @author Lin Weizhi (ecc.weizhi@gmail.com)
     */
    private class LoginRequestListener implements RequestListener<LoginResponseBody> {
        private final String TAG = LoginRequestListener.class.getSimpleName();

        @Override
        public void onRequestFailure(SpiceException e) {
            Log.w(TAG, "Login request fail. mState: " + mState + ", SpiceException: " + e.toString());

            // No-op if mState != IN_VERIFYING
            if(mState == State.IN_VERIFYING) {
                changeState(State.VERIFIED_FAILED);
            }
        }

        @Override
        public void onRequestSuccess(LoginResponseBody result) {
            Log.i(TAG, "Login request succeed. mState: " + mState + ", LoginResponse: " + result.toString());

            if(mState == State.IN_VERIFYING){
                // Extract response.
                mUser.setUserId(result.getxUserId());
                mUser.setAuthToken(result.getxAuthToken());

                changeState(State.VERIFIED_OK);
            }
        }
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

    private void showResponseText(int textResource){
        ((TextView) mViewResponseText).setText(textResource);
        mViewResponseText.setVisibility(View.VISIBLE);
    }

    private void showResponseText(String textResource){
        ((TextView) mViewResponseText).setText(textResource);
        mViewResponseText.setVisibility(View.VISIBLE);
    }

    private void showCancelButton(boolean show){
        mViewCancelButton.setVisibility(show ? View.VISIBLE : View.GONE);
        mViewRetryButton.setVisibility(show ? View.GONE : View.VISIBLE);
    }



    /*=========================================================================
     * Button methods and facebook login callback
     *=======================================================================*/
    /**
     * This method is called when user clicked on retry button.
     * Change state to NOT_LOGIN.
     *
     * @param view Button view object
     */
    public void retry(View view) {
        changeState(State.NOT_LOGIN);
    }

    /**
     * This method is called when user clicked on cancel button.
     * Change state to NOT_LOGIN.
     *
     * @param view Button view object.
     */
    public void cancel(View view){
        changeState(State.NOT_LOGIN);
    }

    // Facebook stuff
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "Facebook login returned. resultCode: " + resultCode + ", mState: " + mState);

        if(resultCode == RESULT_OK) {
            changeState(State.IN_VERIFYING);
        }
        else{
            changeState(State.NOT_LOGIN);
        }
    }



    /*=========================================================================
     * Main flow methods.
     *=======================================================================*/
    /**
     * Method to control which UI element is visible at different state.
     */
    private void updateUI(){
        String responseText;

        switch (mState) {
            case NOT_LOGIN:
                showFacebookButton(true);
                showVerifyingView(false);
                break;
            case IN_VERIFYING:
                showFacebookButton(false);
                showLoadingWheel(true);
                showResponseText(R.string.login_activity_login_wait);
                showCancelButton(true);
                showVerifyingView(true);
                break;
            case VERIFIED_OK:
                showFacebookButton(false);
                showVerifyingView(false);
                break;
            case VERIFIED_FAILED:
                showFacebookButton(false);
                showLoadingWheel(false);
                responseText = getString(R.string.login_activity_login_hey)+
                        mUser.getUserName()+
                        getString(R.string.login_activity_login_exclamation)+
                        getString(R.string.login_activity_verification_error);
                showResponseText(responseText);
                showCancelButton(false);
                showVerifyingView(true);
                break;
        }
    }

    /**
     * Method to control what is performed at different state.
     */
    private void doVerification(){
        switch (mState){
            case NOT_LOGIN:
                // Try to logout of facebook
                LoginManager.getInstance().logOut();
                mUser.clearAll();
                break;
            case IN_VERIFYING:
                if(AccessToken.getCurrentAccessToken() == null){
                    // AccessToken not ready yet. Set up tracker to wait for access token.
                    if(mAccessTokenTracker == null) {
                        mAccessTokenTracker = new AccessTokenTracker() {
                            @Override
                            protected void onCurrentAccessTokenChanged(AccessToken accessToken, AccessToken accessToken1) {
                                stopTracking();
                                Log.v(TAG, "TOKEN CHANGED. NEW TOKEN=" + accessToken1.getToken());
                                changeState(State.IN_VERIFYING);
                            }
                        };
                    }
                    if(!mAccessTokenTracker.isTracking()){
                        mAccessTokenTracker.startTracking();
                        Log.v(TAG, "TOKEN NOT READY. WE START TRACKING");
                    }
                    break;
                }

                if(Profile.getCurrentProfile() == null){
                    //Profile not ready yet. Set up tracker to wait for profile.
                    if(mProfileTracker == null){
                        mProfileTracker = new ProfileTracker() {
                            @Override
                            protected void onCurrentProfileChanged(Profile profile, Profile profile1) {
                                stopTracking();
                                Log.v(TAG, "PROFILE CHANGED. NEW PROFILE="+profile1.getName());
                                changeState(State.IN_VERIFYING);
                            }
                        };
                    }
                    if(!mProfileTracker.isTracking()){
                        mProfileTracker.startTracking();
                        Log.v(TAG, "PROFILE NOT READY. WE START TRACKING");
                    }
                    break;
                }

                Log.v(TAG, "IN_VERIFYING. NAME="+Profile.getCurrentProfile().getName()
                        +" TOKEN="+AccessToken.getCurrentAccessToken().getToken());
                mUser.setFacebookAccessToken(AccessToken.getCurrentAccessToken().getToken());
                mUser.setUserName(Profile.getCurrentProfile().getName());
                LoginRequest mLoginRequest = new LoginRequest(mUser.getFacebookAccessToken(), this);
                spiceManager.execute(mLoginRequest, new LoginRequestListener());
                break;
            case VERIFIED_OK:
                launchHelloActivity();
                break;
            case VERIFIED_FAILED:
                break;
        }
    }

    /**
     * Set {@code mState} to {@code state}. Changes to {@code mState} must
     * go through this method. Perform a call to {@code updateUI()} and {@code doVerification()}.
     *
     * @param state Login state to set {@code mState} to.
     */
    private void changeState(State state){
        Log.v(TAG, "Change state. "+mState+"->"+state);

        mState = state;
        updateUI();
        doVerification();
    }

    /**
     * Method to launch the next activity.
     */
    private void launchHelloActivity(){
        Log.v(TAG, "Launching HelloActivity. userId="+mUser.getUserId()
        +" authToken="+mUser.getAuthToken()+" userName="+ mUser.getUserName()
        +" accessToken="+mUser.getFacebookAccessToken());

        Bundle mBundle = new Bundle();
        mBundle.putString(getString(R.string.bundle_x_user_id), mUser.getUserId());
        mBundle.putString(getString(R.string.bundle_x_auth_token), mUser.getAuthToken());
        mBundle.putString(getString(R.string.bundle_user_name), mUser.getUserName());
        mBundle.putString(getString(R.string.bundle_access_token), mUser.getFacebookAccessToken());

        Intent intent = new Intent(getApplicationContext(), HelloActivity.class);
        intent.putExtras(mBundle);

        finish();
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

        // Assign views to references
        mViewVerifying = findViewById(R.id.verifying_viewgroup);
        mViewLoginButton = (LoginButton) findViewById(R.id.login_button);
        mViewLoadingWheel = findViewById(R.id.loading_wheel);
        mViewResponseText = findViewById(R.id.response_text);
        mViewCancelButton = findViewById(R.id.login_cancel_button);
        mViewRetryButton = findViewById(R.id.login_retry_button);

        // Setting up Facebook login button stuff.
        callbackManager = CallbackManager.Factory.create();
        mViewLoginButton.setReadPermissions("public_profile");
        mViewLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            private final String TAG = FacebookCallback.class.getSimpleName();

            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.i(TAG, "Facebook login succeeded.");
            }

            @Override
            public void onCancel() {
                Log.v(TAG, "Facebook login cancelled.");
            }

            @Override
            public void onError(FacebookException exception) {
                Log.e(TAG, "Facebook login error.");
            }
        });

        if(savedInstanceState != null){
            mState = State.toEnum(savedInstanceState.getInt(
                    getString(R.string.bundle_login_state)));

            if(mUser == null)
                mUser = new User();
            mUser.setUserId(savedInstanceState.getString(
                    getString(R.string.bundle_x_user_id)));
            mUser.setAuthToken(savedInstanceState.getString(
                    getString(R.string.bundle_x_auth_token)));
            mUser.setUserName(savedInstanceState.getString(
                    getString(R.string.bundle_user_name)));
            mUser.setFacebookAccessToken(savedInstanceState.getString(
                    getString(R.string.bundle_access_token)));
        }
        else{
            mState = State.NOT_LOGIN;
            if(mUser == null)
                mUser = new User();
        }
        Log.v(TAG, "mState at end of onCreate:" + mState);
    }

    @Override
    protected void onStart(){
        super.onStart();
        spiceManager.start(this);
    }

    @Override
    protected void onResume(){
        super.onResume();
        changeState(mState);
    }

    @Override
    protected void onStop(){
        spiceManager.shouldStop();
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState (Bundle outState){
        super.onSaveInstanceState(outState);

        outState.putInt(getString(R.string.bundle_login_state), mState.getValue());
        outState.putString(getString(R.string.bundle_x_user_id), mUser.getUserId());
        outState.putString(getString(R.string.bundle_x_auth_token), mUser.getAuthToken());
        outState.putString(getString(R.string.bundle_access_token), mUser.getFacebookAccessToken());
        outState.putString(getString(R.string.bundle_user_name), mUser.getUserName());

        Log.v(TAG, "mState at end of onSaveInstanceState:" + mState);
    }
}