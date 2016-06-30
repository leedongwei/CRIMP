package rocks.crimp.crimp.network;

import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Response;
import rocks.crimp.crimp.hello.score.ScoreFragment;
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
    private static final int TIME_TO_RESPOND = 10000;

    @Override
    public Response<CategoriesJs> getCategories() throws IOException {
        Timber.d("sending getCategories request...");
        try {
            Thread.sleep(TIME_TO_RESPOND);
        } catch (InterruptedException e) {
            // Restore interrupt status.
            Thread.currentThread().interrupt();
        }
        Timber.d("getCategories request completed");

        // Category A
        RouteJs routeA1 = new RouteJs();
        routeA1.setRouteId("routeIdA1");
        routeA1.setRouteName("routeA1");
        routeA1.setScoreRules(ScoreFragment.RULES_IFSC_TOP_BONUS);
        RouteJs routeA2 = new RouteJs();
        routeA2.setRouteId("routeIdA2");
        routeA2.setRouteName("routeA2");
        routeA2.setScoreRules(ScoreFragment.RULES_IFSC_TOP_BONUS);
        ArrayList<RouteJs> routeListA = new ArrayList<>();
        routeListA.add(routeA1);
        routeListA.add(routeA2);
        CategoryJs categoryA = new CategoryJs();
        categoryA.setCategoryId("categoryIdA");
        categoryA.setCategoryName("categoryA");
        categoryA.setAcronym("AAA");
        categoryA.setTimeStart("timeStartA");
        categoryA.setTimeEnd("timeEndA");
        categoryA.setRoutes(routeListA);

        // Category B
        RouteJs routeB1 = new RouteJs();
        routeB1.setRouteId("routeIdB1");
        routeB1.setRouteName("routeB1");
        routeB1.setScoreRules(ScoreFragment.RULES_TOP_B1_B2);
        RouteJs routeB2 = new RouteJs();
        routeB2.setRouteId("routeIdB2");
        routeB2.setRouteName("routeB2");
        routeB2.setScoreRules(ScoreFragment.RULES_TOP_B1_B2);
        ArrayList<RouteJs> routeListB = new ArrayList<>();
        routeListB.add(routeB1);
        routeListB.add(routeB2);
        CategoryJs categoryB = new CategoryJs();
        categoryB.setCategoryId("categoryIdB");
        categoryB.setCategoryName("categoryB");
        categoryB.setAcronym("BBB");
        categoryB.setTimeStart("timeStartB");
        categoryB.setTimeEnd("timeEndB");
        categoryB.setRoutes(routeListB);

        // Category C
        RouteJs routeC1 = new RouteJs();
        routeC1.setRouteId("routeIdC1");
        routeC1.setRouteName("routeC1");
        routeC1.setScoreRules(ScoreFragment.RULES_POINTS+"__50");
        RouteJs routeC2 = new RouteJs();
        routeC2.setRouteId("routeIdC2");
        routeC2.setRouteName("routeC2");
        routeC2.setScoreRules(ScoreFragment.RULES_POINTS+"__100");
        ArrayList<RouteJs> routeListC = new ArrayList<>();
        routeListC.add(routeC1);
        routeListC.add(routeC2);
        CategoryJs categoryC = new CategoryJs();
        categoryC.setCategoryId("categoryIdC");
        categoryC.setCategoryName("categoryC");
        categoryC.setAcronym("CCC");
        categoryC.setTimeStart("timeStartC");
        categoryC.setTimeEnd("timeEndC");
        categoryC.setRoutes(routeListC);

        ArrayList<CategoryJs> categoryList = new ArrayList<>();
        categoryList.add(categoryA);
        categoryList.add(categoryB);
        categoryList.add(categoryC);
        CategoriesJs responseBody = new CategoriesJs();
        responseBody.setCategories(categoryList);

        return Response.success(responseBody);
    }

    @Override
    public Response<GetScoreJs> getScore(RequestBean requestBean) throws IOException {
        Timber.d("sending getScore request...");
        try {
            Thread.sleep(TIME_TO_RESPOND);
        } catch (InterruptedException e) {
            // Restore interrupt status.
            Thread.currentThread().interrupt();
        }
        Timber.d("getScore request completed");

        requestBean.getQueryBean().getMarkerId();

        ScoreJs scoreJs = new ScoreJs();
        scoreJs.setMarkerId(requestBean.getQueryBean().getMarkerId());
        scoreJs.setCategoryId("categoryId");
        scoreJs.setRouteId(requestBean.getQueryBean().getRouteId());
        scoreJs.setScore("11");
        ClimberScoreJs climberScoreJs = new ClimberScoreJs();
        climberScoreJs.setClimberId("climberId");
        climberScoreJs.setClimberName("climberName");
        ArrayList<ScoreJs> scores = new ArrayList<>();
        scores.add(scoreJs);
        climberScoreJs.setScores(scores);
        GetScoreJs getScoreJs = new GetScoreJs();
        ArrayList<ClimberScoreJs> climberScores = new ArrayList<>();
        climberScores.add(climberScoreJs);
        getScoreJs.setClimberScores(climberScores);

        return Response.success(getScoreJs);
    }

    @Override
    public Response<SetActiveJs> setActive(RequestBean requestBean) throws IOException {
        Timber.d("sending setActive request...");
        try {
            Thread.sleep(TIME_TO_RESPOND);
        } catch (InterruptedException e) {
            // Restore interrupt status.
            Thread.currentThread().interrupt();
        }
        Timber.d("setActive request completed");

        return Response.success(new SetActiveJs());
    }

    @Override
    public Response<ClearActiveJs> clearActive(RequestBean requestBean) throws IOException {
        Timber.d("sending clearActive request...");
        try {
            Thread.sleep(TIME_TO_RESPOND);
        } catch (InterruptedException e) {
            // Restore interrupt status.
            Thread.currentThread().interrupt();
        }
        Timber.d("clearActive request completed");

        return Response.success(new ClearActiveJs());
    }

    @Override
    public Response<LoginJs> login(RequestBean requestBean) throws IOException {
        Timber.d("sending login request...");
        try {
            Thread.sleep(TIME_TO_RESPOND);
        } catch (InterruptedException e) {
            // Restore interrupt status.
            Thread.currentThread().interrupt();
        }
        Timber.d("login request completed");

        LoginJs loginJs = new LoginJs();
        loginJs.setxUserId("xUserId");
        loginJs.setxAuthToken("xAuthToken");
        ArrayList<String> roles = new ArrayList<>();
        roles.add("admin");
        loginJs.setRoles(roles);
        loginJs.setError(null);
        loginJs.setRemindLogout(false);

        return Response.success(loginJs);
    }

    @Override
    public Response<ReportJs> reportIn(RequestBean requestBean) throws IOException {
        Timber.d("sending report request...");
        try {
            Thread.sleep(TIME_TO_RESPOND);
        } catch (InterruptedException e) {
            // Restore interrupt status.
            Thread.currentThread().interrupt();
        }
        Timber.d("reportIn request completed");

        ReportJs reportJs = new ReportJs();
        reportJs.setxUserId("xUserId");
        reportJs.setUserName("userName");
        reportJs.setCategoryId("categoryId");
        reportJs.setRouteId("routeId");

        return Response.success(reportJs);
    }

    @Override
    public Response<HelpMeJs> requestHelp(RequestBean requestBean) throws IOException {
        Timber.d("sending requestHelp request...");
        try {
            Thread.sleep(TIME_TO_RESPOND);
        } catch (InterruptedException e) {
            // Restore interrupt status.
            Thread.currentThread().interrupt();
        }
        Timber.d("requestHelp request completed");

        return Response.success(new HelpMeJs());
    }

    @Override
    public Response<PostScoreJs> postScore(RequestBean requestBean) throws IOException {
        Timber.d("sending postScore request...");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            // Restore interrupt status.
            Thread.currentThread().interrupt();
        }
        Timber.d("postScore request completed");

        PostScoreJs postScoreJs = new PostScoreJs();
        postScoreJs.setClimberId("climberId");
        postScoreJs.setCategoryId("categoryId");
        postScoreJs.setRouteId("routeId");
        postScoreJs.setMarkerId("markerId");
        postScoreJs.setScore("11");

        /*
        return Response.error(500, new ResponseBody() {
            @Override
            public MediaType contentType() {
                return null;
            }

            @Override
            public long contentLength() {
                return 0;
            }

            @Override
            public BufferedSource source() {
                return null;
            }
        });
        */
        return Response.success(postScoreJs);
    }

    @Override
    public Response<LogoutJs> logout(RequestBean requestBean) throws IOException {
        Timber.d("sending logout request...");
        try {
            Thread.sleep(TIME_TO_RESPOND);
        } catch (InterruptedException e) {
            // Restore interrupt status.
            Thread.currentThread().interrupt();
        }
        Timber.d("logout request completed");

        return Response.success(new LogoutJs());
    }
}
