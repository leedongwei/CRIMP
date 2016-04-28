package com.nusclimb.live.crimp.hello.route;

import android.support.v4.widget.SwipeRefreshLayout;

import com.nusclimb.live.crimp.common.Action;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class RefreshListener implements SwipeRefreshLayout.OnRefreshListener {
    private final Action mAction;

    public RefreshListener(Action action){
        this.mAction = action;
    }

    @Override
    public void onRefresh() {
        mAction.act();
    }
}
