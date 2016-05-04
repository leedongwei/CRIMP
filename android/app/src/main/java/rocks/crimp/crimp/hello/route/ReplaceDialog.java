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
public class ReplaceDialog {
    public static AlertDialog create(Context context, final Action replace, final Action cancel,
                                     String currentJudge, String categoryName, String routeName){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        String question = String.format(context.getString(R.string.route_fragment_replace_question),
                currentJudge, categoryName, routeName, currentJudge);

        builder.setTitle(R.string.route_fragment_replace_title)
                .setMessage(question)
                .setPositiveButton(R.string.route_fragment_replace_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Timber.d("Pressed on Positive button");
                        replace.act();
                    }
                })
                .setNegativeButton(R.string.route_fragment_replace_negative, new DialogInterface.OnClickListener() {
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
