package rocks.crimp.crimp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import rocks.crimp.crimp.service.RestHandler;
import rocks.crimp.crimp.service.ScoreHandler;
import timber.log.Timber;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class NetworkChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, final Intent intent) {
        Timber.d("Network connectivity change");

        if (intent.getExtras() != null) {
            final ConnectivityManager connectivityManager = (ConnectivityManager)context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo ni = connectivityManager.getActiveNetworkInfo();

            // Connected
            if (ni != null && ni.isConnectedOrConnecting()) {
                Timber.d("Network %s connected", ni.getTypeName());

                context.unregisterReceiver(this);

                CrimpApplication.getScoreHandler()
                        .obtainMessage(ScoreHandler.RESUME_UPLOAD)
                        .sendToTarget();
            }
        }
    }
}
