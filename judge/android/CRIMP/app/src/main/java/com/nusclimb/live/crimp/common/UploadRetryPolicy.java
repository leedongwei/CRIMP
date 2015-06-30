package com.nusclimb.live.crimp.common;

import com.octo.android.robospice.retry.DefaultRetryPolicy;

/**
 * Subclass of DefaultRetryPolicy to retry failed request indefinitely.
 * Uses exponential back off.
 * 
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 *
 */
public class UploadRetryPolicy extends DefaultRetryPolicy {
	@Override
    public int getRetryCount() {
        return 1;
    }
}
