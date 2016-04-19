package com.nusclimb.live.crimp.network;

import android.util.Log;

import com.nusclimb.live.crimp.network.model.CategoriesJs;
import com.nusclimb.live.crimp.network.model.ClearActiveJs;
import com.nusclimb.live.crimp.network.model.GetScoreJs;
import com.nusclimb.live.crimp.network.model.HelpMeJs;
import com.nusclimb.live.crimp.network.model.LoginJs;
import com.nusclimb.live.crimp.network.model.LogoutJs;
import com.nusclimb.live.crimp.network.model.PostScoreJs;
import com.nusclimb.live.crimp.network.model.ReportJs;
import com.nusclimb.live.crimp.network.model.RequestBean;
import com.nusclimb.live.crimp.network.model.SetActiveJs;

import java.io.IOException;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class StubWS implements CrimpWS {
    private static final String TAG = "StubWS";
    private static final boolean DEBUG = true;

    private static final int TIME_TO_RESPOND = 2000;

    @Override
    public String getBaseUrl() {
        return null;
    }

    @Override
    public CategoriesJs getCategories() throws IOException {
        if(DEBUG) Log.d(TAG, "sending getCategories request...");
        try {
            Thread.sleep(TIME_TO_RESPOND);
        } catch (InterruptedException e) {
            // Restore interrupt status.
            Thread.currentThread().interrupt();
        }
        if(DEBUG) Log.d(TAG, "getCategories request completed");
        return null;
    }

    @Override
    public GetScoreJs getScore(RequestBean requestBean) throws IOException {
        if(DEBUG) Log.d(TAG, "sending getScore request...");
        try {
            Thread.sleep(TIME_TO_RESPOND);
        } catch (InterruptedException e) {
            // Restore interrupt status.
            Thread.currentThread().interrupt();
        }
        if(DEBUG) Log.d(TAG, "getScore request completed");
        return null;
    }

    @Override
    public SetActiveJs setActive(RequestBean requestBean) throws IOException {
        if(DEBUG) Log.d(TAG, "sending setActive request...");
        try {
            Thread.sleep(TIME_TO_RESPOND);
        } catch (InterruptedException e) {
            // Restore interrupt status.
            Thread.currentThread().interrupt();
        }
        if(DEBUG) Log.d(TAG, "setActive request completed");
        return null;
    }

    @Override
    public ClearActiveJs clearActive(RequestBean requestBean) throws IOException {
        if(DEBUG) Log.d(TAG, "sending clearActive request...");
        try {
            Thread.sleep(TIME_TO_RESPOND);
        } catch (InterruptedException e) {
            // Restore interrupt status.
            Thread.currentThread().interrupt();
        }
        if(DEBUG) Log.d(TAG, "clearActive request completed");
        return null;
    }

    @Override
    public LoginJs login(RequestBean requestBean) throws IOException {
        if(DEBUG) Log.d(TAG, "sending login request...");
        try {
            Thread.sleep(TIME_TO_RESPOND);
        } catch (InterruptedException e) {
            // Restore interrupt status.
            Thread.currentThread().interrupt();
        }
        if(DEBUG) Log.d(TAG, "login request completed");
        return null;
    }

    @Override
    public ReportJs reportIn(RequestBean requestBean) throws IOException {
        if(DEBUG) Log.d(TAG, "sending report request...");
        try {
            Thread.sleep(TIME_TO_RESPOND);
        } catch (InterruptedException e) {
            // Restore interrupt status.
            Thread.currentThread().interrupt();
        }
        if(DEBUG) Log.d(TAG, "reportIn request completed");
        return null;
    }

    @Override
    public HelpMeJs requestHelp(RequestBean requestBean) throws IOException {
        if(DEBUG) Log.d(TAG, "sending requestHelp request...");
        try {
            Thread.sleep(TIME_TO_RESPOND);
        } catch (InterruptedException e) {
            // Restore interrupt status.
            Thread.currentThread().interrupt();
        }
        if(DEBUG) Log.d(TAG, "requestHelp request completed");
        return null;
    }

    @Override
    public PostScoreJs postScore(RequestBean requestBean) throws IOException {
        if(DEBUG) Log.d(TAG, "sending postScore request...");
        try {
            Thread.sleep(TIME_TO_RESPOND);
        } catch (InterruptedException e) {
            // Restore interrupt status.
            Thread.currentThread().interrupt();
        }
        if(DEBUG) Log.d(TAG, "postScore request completed");
        return null;
    }

    @Override
    public LogoutJs logout(RequestBean requestBean) throws IOException {
        if(DEBUG) Log.d(TAG, "sending logout request...");
        try {
            Thread.sleep(TIME_TO_RESPOND);
        } catch (InterruptedException e) {
            // Restore interrupt status.
            Thread.currentThread().interrupt();
        }
        if(DEBUG) Log.d(TAG, "logout request completed");
        return null;
    }
}
