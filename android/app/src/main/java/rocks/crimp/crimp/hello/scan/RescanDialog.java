package rocks.crimp.crimp.hello.scan;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import rocks.crimp.crimp.R;
import rocks.crimp.crimp.common.Action;
import timber.log.Timber;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class RescanDialog {
    public static AlertDialog create(Context context, final Action rescan, final Action cancel){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle("Rescan")
                .setMessage("Rescan will discard changes you made in Score tab.")
                .setPositiveButton("Rescan",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Timber.d("Pressed on Positive button");
                                rescan.act();
                            }
                        })
                .setNegativeButton("Cancel",
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
                        Timber.d("Logout dialog cancelled");
                        cancel.act();
                    }
                });

        return builder.create();
    }
}
