package rocks.crimp.crimp.login;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import rocks.crimp.crimp.R;
import rocks.crimp.crimp.common.Action;
import timber.log.Timber;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class LoginReminder {
    public static AlertDialog create(Context context, final Action login, final Action cancel){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(R.string.login_activity_login_dialog_title)
                .setMessage(R.string.login_activity_login_dialog)
                .setPositiveButton(R.string.login_activity_login_dialog_positive,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Timber.d("Pressed on Positive button");
                                login.act();
                            }
                        })
                .setNegativeButton(R.string.login_activity_login_dialog_negative,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Timber.d("Pressed on Negative button");
                                cancel.act();
                            }
                        })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        Timber.d("Login Reminder cancelled");
                        cancel.act();
                    }
                });

        return builder.create();
    }
}
