package rocks.crimp.crimp.hello.score;

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
public class CloseDialog {
    public static AlertDialog create(@NonNull Context context, @Nullable final Action close,
                                     @Nullable final Action cancel){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(R.string.score_fragment_close_dialog_title)
                .setPositiveButton(R.string.score_fragment_close_dialog_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Timber.d("Pressed on Positive button");
                        if(close != null){
                            close.act();
                        }
                    }
                })
                .setNegativeButton(R.string.score_fragment_close_dialog_negative, new DialogInterface.OnClickListener() {
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
                        Timber.d("Close dialog cancelled");
                        if(cancel != null){
                            cancel.act();
                        }
                    }
                });

        return builder.create();
    }
}
