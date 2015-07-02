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
import com.nusclimb.live.crimp.common.json.CategoriesResponse;
import com.nusclimb.live.crimp.common.json.Category;
import com.nusclimb.live.crimp.common.json.LoginResponse;
import com.nusclimb.live.crimp.common.spicerequest.CategoriesRequest;
import com.nusclimb.live.crimp.common.spicerequest.LoginRequest;
import com.nusclimb.live.crimp.hello.HelloActivity;
import com.nusclimb.live.crimp.service.CrimpService;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Login activity of CRIMP.
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class LoginActivity extends Activity {
    private final String TAG = LoginActivity.class.getSimpleName();

    // Various state of activity.
    private enum LoginState {
        NOT_LOGIN,              // Not login to facebook. Not login to CRIMP server.
        IN_FACEBOOK,            // In facebook login activity.
        FACEBOOK_OK,            // Successfully login to facebook.
        IN_VERIFYING,           // Trying to login to CRIMP server.
        VERIFIED_OK,            // CRIMP server reply ok.
        VERIFIED_NOT_OK,        // CRIMP server reject user.
        VERIFIED_FAILED,        // No response or unknown response from CRIMP server.

        IN_REQUEST_CATEGORIES,  // Requesting list of routes from CRIMP server.
        CATEGORIES_OK,          // Received list of routes from CRIMP server correctly.
        CATEGORIES_NOT_OK,      // Rejected by CRIMP server when requesting route list.
        CATEGORIES_FAILED       // No response or unknown response from CRIMP server.
    }

    // Stuff for communicating with CRIMP server
    private String loginRequestCacheKey;
    private String categoriesRequestCacheKey;
    private SpiceManager spiceManager = new SpiceManager(
            CrimpService.class);
    private String xUserId;
    private String xAuthToken;
    private List<String> roles;
    private List<String> categoryIdList = new ArrayList<String>();
    private List<String> categoryNameList = new ArrayList<String>();
    private List<Integer> categoryRouteCountList = new ArrayList<Integer>();

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

            if(mState == LoginState.IN_VERIFYING)
                changeState(LoginState.VERIFIED_FAILED);
        }

        @Override
        public void onRequestSuccess(LoginResponse result) {
            Log.d(TAG, "LoginRequestListener request succeed. result="+result.toString());

            loginRequestCacheKey = null;
            xUserId = result.getxUserId();
            xAuthToken = result.getxAuthToken();
            roles = result.getRoles();

            // Verify response.
            boolean isContainJudgeOrAbove = false;
            if(roles!=null){
                int i = 0;
                while(i<roles.size() && !isContainJudgeOrAbove){
                    isContainJudgeOrAbove = roles.get(i).equalsIgnoreCase("judge");
                    i++;
                }
                i=0;
                while(i<roles.size() && !isContainJudgeOrAbove){
                    isContainJudgeOrAbove = roles.get(i).equalsIgnoreCase("admin");
                    i++;
                }
                i=0;
                while(i<roles.size() && !isContainJudgeOrAbove){
                    isContainJudgeOrAbove = roles.get(i).equalsIgnoreCase("hukkataival");
                    i++;
                }
            }

            // roles need to contain "judge" to be verified ok.
            if(isContainJudgeOrAbove) {
                Log.d(TAG, "Roles contain judge or above. mState="+mState);
                if (mState == LoginState.IN_VERIFYING)
                    changeState(LoginState.VERIFIED_OK);
            }
            else{
                Log.d(TAG, "Roles don't contain judge or above. mState="+mState);
                if (mState == LoginState.IN_VERIFYING)
                    changeState(LoginState.VERIFIED_NOT_OK);
            }
        }
    }

    private class CategoriesRequestListener implements RequestListener<CategoriesResponse> {
        @Override
        public void onRequestFailure(SpiceException e) {
            Log.d(TAG, "CategoriesRequestListener request fail.");

            categoriesRequestCacheKey = null;

            if(mState == LoginState.IN_REQUEST_CATEGORIES)
                changeState(LoginState.CATEGORIES_FAILED);
        }

        @Override
        public void onRequestSuccess(CategoriesResponse result) {
            Log.d(TAG, "CategoriesRequestListener request succeed.");

            categoriesRequestCacheKey = null;

            categoryIdList.clear();
            categoryNameList.clear();
            categoryRouteCountList.clear();

            // parse response.
            for(Category c:result){
                categoryIdList.add(c.getCategoryId());
                categoryNameList.add(c.getCategoryName());
                categoryRouteCountList.add(c.getRouteCount());
            }

            if (mState == LoginState.IN_REQUEST_CATEGORIES)
                changeState(LoginState.CATEGORIES_OK);
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

    private void showResponseText(boolean show){
        mViewResponseText.setVisibility(show ? View.VISIBLE : View.GONE);
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

    /**
     * Method to control which UI element is visible at different state.
     */
    private void updateUI(){
        Log.d(TAG, "Update UI. mState:" + mState);
        String responseText;

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
                showFacebookButton(false);
                showLoadingWheel(false);
                responseText = getString(R.string.login_activity_login_hey)+
                        Profile.getCurrentProfile().getName()+
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
                        Profile.getCurrentProfile().getName()+
                        getString(R.string.login_activity_login_exclamation)+
                        getString(R.string.login_activity_verification_error);
                showResponseText(responseText);
                showCancelButton(false);
                showVerifyingView(true);
                break;

            case IN_REQUEST_CATEGORIES:
                showFacebookButton(false);
                showLoadingWheel(true);
                showResponseText(R.string.login_activity_categories_wait);
                showCancelButton(true);
                showVerifyingView(true);
                break;
            case CATEGORIES_OK:
                break;
            case CATEGORIES_NOT_OK:showFacebookButton(false);
                showLoadingWheel(false);
                responseText = getString(R.string.login_activity_login_hey)+
                        Profile.getCurrentProfile().getName()+
                        getString(R.string.login_activity_login_exclamation)+
                        getString(R.string.login_activity_categories_fail);
                showResponseText(responseText);
                showCancelButton(false);
                showVerifyingView(true);
                break;
            case CATEGORIES_FAILED:
                showFacebookButton(false);
                showLoadingWheel(false);
                responseText = getString(R.string.login_activity_login_hey)+
                        Profile.getCurrentProfile().getName()+
                        getString(R.string.login_activity_login_exclamation)+
                        getString(R.string.login_activity_categories_error);
                showResponseText(responseText);
                showCancelButton(false);
                showVerifyingView(true);
                break;
        }
    }


    // Facebook stuff
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
                if(loginRequestCacheKey==null) {
                    LoginRequest mLoginRequest = new LoginRequest(AccessToken.getCurrentAccessToken().getToken(),
                            String.valueOf(AccessToken.getCurrentAccessToken().getExpires().getTime()),
                            AccessToken.getCurrentAccessToken().getUserId(),
                            this);

                    loginRequestCacheKey = mLoginRequest.createCacheKey();
                    spiceManager.execute(mLoginRequest, loginRequestCacheKey,
                            DurationInMillis.ALWAYS_EXPIRED,
                            new LoginRequestListener());
                }
                break;
            case VERIFIED_OK:
                changeState(LoginState.IN_REQUEST_CATEGORIES);
                break;
            case VERIFIED_NOT_OK:
                break;
            case VERIFIED_FAILED:
                break;

            case IN_REQUEST_CATEGORIES:
                if(categoriesRequestCacheKey==null) {
                    CategoriesRequest mCategoriesRequest = new CategoriesRequest(xUserId, xAuthToken, this);
                    categoriesRequestCacheKey = mCategoriesRequest.createCacheKey();
                    spiceManager.execute(mCategoriesRequest, categoriesRequestCacheKey,
                            DurationInMillis.ALWAYS_EXPIRED,
                            new CategoriesRequestListener());
                }
                break;
            case CATEGORIES_OK:
                launchHelloActivity();
                break;
            case CATEGORIES_NOT_OK:
                break;
            case CATEGORIES_FAILED:
                break;
        }
    }

    /**
     * Set {@code mState} to {@code state}. Changes to {@code mState} must
     * go through this method.
     *
     * @param state Login state to set {@code mState} to.
     */
    private void changeState(LoginState state){
        Log.d(TAG, "Change state: "+mState+"->"+state);

        mState = state;
        updateUI();
        doVerification();
    }

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

        if(categoriesRequestCacheKey!=null) {
            Log.d(TAG, "Pressed cancel. mState:" + mState+". Cancel request: "+categoriesRequestCacheKey);
        }
        else{
            Log.d(TAG, "Pressed cancel. mState:" + mState);
        }
        if(categoriesRequestCacheKey != null){
            spiceManager.cancel(CategoriesResponse.class, categoriesRequestCacheKey);
            categoriesRequestCacheKey = null;
        }

        LoginManager.getInstance().logOut();

        changeState(LoginState.NOT_LOGIN);
    }

    private void launchHelloActivity(){
        Log.d(TAG, "Launching hello activity. mState: " + mState);
        Intent intent = new Intent(getApplicationContext(), HelloActivity.class);
        intent.putExtra(getString(R.string.package_name) + getString(R.string.login_activity_intent_xUserId), xUserId);
        intent.putExtra(getString(R.string.package_name) + getString(R.string.login_activity_intent_xAuthToken), xAuthToken);
        String[] categoryIdListAsArray = categoryIdList.toArray(new String[categoryIdList.size()]);
        intent.putExtra(getString(R.string.package_name) + getString(R.string.login_activity_intent_categoryIdList), categoryIdListAsArray);
        String[] categoryNameListAsArray = categoryNameList.toArray(new String[categoryNameList.size()]);
        intent.putExtra(getString(R.string.package_name) + getString(R.string.login_activity_intent_categoryNameList), categoryNameListAsArray);
        int[] categoryRouteCountListAsArray = new int[categoryRouteCountList.size()];
        for(int i=0; i<categoryRouteCountList.size(); i++){
            categoryRouteCountListAsArray[i] = categoryRouteCountList.get(i);
        }
        intent.putExtra(getString(R.string.package_name) + getString(R.string.login_activity_intent_categoryRouteCountList), categoryRouteCountListAsArray);
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

        if(categoriesRequestCacheKey != null) {
            Log.d(TAG, "In onPause(). mState: " + mState+". Cancelling categoriesRequest: "+categoriesRequestCacheKey);
        }
        else{
            Log.d(TAG, "In onPause(). mState: " + mState);
        }

        if(categoriesRequestCacheKey != null){
            spiceManager.cancel(LoginResponse.class, categoriesRequestCacheKey);
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

