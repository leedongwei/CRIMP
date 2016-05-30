package rocks.crimp.crimp;

import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.test.suitebuilder.annotation.MediumTest;

import org.hamcrest.core.IsNull;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.mockwebserver.MockWebServer;
import retrofit2.Response;
import rocks.crimp.crimp.network.CrimpWS;
import rocks.crimp.crimp.network.CrimpWsImpl;
import rocks.crimp.crimp.network.model.CategoriesJs;
import rocks.crimp.crimp.network.model.CategoryJs;
import rocks.crimp.crimp.network.model.ClimberScoreJs;
import rocks.crimp.crimp.network.model.GetScoreJs;
import rocks.crimp.crimp.network.model.HeaderBean;
import rocks.crimp.crimp.network.model.LoginJs;
import rocks.crimp.crimp.network.model.MetaBean;
import rocks.crimp.crimp.network.model.PathBean;
import rocks.crimp.crimp.network.model.QueryBean;
import rocks.crimp.crimp.network.model.ReportJs;
import rocks.crimp.crimp.network.model.RequestBean;
import rocks.crimp.crimp.network.model.RequestBodyJs;
import rocks.crimp.crimp.network.model.RouteJs;
import rocks.crimp.crimp.network.model.ScoreJs;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsNot.not;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class WebServiceServerTest {
    private static final String BELOW_JUDGE_FB_TOKEN = "EAAUlqtPT3pUBAJOPLWqfvyIqZAaCWKRCFe7LpZCblwTqCAye3GJXcXNWt466eXLSfukuBg6H2hJ9ZB8ET0sGJRX2SMiaQpGArqruMPKhwNktT3MCqVXOkgovZAWWO16RM5BRlZCqwsKzsbhE5Ux0MXeBZAAJjYRRjovboZBMxo6ZA3fuWKVgEPpt";
    private static final String JUDGE_FB_TOKEN = "EAAUlqtPT3pUBAMbZABjwvtVOPigStWQYDsqizHpy5MOFyshUmX5NNRnAaew40M4rpGPAISOWpLTZAcHFi5aUI7Pb9fBz8b35xsyBNk7G0BhVgCnnAfaSzZCYnZAI3uCqv5LZCORZA1spxFSDcn1RvpDXGIWlgu65L6p3VsDH8ueAZDZD";

    private static Set<String> roleSet;
    private static CrimpWS mCrimpWSImpl;

    private String judgeXUserId;
    private String judgeXAuthToken;
    private String categoryId;
    private String routeId;
    private String markerId;
    private String climberId;

    @BeforeClass
    public static void initializeRoleSet(){
        roleSet = new HashSet<>();
        roleSet.add("denied");
        roleSet.add("pending");
        roleSet.add("partner");
        roleSet.add("judge");
        roleSet.add("admin");
        roleSet.add("hukkataival");
    }

    @BeforeClass
    public static void createCrimpWS() {
        mCrimpWSImpl = new CrimpWsImpl(CrimpWsImpl.BASEURL);
    }

    @Before
    public void loginAndGetStuff() throws IOException {
        RequestBodyJs body;
        RequestBean requestBean;

        // Craft request
        body = new RequestBodyJs();
        body.setFbAccessToken(JUDGE_FB_TOKEN);
        requestBean = new RequestBean();
        requestBean.setRequestBodyJs(body);

        // Make request
        Response<LoginJs> response1 = mCrimpWSImpl.login(requestBean);

        // Verify response
        judgeXUserId = response1.body().getxUserId();
        judgeXAuthToken = response1.body().getxAuthToken();
        if(judgeXUserId == null || judgeXAuthToken == null){
            throw new NullPointerException("Failed to get X-User-Id and/or X-Auth-Token");
        }

        /*********************************************************************/

        // Make request
        Response<CategoriesJs> response2 = mCrimpWSImpl.getCategories();

        // Verify response
        List<CategoryJs> categoryJsList = response2.body().getCategories();
        if(categoryJsList.size() == 0){
            throw new RuntimeException("Get categories request return a list of 0 category");
        }
        for(CategoryJs categoryJs:categoryJsList){
            if(categoryJs.getRoutes().size() == 0){
                throw new RuntimeException("Category id: "+categoryJs.getCategoryId()+" has a list of 0 routes");
            }
        }

        categoryId = response2.body().getCategories().get(0).getCategoryId();
        routeId = response2.body().getCategories().get(0).getRoutes().get(0).getRouteId();
        if(categoryId == null || routeId == null){
            throw new NullPointerException("Failed to get categoryId and/or routeId");
        }
    }

    @After
    public void logoutAfterTest() throws IOException {
        HeaderBean header;
        RequestBodyJs body;
        RequestBean requestBean;

        // Logout
        header = new HeaderBean();
        header.setxUserId(judgeXUserId);
        header.setxAuthToken(judgeXAuthToken);
        requestBean = new RequestBean();
        requestBean.setHeaderBean(header);
        mCrimpWSImpl.logout(requestBean);
    }

    @Test
    public void testPostLoginAndLogout() throws IOException {
        HeaderBean header;
        RequestBodyJs body;
        RequestBean requestBean;

        // Craft request
        body = new RequestBodyJs();
        body.setFbAccessToken(JUDGE_FB_TOKEN);
        requestBean = new RequestBean();
        requestBean.setRequestBodyJs(body);

        // Make request
        Response<LoginJs> response1 = mCrimpWSImpl.login(requestBean);

        // Verify response
        judgeXUserId = response1.body().getxUserId();
        judgeXAuthToken = response1.body().getxAuthToken();
        assertThat(response1.body().getxUserId(), is(not(nullValue())));
        assertThat(response1.body().getxAuthToken(), is(not(nullValue())));
        assertThat(response1.body().getRemindLogout(), is(not(nullValue())));
        assertThat(response1.body().getRoles(), is(not(nullValue())));
        assertThat(response1.body().getError(), is(nullValue()));

        List<String> roleList1 = response1.body().getRoles();
        assertThat(roleList1.size(), is(greaterThan(0)));
        // Check that all roles in roleList is valid
        for(String s:roleList1){
            assertThat(roleSet.contains(s), is(true));
        }
        // Check for repeats in roleList
        Set<String> roleListAsSet1 = new HashSet<>(roleList1);
        assertThat(roleListAsSet1.size(), is(roleList1.size()));
        // Check that roleList contain role of judge or above
        roleListAsSet1.remove("denied");
        roleListAsSet1.remove("pending");
        roleListAsSet1.remove("partner");
        assertThat(roleListAsSet1.size(), is(greaterThan(0)));

        /*********************************************************************/

        // Logout
        header = new HeaderBean();
        header.setxUserId(judgeXUserId);
        header.setxAuthToken(judgeXAuthToken);
        requestBean = new RequestBean();
        requestBean.setHeaderBean(header);
        mCrimpWSImpl.logout(requestBean);

        // Craft request
        body = new RequestBodyJs();
        body.setFbAccessToken(JUDGE_FB_TOKEN);
        requestBean = new RequestBean();
        requestBean.setRequestBodyJs(body);

        // Make request
        Response<LoginJs> response2 = mCrimpWSImpl.login(requestBean);

        // Verify response
        assertThat(response2.body().getxUserId(), is(not(nullValue())));
        assertThat(response2.body().getxAuthToken(), is(not(nullValue())));
        assertThat(response2.body().getRemindLogout(), is(false));
        assertThat(response2.body().getRoles(), is(not(nullValue())));
        assertThat(response2.body().getError(), is(nullValue()));

        List<String> roleList2 = response2.body().getRoles();
        assertThat(roleList2.size(), is(greaterThan(0)));
        // Check that all roles in roleList is valid
        for(String s:roleList2){
            assertThat(roleSet.contains(s), is(true));
        }
        // Check for repeats in roleList
        Set<String> roleListAsSet2 = new HashSet<>(roleList2);
        assertThat(roleListAsSet2.size(), is(roleList2.size()));
        // Check that roleList contain role of judge or above
        roleListAsSet2.remove("denied");
        roleListAsSet2.remove("pending");
        roleListAsSet2.remove("partner");
        assertThat(roleListAsSet2.size(), is(greaterThan(0)));



        // Craft request
        body = new RequestBodyJs();
        body.setFbAccessToken(JUDGE_FB_TOKEN);
        requestBean = new RequestBean();
        requestBean.setRequestBodyJs(body);

        // Make request
        Response<LoginJs> response3 = mCrimpWSImpl.login(requestBean);

        // Verify response
        assertThat(response3.body().getxUserId(), is(not(nullValue())));
        assertThat(response3.body().getxAuthToken(), is(not(nullValue())));
        assertThat(response3.body().getRemindLogout(), is(true));
        assertThat(response3.body().getRoles(), is(not(nullValue())));
        assertThat(response3.body().getError(), is(nullValue()));

        List<String> roleList3 = response3.body().getRoles();
        assertThat(roleList3.size(), is(greaterThan(0)));
        // Check that all roles in roleList is valid
        for(String s:roleList3){
            assertThat(roleSet.contains(s), is(true));
        }
        // Check for repeats in roleList
        Set<String> roleListAsSet3 = new HashSet<>(roleList3);
        assertThat(roleListAsSet3.size(), is(roleList3.size()));
        // Check that roleList contain role of judge or above
        roleListAsSet3.remove("denied");
        roleListAsSet3.remove("pending");
        roleListAsSet3.remove("partner");
        assertThat(roleListAsSet3.size(), is(greaterThan(0)));

        /*********************************************************************/

        /*
        // Craft request
        body = new RequestBodyJs();
        body.setFbAccessToken(BELOW_JUDGE_FB_TOKEN);
        requestBean = new RequestBean();
        requestBean.setRequestBodyJs(body);

        // Make request
        LoginJs response4 = mCrimpWSImpl.login(requestBean);

        // Verify response
        assertThat(response4.getxUserId(), is(nullValue()));
        assertThat(response4.getxAuthToken(), is(nullValue()));
        assertThat(response4.getRemindLogout(), is(nullValue()));
        assertThat(response4.getRoles(), is(nullValue()));
        assertThat(response4.getError(), is(not(nullValue())));
        */
    }

    @Test
    public void testGetCategories() throws IOException {
        // Make request
        Response<CategoriesJs> response1 = mCrimpWSImpl.getCategories();

        // Verify response
        assertThat(response1, is(not(nullValue())));
        assertThat(response1.body().getCategories(), is(not(nullValue())));
        for(CategoryJs category:response1.body().getCategories()){
            assertThat(category.getCategoryId(), is(not(nullValue())));
            assertThat(category.getCategoryName(), is(not(nullValue())));
            assertThat(category.getAcronym(), is(not(nullValue())));
            assertThat(category.getTimeStart(), is(not(nullValue())));
            assertThat(category.getTimeEnd(), is(not(nullValue())));
            assertThat(category.getRoutes(), is(not(nullValue())));

            for(RouteJs route:category.getRoutes()){
                assertThat(route.getRouteId(), is(not(nullValue())));
                assertThat(route.getRouteName(), is(not(nullValue())));
                assertThat(route.getScoreRules(), is(not(nullValue())));
            }
        }
    }

    @Test
    public void testReport() throws IOException {
        HeaderBean header;
        RequestBodyJs body;
        RequestBean requestBean;

        // Craft request
        header = new HeaderBean();
        header.setxUserId(judgeXUserId);
        header.setxAuthToken(judgeXAuthToken);
        body = new RequestBodyJs();
        body.setCategoryId(categoryId);
        body.setRouteId(routeId);
        body.setForceReport(false);
        requestBean = new RequestBean();
        requestBean.setRequestBodyJs(body);
        requestBean.setHeaderBean(header);

        // Make request
        Response<ReportJs> response1 = mCrimpWSImpl.reportIn(requestBean);

        // Verify response
        assertThat(response1.body().getxUserId(), is(not(nullValue())));
        assertThat(response1.body().getUserName(), is(not(nullValue())));
        assertThat(response1.body().getCategoryId(), is(categoryId));
        assertThat(response1.body().getRouteId(), is(routeId));
    }

    @Test
    public void testGetScore() throws IOException {
        HeaderBean header;
        QueryBean query;
        RequestBean requestBean;

        // Craft request
        header = new HeaderBean();
        header.setxUserId(judgeXUserId);
        header.setxAuthToken(judgeXAuthToken);
        query = new QueryBean();
        requestBean = new RequestBean();
        requestBean.setQueryBean(query);
        requestBean.setHeaderBean(header);

        // Make request
        Response<GetScoreJs> response1 = mCrimpWSImpl.getScore(requestBean);

        // Verify response
        assertThat(response1, is(not(nullValue())));
        assertThat(response1.body().getClimberScores(), is(not(nullValue())));
        assertThat(response1.body().getClimberScores().size(), is(greaterThan(0)));
        assertThat(response1.body().getClimberScores().get(0).getScores(), is(not(nullValue())));
        assertThat(response1.body().getClimberScores().get(0).getScores().size(), is(greaterThan(0)));
        assertThat(response1.body().getClimberScores().get(0).getScores().get(0).getRouteId(), is(not(nullValue())));
        climberId = response1.body().getClimberScores().get(0).getClimberId();
        markerId = response1.body().getClimberScores().get(0).getScores().get(0).getMarkerId();

        for(ClimberScoreJs climber:response1.body().getClimberScores()){
            assertThat(climber.getClimberId(), is(not(nullValue())));
            assertThat(climber.getClimberName(), is(not(nullValue())));
            assertThat(climber.getScores(), is(not(nullValue())));
            for(ScoreJs score:climber.getScores()){
                assertThat(score.getMarkerId(), is(not(nullValue())));
                assertThat(score.getRouteId(), is(not(nullValue())));
                assertThat(score.getScore(), is(not(nullValue())));
                assertThat(score.getCategoryId(), is(not(nullValue())));
            }
        }

        /*********************************************************************/

        // Craft request
        header = new HeaderBean();
        header.setxUserId(judgeXUserId);
        header.setxAuthToken(judgeXAuthToken);
        query = new QueryBean();
        query.setClimberId(climberId);
        requestBean = new RequestBean();
        requestBean.setQueryBean(query);
        requestBean.setHeaderBean(header);

        // Make request
        Response<GetScoreJs> response2 = mCrimpWSImpl.getScore(requestBean);

        // Verify response
        for(ClimberScoreJs climber:response2.body().getClimberScores()){
            assertThat(climber.getClimberId(), is(climberId));
            assertThat(climber.getClimberName(), is(not(nullValue())));
            assertThat(climber.getScores(), is(not(nullValue())));
            for(ScoreJs score:climber.getScores()){
                assertThat(score.getMarkerId(), is(not(nullValue())));
                assertThat(score.getRouteId(), is(not(nullValue())));
                assertThat(score.getScore(), is(not(nullValue())));
                assertThat(score.getCategoryId(), is(not(nullValue())));
            }
        }

        /*********************************************************************/

        // Craft request
        header = new HeaderBean();
        header.setxUserId(judgeXUserId);
        header.setxAuthToken(judgeXAuthToken);
        query = new QueryBean();
        query.setCategoryId(categoryId);
        requestBean = new RequestBean();
        requestBean.setQueryBean(query);
        requestBean.setHeaderBean(header);

        // Make request
        Response<GetScoreJs> response3 = mCrimpWSImpl.getScore(requestBean);

        // Verify response
        for(ClimberScoreJs climber:response3.body().getClimberScores()){
            assertThat(climber.getClimberId(), is(not(nullValue())));
            assertThat(climber.getClimberName(), is(not(nullValue())));
            assertThat(climber.getScores(), is(not(nullValue())));
            for(ScoreJs score:climber.getScores()){
                assertThat(score.getMarkerId(), is(not(nullValue())));
                assertThat(score.getRouteId(), is(not(nullValue())));
                assertThat(score.getScore(), is(not(nullValue())));
                assertThat(score.getCategoryId(), is(categoryId));
            }
        }

        /*********************************************************************/

        // Craft request
        header = new HeaderBean();
        header.setxUserId(judgeXUserId);
        header.setxAuthToken(judgeXAuthToken);
        query = new QueryBean();
        query.setRouteId(routeId);
        requestBean = new RequestBean();
        requestBean.setQueryBean(query);
        requestBean.setHeaderBean(header);

        // Make request
        Response<GetScoreJs> response4 = mCrimpWSImpl.getScore(requestBean);

        // Verify response
        for(ClimberScoreJs climber:response4.body().getClimberScores()){
            assertThat(climber.getClimberId(), is(not(nullValue())));
            assertThat(climber.getClimberName(), is(not(nullValue())));
            assertThat(climber.getScores(), is(not(nullValue())));
            for(ScoreJs score:climber.getScores()){
                assertThat(score.getMarkerId(), is(not(nullValue())));
                assertThat(score.getRouteId(), is(routeId));
                assertThat(score.getScore(), is(not(nullValue())));
                assertThat(score.getCategoryId(), is(not(nullValue())));
            }
        }
    }
}
