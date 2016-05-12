package rocks.crimp.crimp.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import rocks.crimp.crimp.network.model.CategoriesJs;
import rocks.crimp.crimp.network.model.CategoryJs;
import rocks.crimp.crimp.network.model.ClearActiveJs;
import rocks.crimp.crimp.network.model.ClimberScoreJs;
import rocks.crimp.crimp.network.model.GetScoreJs;
import rocks.crimp.crimp.network.model.HelpMeJs;
import rocks.crimp.crimp.network.model.LoginJs;
import rocks.crimp.crimp.network.model.LogoutJs;
import rocks.crimp.crimp.network.model.PostScoreJs;
import rocks.crimp.crimp.network.model.ReportJs;
import rocks.crimp.crimp.network.model.RequestBean;
import rocks.crimp.crimp.network.model.RouteJs;
import rocks.crimp.crimp.network.model.ScoreJs;
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

        // INJECTION
        RouteJs routeA1 = new RouteJs();
        routeA1.setRouteName("route A1");
        routeA1.setRouteId(1);
        routeA1.setScoreType("top_bonus");
        routeA1.setScoreFinalized(false);
        routeA1.setTimeStart(new Date());
        routeA1.setTimeEnd(new Date());

        RouteJs routeA2 = new RouteJs();
        routeA2.setRouteName("route A2");
        routeA2.setRouteId(2);
        routeA2.setScoreType("top_bonus");
        routeA2.setScoreFinalized(false);
        routeA2.setTimeStart(new Date());
        routeA2.setTimeEnd(new Date());

        RouteJs routeB1 = new RouteJs();
        routeB1.setRouteName("route B1");
        routeB1.setRouteId(3);
        routeB1.setScoreType("bonus_2");
        routeB1.setScoreFinalized(false);
        routeB1.setTimeStart(new Date());
        routeB1.setTimeEnd(new Date());

        RouteJs routeB2 = new RouteJs();
        routeB2.setRouteName("route B2");
        routeB2.setRouteId(4);
        routeB2.setScoreType("bonus_2");
        routeB2.setScoreFinalized(false);
        routeB2.setTimeStart(new Date());
        routeB2.setTimeEnd(new Date());

        CategoryJs categoryA = new CategoryJs();
        categoryA.setCategoryName("Novice Man Qualifier");
        categoryA.setCategoryId(1);
        categoryA.setAcronym("NMQ");
        ArrayList<RouteJs> cat1Route = new ArrayList<>();
        cat1Route.add(routeA1);
        cat1Route.add(routeA2);
        categoryA.setRoutes(cat1Route);

        CategoryJs categoryB = new CategoryJs();
        categoryB.setCategoryName("Inter Woman Final");
        categoryB.setCategoryId(2);
        categoryB.setAcronym("IWF");
        ArrayList<RouteJs> cat2Route = new ArrayList<>();
        cat2Route.add(routeB1);
        cat2Route.add(routeB2);
        categoryB.setRoutes(cat2Route);

        ArrayList<CategoryJs> categoryList = new ArrayList<>();
        categoryList.add(categoryA);
        categoryList.add(categoryB);

        CategoriesJs categoriesJs = new CategoriesJs();
        categoriesJs.setCategories(categoryList);

        return categoriesJs;
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

        if(requestBean.getRequestBodyJs().getMarkerId().matches("NMQ.++")){
            ScoreJs scoreJs = new ScoreJs();
            scoreJs.setScore("11");
            scoreJs.setMarkerId(requestBean.getRequestBodyJs().getMarkerId());
            ArrayList<ScoreJs> scoreList = new ArrayList<>();
            scoreList.add(scoreJs);
            ClimberScoreJs climberScoreJs = new ClimberScoreJs();
            climberScoreJs.setScores(scoreList);
            climberScoreJs.setClimberName("Lee Dong Dong");
            ArrayList<ClimberScoreJs> climberScoreList = new ArrayList<>();
            climberScoreList.add(climberScoreJs);

            GetScoreJs result = new GetScoreJs();
            result.setClimberScores(climberScoreList);
            return result;
        }
        else if(requestBean.getRequestBodyJs().getMarkerId().matches("IWF.++")){
            ScoreJs scoreJs = new ScoreJs();
            scoreJs.setMarkerId(requestBean.getRequestBodyJs().getMarkerId());
            scoreJs.setScore("B1");
            ArrayList<ScoreJs> scoreList = new ArrayList<>();
            scoreList.add(scoreJs);
            ClimberScoreJs climberScoreJs = new ClimberScoreJs();
            climberScoreJs.setScores(scoreList);
            climberScoreJs.setClimberName("Tan Silly");
            ArrayList<ClimberScoreJs> climberScoreList = new ArrayList<>();
            climberScoreList.add(climberScoreJs);

            GetScoreJs result = new GetScoreJs();
            result.setClimberScores(climberScoreList);
            return result;
        }
        else{
            // Make noise
            throw new IllegalArgumentException("GetScore request with invalid marker id");
        }
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
        return new SetActiveJs();
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
        return new ClearActiveJs();
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

        LoginJs loginJs;
        boolean force = requestBean.getRequestBodyJs().isForceLogin();
        if(force){
            loginJs = new LoginJs();
            loginJs.setFbUserId("fbUserId");
            loginJs.setFbAccessToken("fbAccessToken");
            loginJs.setUserName("userName");
            loginJs.setRemindLogout(false);
            loginJs.setSequentialToken(1);
        }
        else{
            loginJs = new LoginJs();
            loginJs.setFbUserId("fbUserId");
            loginJs.setFbAccessToken("fbAccessToken");
            loginJs.setUserName("userName");
            loginJs.setRemindLogout(true);
            loginJs.setSequentialToken(1);
        }

        return loginJs;
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

        ReportJs reportJs;
        boolean force = requestBean.getRequestBodyJs().isForceReport();
        if(force){
            reportJs = new ReportJs();
            reportJs.setFbUserId(requestBean.getHeaderBean().getFbUserId());
            reportJs.setUserName("userName");
            reportJs.setCategoryId(requestBean.getRequestBodyJs().getCategoryId());
            reportJs.setRouteId(requestBean.getRequestBodyJs().getRouteId());
        }
        else{
            reportJs = new ReportJs();
            reportJs.setFbUserId("someOtherUserId");
            reportJs.setUserName("someOtherGuy");
            reportJs.setCategoryId(requestBean.getRequestBodyJs().getCategoryId());
            reportJs.setRouteId(requestBean.getRequestBodyJs().getRouteId());
        }

        return reportJs;
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
