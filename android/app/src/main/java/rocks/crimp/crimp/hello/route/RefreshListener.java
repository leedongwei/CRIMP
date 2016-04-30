package rocks.crimp.crimp.hello.route;

import android.support.v4.widget.SwipeRefreshLayout;

import rocks.crimp.crimp.common.Action;

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
