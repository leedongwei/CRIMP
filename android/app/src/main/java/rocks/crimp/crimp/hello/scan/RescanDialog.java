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
    public static AlertDialog create(Context context, final Action rescan, final Action cancel,
                                     String markerId, String climberName, String routeName){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        String question = String.format(context.getString(R.string.scan_fragment_rescan_dialog),
                markerId, climberName, routeName);

        builder.setTitle(R.string.scan_fragment_rescan_dialog_title)
                .setMessage(question)
                .setPositiveButton(R.string.scan_fragment_rescan_dialog_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Timber.d("Pressed on Positive button");
                        rescan.act();
                    }
                })
                .setNegativeButton(R.string.scan_fragment_rescan_dialog_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Timber.d("Pressed on Negative button");
                        cancel.act();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        Timber.d("Rescan dialog cancelled");
                        cancel.act();
                    }
                });

        return builder.create();
    }
}
