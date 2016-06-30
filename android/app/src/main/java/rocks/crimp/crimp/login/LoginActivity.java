package rocks.crimp.crimp.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
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
import com.squareup.otto.Subscribe;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import io.fabric.sdk.android.Fabric;
import rocks.crimp.crimp.CrimpApplication;
import rocks.crimp.crimp.R;
import rocks.crimp.crimp.common.Action;
import rocks.crimp.crimp.common.Helper;
import rocks.crimp.crimp.common.event.RequestFailed;
import rocks.crimp.crimp.common.event.RequestSucceed;
import rocks.crimp.crimp.hello.HelloActivity;
import rocks.crimp.crimp.network.model.LoginJs;
import rocks.crimp.crimp.service.ServiceHelper;
import timber.log.Timber;

public class LoginActivity extends AppCompatActivity {
    private static final String SAVE_LOGIN_TXID = "save_login_txid";
    private static final String SAVE_LOGOUT_TXID = "save_logout_txid";

    private String mFbAccessToken;
    private String mFbUserName;
    private String mXUserId;
    private String mXAuthToken;
    private Set<String> mRoles;
    private UUID mLoginTxId;
    private UUID mLogoutTxId;

    // UI references.
    private TextView mErrorMsg;
    private ProgressBar mLoadingWheel;
    private TextView mResponseText;
    private LoginButton mLoginButton;

    // Facebook references.
    private CallbackManager mFbCallbackManager;

    private void showVerifyingUI(String responseText){
        Timber.d("showVerifyingUI");
        mErrorMsg.setVisibility(View.INVISIBLE);
        mLoadingWheel.setVisibility(View.VISIBLE);
        mResponseText.setText(responseText);
        mResponseText.setVisibility(View.VISIBLE);
        mLoginButton.setVisibility(View.GONE);
    }

    private void showErrorUI(String errorText){
        Timber.d("showErrorUI");
        mErrorMsg.setText(errorText);
        mErrorMsg.setVisibility(View.VISIBLE);
        mLoadingWheel.setVisibility(View.GONE);
        mResponseText.setVisibility(View.GONE);
        mLoginButton.setVisibility(View.VISIBLE);
    }

    private void showDefaultUI(){
        Timber.d("showDefaultUI");
        mErrorMsg.setVisibility(View.INVISIBLE);
        mLoadingWheel.setVisibility(View.GONE);
        mResponseText.setVisibility(View.GONE);
        mLoginButton.setVisibility(View.VISIBLE);
    }

    private void launchHelloActivity(){
        Crashlytics.setUserIdentifier(CrimpApplication.getAppState()
                .getString(CrimpApplication.X_USER_ID, null));
        Helper.assertStuff(true, true, true, true, true);

        Timber.d("Launching HelloActivity");
        Intent intent = new Intent(this, HelloActivity.class);
        finish();
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);

        // Get reference for views
        mErrorMsg = (TextView) findViewById(R.id.error_msg);
        mLoadingWheel = (ProgressBar) findViewById(R.id.loading_wheel);
        mResponseText = (TextView) findViewById(R.id.response_text);
        mLoginButton = (LoginButton) findViewById(R.id.login_button);

        // Setting up Facebook login button stuff.
        mFbCallbackManager = CallbackManager.Factory.create();
        mLoginButton.setReadPermissions("public_profile");
        mLoginButton.registerCallback(mFbCallbackManager, new FacebookCallback<LoginResult>() {
            private ProfileTracker mProfileTracker;

            @Override
            public void onSuccess(LoginResult loginResult) {
                Timber.d("Facebook login onSuccess with token: %s", loginResult.getAccessToken());

                mFbAccessToken = loginResult.getAccessToken().getToken();
                if(mFbAccessToken == null){
                    throw new IllegalStateException("fb access token is null");
                }
                CrimpApplication.getAppState().edit()
                        .putString(CrimpApplication.FB_ACCESS_TOKEN, mFbAccessToken)
                        .apply();

                if(Profile.getCurrentProfile() == null) {
                    Helper.assertStuff(true, false, null, null, null);

                    mProfileTracker = new ProfileTracker() {
                        @Override
                        protected void onCurrentProfileChanged(Profile profile, Profile profile2) {
                            Helper.assertStuff(true, false, null, null, null);

                            // profile2 is the new profile
                            mFbUserName = profile2.getName();
                            mProfileTracker.stopTracking();
                            Crashlytics.setUserName(mFbUserName);

                            CrimpApplication.getAppState().edit()
                                    .putString(CrimpApplication.FB_USER_NAME, mFbUserName)
                                    .apply();

                            doLogin(mFbAccessToken);
                            showVerifyingUI("Logging in to CRIMP...");
                        }
                    };
                }
                else {
                    mFbUserName = Profile.getCurrentProfile().getName();
                    Crashlytics.setUserName(mFbUserName);
                    CrimpApplication.getAppState().edit()
                            .putString(CrimpApplication.FB_USER_NAME, mFbUserName)
                            .apply();

                    Helper.assertStuff(true, true, null, null, null);
                    doLogin(mFbAccessToken);
                    showVerifyingUI("Logging in to CRIMP...");
                }
            }

            @Override
            public void onCancel() {
                Timber.d("Facebook login cancelled.");
            }

            @Override
            public void onError(FacebookException exception) {
                Timber.e(exception, "Facebook login error");
            }
        });

        // Restore state
        if(savedInstanceState != null){
            mLoginTxId = (UUID) savedInstanceState.getSerializable(SAVE_LOGIN_TXID);
            mLogoutTxId = (UUID) savedInstanceState.getSerializable(SAVE_LOGOUT_TXID);
        }

        mFbAccessToken = CrimpApplication.getAppState()
                .getString(CrimpApplication.FB_ACCESS_TOKEN, null);
        mFbUserName = CrimpApplication.getAppState().getString(CrimpApplication.FB_USER_NAME, null);
        mXUserId = CrimpApplication.getAppState().getString(CrimpApplication.X_USER_ID, null);
        mXAuthToken = CrimpApplication.getAppState().getString(CrimpApplication.X_AUTH_TOKEN, null);
        mRoles = CrimpApplication.getAppState()
                .getStringSet(CrimpApplication.ROLES, null);

        Crashlytics.setUserName(mFbUserName);

        // Check if we already log in
        if(mXUserId != null && mXAuthToken != null && Helper.isJudgeOrAbove(mRoles)){
            launchHelloActivity();
        }
    }

    @Override
    protected void onStart(){
        super.onStart();
        CrimpApplication.getBusInstance().register(this);

        if(mLoginTxId != null){
            // some assertion
            Helper.assertStuff(true, true, false, false, false);

            doLogin(mFbAccessToken);
            showVerifyingUI("Logging in to CRIMP...");
        }
        else if(mLogoutTxId != null){
            // some assertion
            Helper.assertStuff(true, true, true, true, true);

            doLogout(mXUserId, mXAuthToken);
            showVerifyingUI("Logging out of CRIMP...");
        }
        else{
            // some assertion
            // I am not sure if this line is correct. Disabling it for now since it has been crashing
            // app.
            Helper.assertStuff(false, false, false, false, false);

            showDefaultUI();
        }
    }

    @Override
    protected void onStop(){
        CrimpApplication.getBusInstance().unregister(this);
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState (Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putSerializable(SAVE_LOGIN_TXID, mLoginTxId);
        outState.putSerializable(SAVE_LOGOUT_TXID, mLogoutTxId);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mFbCallbackManager.onActivityResult(requestCode, resultCode, data);
        Timber.d("Facebook login returned. resultCode: %s", resultCode);

        if(resultCode == RESULT_OK){
            AccessToken accessToken = AccessToken.getCurrentAccessToken();
            if(accessToken != null){
                mFbAccessToken = accessToken.getToken();
                CrimpApplication.getAppState().edit()
                        .putString(CrimpApplication.FB_ACCESS_TOKEN, mFbAccessToken)
                        .apply();
            }
            else{
                // This should not happen normally. Attempt to fix it by logging out user and set
                // to default UI.
                mFbAccessToken = null;
                mFbUserName = null;
                CrimpApplication.getAppState().edit()
                        .remove(CrimpApplication.FB_ACCESS_TOKEN)
                        .remove(CrimpApplication.FB_USER_NAME)
                        .apply();
                LoginManager.getInstance().logOut();
                showDefaultUI();
            }
        }
    }

    private void doLogin(@NonNull String accessToken){
        Helper.assertStuff(true, true, false, false, false);

        mLoginTxId = ServiceHelper.login(this, mLoginTxId, accessToken);
    }

    private void doLogout(@NonNull String xUserId, @NonNull String mXAuthToken){
        mLogoutTxId = ServiceHelper.logout(this, mLogoutTxId, xUserId, mXAuthToken);
    }

    @Subscribe
    public void requestSucceedReceived(RequestSucceed event) {
        if(event.txId.equals(mLoginTxId)){
            Timber.d("Received RequestSucceed for Login: %s", event.txId);
            final LoginJs result = (LoginJs) event.value;
            mLoginTxId = null;

            mXUserId = result.getxUserId();
            mXAuthToken = result.getxAuthToken();
            boolean remindLogout = result.getRemindLogout();
            mRoles = new HashSet<>(result.getRoles());
            String error = result.getError();

            if(error == null){
                // Some assertion
                if(!Helper.isJudgeOrAbove(mRoles)){
                    Toast.makeText(this, "Ask admin to approve then try login again", Toast.LENGTH_LONG).show();
                    doLogout(mXUserId, mXAuthToken);
                    showDefaultUI();
                    return;
                }

                CrimpApplication.getAppState().edit()
                        .putString(CrimpApplication.X_USER_ID, mXUserId)
                        .putString(CrimpApplication.X_AUTH_TOKEN, mXAuthToken)
                        .putStringSet(CrimpApplication.ROLES, mRoles)
                        .apply();

                if(remindLogout){
                    AlertDialog dialog = LoginReminder.create(this, new Action() {
                        @Override
                        public void act() {
                            // Some assertion
                            Helper.assertStuff(true, true, true, true, true);

                            launchHelloActivity();
                        }
                    }, new Action() {
                        @Override
                        public void act() {
                            // Some assertion
                            Helper.assertStuff(true, true, true, true, true);

                            doLogout(mXUserId, mXAuthToken);
                            showVerifyingUI("Logging out of CRIMP...");
                        }
                    });
                    dialog.show();
                }
                else{
                    // Some assertion
                    Helper.assertStuff(true, true, true, true, true);

                    launchHelloActivity();
                }
            }
            else{
                showErrorUI(error);
            }
        }
        else if(event.txId.equals(mLogoutTxId)){
            Timber.d("Received RequestSucceed for Logout: %s", event.txId);
            mLogoutTxId = null;

            LoginManager.getInstance().logOut();
            mFbAccessToken = null;
            mFbUserName = null;
            mXUserId = null;
            mXAuthToken = null;
            mRoles = null;
            CrimpApplication.getAppState().edit()
                    .remove(CrimpApplication.FB_USER_NAME)
                    .remove(CrimpApplication.FB_ACCESS_TOKEN)
                    .remove(CrimpApplication.X_USER_ID)
                    .remove(CrimpApplication.X_AUTH_TOKEN)
                    .remove(CrimpApplication.ROLES)
                    .apply();
        }
    }

    @Subscribe
    public void requestFailedReceived(RequestFailed event) {
        if(event.txId.equals(mLoginTxId)){
            Timber.d("Received RequestFailed for Login: %s", event.txId);
            mLoginTxId = null;

            // some assertion
            Helper.assertStuff(true, true, false, false, false);

            mFbAccessToken = null;
            mFbUserName = null;
            CrimpApplication.getAppState().edit()
                    .remove(CrimpApplication.FB_ACCESS_TOKEN)
                    .remove(CrimpApplication.FB_USER_NAME)
                    .apply();
            LoginManager.getInstance().logOut();
            showErrorUI("Request failed");
        }
        else if(event.txId.equals(mLogoutTxId)){
            Timber.d("Received RequestFailed for Logout: %s", event.txId);
            mLogoutTxId = null;

            // some assertion
            Helper.assertStuff(true, true, true, true, true);

            LoginManager.getInstance().logOut();
            mFbAccessToken = null;
            mFbUserName = null;
            mXUserId = null;
            mXAuthToken = null;
            mRoles = null;
            CrimpApplication.getAppState().edit()
                    .remove(CrimpApplication.FB_USER_NAME)
                    .remove(CrimpApplication.FB_ACCESS_TOKEN)
                    .remove(CrimpApplication.X_USER_ID)
                    .remove(CrimpApplication.X_AUTH_TOKEN)
                    .remove(CrimpApplication.ROLES)
                    .apply();
        }
    }
}
