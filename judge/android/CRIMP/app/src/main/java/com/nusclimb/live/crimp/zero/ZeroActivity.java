package com.nusclimb.live.crimp.zero;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.hello.HelloActivity;
import com.nusclimb.live.crimp.login.LoginActivity;

/**
 * Created by user on 20-Jul-15.
 */
public class ZeroActivity extends Activity {
    private final String TAG = ZeroActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        SharedPreferences mSharedPreferences = getPreferences(MODE_PRIVATE);
        boolean isLogin = mSharedPreferences.getBoolean(getString(R.string.preference_is_login), false);

        if(isLogin){
            Log.d(TAG+".onCreate()", "Login true. Launching HelloActivity");
            Intent intent = new Intent(getApplicationContext(), HelloActivity.class);
            startActivity(intent);
        }
        else{
            Log.d(TAG+".onCreate()", "Login false. Launching LoginActivity");
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        }
    }
}
