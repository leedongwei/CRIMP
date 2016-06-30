package rocks.crimp.crimp.hello.route;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;

import rocks.crimp.crimp.R;
import rocks.crimp.crimp.common.Action;
import timber.log.Timber;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class NextDialog {
    public static AlertDialog create(@NonNull Context context, @Nullable final Action proceed,
                                     @Nullable final Action cancel, @NonNull String markerId,
                                     @NonNull String climberName, @NonNull String routeName){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        String question = String.format(context.getString(R.string.route_fragment_next_dialog),
                markerId, climberName, routeName);

        builder.setTitle(R.string.route_fragment_next_dialog_title)
                .setMessage(question)
                .setPositiveButton(R.string.route_fragment_next_dialog_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Timber.d("Pressed on Positive button");
                        if(proceed != null){
                            proceed.act();
                        }
                    }
                })
                .setNegativeButton(R.string.route_fragment_next_dialog_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Timber.d("Pressed on Negative button");
                        if(cancel != null){
                            cancel.act();
                        }
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        Timber.d("Next dialog cancelled");
                        if(cancel != null){
                            cancel.act();
                        }
                    }
                });

        return builder.create();
    }
}
