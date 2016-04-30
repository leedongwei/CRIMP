package rocks.crimp.crimp.network;

import java.io.IOException;

import rocks.crimp.crimp.network.model.CategoriesJs;
import rocks.crimp.crimp.network.model.ClearActiveJs;
import rocks.crimp.crimp.network.model.GetScoreJs;
import rocks.crimp.crimp.network.model.HelpMeJs;
import rocks.crimp.crimp.network.model.LoginJs;
import rocks.crimp.crimp.network.model.LogoutJs;
import rocks.crimp.crimp.network.model.PostScoreJs;
import rocks.crimp.crimp.network.model.ReportJs;
import rocks.crimp.crimp.network.model.RequestBean;
import rocks.crimp.crimp.network.model.SetActiveJs;
import timber.log.Timber;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class StubWS implements CrimpWS {
    private static final int TIME_TO_RESPOND = 2000;

    @Override
    public String getBaseUrl() {
        return null;
    }

    @Override
    public CategoriesJs getCategories() throws IOException {
        Timber.d("sending getCategories request...");
        try {
            Thread.sleep(TIME_TO_RESPOND);
        } catch (InterruptedException e) {
            // Restore interrupt status.
            Thread.currentThread().interrupt();
        }
        Timber.d("getCategories request completed");
        return null;
    }

    @Override
    public GetScoreJs getScore(RequestBean requestBean) throws IOException {
        Timber.d("sending getScore request...");
        try {
            Thread.sleep(TIME_TO_RESPOND);
        } catch (InterruptedException e) {
            // Restore interrupt status.
            Thread.currentThread().interrupt();
        }
        Timber.d("getScore request completed");
        return null;
    }

    @Override
    public SetActiveJs setActive(RequestBean requestBean) throws IOException {
        Timber.d("sending setActive request...");
        try {
            Thread.sleep(TIME_TO_RESPOND);
        } catch (InterruptedException e) {
            // Restore interrupt status.
            Thread.currentThread().interrupt();
        }
        Timber.d("setActive request completed");
        return null;
    }

    @Override
    public ClearActiveJs clearActive(RequestBean requestBean) throws IOException {
        Timber.d("sending clearActive request...");
        try {
            Thread.sleep(TIME_TO_RESPOND);
        } catch (InterruptedException e) {
            // Restore interrupt status.
            Thread.currentThread().interrupt();
        }
        Timber.d("clearActive request completed");
        return null;
    }

    @Override
    public LoginJs login(RequestBean requestBean) throws IOException {
        Timber.d("sending login request...");
        try {
            Thread.sleep(TIME_TO_RESPOND);
        } catch (InterruptedException e) {
            // Restore interrupt status.
            Thread.currentThread().interrupt();
        }
        Timber.d("login request completed");
        return null;
    }

    @Override
    public ReportJs reportIn(RequestBean requestBean) throws IOException {
        Timber.d("sending report request...");
        try {
            Thread.sleep(TIME_TO_RESPOND);
        } catch (InterruptedException e) {
            // Restore interrupt status.
            Thread.currentThread().interrupt();
        }
        Timber.d("reportIn request completed");
        return null;
    }

    @Override
    public HelpMeJs requestHelp(RequestBean requestBean) throws IOException {
        Timber.d("sending requestHelp request...");
        try {
            Thread.sleep(TIME_TO_RESPOND);
        } catch (InterruptedException e) {
            // Restore interrupt status.
            Thread.currentThread().interrupt();
        }
        Timber.d("requestHelp request completed");
        return null;
    }

    @Override
    public PostScoreJs postScore(RequestBean requestBean) throws IOException {
        Timber.d("sending postScore request...");
        try {
            Thread.sleep(TIME_TO_RESPOND);
        } catch (InterruptedException e) {
            // Restore interrupt status.
            Thread.currentThread().interrupt();
        }
        Timber.d("postScore request completed");
        return null;
    }

    @Override
    public LogoutJs logout(RequestBean requestBean) throws IOException {
        Timber.d("sending logout request...");
        try {
            Thread.sleep(TIME_TO_RESPOND);
        } catch (InterruptedException e) {
            // Restore interrupt status.
            Thread.currentThread().interrupt();
        }
        Timber.d("logout request completed");
        return null;
    }
}
