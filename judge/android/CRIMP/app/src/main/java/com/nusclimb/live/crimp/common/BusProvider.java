package com.nusclimb.live.crimp.common;

import com.squareup.otto.Bus;

/**
 * Created by Zhi on 7/12/2015.
 */
public final class BusProvider {
    private static final Bus BUS = new Bus();

    public static Bus getInstance() {
        return BUS;
    }

    private BusProvider() {
        // No instances.
    }
}
