package rocks.crimp.crimp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import timber.log.Timber;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.d("Received intent: %s", intent.getAction());
        // No-op. CrimpApplication will need to be running for this BroadcastReceiver to run.
        // CrimpApplication onCreate would have take care of starting score upload.
    }

}