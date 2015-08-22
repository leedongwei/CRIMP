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
        FACEBOOK_OK_STAY,       // Successfully login to facebook. For use with onPause().
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

    // RoboSpice stuff
    private SpiceManager spiceManager = new SpiceManager(
            CrimpService.class);

    // Stuff for communicating with CRIMP server
    private String xUserId;
    private String xAuthToken;
    private List<String> roles;
    private List<String> categoryIdList = new ArrayList<String>();
    private List<String> categoryNameList = new ArrayList<String>();
    private List<Integer> categoryRouteCountList = new ArrayList<Integer>();

    // Activity state
    private LoginState mState;

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
        private final String TAG = LoginRequestListener.class.getSimpleName();

        @Override
        public void onRequestFailure(SpiceException e) {
            Log.d(TAG+".onRequestFailure()", "mState="+mState);

            // We only changeState if mState == IN_VERIFYING
            if(mState == LoginState.IN_VERIFYING)
                changeState(LoginState.VERIFIED_FAILED);
        }

        @Override
        public void onRequestSuccess(LoginResponse result) {
            Log.d(TAG+".onRequestSuccess()", "mState="+mState+" LoginResponse="+result.toString());

            if (mState != LoginState.IN_VERIFYING) {
                Log.w(TAG+".onRequestSuccess()", "mState !=IN_VERIFYING. Not doing anything.");
                return;
            }

            // Extract response.
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
                Log.d(TAG+".onRequestSuccess()", "Roles contain judge or above. "
                        +mState+" -> "+LoginState.VERIFIED_OK);
                changeState(LoginState.VERIFIED_OK);
            }
            else{
                Log.d(TAG+".onRequestSuccess()", "Roles don't contain judge or above. "
                        +mState+" -> "+LoginState.VERIFIED_NOT_OK);
                changeState(LoginState.VERIFIED_NOT_OK);
            }
        }
    }

    /**
     * RequestListener for receiving response of categories request.
     *
     * @author Lin Weizhi (ecc.weizhi@gmail.com)
     */
    private class CategoriesRequestListener implements RequestListener<CategoriesResponse> {
        private final String TAG = CategoriesRequestListener.class.getSimpleName();

        @Override
        public void onRequestFailure(SpiceException e) {
            Log.d(TAG+".onRequestFailure()", "mState="+mState);

            // We only changeState if mState == IN_REQUEST_CATEGORIES.
            if(mState == LoginState.IN_REQUEST_CATEGORIES)
                changeState(LoginState.CATEGORIES_FAILED);
        }

        @Override
        public void onRequestSuccess(CategoriesResponse result) {
            Log.d(TAG + ".onRequestSuccess()", "mState="+mState+" CategoriesResponse=" + result.toString());

            if (mState == LoginState.IN_REQUEST_CATEGORIES) {
                categoryIdList.clear();
                categoryNameList.clear();
                categoryRouteCountList.clear();

                // parse response.
                for (Category c : result) {
                    categoryIdList.add(c.getCategoryId());
                    categoryNameList.add(c.getCategoryName());
                    categoryRouteCountList.add(c.getRouteCount());
                }

                changeState(LoginState.CATEGORIES_OK);
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
        Log.v(TAG+".retry()", "mState: "+mState);
        LoginManager.getInstance().logOut();
        changeState(LoginState.NOT_LOGIN);
    }

    /**
     * This method is called when user clicked on cancel button. Log out from
     * facebook (if user is not already logged out). Change state to NOT_LOGIN.
     *
     * @param view Button view object.
     */
    public void cancel(View view){
        Log.v(TAG + ".cancel()", "mState: " + mState);
        LoginManager.getInstance().logOut();
        changeState(LoginState.NOT_LOGIN);
    }

    // Facebook stuff
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        Log.v(TAG + ".onActivityResult()", "mState: " + mState + "; resultcode: " + resultCode + "; ok is " + RESULT_OK);

        if(resultCode == RESULT_OK)
            changeState(LoginState.FACEBOOK_OK);
    }



    /*=========================================================================
     * Main flow methods.
     *=======================================================================*/
    /**
     * Method to control which UI element is visible at different state.
     */
    private void updateUI(){
        Log.v(TAG + ".updateUI()", "mState:" + mState);
        String responseText;

        switch (mState) {
            case NOT_LOGIN:
                showFacebookButton(true);
                showVerifyingView(false);
                break;
            case IN_FACEBOOK:
                break;
            case FACEBOOK_OK_STAY:
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

    /**
     * Method to control what is performed at different state.
     */
    private void doVerification(){
        Log.v(TAG + ".doVerification()", "mState:" + mState);
        switch (mState){
            case NOT_LOGIN:
                xUserId = null;
                xAuthToken = null;
                categoryIdList.clear();
                categoryNameList.clear();
                categoryRouteCountList.clear();
                break;
            case IN_FACEBOOK:
                break;
            case FACEBOOK_OK_STAY:
                break;
            case FACEBOOK_OK:
                xUserId = null;
                xAuthToken = null;
                categoryIdList.clear();
                categoryNameList.clear();
                categoryRouteCountList.clear();
                changeState(LoginState.IN_VERIFYING);
                break;
            case IN_VERIFYING:
                LoginRequest mLoginRequest = new LoginRequest(AccessToken.getCurrentAccessToken().getToken(),
                        String.valueOf(AccessToken.getCurrentAccessToken().getExpires().getTime()),
                        AccessToken.getCurrentAccessToken().getUserId(),
                        this);

                spiceManager.execute(mLoginRequest, mLoginRequest.createCacheKey(),
                        DurationInMillis.ALWAYS_EXPIRED,
                        new LoginRequestListener());
                break;
            case VERIFIED_OK:
                if((xUserId == null) || (xAuthToken == null) ){
                    Log.w(TAG+".doVerification()", "xUserId/xAuthToken is null. mState: "+mState );
                    changeState(LoginState.FACEBOOK_OK);
                }
                else {
                    changeState(LoginState.IN_REQUEST_CATEGORIES);
                }
                break;
            case VERIFIED_NOT_OK:
                break;
            case VERIFIED_FAILED:
                break;

            case IN_REQUEST_CATEGORIES:
                CategoriesRequest mCategoriesRequest = new CategoriesRequest(xUserId, xAuthToken, this);
                spiceManager.execute(mCategoriesRequest, mCategoriesRequest.createCacheKey(),
                        DurationInMillis.ALWAYS_EXPIRED,
                        new CategoriesRequestListener());
                break;
            case CATEGORIES_OK:
                if((xUserId == null) || (xAuthToken == null) || (categoryIdList.size() == 0) ||
                        (categoryNameList.size() == 0) || (categoryRouteCountList.size() == 0) ){
                    Log.w(TAG+".doVerification()", "xUserId/xAuthToken is null or category is empty. mState: "+mState );
                    changeState(LoginState.FACEBOOK_OK);
                }
                else {
                    launchHelloActivity();
                }
                break;
            case CATEGORIES_NOT_OK:
                break;
            case CATEGORIES_FAILED:
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
        Log.v(TAG+".changeState()", mState+"->"+state);

        mState = state;
        updateUI();
        doVerification();
    }

    /**
     * Method to launch the next activity.
     */
    private void launchHelloActivity(){
        // Prepare stuff to put in intent.
        String[] categoryIdListAsArray = categoryIdList.toArray(new String[categoryIdList.size()]);
        String[] categoryNameListAsArray = categoryNameList.toArray(new String[categoryNameList.size()]);
        int[] categoryRouteCountListAsArray = new int[categoryRouteCountList.size()];
        for(int i=0; i<categoryRouteCountList.size(); i++){
            categoryRouteCountListAsArray[i] = categoryRouteCountList.get(i);
        }

        if(xUserId!=null && xAuthToken!=null && categoryIdListAsArray.length>0 &&
                categoryIdListAsArray.length==categoryNameListAsArray.length &&
                categoryNameListAsArray.length==categoryRouteCountListAsArray.length) {
            // Putting stuff into bundle to put into intent.
            Intent intent = new Intent(getApplicationContext(), HelloActivity.class);

            Bundle mBundle = new Bundle();
            mBundle.putString(getString(R.string.bundle_x_user_id), xUserId);
            mBundle.putString(getString(R.string.bundle_x_auth_token), xAuthToken);
            mBundle.putStringArray(getString(R.string.bundle_category_id_list), categoryIdListAsArray);
            mBundle.putStringArray(getString(R.string.bundle_category_name_list), categoryNameListAsArray);
            mBundle.putIntArray(getString(R.string.bundle_category_route_count_list), categoryRouteCountListAsArray);

            intent.putExtras(mBundle);

            finish();
            Log.v(TAG + ".launchHelloActivity()", "mState: " + mState);
            startActivity(intent);
        }
        else{
            Log.e(TAG+".launchHelloActivity()", "Not all info present.");
        }
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
            private final String TAG = FacebookCallback.class.getSimpleName();

            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.v(TAG + ".onSuccess()", "Facebook login succeeded.");
            }

            @Override
            public void onCancel() {
                Log.v(TAG + ".onCancel()", "Facebook login cancelled.");
            }

            @Override
            public void onError(FacebookException exception) {
                Log.e(TAG + ".onError()", "Facebook login error.");
            }
        });
    }

    @Override
    protected void onStart(){
        super.onStart();
        spiceManager.start(this);

        // Initialize mState here.
        if(AccessToken.getCurrentAccessToken()==null){
            mState = LoginState.NOT_LOGIN;
            Log.v(TAG+".onStart()", "mState initialized to "+mState);
        }
        else{
            mState = LoginState.FACEBOOK_OK_STAY;
            Log.v(TAG+".onStart()", "mState initialized to "+mState);
        }
    }

    @Override
    protected void onRestart(){
        super.onRestart();
        Log.v(TAG + ".onRestart()", "mState:" + mState);
    }

    @Override
    protected void onResume(){
        super.onResume();
        // TODO should we cancel all request here?

        // There can only be 2 state possible here.
        // NOT_LOGIN or FACEBOOK_OK_STAY.
        if(mState == LoginState.NOT_LOGIN){
            // No-Op
        }

        if(mState == LoginState.FACEBOOK_OK_STAY){
            changeState(LoginState.FACEBOOK_OK);
        }
    }

    @Override
    protected void onPause(){
        super.onPause();

        // We are pausing. Disregard all pending request.
        // Set state to either NOT_LOGIN or FACEBOOK_OK_STAY.
        if(mState != LoginState.NOT_LOGIN && mState != LoginState.IN_FACEBOOK) {
            Log.v(TAG+".onPause()", mState+" -> "+LoginState.FACEBOOK_OK_STAY);
            changeState(LoginState.FACEBOOK_OK_STAY);
        }
        else{
            // No-Op
            Log.v(TAG+".onPause()", mState+" -> "+LoginState.NOT_LOGIN);
            changeState(LoginState.NOT_LOGIN);
        }
    }

    @Override
    protected void onStop(){
        Log.v(TAG+".onStop()", "mState:" + mState);
        spiceManager.shouldStop();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG+".onDestroy()", "mState:" + mState);
    }
}