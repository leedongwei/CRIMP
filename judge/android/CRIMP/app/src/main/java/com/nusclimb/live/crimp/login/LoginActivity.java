package com.nusclimb.live.crimp.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
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
 * Login activity of CRIMP.
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class LoginActivity extends Activity {
    private final String TAG = LoginActivity.class.getSimpleName();

    // Various state of activity.
    private enum State {
        NOT_LOGIN(0),               // Not login to facebook. Not login to CRIMP server.
        IN_FACEBOOK(1),             // In facebook login activity.
        FACEBOOK_OK(2),             // Successfully login to facebook.
        FACEBOOK_NOT_OK(3),         // Fail to login to facebook.
        IN_VERIFYING(4),            // Trying to login to CRIMP server.
        VERIFIED_OK(5),             // CRIMP server reply ok.
        VERIFIED_NOT_OK(6),         // CRIMP server reject user.
        VERIFIED_FAILED(7);         // No response or unknown response from CRIMP server.

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
                    return IN_FACEBOOK;
                case 2:
                    return FACEBOOK_OK;
                case 3:
                    return FACEBOOK_NOT_OK;
                case 4:
                    return IN_VERIFYING;
                case 5:
                    return VERIFIED_OK;
                case 6:
                    return VERIFIED_NOT_OK;
                case 7:
                    return VERIFIED_FAILED;
                default:
                    return null;
            }
        }
    }

    private SpiceManager spiceManager = new SpiceManager(CrimpService.class);
    private User mUser = new User();
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

            // TODO do something about the exception

            // No-op if mState != IN_VERIFYING
            if(mState == State.IN_VERIFYING) {
                changeState(State.VERIFIED_FAILED);
            }
            else{
                return;
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
            else{
                return;
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
     * This method is called when user clicked on retry button. Log out from
     * facebook (if user is not already logged out). Change state to NOT_LOGIN.
     *
     * @param view Button view object
     */
    public void retry(View view) {
        // TODO i should cancel all pending request and wait for the current midflight request to return

        LoginManager.getInstance().logOut();
        changeState(State.NOT_LOGIN);
    }

    /**
     * This method is called when user clicked on cancel button. Log out from
     * facebook (if user is not already logged out). Change state to NOT_LOGIN.
     *
     * @param view Button view object.
     */
    public void cancel(View view){
        // TODO i should cancel all pending request and wait for the current midflight request to return
        LoginManager.getInstance().logOut();
        changeState(State.NOT_LOGIN);
    }

    // Facebook stuff
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "Facebook login result. resultCode: " + resultCode + ", mState: " + mState);

        if(resultCode == RESULT_OK) {
            mUser.setFacebookAccessToken(AccessToken.getCurrentAccessToken().getToken());
            mUser.setUserName(Profile.getCurrentProfile().getName());
            changeState(State.FACEBOOK_OK);
        }
        else{
            changeState(State.FACEBOOK_NOT_OK);
        }
    }



    /*=========================================================================
     * Main flow methods.
     *=======================================================================*/
    /**
     * Method to control which UI element is visible at different state.
     */
    private void updateUI(){
        Log.v(TAG, "Update UI. mState: " + mState);
        String responseText;

        switch (mState) {
            case NOT_LOGIN:
                showFacebookButton(true);
                showVerifyingView(false);
                break;
            case IN_FACEBOOK:
                showFacebookButton(false);
                showVerifyingView(false);
                break;
            case FACEBOOK_OK:
                showFacebookButton(false);
                showVerifyingView(false);
                break;
            case FACEBOOK_NOT_OK:   // TODO should provide feedback to user
                showFacebookButton(false);
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
            case VERIFIED_NOT_OK:   // TODO use spiceexception for error message
                showFacebookButton(false);
                showLoadingWheel(false);
                responseText = getString(R.string.login_activity_login_hey)+
                        mUser.getUserName()+
                        getString(R.string.login_activity_login_exclamation)+
                        getString(R.string.login_activity_verification_fail);
                showResponseText(responseText);
                showCancelButton(false);
                showVerifyingView(true);
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
        Log.v(TAG + ".doVerification()", "mState:" + mState);
        switch (mState){
            case NOT_LOGIN:
                mUser.clearAll();
                break;
            case IN_FACEBOOK:
                break;
            case FACEBOOK_OK:
                mUser.clearAll();
                mUser.setUserName(Profile.getCurrentProfile().getName());
                mUser.setFacebookAccessToken(AccessToken.getCurrentAccessToken().getToken());
                changeState(State.IN_VERIFYING);
                break;
            case FACEBOOK_NOT_OK:
                changeState(State.NOT_LOGIN);
                break;
            case IN_VERIFYING:
                LoginRequest mLoginRequest = new LoginRequest(mUser.getFacebookAccessToken(), this);
                spiceManager.execute(mLoginRequest, new LoginRequestListener());
                break;
            case VERIFIED_OK:
                launchHelloActivity();
                break;
            case VERIFIED_NOT_OK:
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
            switch (mState) {
                // Deliberate fall through
                case VERIFIED_OK:
                    mUser.setUserId(savedInstanceState.getString(
                            getString(R.string.bundle_x_user_id)));
                    mUser.setAuthToken(savedInstanceState.getString(
                            getString(R.string.bundle_x_auth_token)));
                case VERIFIED_NOT_OK:
                case VERIFIED_FAILED:
                case IN_VERIFYING:
                case FACEBOOK_OK:
                    mUser.setUserName(savedInstanceState.getString(
                            getString(R.string.bundle_user_name)));
                    mUser.setFacebookAccessToken(savedInstanceState.getString(
                            getString(R.string.bundle_access_token)));
                    break;

                default:
                    break;
            }
        }
        else{
            mState = State.NOT_LOGIN;
            mUser.clearAll();
        }
    }

    @Override
    protected void onStart(){
        super.onStart();
        spiceManager.start(this);
    }

    @Override
    protected void onResume(){
        super.onResume();
        switch(mState){
            case VERIFIED_OK:
                changeState(State.VERIFIED_OK);
                break;
            case VERIFIED_NOT_OK:
            case VERIFIED_FAILED:
            case IN_VERIFYING:
            case FACEBOOK_OK:
                changeState(State.FACEBOOK_OK);
                break;
            case FACEBOOK_NOT_OK:
            case IN_FACEBOOK:
            case NOT_LOGIN:
                changeState(State.NOT_LOGIN);
        }
    }

    @Override
    protected void onStop(){
        spiceManager.shouldStop();
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState (Bundle outState){
        super.onSaveInstanceState(outState);

        switch (mState){
            case VERIFIED_OK:
                outState.putString(getString(R.string.bundle_x_user_id), mUser.getUserId());
                outState.putString(getString(R.string.bundle_x_auth_token), mUser.getAuthToken());
            case IN_VERIFYING:
            case VERIFIED_FAILED:
            case VERIFIED_NOT_OK:
            case FACEBOOK_OK:
                outState.putString(getString(R.string.bundle_access_token), mUser.getFacebookAccessToken());
                outState.putString(getString(R.string.bundle_user_name), mUser.getUserName());
            case NOT_LOGIN:
            case IN_FACEBOOK:
            case FACEBOOK_NOT_OK:
                outState.putInt(getString(R.string.bundle_login_state), mState.getValue());
                break;
        }
    }
}