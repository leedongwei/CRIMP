package com.nusclimb.live.crimp.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.nusclimb.live.crimp.CrimpApplication2;
import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.common.Action;
import com.nusclimb.live.crimp.common.dao.User;
import com.nusclimb.live.crimp.common.event.RequestFailed;
import com.nusclimb.live.crimp.common.event.RequestSucceed;
import com.nusclimb.live.crimp.hello.HelloActivity;
import com.nusclimb.live.crimp.network.model.LoginJs;
import com.nusclimb.live.crimp.servicehelper.ServiceHelper;
import com.squareup.otto.Subscribe;

import java.util.UUID;

import timber.log.Timber;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final String SAVE_UUID = "save_uuid";
    private static final String SAVE_USER = "save_user";

    private static final boolean DEBUG = true;

    private User mUser;
    private UUID txId;

    // UI references.
    private TextView mErrorMsg;
    private ProgressBar mLoadingWheel;
    private TextView mResponseText;
    private LoginButton mLoginButton;

    // Facebook references.
    private CallbackManager fbCallbackManager;

    private void showVerifyingUI(String responseText){
        if(DEBUG) Log.d(TAG, "showVerifyingUI");
        mErrorMsg.setVisibility(View.INVISIBLE);
        mLoadingWheel.setVisibility(View.VISIBLE);
        mResponseText.setText(responseText);
        mResponseText.setVisibility(View.VISIBLE);
        mLoginButton.setVisibility(View.GONE);
    }

    private void showErrorUI(String errorText){
        if(DEBUG) Log.d(TAG, "showErrorUI");
        mErrorMsg.setText(errorText);
        mErrorMsg.setVisibility(View.VISIBLE);
        mLoadingWheel.setVisibility(View.GONE);
        mResponseText.setVisibility(View.GONE);
        mLoginButton.setVisibility(View.VISIBLE);
    }

    private void showDefaultUI(){
        if(DEBUG) Log.d(TAG, "showDefaultUI");
        mErrorMsg.setVisibility(View.INVISIBLE);
        mLoadingWheel.setVisibility(View.GONE);
        mResponseText.setVisibility(View.GONE);
        mLoginButton.setVisibility(View.VISIBLE);
    }

    private void launchHelloActivity(){
        if(DEBUG) Log.d(TAG, "Launching HelloActivity");
        Intent intent = new Intent(getApplicationContext(), HelloActivity.class);
        intent.putExtra(HelloActivity.SAVE_USER, mUser);

        finish();
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);

        // Get reference for views
        mErrorMsg = (TextView) findViewById(R.id.error_msg);
        mLoadingWheel = (ProgressBar) findViewById(R.id.loading_wheel);
        mResponseText = (TextView) findViewById(R.id.response_text);
        mLoginButton = (LoginButton) findViewById(R.id.login_button);

        // Setting up Facebook login button stuff.
        fbCallbackManager = CallbackManager.Factory.create();
        mLoginButton.setReadPermissions("public_profile");
        mLoginButton.registerCallback(fbCallbackManager, new FacebookCallback<LoginResult>() {
            private final String TAG = FacebookCallback.class.getSimpleName();

            @Override
            public void onSuccess(LoginResult loginResult) {
                if(DEBUG){
                    AccessToken accessToken = loginResult.getAccessToken();
                    if(accessToken != null) {
                        Log.d(TAG, "Facebook login onSuccess with userId:"
                                + accessToken.getUserId());
                    }
                    else{
                        Log.d(TAG, "Facebook login onSuccess with null token");
                    }
                }
            }

            @Override
            public void onCancel() {
                if(DEBUG) Log.d(TAG, "Facebook login cancelled.");
            }

            @Override
            public void onError(FacebookException exception) {
                if(DEBUG) Log.d(TAG, "Facebook login error: " + exception.getMessage());
            }
        });

        // Restore state
        if(savedInstanceState != null){
            txId = (UUID) savedInstanceState.getSerializable(SAVE_UUID);
            mUser = (User) savedInstanceState.getSerializable(SAVE_USER);
        }
        else{
            mUser = new User();
        }
    }

    @Override
    protected void onStart(){
        super.onStart();
        CrimpApplication2.getBusInstance().register(this);

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if(accessToken != null){
            if(txId == null){
                doLogin(accessToken.getToken(), accessToken.getUserId(), false);
                showVerifyingUI("Logging in to CRIMP...");
            }
            else{
                // no-op
            }
        }
        else{
            showDefaultUI();
        }
    }

    @Override
    protected void onStop(){
        CrimpApplication2.getBusInstance().unregister(this);
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState (Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putSerializable(SAVE_UUID, txId);
        outState.putSerializable(SAVE_USER, mUser);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        fbCallbackManager.onActivityResult(requestCode, resultCode, data);
        if(DEBUG) Log.d(TAG, "Facebook login returned. resultCode: " + resultCode);

        if(resultCode == RESULT_OK){
            AccessToken accessToken = AccessToken.getCurrentAccessToken();
            if(accessToken != null){
                doLogin(accessToken.getToken(), accessToken.getUserId(), false);
                showVerifyingUI("Logging in to CRIMP...");
            }
            else{
                // This should not happen normally. Attempt to fix it by logging out user and set
                // to default UI.
                doLogout();
                showDefaultUI();
            }
        }
    }

    private void doLogin(@NonNull String accessToken, @NonNull String userId, boolean force){
        if(DEBUG) Log.d(TAG, "Sending login request to ServiceHelper. txId:"+txId);
        txId = ServiceHelper.login(this, txId, userId, accessToken, force);
        if(DEBUG) Log.d(TAG, "Login request sent to ServiceHelper. txId:" + txId);
    }

    private void doLogout(){
        LoginManager.getInstance().logOut();
        mUser = new User();
        if(DEBUG) Log.d(TAG, "logout done");
    }

    @Subscribe
    public void requestSucceedReceived(RequestSucceed event) {
        if(!event.txId.equals(txId)){
            return;
        }

        // TODO: React to the event somehow! REMEMBER TO CLEAR THE TXID
        Timber.d("Received RequestSucceed %s", event.txId);

        //TODO REMOVE THIS INJECTION
        final LoginJs result1 = CrimpApplication2.getLocalModel().fetch(txId.toString(), LoginJs.class);
        final LoginJs result = new LoginJs();
        result.setFbAccessToken("stubAccessToken");
        result.setFbUserId("stubUserId");
        result.setUserName("stubUserName");
        result.setRemindLogout(false);

        txId = null;
        mUser.setName(result.getUserName());
        mUser.setUserId(result.getFbUserId());
        mUser.setAccessToken(result.getFbAccessToken());

        if(result.isRemindLogout()){
            AlertDialog dialog = LoginReminder.create(this, new Action() {
                @Override
                public void act() {
                    doLogin(result.getFbAccessToken(), result.getFbUserId(), true);
                    showVerifyingUI("Logging in to CRIMP...");
                }
            }, new Action() {
                @Override
                public void act() {
                    doLogout();
                    showDefaultUI();
                }
            });
            dialog.show();
        }
        else{
            // TODO login succeed
            if(DEBUG) Log.d(TAG, "Login completed");
            launchHelloActivity();
        }
    }

    @Subscribe
    public void requestFailedReceived(RequestFailed event) {
        if(!event.txId.equals(txId)){
            return;
        }

        // TODO: React to the event somehow! REMEMBER TO CLEAR THE TXID
        Timber.d("Received RequestFailed %s", event.txId);
        txId = null;
        doLogout();
        showErrorUI("Server exploded");
    }
}
