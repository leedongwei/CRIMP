package com.nusclimb.live.crimp.retry;

import com.octo.android.robospice.retry.DefaultRetryPolicy;

/**
 * Subclass of DefaultRetryPolicy to retry failed request indefinitely.
 * Uses exponential back off.
 * 
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 *
 */
public class CrimpRetryPolicy extends DefaultRetryPolicy {
	@Override
    public int getRetryCount() {
        return 1;
    }
}
