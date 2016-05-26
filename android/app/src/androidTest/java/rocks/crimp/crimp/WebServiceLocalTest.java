package rocks.crimp.crimp;

import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.DisableOnAndroidDebug;
import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.MediumTest;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import retrofit2.http.Body;
import rocks.crimp.crimp.network.CrimpWS;
import rocks.crimp.crimp.network.CrimpWsImpl;
import rocks.crimp.crimp.network.model.CategoriesJs;
import rocks.crimp.crimp.network.model.CategoryJs;
import rocks.crimp.crimp.network.model.ClearActiveJs;
import rocks.crimp.crimp.network.model.ClimberScoreJs;
import rocks.crimp.crimp.network.model.GetScoreJs;
import rocks.crimp.crimp.network.model.HeaderBean;
import rocks.crimp.crimp.network.model.HelpMeJs;
import rocks.crimp.crimp.network.model.LoginJs;
import rocks.crimp.crimp.network.model.LogoutJs;
import rocks.crimp.crimp.network.model.PathBean;
import rocks.crimp.crimp.network.model.PostScoreJs;
import rocks.crimp.crimp.network.model.QueryBean;
import rocks.crimp.crimp.network.model.ReportJs;
import rocks.crimp.crimp.network.model.RequestBean;
import rocks.crimp.crimp.network.model.RequestBodyJs;
import rocks.crimp.crimp.network.model.RouteJs;
import rocks.crimp.crimp.network.model.SetActiveJs;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsNot.not;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
@RunWith(AndroidJUnit4.class)
@MediumTest
public class WebServiceLocalTest extends InstrumentationTestCase {
    private static Set<String> roleSet;
    private static MockWebServer server;
    private static CrimpWS mCrimpWSImpl;

    @Rule
    public TestRule timeout = new DisableOnAndroidDebug(new Timeout(20, TimeUnit.MINUTES));

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

    @NonNull
    public static String generateResponseString(String file){
        InputStream inputStream = null;
        Scanner scanner = null;
        String serverResponseString = null;
        try {
            inputStream = InstrumentationRegistry.getContext().getResources()
                    .getAssets().open(file);
            scanner = new Scanner(inputStream).useDelimiter("\\A");
            serverResponseString = scanner.hasNext() ? scanner.next() : "";
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(scanner != null){
                scanner.close();
            }
            else {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if(serverResponseString == null){
            throw new NullPointerException("Unable to generate response string from file: "+file);
        }
        else{
            return serverResponseString;
        }
    }

    @BeforeClass
    public static void setupServerAndWebService() throws IOException {
        server = new MockWebServer();

        final Dispatcher dispatcher = new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                if(request.getPath().equals("/api/judge/login") && request.getMethod().equals("POST")){
                    String serverResponseString = generateResponseString("login");
                    return new MockResponse().setBody(serverResponseString)
                            .setHeader("Content-Type", "application/json");
                }
                else if(request.getPath().equals("/api/judge/categories") && request.getMethod().equals("GET")){
                    String serverResponseString = generateResponseString("categories");
                    return new MockResponse().setBody(serverResponseString)
                            .setHeader("Content-Type", "application/json");
                }
                else if(request.getPath().equals("/api/judge/report") && request.getMethod().equals("POST")){
                    String serverResponseString = generateResponseString("report");
                    return new MockResponse().setBody(serverResponseString)
                            .setHeader("Content-Type", "application/json");
                }
                else if(request.getPath().equals("/api/judge/logout") && request.getMethod().equals("POST")){
                    String serverResponseString = generateResponseString("logout");
                    return new MockResponse().setBody(serverResponseString)
                            .setHeader("Content-Type", "application/json");
                }
                else if(request.getPath().startsWith("/api/judge/score") && request.getMethod().equals("GET")){
                    String fileName = "getscore";

                    if(request.getPath().contains("?")) {
                        String[] half = request.getPath().split("\\?");
                        String[] queries = half[1].split("&");

                        for (int i = 0; i < queries.length; i++) {
                            String[] token = queries[i].split("=");
                            fileName = fileName + "-" + token[0];
                        }
                    }

                    String serverResponseString = generateResponseString(fileName);
                    return new MockResponse().setBody(serverResponseString)
                            .setHeader("Content-Type", "application/json");
                }
                else if(request.getPath().startsWith("/api/judge/score") && request.getMethod().equals("POST")){
                    String serverResponseString = generateResponseString("postscore");
                    return new MockResponse().setBody(serverResponseString)
                            .setHeader("Content-Type", "application/json");
                }
                else if(request.getPath().equals("/api/judge/helpme") && request.getMethod().equals("POST")){
                    String serverResponseString = generateResponseString("helpme");
                    return new MockResponse().setBody(serverResponseString)
                            .setHeader("Content-Type", "application/json");
                }
                else if(request.getPath().equals("/api/judge/setactive") && request.getMethod().equals("PUT")){
                    String serverResponseString = generateResponseString("setactive");
                    return new MockResponse().setBody(serverResponseString)
                            .setHeader("Content-Type", "application/json");
                }
                else if(request.getPath().equals("/api/judge/clearactive") && request.getMethod().equals("PUT")){
                    String serverResponseString = generateResponseString("clearactive");
                    return new MockResponse().setBody(serverResponseString)
                            .setHeader("Content-Type", "application/json");
                }

                return new MockResponse().setResponseCode(404);
            }
        };
        server.setDispatcher(dispatcher);
        server.start();

        // Prepare Web service
        mCrimpWSImpl = new CrimpWsImpl(server.url("").toString());
    }

    @AfterClass
    public static void tearDownServer() throws IOException {
        server.shutdown();
    }

    @Test
    public void testLogin() throws IOException {
        RequestBodyJs body;
        RequestBean requestBean;

        // Prepare request
        body = new RequestBodyJs();
        body.setFbAccessToken("EAAUlqtPT3pUBALMOREbIB3zwwIYCUJLytvnO0ftbOBO2jl91nXMnxmbJnMtFPLrURZA4ANZAvqX3jKi6qWebJvDY7QUkVwkLUPN0hsPE7wUGOZAxXP8HYwL6Sw8qIlXoDNZC9UZCxc0JLZBKDS2jUu4ZAXx1cl2oZACOrZAAYw3LPsQsZBRSDVGCKB");
        requestBean = new RequestBean();
        requestBean.setRequestBodyJs(body);

        // Make request
        LoginJs response1 = mCrimpWSImpl.login(requestBean);

        // Verify response
        assertThat(response1.getxUserId(), is("6Sw3aLjKMtaDRHMor"));
        assertThat(response1.getxAuthToken(), is("VpQmdlKiikB-aauJaEi2wv-x-b0EZKOs7DC_nNIS2Kn"));
        assertThat(response1.getRemindLogout(), is(true));
        List<String> roleList = response1.getRoles();
        assertThat(roleList.size(), is(2));
        assertThat(roleList.contains("judge"), is(true));
        assertThat(roleList.contains("admin"), is(true));
        assertThat(response1.getError(), is(nullValue()));

        /*********************************************************************/

        /*
        // Prepare server responses
        InputStream inputStream = InstrumentationRegistry.getContext().getResources()
                .getAssets().open("login_error");
        Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
        String serverResponseString = scanner.hasNext() ? scanner.next() : "";
        scanner.close();

        // Prepare server
        MockWebServer loginFailServer = new MockWebServer();
        loginFailServer.enqueue(new MockResponse().setBody(serverResponseString)
                .setHeader("Content-Type", "application/json"));
        loginFailServer.start();

        // Prepare Web service
        HttpUrl fullUrl = loginFailServer.url("/api/judge/login");
        String baseUrl = "http://"+fullUrl.host()+":"+fullUrl.port()+"/";
        mCrimpWSImpl = new CrimpWsImpl(baseUrl.toString());

        // Prepare request
        body = new RequestBodyJs();
        body.setFbAccessToken("EAAUlqtPT3pUBALMOREbIB3zwwIYCUJLytvnO0ftbOBO2jl91nXMnxmbJnMtFPLrURZA4ANZAvqX3jKi6qWebJvDY7QUkVwkLUPN0hsPE7wUGOZAxXP8HYwL6Sw8qIlXoDNZC9UZCxc0JLZBKDS2jUu4ZAXx1cl2oZACOrZAAYw3LPsQsZBRSDVGCKB");
        requestBean = new RequestBean();
        requestBean.setRequestBodyJs(body);

        // Make request
        LoginJs response2 = mCrimpWSImpl.login(requestBean);

        // Verify response
        assertThat(response2.getxUserId(), is(nullValue()));
        assertThat(response2.getxAuthToken(), is(nullValue()));
        assertThat(response2.getRemindLogout(), is(nullValue()));
        assertThat(response2.getRoles(), is(nullValue()));
        assertThat(response2.getError(), is(not(nullValue())));

        // shutdown server
        loginFailServer.shutdown();
        */
    }

    @Test
    public void testGetCategories() throws IOException {
        // Make request
        CategoriesJs response1 = mCrimpWSImpl.getCategories();

        // Verify response
        assertThat(response1.getCategories().size(), is(2));

        CategoryJs category0 = response1.getCategories().get(0);
        assertThat(category0.getCategoryId(), is("iaA4T76ihpvmAhEtc"));
        assertThat(category0.getCategoryName(), is("Novice Men Qualifiers"));
        assertThat(category0.getAcronym(), is("NMQ"));
        assertThat(category0.getTimeStart(), is("Thu Jul 30 2015 12:00:00 GMT+0800"));
        assertThat(category0.getTimeEnd(), is("Thu Jul 30 2015 12:00:00 GMT+0800"));
        assertThat(category0.getRoutes().size(), is(3));
        assertThat(category0.getRoutes().get(0).getRouteId(), is("ZcakJrZnpuwg9fXoE"));
        assertThat(category0.getRoutes().get(0).getRouteName(), is("Route 1"));
        assertThat(category0.getRoutes().get(0).getScoreRules(), is("points__1000"));
        assertThat(category0.getRoutes().get(1).getRouteId(), is("2uALvGuYPxB9m8jAm"));
        assertThat(category0.getRoutes().get(1).getRouteName(), is("Route 2"));
        assertThat(category0.getRoutes().get(1).getScoreRules(), is("points__800"));
        assertThat(category0.getRoutes().get(2).getRouteId(), is("juoFyiJSfnmTWvsXC"));
        assertThat(category0.getRoutes().get(2).getRouteName(), is("Route 3"));
        assertThat(category0.getRoutes().get(2).getScoreRules(), is("points__1800"));

        CategoryJs category1 = response1.getCategories().get(1);
        assertThat(category1.getCategoryId(), is("NNS2rzA5ayNJs9i94"));
        assertThat(category1.getCategoryName(), is("Novice Women Qualifiers"));
        assertThat(category1.getAcronym(), is("NWQ"));
        assertThat(category1.getTimeStart(), is("Thu Jul 30 2015 12:00:00 GMT+0800"));
        assertThat(category1.getTimeEnd(), is("Thu Jul 30 2015 12:00:00 GMT+0800"));
        assertThat(category1.getRoutes().size(), is(3));
        assertThat(category1.getRoutes().get(0).getRouteId(), is("hijbH8aYFFQrCXxhu"));
        assertThat(category1.getRoutes().get(0).getRouteName(), is("Route 1"));
        assertThat(category1.getRoutes().get(0).getScoreRules(), is("ifsc-top-bonus"));
        assertThat(category1.getRoutes().get(1).getRouteId(), is("NS3pLaS9tDe9MQ9dw"));
        assertThat(category1.getRoutes().get(1).getRouteName(), is("Route 2"));
        assertThat(category1.getRoutes().get(1).getScoreRules(), is("ifsc-top-bonus"));
        assertThat(category1.getRoutes().get(2).getRouteId(), is("YNPAJBHxrWvXJDTxc"));
        assertThat(category1.getRoutes().get(2).getRouteName(), is("Route 3"));
        assertThat(category1.getRoutes().get(2).getScoreRules(), is("ifsc-top-bonus"));
    }

    @Test
    public void testReport() throws IOException {
        RequestBodyJs body;
        HeaderBean header;
        RequestBean requestBean;

        // Prepare request
        header = new HeaderBean();
        header.setxUserId("6Sw3aLjKMtaDRHMor");
        header.setxAuthToken("VpQmdlKiikB-aauJaEi2wv-x-b0EZKOs7DC_nNIS2Kn");
        body = new RequestBodyJs();
        body.setCategoryId("iaA4T76ihpvmAhEtc");
        body.setRouteId("ZcakJrZnpuwg9fXoE");
        body.setForceReport(false);
        requestBean = new RequestBean();
        requestBean.setRequestBodyJs(body);
        requestBean.setHeaderBean(header);

        // Make request
        ReportJs response1 = mCrimpWSImpl.reportIn(requestBean);

        // Verify response
        assertThat(response1.getxUserId(), is("6Sw3aLjKMtaDRHMor"));
        assertThat(response1.getUserName(), is("Weizhi"));
        assertThat(response1.getCategoryId(), is("iaA4T76ihpvmAhEtc"));
        assertThat(response1.getRouteId(), is("ZcakJrZnpuwg9fXoE"));
    }

    @Test
    public void testLogout() throws IOException {
        HeaderBean header;
        RequestBean requestBean;

        // Prepare request
        header = new HeaderBean();
        header.setxUserId("6Sw3aLjKMtaDRHMor");
        header.setxAuthToken("VpQmdlKiikB-aauJaEi2wv-x-b0EZKOs7DC_nNIS2Kn");
        requestBean = new RequestBean();
        requestBean.setHeaderBean(header);

        // Make request
        LogoutJs response1 = mCrimpWSImpl.logout(requestBean);

        // Verify response
        assertThat(response1, is(not(nullValue())));
    }

    @Test
    public void testGetScore() throws IOException {
        HeaderBean header;
        RequestBean requestBean;
        QueryBean query;
        ClimberScoreJs climber0;
        ClimberScoreJs climber1;
        ClimberScoreJs climber2;
        GetScoreJs getScoreJs;

        // Prepare request
        header = new HeaderBean();
        header.setxUserId("6Sw3aLjKMtaDRHMor");
        header.setxAuthToken("VpQmdlKiikB-aauJaEi2wv-x-b0EZKOs7DC_nNIS2Kn");
        query = new QueryBean();
        requestBean = new RequestBean();
        requestBean.setHeaderBean(header);
        requestBean.setQueryBean(query);

        // Make request
        getScoreJs = mCrimpWSImpl.getScore(requestBean);

        // Verify response
        assertThat(getScoreJs.getClimberScores().size(), is(3));

        climber0 = getScoreJs.getClimberScores().get(0);
        assertThat(climber0.getClimberId(), is("climberId1"));
        assertThat(climber0.getClimberName(), is("Antonio Paul"));
        assertThat(climber0.getScores().size(), is(3));
        assertThat(climber0.getScores().get(0).getMarkerId(), is("NMQ004"));
        assertThat(climber0.getScores().get(0).getCategoryId(), is("iaA4T76ihpvmAhEtc"));
        assertThat(climber0.getScores().get(0).getRouteId(), is("ZcakJrZnpuwg9fXoE"));
        assertThat(climber0.getScores().get(0).getScore(), is("1000"));
        assertThat(climber0.getScores().get(1).getMarkerId(), is("NMQ004"));
        assertThat(climber0.getScores().get(1).getCategoryId(), is("iaA4T76ihpvmAhEtc"));
        assertThat(climber0.getScores().get(1).getRouteId(), is("2uALvGuYPxB9m8jAm"));
        assertThat(climber0.getScores().get(1).getScore(), is("800"));
        assertThat(climber0.getScores().get(2).getMarkerId(), is("NMQ004"));
        assertThat(climber0.getScores().get(2).getCategoryId(), is("iaA4T76ihpvmAhEtc"));
        assertThat(climber0.getScores().get(2).getRouteId(), is("juoFyiJSfnmTWvsXC"));
        assertThat(climber0.getScores().get(2).getScore(), is(""));

        climber1 = getScoreJs.getClimberScores().get(1);
        assertThat(climber1.getClimberId(), is("climberId2"));
        assertThat(climber1.getClimberName(), is("Romani"));
        assertThat(climber1.getScores().size(), is(3));
        assertThat(climber1.getScores().get(0).getMarkerId(), is("NWQ002"));
        assertThat(climber1.getScores().get(0).getCategoryId(), is("NNS2rzA5ayNJs9i94"));
        assertThat(climber1.getScores().get(0).getRouteId(), is("hijbH8aYFFQrCXxhu"));
        assertThat(climber1.getScores().get(0).getScore(), is("11T"));
        assertThat(climber1.getScores().get(1).getMarkerId(), is("NWQ002"));
        assertThat(climber1.getScores().get(1).getCategoryId(), is("NNS2rzA5ayNJs9i94"));
        assertThat(climber1.getScores().get(1).getRouteId(), is("NS3pLaS9tDe9MQ9dw"));
        assertThat(climber1.getScores().get(1).getScore(), is(""));
        assertThat(climber1.getScores().get(2).getMarkerId(), is("NWQ002"));
        assertThat(climber1.getScores().get(2).getCategoryId(), is("NNS2rzA5ayNJs9i94"));
        assertThat(climber1.getScores().get(2).getRouteId(), is("YNPAJBHxrWvXJDTxc"));
        assertThat(climber1.getScores().get(2).getScore(), is("B11T"));

        climber2 = getScoreJs.getClimberScores().get(2);
        assertThat(climber2.getClimberId(), is("climberId3"));
        assertThat(climber2.getClimberName(), is("Tranny"));
        assertThat(climber2.getScores().size(), is(6));
        assertThat(climber2.getScores().get(0).getMarkerId(), is("NMQ006"));
        assertThat(climber2.getScores().get(0).getCategoryId(), is("iaA4T76ihpvmAhEtc"));
        assertThat(climber2.getScores().get(0).getRouteId(), is("ZcakJrZnpuwg9fXoE"));
        assertThat(climber2.getScores().get(0).getScore(), is(""));
        assertThat(climber2.getScores().get(1).getMarkerId(), is("NMQ006"));
        assertThat(climber2.getScores().get(1).getCategoryId(), is("iaA4T76ihpvmAhEtc"));
        assertThat(climber2.getScores().get(1).getRouteId(), is("2uALvGuYPxB9m8jAm"));
        assertThat(climber2.getScores().get(1).getScore(), is("800"));
        assertThat(climber2.getScores().get(2).getMarkerId(), is("NMQ006"));
        assertThat(climber2.getScores().get(2).getCategoryId(), is("iaA4T76ihpvmAhEtc"));
        assertThat(climber2.getScores().get(2).getRouteId(), is("juoFyiJSfnmTWvsXC"));
        assertThat(climber2.getScores().get(2).getScore(), is("1800"));
        assertThat(climber2.getScores().get(3).getMarkerId(), is("NWQ006"));
        assertThat(climber2.getScores().get(3).getCategoryId(), is("NNS2rzA5ayNJs9i94"));
        assertThat(climber2.getScores().get(3).getRouteId(), is("hijbH8aYFFQrCXxhu"));
        assertThat(climber2.getScores().get(3).getScore(), is("1"));
        assertThat(climber2.getScores().get(4).getMarkerId(), is("NWQ006"));
        assertThat(climber2.getScores().get(4).getCategoryId(), is("NNS2rzA5ayNJs9i94"));
        assertThat(climber2.getScores().get(4).getRouteId(), is("NS3pLaS9tDe9MQ9dw"));
        assertThat(climber2.getScores().get(4).getScore(), is(""));
        assertThat(climber2.getScores().get(5).getMarkerId(), is("NWQ006"));
        assertThat(climber2.getScores().get(5).getCategoryId(), is("NNS2rzA5ayNJs9i94"));
        assertThat(climber2.getScores().get(5).getRouteId(), is("YNPAJBHxrWvXJDTxc"));
        assertThat(climber2.getScores().get(5).getScore(), is("T"));

        /*********************************************************************/

        // Prepare request
        header = new HeaderBean();
        header.setxUserId("6Sw3aLjKMtaDRHMor");
        header.setxAuthToken("VpQmdlKiikB-aauJaEi2wv-x-b0EZKOs7DC_nNIS2Kn");
        query = new QueryBean();
        query.setClimberId("climberId3");
        requestBean = new RequestBean();
        requestBean.setHeaderBean(header);
        requestBean.setQueryBean(query);

        // Make request
        getScoreJs = mCrimpWSImpl.getScore(requestBean);

        // Verify response
        assertThat(getScoreJs.getClimberScores().size(), is(1));

        climber0 = getScoreJs.getClimberScores().get(0);
        assertThat(climber0.getClimberId(), is("climberId3"));
        assertThat(climber0.getClimberName(), is("Tranny"));
        assertThat(climber0.getScores().size(), is(6));
        assertThat(climber0.getScores().get(0).getMarkerId(), is("NMQ006"));
        assertThat(climber0.getScores().get(0).getCategoryId(), is("iaA4T76ihpvmAhEtc"));
        assertThat(climber0.getScores().get(0).getRouteId(), is("ZcakJrZnpuwg9fXoE"));
        assertThat(climber0.getScores().get(0).getScore(), is(""));
        assertThat(climber0.getScores().get(1).getMarkerId(), is("NMQ006"));
        assertThat(climber0.getScores().get(1).getCategoryId(), is("iaA4T76ihpvmAhEtc"));
        assertThat(climber0.getScores().get(1).getRouteId(), is("2uALvGuYPxB9m8jAm"));
        assertThat(climber0.getScores().get(1).getScore(), is("800"));
        assertThat(climber0.getScores().get(2).getMarkerId(), is("NMQ006"));
        assertThat(climber0.getScores().get(2).getCategoryId(), is("iaA4T76ihpvmAhEtc"));
        assertThat(climber0.getScores().get(2).getRouteId(), is("juoFyiJSfnmTWvsXC"));
        assertThat(climber0.getScores().get(2).getScore(), is("1800"));
        assertThat(climber0.getScores().get(3).getMarkerId(), is("NWQ006"));
        assertThat(climber0.getScores().get(3).getCategoryId(), is("NNS2rzA5ayNJs9i94"));
        assertThat(climber0.getScores().get(3).getRouteId(), is("hijbH8aYFFQrCXxhu"));
        assertThat(climber0.getScores().get(3).getScore(), is("1"));
        assertThat(climber0.getScores().get(4).getMarkerId(), is("NWQ006"));
        assertThat(climber0.getScores().get(4).getCategoryId(), is("NNS2rzA5ayNJs9i94"));
        assertThat(climber0.getScores().get(4).getRouteId(), is("NS3pLaS9tDe9MQ9dw"));
        assertThat(climber0.getScores().get(4).getScore(), is(""));
        assertThat(climber0.getScores().get(5).getMarkerId(), is("NWQ006"));
        assertThat(climber0.getScores().get(5).getCategoryId(), is("NNS2rzA5ayNJs9i94"));
        assertThat(climber0.getScores().get(5).getRouteId(), is("YNPAJBHxrWvXJDTxc"));
        assertThat(climber0.getScores().get(5).getScore(), is("T"));

        /*********************************************************************/

        // Prepare request
        header = new HeaderBean();
        header.setxUserId("6Sw3aLjKMtaDRHMor");
        header.setxAuthToken("VpQmdlKiikB-aauJaEi2wv-x-b0EZKOs7DC_nNIS2Kn");
        query = new QueryBean();
        query.setCategoryId("iaA4T76ihpvmAhEtc");
        requestBean = new RequestBean();
        requestBean.setHeaderBean(header);
        requestBean.setQueryBean(query);

        // Make request
        getScoreJs = mCrimpWSImpl.getScore(requestBean);

        // Verify response
        assertThat(getScoreJs.getClimberScores().size(), is(2));

        climber0 = getScoreJs.getClimberScores().get(0);
        assertThat(climber0.getClimberId(), is("climberId1"));
        assertThat(climber0.getClimberName(), is("Antonio Paul"));
        assertThat(climber0.getScores().size(), is(3));
        assertThat(climber0.getScores().get(0).getMarkerId(), is("NMQ004"));
        assertThat(climber0.getScores().get(0).getCategoryId(), is("iaA4T76ihpvmAhEtc"));
        assertThat(climber0.getScores().get(0).getRouteId(), is("ZcakJrZnpuwg9fXoE"));
        assertThat(climber0.getScores().get(0).getScore(), is("1000"));
        assertThat(climber0.getScores().get(1).getMarkerId(), is("NMQ004"));
        assertThat(climber0.getScores().get(1).getCategoryId(), is("iaA4T76ihpvmAhEtc"));
        assertThat(climber0.getScores().get(1).getRouteId(), is("2uALvGuYPxB9m8jAm"));
        assertThat(climber0.getScores().get(1).getScore(), is("800"));
        assertThat(climber0.getScores().get(2).getMarkerId(), is("NMQ004"));
        assertThat(climber0.getScores().get(2).getCategoryId(), is("iaA4T76ihpvmAhEtc"));
        assertThat(climber0.getScores().get(2).getRouteId(), is("juoFyiJSfnmTWvsXC"));
        assertThat(climber0.getScores().get(2).getScore(), is(""));

        climber1 = getScoreJs.getClimberScores().get(1);
        assertThat(climber1.getClimberId(), is("climberId3"));
        assertThat(climber1.getClimberName(), is("Tranny"));
        assertThat(climber1.getScores().size(), is(3));
        assertThat(climber1.getScores().get(0).getMarkerId(), is("NMQ006"));
        assertThat(climber1.getScores().get(0).getCategoryId(), is("iaA4T76ihpvmAhEtc"));
        assertThat(climber1.getScores().get(0).getRouteId(), is("ZcakJrZnpuwg9fXoE"));
        assertThat(climber1.getScores().get(0).getScore(), is(""));
        assertThat(climber1.getScores().get(1).getMarkerId(), is("NMQ006"));
        assertThat(climber1.getScores().get(1).getCategoryId(), is("iaA4T76ihpvmAhEtc"));
        assertThat(climber1.getScores().get(1).getRouteId(), is("2uALvGuYPxB9m8jAm"));
        assertThat(climber1.getScores().get(1).getScore(), is("800"));
        assertThat(climber1.getScores().get(2).getMarkerId(), is("NMQ006"));
        assertThat(climber1.getScores().get(2).getCategoryId(), is("iaA4T76ihpvmAhEtc"));
        assertThat(climber1.getScores().get(2).getRouteId(), is("juoFyiJSfnmTWvsXC"));
        assertThat(climber1.getScores().get(2).getScore(), is("1800"));

        /*********************************************************************/

        // Prepare request
        header = new HeaderBean();
        header.setxUserId("6Sw3aLjKMtaDRHMor");
        header.setxAuthToken("VpQmdlKiikB-aauJaEi2wv-x-b0EZKOs7DC_nNIS2Kn");
        query = new QueryBean();
        query.setRouteId("ZcakJrZnpuwg9fXoE");
        requestBean = new RequestBean();
        requestBean.setHeaderBean(header);
        requestBean.setQueryBean(query);

        // Make request
        getScoreJs = mCrimpWSImpl.getScore(requestBean);

        // Verify response
        assertThat(getScoreJs.getClimberScores().size(), is(2));

        climber0 = getScoreJs.getClimberScores().get(0);
        assertThat(climber0.getClimberId(), is("climberId1"));
        assertThat(climber0.getClimberName(), is("Antonio Paul"));
        assertThat(climber0.getScores().size(), is(1));
        assertThat(climber0.getScores().get(0).getMarkerId(), is("NMQ004"));
        assertThat(climber0.getScores().get(0).getCategoryId(), is("iaA4T76ihpvmAhEtc"));
        assertThat(climber0.getScores().get(0).getRouteId(), is("ZcakJrZnpuwg9fXoE"));
        assertThat(climber0.getScores().get(0).getScore(), is("1000"));

        climber1 = getScoreJs.getClimberScores().get(1);
        assertThat(climber1.getClimberId(), is("climberId3"));
        assertThat(climber1.getClimberName(), is("Tranny"));
        assertThat(climber1.getScores().size(), is(1));
        assertThat(climber1.getScores().get(0).getMarkerId(), is("NMQ006"));
        assertThat(climber1.getScores().get(0).getCategoryId(), is("iaA4T76ihpvmAhEtc"));
        assertThat(climber1.getScores().get(0).getRouteId(), is("ZcakJrZnpuwg9fXoE"));
        assertThat(climber1.getScores().get(0).getScore(), is(""));

        /*********************************************************************/

        // Prepare request
        header = new HeaderBean();
        header.setxUserId("6Sw3aLjKMtaDRHMor");
        header.setxAuthToken("VpQmdlKiikB-aauJaEi2wv-x-b0EZKOs7DC_nNIS2Kn");
        query = new QueryBean();
        query.setMarkerId("NMQ006");
        requestBean = new RequestBean();
        requestBean.setHeaderBean(header);
        requestBean.setQueryBean(query);

        // Make request
        getScoreJs = mCrimpWSImpl.getScore(requestBean);

        // Verify response
        assertThat(getScoreJs.getClimberScores().size(), is(1));

        climber0 = getScoreJs.getClimberScores().get(0);
        assertThat(climber0.getClimberId(), is("climberId3"));
        assertThat(climber0.getClimberName(), is("Tranny"));
        assertThat(climber0.getScores().size(), is(3));
        assertThat(climber0.getScores().get(0).getMarkerId(), is("NMQ006"));
        assertThat(climber0.getScores().get(0).getCategoryId(), is("iaA4T76ihpvmAhEtc"));
        assertThat(climber0.getScores().get(0).getRouteId(), is("ZcakJrZnpuwg9fXoE"));
        assertThat(climber0.getScores().get(0).getScore(), is(""));
        assertThat(climber0.getScores().get(1).getMarkerId(), is("NMQ006"));
        assertThat(climber0.getScores().get(1).getCategoryId(), is("iaA4T76ihpvmAhEtc"));
        assertThat(climber0.getScores().get(1).getRouteId(), is("2uALvGuYPxB9m8jAm"));
        assertThat(climber0.getScores().get(1).getScore(), is("800"));
        assertThat(climber0.getScores().get(2).getMarkerId(), is("NMQ006"));
        assertThat(climber0.getScores().get(2).getCategoryId(), is("iaA4T76ihpvmAhEtc"));
        assertThat(climber0.getScores().get(2).getRouteId(), is("juoFyiJSfnmTWvsXC"));
        assertThat(climber0.getScores().get(2).getScore(), is("1800"));

        /*********************************************************************/

        // Prepare request
        header = new HeaderBean();
        header.setxUserId("6Sw3aLjKMtaDRHMor");
        header.setxAuthToken("VpQmdlKiikB-aauJaEi2wv-x-b0EZKOs7DC_nNIS2Kn");
        query = new QueryBean();
        query.setClimberId("climberId3");
        query.setCategoryId("iaA4T76ihpvmAhEtc");
        requestBean = new RequestBean();
        requestBean.setHeaderBean(header);
        requestBean.setQueryBean(query);

        // Make request
        getScoreJs = mCrimpWSImpl.getScore(requestBean);

        // Verify response
        assertThat(getScoreJs.getClimberScores().size(), is(1));

        climber0 = getScoreJs.getClimberScores().get(0);
        assertThat(climber0.getClimberId(), is("climberId3"));
        assertThat(climber0.getClimberName(), is("Tranny"));
        assertThat(climber0.getScores().size(), is(3));
        assertThat(climber0.getScores().get(0).getMarkerId(), is("NMQ006"));
        assertThat(climber0.getScores().get(0).getCategoryId(), is("iaA4T76ihpvmAhEtc"));
        assertThat(climber0.getScores().get(0).getRouteId(), is("ZcakJrZnpuwg9fXoE"));
        assertThat(climber0.getScores().get(0).getScore(), is(""));
        assertThat(climber0.getScores().get(1).getMarkerId(), is("NMQ006"));
        assertThat(climber0.getScores().get(1).getCategoryId(), is("iaA4T76ihpvmAhEtc"));
        assertThat(climber0.getScores().get(1).getRouteId(), is("2uALvGuYPxB9m8jAm"));
        assertThat(climber0.getScores().get(1).getScore(), is("800"));
        assertThat(climber0.getScores().get(2).getMarkerId(), is("NMQ006"));
        assertThat(climber0.getScores().get(2).getCategoryId(), is("iaA4T76ihpvmAhEtc"));
        assertThat(climber0.getScores().get(2).getRouteId(), is("juoFyiJSfnmTWvsXC"));
        assertThat(climber0.getScores().get(2).getScore(), is("1800"));

        /*********************************************************************/

        // Prepare request
        header = new HeaderBean();
        header.setxUserId("6Sw3aLjKMtaDRHMor");
        header.setxAuthToken("VpQmdlKiikB-aauJaEi2wv-x-b0EZKOs7DC_nNIS2Kn");
        query = new QueryBean();
        query.setClimberId("climberId3");
        query.setRouteId("ZcakJrZnpuwg9fXoE");
        requestBean = new RequestBean();
        requestBean.setHeaderBean(header);
        requestBean.setQueryBean(query);

        // Make request
        getScoreJs = mCrimpWSImpl.getScore(requestBean);

        // Verify response
        assertThat(getScoreJs.getClimberScores().size(), is(1));

        climber0 = getScoreJs.getClimberScores().get(0);
        assertThat(climber0.getClimberId(), is("climberId3"));
        assertThat(climber0.getClimberName(), is("Tranny"));
        assertThat(climber0.getScores().size(), is(1));
        assertThat(climber0.getScores().get(0).getMarkerId(), is("NMQ006"));
        assertThat(climber0.getScores().get(0).getCategoryId(), is("iaA4T76ihpvmAhEtc"));
        assertThat(climber0.getScores().get(0).getRouteId(), is("ZcakJrZnpuwg9fXoE"));
        assertThat(climber0.getScores().get(0).getScore(), is(""));

        /*********************************************************************/

        // Prepare request
        header = new HeaderBean();
        header.setxUserId("6Sw3aLjKMtaDRHMor");
        header.setxAuthToken("VpQmdlKiikB-aauJaEi2wv-x-b0EZKOs7DC_nNIS2Kn");
        query = new QueryBean();
        query.setClimberId("climberId3");
        query.setMarkerId("NMQ006");
        requestBean = new RequestBean();
        requestBean.setHeaderBean(header);
        requestBean.setQueryBean(query);

        // Make request
        getScoreJs = mCrimpWSImpl.getScore(requestBean);

        // Verify response
        assertThat(getScoreJs.getClimberScores().size(), is(1));

        climber0 = getScoreJs.getClimberScores().get(0);
        assertThat(climber0.getClimberId(), is("climberId3"));
        assertThat(climber0.getClimberName(), is("Tranny"));
        assertThat(climber0.getScores().size(), is(3));
        assertThat(climber0.getScores().get(0).getMarkerId(), is("NMQ006"));
        assertThat(climber0.getScores().get(0).getCategoryId(), is("iaA4T76ihpvmAhEtc"));
        assertThat(climber0.getScores().get(0).getRouteId(), is("ZcakJrZnpuwg9fXoE"));
        assertThat(climber0.getScores().get(0).getScore(), is(""));
        assertThat(climber0.getScores().get(1).getMarkerId(), is("NMQ006"));
        assertThat(climber0.getScores().get(1).getCategoryId(), is("iaA4T76ihpvmAhEtc"));
        assertThat(climber0.getScores().get(1).getRouteId(), is("2uALvGuYPxB9m8jAm"));
        assertThat(climber0.getScores().get(1).getScore(), is("800"));
        assertThat(climber0.getScores().get(2).getMarkerId(), is("NMQ006"));
        assertThat(climber0.getScores().get(2).getCategoryId(), is("iaA4T76ihpvmAhEtc"));
        assertThat(climber0.getScores().get(2).getRouteId(), is("juoFyiJSfnmTWvsXC"));
        assertThat(climber0.getScores().get(2).getScore(), is("1800"));

        /*********************************************************************/

        // Prepare request
        header = new HeaderBean();
        header.setxUserId("6Sw3aLjKMtaDRHMor");
        header.setxAuthToken("VpQmdlKiikB-aauJaEi2wv-x-b0EZKOs7DC_nNIS2Kn");
        query = new QueryBean();
        query.setCategoryId("iaA4T76ihpvmAhEtc");
        query.setRouteId("ZcakJrZnpuwg9fXoE");
        requestBean = new RequestBean();
        requestBean.setHeaderBean(header);
        requestBean.setQueryBean(query);

        // Make request
        getScoreJs = mCrimpWSImpl.getScore(requestBean);

        // Verify response
        assertThat(getScoreJs.getClimberScores().size(), is(2));

        climber0 = getScoreJs.getClimberScores().get(0);
        assertThat(climber0.getClimberId(), is("climberId1"));
        assertThat(climber0.getClimberName(), is("Antonio Paul"));
        assertThat(climber0.getScores().size(), is(1));
        assertThat(climber0.getScores().get(0).getMarkerId(), is("NMQ004"));
        assertThat(climber0.getScores().get(0).getCategoryId(), is("iaA4T76ihpvmAhEtc"));
        assertThat(climber0.getScores().get(0).getRouteId(), is("ZcakJrZnpuwg9fXoE"));
        assertThat(climber0.getScores().get(0).getScore(), is("1000"));

        climber1 = getScoreJs.getClimberScores().get(1);
        assertThat(climber1.getClimberId(), is("climberId3"));
        assertThat(climber1.getClimberName(), is("Tranny"));
        assertThat(climber1.getScores().size(), is(1));
        assertThat(climber1.getScores().get(0).getMarkerId(), is("NMQ006"));
        assertThat(climber1.getScores().get(0).getCategoryId(), is("iaA4T76ihpvmAhEtc"));
        assertThat(climber1.getScores().get(0).getRouteId(), is("ZcakJrZnpuwg9fXoE"));
        assertThat(climber1.getScores().get(0).getScore(), is(""));

        /*********************************************************************/

        // Prepare request
        header = new HeaderBean();
        header.setxUserId("6Sw3aLjKMtaDRHMor");
        header.setxAuthToken("VpQmdlKiikB-aauJaEi2wv-x-b0EZKOs7DC_nNIS2Kn");
        query = new QueryBean();
        query.setCategoryId("iaA4T76ihpvmAhEtc");
        query.setMarkerId("NMQ004");
        requestBean = new RequestBean();
        requestBean.setHeaderBean(header);
        requestBean.setQueryBean(query);

        // Make request
        getScoreJs = mCrimpWSImpl.getScore(requestBean);

        // Verify response
        assertThat(getScoreJs.getClimberScores().size(), is(1));

        climber0 = getScoreJs.getClimberScores().get(0);
        assertThat(climber0.getClimberId(), is("climberId1"));
        assertThat(climber0.getClimberName(), is("Antonio Paul"));
        assertThat(climber0.getScores().size(), is(3));
        assertThat(climber0.getScores().get(0).getMarkerId(), is("NMQ004"));
        assertThat(climber0.getScores().get(0).getCategoryId(), is("iaA4T76ihpvmAhEtc"));
        assertThat(climber0.getScores().get(0).getRouteId(), is("ZcakJrZnpuwg9fXoE"));
        assertThat(climber0.getScores().get(0).getScore(), is("1000"));
        assertThat(climber0.getScores().get(1).getMarkerId(), is("NMQ004"));
        assertThat(climber0.getScores().get(1).getCategoryId(), is("iaA4T76ihpvmAhEtc"));
        assertThat(climber0.getScores().get(1).getRouteId(), is("2uALvGuYPxB9m8jAm"));
        assertThat(climber0.getScores().get(1).getScore(), is("800"));
        assertThat(climber0.getScores().get(2).getMarkerId(), is("NMQ004"));
        assertThat(climber0.getScores().get(2).getCategoryId(), is("iaA4T76ihpvmAhEtc"));
        assertThat(climber0.getScores().get(2).getRouteId(), is("juoFyiJSfnmTWvsXC"));
        assertThat(climber0.getScores().get(2).getScore(), is(""));

        /*********************************************************************/

        // Prepare request
        header = new HeaderBean();
        header.setxUserId("6Sw3aLjKMtaDRHMor");
        header.setxAuthToken("VpQmdlKiikB-aauJaEi2wv-x-b0EZKOs7DC_nNIS2Kn");
        query = new QueryBean();
        query.setRouteId("ZcakJrZnpuwg9fXoE");
        query.setMarkerId("NMQ004");
        requestBean = new RequestBean();
        requestBean.setHeaderBean(header);
        requestBean.setQueryBean(query);

        // Make request
        getScoreJs = mCrimpWSImpl.getScore(requestBean);

        // Verify response
        assertThat(getScoreJs.getClimberScores().size(), is(1));

        climber0 = getScoreJs.getClimberScores().get(0);
        assertThat(climber0.getClimberId(), is("climberId1"));
        assertThat(climber0.getClimberName(), is("Antonio Paul"));
        assertThat(climber0.getScores().size(), is(1));
        assertThat(climber0.getScores().get(0).getMarkerId(), is("NMQ004"));
        assertThat(climber0.getScores().get(0).getCategoryId(), is("iaA4T76ihpvmAhEtc"));
        assertThat(climber0.getScores().get(0).getRouteId(), is("ZcakJrZnpuwg9fXoE"));
        assertThat(climber0.getScores().get(0).getScore(), is("1000"));

        /*********************************************************************/

        // Prepare request
        header = new HeaderBean();
        header.setxUserId("6Sw3aLjKMtaDRHMor");
        header.setxAuthToken("VpQmdlKiikB-aauJaEi2wv-x-b0EZKOs7DC_nNIS2Kn");
        query = new QueryBean();
        query.setClimberId("climberId3");
        query.setCategoryId("iaA4T76ihpvmAhEtc");
        query.setRouteId("ZcakJrZnpuwg9fXoE");
        requestBean = new RequestBean();
        requestBean.setHeaderBean(header);
        requestBean.setQueryBean(query);

        // Make request
        getScoreJs = mCrimpWSImpl.getScore(requestBean);

        // Verify response
        assertThat(getScoreJs.getClimberScores().size(), is(1));

        climber0 = getScoreJs.getClimberScores().get(0);
        assertThat(climber0.getClimberId(), is("climberId3"));
        assertThat(climber0.getClimberName(), is("Tranny"));
        assertThat(climber0.getScores().size(), is(1));
        assertThat(climber0.getScores().get(0).getMarkerId(), is("NMQ006"));
        assertThat(climber0.getScores().get(0).getCategoryId(), is("iaA4T76ihpvmAhEtc"));
        assertThat(climber0.getScores().get(0).getRouteId(), is("ZcakJrZnpuwg9fXoE"));
        assertThat(climber0.getScores().get(0).getScore(), is(""));

        /*********************************************************************/

        // Prepare request
        header = new HeaderBean();
        header.setxUserId("6Sw3aLjKMtaDRHMor");
        header.setxAuthToken("VpQmdlKiikB-aauJaEi2wv-x-b0EZKOs7DC_nNIS2Kn");
        query = new QueryBean();
        query.setClimberId("climberId3");
        query.setCategoryId("iaA4T76ihpvmAhEtc");
        query.setMarkerId("NMQ006");
        requestBean = new RequestBean();
        requestBean.setHeaderBean(header);
        requestBean.setQueryBean(query);

        // Make request
        getScoreJs = mCrimpWSImpl.getScore(requestBean);

        // Verify response
        assertThat(getScoreJs.getClimberScores().size(), is(1));

        climber0 = getScoreJs.getClimberScores().get(0);
        assertThat(climber0.getClimberId(), is("climberId3"));
        assertThat(climber0.getClimberName(), is("Tranny"));
        assertThat(climber0.getScores().size(), is(3));
        assertThat(climber0.getScores().get(0).getMarkerId(), is("NMQ006"));
        assertThat(climber0.getScores().get(0).getCategoryId(), is("iaA4T76ihpvmAhEtc"));
        assertThat(climber0.getScores().get(0).getRouteId(), is("ZcakJrZnpuwg9fXoE"));
        assertThat(climber0.getScores().get(0).getScore(), is(""));
        assertThat(climber0.getScores().get(1).getMarkerId(), is("NMQ006"));
        assertThat(climber0.getScores().get(1).getCategoryId(), is("iaA4T76ihpvmAhEtc"));
        assertThat(climber0.getScores().get(1).getRouteId(), is("2uALvGuYPxB9m8jAm"));
        assertThat(climber0.getScores().get(1).getScore(), is("800"));
        assertThat(climber0.getScores().get(2).getMarkerId(), is("NMQ006"));
        assertThat(climber0.getScores().get(2).getCategoryId(), is("iaA4T76ihpvmAhEtc"));
        assertThat(climber0.getScores().get(2).getRouteId(), is("juoFyiJSfnmTWvsXC"));
        assertThat(climber0.getScores().get(2).getScore(), is("1800"));

        /*********************************************************************/

        // Prepare request
        header = new HeaderBean();
        header.setxUserId("6Sw3aLjKMtaDRHMor");
        header.setxAuthToken("VpQmdlKiikB-aauJaEi2wv-x-b0EZKOs7DC_nNIS2Kn");
        query = new QueryBean();
        query.setClimberId("climberId3");
        query.setRouteId("ZcakJrZnpuwg9fXoE");
        query.setMarkerId("NMQ006");
        requestBean = new RequestBean();
        requestBean.setHeaderBean(header);
        requestBean.setQueryBean(query);

        // Make request
        getScoreJs = mCrimpWSImpl.getScore(requestBean);

        // Verify response
        assertThat(getScoreJs.getClimberScores().size(), is(1));

        climber0 = getScoreJs.getClimberScores().get(0);
        assertThat(climber0.getClimberId(), is("climberId3"));
        assertThat(climber0.getClimberName(), is("Tranny"));
        assertThat(climber0.getScores().size(), is(1));
        assertThat(climber0.getScores().get(0).getMarkerId(), is("NMQ006"));
        assertThat(climber0.getScores().get(0).getCategoryId(), is("iaA4T76ihpvmAhEtc"));
        assertThat(climber0.getScores().get(0).getRouteId(), is("ZcakJrZnpuwg9fXoE"));
        assertThat(climber0.getScores().get(0).getScore(), is(""));

        /*********************************************************************/

        // Prepare request
        header = new HeaderBean();
        header.setxUserId("6Sw3aLjKMtaDRHMor");
        header.setxAuthToken("VpQmdlKiikB-aauJaEi2wv-x-b0EZKOs7DC_nNIS2Kn");
        query = new QueryBean();
        query.setCategoryId("iaA4T76ihpvmAhEtc");
        query.setRouteId("ZcakJrZnpuwg9fXoE");
        query.setMarkerId("NMQ004");
        requestBean = new RequestBean();
        requestBean.setHeaderBean(header);
        requestBean.setQueryBean(query);

        // Make request
        getScoreJs = mCrimpWSImpl.getScore(requestBean);

        // Verify response
        assertThat(getScoreJs.getClimberScores().size(), is(1));

        climber0 = getScoreJs.getClimberScores().get(0);
        assertThat(climber0.getClimberId(), is("climberId1"));
        assertThat(climber0.getClimberName(), is("Antonio Paul"));
        assertThat(climber0.getScores().size(), is(1));
        assertThat(climber0.getScores().get(0).getMarkerId(), is("NMQ004"));
        assertThat(climber0.getScores().get(0).getCategoryId(), is("iaA4T76ihpvmAhEtc"));
        assertThat(climber0.getScores().get(0).getRouteId(), is("ZcakJrZnpuwg9fXoE"));
        assertThat(climber0.getScores().get(0).getScore(), is("1000"));

        /*********************************************************************/

        // Prepare request
        header = new HeaderBean();
        header.setxUserId("6Sw3aLjKMtaDRHMor");
        header.setxAuthToken("VpQmdlKiikB-aauJaEi2wv-x-b0EZKOs7DC_nNIS2Kn");
        query = new QueryBean();
        query.setClimberId("climberId3");
        query.setCategoryId("iaA4T76ihpvmAhEtc");
        query.setRouteId("ZcakJrZnpuwg9fXoE");
        query.setMarkerId("NMQ006");
        requestBean = new RequestBean();
        requestBean.setHeaderBean(header);
        requestBean.setQueryBean(query);

        // Make request
        getScoreJs = mCrimpWSImpl.getScore(requestBean);

        // Verify response
        assertThat(getScoreJs.getClimberScores().size(), is(1));

        climber0 = getScoreJs.getClimberScores().get(0);
        assertThat(climber0.getClimberId(), is("climberId3"));
        assertThat(climber0.getClimberName(), is("Tranny"));
        assertThat(climber0.getScores().size(), is(1));
        assertThat(climber0.getScores().get(0).getMarkerId(), is("NMQ006"));
        assertThat(climber0.getScores().get(0).getCategoryId(), is("iaA4T76ihpvmAhEtc"));
        assertThat(climber0.getScores().get(0).getRouteId(), is("ZcakJrZnpuwg9fXoE"));
        assertThat(climber0.getScores().get(0).getScore(), is(""));
    }

    @Test
    public void testPostScore() throws IOException {
        RequestBodyJs body;
        PathBean path;
        HeaderBean header;
        RequestBean requestBean;

        // Prepare request
        header = new HeaderBean();
        header.setxUserId("6Sw3aLjKMtaDRHMor");
        header.setxAuthToken("VpQmdlKiikB-aauJaEi2wv-x-b0EZKOs7DC_nNIS2Kn");
        body = new RequestBodyJs();
        body.setScoreString("11T");
        path = new PathBean();
        path.setRouteId("ZcakJrZnpuwg9fXoE");
        path.setMarkerId("NMF002");
        requestBean = new RequestBean();
        requestBean.setRequestBodyJs(body);
        requestBean.setHeaderBean(header);
        requestBean.setPathBean(path);

        // Make request
        PostScoreJs response1 = mCrimpWSImpl.postScore(requestBean);

        // Verify response
        assertThat(response1.getClimberId(), is("climberId1"));
        assertThat(response1.getCategoryId(), is("iaA4T76ihpvmAhEtc"));
        assertThat(response1.getRouteId(), is("ZcakJrZnpuwg9fXoE"));
        assertThat(response1.getMarkerId(), is("NMF002"));
        assertThat(response1.getScore(), is("11B11T"));
    }

    @Test
    public void testHelpMe() throws IOException {
        HeaderBean header;
        RequestBodyJs body;
        RequestBean requestBean;

        // Prepare request
        header = new HeaderBean();
        header.setxUserId("6Sw3aLjKMtaDRHMor");
        header.setxAuthToken("VpQmdlKiikB-aauJaEi2wv-x-b0EZKOs7DC_nNIS2Kn");
        body = new RequestBodyJs();
        body.setRouteId("ZcakJrZnpuwg9fXoE");
        requestBean = new RequestBean();
        requestBean.setHeaderBean(header);
        requestBean.setRequestBodyJs(body);

        // Make request
        HelpMeJs response1 = mCrimpWSImpl.requestHelp(requestBean);

        // Verify response
        assertThat(response1, is(not(nullValue())));
    }

    @Test
    public void testSetActive() throws IOException {
        HeaderBean header;
        RequestBodyJs body;
        RequestBean requestBean;

        // Prepare request
        header = new HeaderBean();
        header.setxUserId("6Sw3aLjKMtaDRHMor");
        header.setxAuthToken("VpQmdlKiikB-aauJaEi2wv-x-b0EZKOs7DC_nNIS2Kn");
        body = new RequestBodyJs();
        body.setRouteId("ZcakJrZnpuwg9fXoE");
        body.setMarkerId("NMQ004");
        requestBean = new RequestBean();
        requestBean.setHeaderBean(header);
        requestBean.setRequestBodyJs(body);

        // Make request
        SetActiveJs response1 = mCrimpWSImpl.setActive(requestBean);

        // Verify response
        assertThat(response1.getRouteId(), is("ZcakJrZnpuwg9fXoE"));
        assertThat(response1.getMarkerId(), is("NMQ004"));
        assertThat(response1.getClimberId(), is("climberId1"));
        assertThat(response1.getClimberName(), is("Antonio Paul"));
    }

    @Test
    public void testClearActive() throws IOException {
        HeaderBean header;
        RequestBodyJs body;
        RequestBean requestBean;

        // Prepare request
        header = new HeaderBean();
        header.setxUserId("6Sw3aLjKMtaDRHMor");
        header.setxAuthToken("VpQmdlKiikB-aauJaEi2wv-x-b0EZKOs7DC_nNIS2Kn");
        body = new RequestBodyJs();
        body.setRouteId("ZcakJrZnpuwg9fXoE");
        requestBean = new RequestBean();
        requestBean.setHeaderBean(header);
        requestBean.setRequestBodyJs(body);

        // Make request
        ClearActiveJs response1 = mCrimpWSImpl.clearActive(requestBean);

        // Verify response
        assertThat(response1, is(not(nullValue())));
    }
}