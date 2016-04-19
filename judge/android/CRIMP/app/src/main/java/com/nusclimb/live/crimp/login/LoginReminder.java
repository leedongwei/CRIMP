package com.nusclimb.live.crimp.login;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.nusclimb.live.crimp.common.Action;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class LoginReminder {
    private static final String TAG = "LoginReminder";
    private static final boolean DEBUG = true;

    public static AlertDialog create(Context context, final Action login, final Action cancel){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle("Continue with login?")
                .setMessage("Psst... we noticed you might have forgotten to logout CRIMP from " +
                        "another device. Please logout CRIMP from other devices before continuing.")
                .setPositiveButton("Login", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(DEBUG) Log.d(TAG, "Pressed on Positive button");
                        login.act();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(DEBUG) Log.d(TAG, "Pressed on Negative button");
                        cancel.act();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        if(DEBUG) Log.d(TAG, "Login Reminder cancelled");
                        cancel.act();
                    }
                });

        return builder.create();
    }
}
