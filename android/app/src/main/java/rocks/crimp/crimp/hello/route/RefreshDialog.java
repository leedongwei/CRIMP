package rocks.crimp.crimp.hello.route;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import rocks.crimp.crimp.R;
import rocks.crimp.crimp.common.Action;
import timber.log.Timber;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class RefreshDialog {
    public static AlertDialog create(Context context, final Action refresh, final Action cancel,
                                     String markerId, String climberName, String routeName){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        String question = String.format(context.getString(R.string.route_fragment_refresh_dialog),
                markerId, climberName, routeName);

        builder.setTitle(R.string.route_fragment_refresh_dialog_title)
                .setMessage(question)
                .setPositiveButton(R.string.route_fragment_refresh_dialog_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Timber.d("Pressed on Positive button");
                        refresh.act();
                    }
                })
                .setNegativeButton(R.string.route_fragment_refresh_dialog_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Timber.d("Pressed on Negative button");
                        cancel.act();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        Timber.d("Replace dialog cancelled");
                        cancel.act();
                    }
                });

        return builder.create();
    }
}
