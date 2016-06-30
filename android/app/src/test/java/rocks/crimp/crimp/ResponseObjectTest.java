package rocks.crimp.crimp;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

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
import rocks.crimp.crimp.network.model.SetActiveJs;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class ResponseObjectTest {

    @Test
    public void testCategoriesJs() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        File file1 = new File("src/test/testFile", "categories");
        CategoriesJs categoriesJs1 = mapper.readValue(file1, CategoriesJs.class);

        assertThat(categoriesJs1.getCategories().size(), is(2));

        CategoryJs category0 = categoriesJs1.getCategories().get(0);
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

        CategoryJs category1 = categoriesJs1.getCategories().get(1);
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
    public void testLoginJs() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        File file1 = new File("src/test/testFile", "login");
        LoginJs loginJs1 = mapper.readValue(file1, LoginJs.class);

        assertThat(loginJs1.getxUserId(), is("6Sw3aLjKMtaDRHMor"));
        assertThat(loginJs1.getxAuthToken(), is("VpQmdlKiikB-aauJaEi2wv-x-b0EZKOs7DC_nNIS2Kn"));
        assertThat(loginJs1.getRemindLogout(), is(true));
        assertThat(loginJs1.getRoles().size(), is(2));
        assertThat(loginJs1.getRoles().contains("judge"), is(true));
        assertThat(loginJs1.getRoles().contains("admin"), is(true));
        assertThat(loginJs1.getError(), is(nullValue()));

        /*********************************************************************/

        File file2 = new File("src/test/testFile", "login_error");
        LoginJs loginJs2 = mapper.readValue(file2, LoginJs.class);

        assertThat(loginJs2.getxUserId(), is(nullValue()));
        assertThat(loginJs2.getxAuthToken(), is(nullValue()));
        assertThat(loginJs2.getRemindLogout(), is(nullValue()));
        assertThat(loginJs2.getRoles(), is(nullValue()));
        assertThat(loginJs2.getError(), is("some error message"));
    }

    @Test
    public void testReportJs() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        File file1 = new File("src/test/testFile", "report");
        ReportJs reportJs1 = mapper.readValue(file1, ReportJs.class);

        assertThat(reportJs1.getxUserId(), is("6Sw3aLjKMtaDRHMor"));
        assertThat(reportJs1.getUserName(), is("Weizhi"));
        assertThat(reportJs1.getCategoryId(), is("iaA4T76ihpvmAhEtc"));
        assertThat(reportJs1.getRouteId(), is("ZcakJrZnpuwg9fXoE"));
    }

    @Test
    public void testLogoutJs() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        File file1 = new File("src/test/testFile", "logout");
        LogoutJs logoutJs1 = mapper.readValue(file1, LogoutJs.class);

        assertThat(logoutJs1, is(not(nullValue())));
    }

    @Test
    public void testGetScoreJs() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        File file;
        GetScoreJs getScoreJs;
        ClimberScoreJs climber0;
        ClimberScoreJs climber1;
        ClimberScoreJs climber2;

        file = new File("src/test/testFile", "getscore");
        getScoreJs = mapper.readValue(file, GetScoreJs.class);

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

        file = new File("src/test/testFile", "getscore-climber_id");
        getScoreJs = mapper.readValue(file, GetScoreJs.class);

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

        file = new File("src/test/testFile", "getscore-category_id");
        getScoreJs = mapper.readValue(file, GetScoreJs.class);

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

        file = new File("src/test/testFile", "getscore-route_id");
        getScoreJs = mapper.readValue(file, GetScoreJs.class);

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

        file = new File("src/test/testFile", "getscore-marker_id");
        getScoreJs = mapper.readValue(file, GetScoreJs.class);

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

        file = new File("src/test/testFile", "getscore-climber_id-category_id");
        getScoreJs = mapper.readValue(file, GetScoreJs.class);

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

        file = new File("src/test/testFile", "getscore-climber_id-route_id");
        getScoreJs = mapper.readValue(file, GetScoreJs.class);

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

        file = new File("src/test/testFile", "getscore-climber_id-marker_id");
        getScoreJs = mapper.readValue(file, GetScoreJs.class);

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

        file = new File("src/test/testFile", "getscore-category_id-route_id");
        getScoreJs = mapper.readValue(file, GetScoreJs.class);

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

        file = new File("src/test/testFile", "getscore-category_id-marker_id");
        getScoreJs = mapper.readValue(file, GetScoreJs.class);

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

        file = new File("src/test/testFile", "getscore-route_id-marker_id");
        getScoreJs = mapper.readValue(file, GetScoreJs.class);

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

        file = new File("src/test/testFile", "getscore-climber_id-category_id-route_id");
        getScoreJs = mapper.readValue(file, GetScoreJs.class);

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

        file = new File("src/test/testFile", "getscore-climber_id-category_id-marker_id");
        getScoreJs = mapper.readValue(file, GetScoreJs.class);

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

        file = new File("src/test/testFile", "getscore-climber_id-route_id-marker_id");
        getScoreJs = mapper.readValue(file, GetScoreJs.class);

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

        file = new File("src/test/testFile", "getscore-category_id-route_id-marker_id");
        getScoreJs = mapper.readValue(file, GetScoreJs.class);

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

        file = new File("src/test/testFile", "getscore-climber_id-category_id-route_id-marker_id");
        getScoreJs = mapper.readValue(file, GetScoreJs.class);

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
    public void testPostScoreJs() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        File file1 = new File("src/test/testFile", "postscore");
        PostScoreJs postScoreJs1 = mapper.readValue(file1, PostScoreJs.class);

        assertThat(postScoreJs1.getClimberId(), is("climberId1"));
        assertThat(postScoreJs1.getCategoryId(), is("iaA4T76ihpvmAhEtc"));
        assertThat(postScoreJs1.getRouteId(), is("ZcakJrZnpuwg9fXoE"));
        assertThat(postScoreJs1.getMarkerId(), is("NMF002"));
        assertThat(postScoreJs1.getScore(), is("11B11T"));
    }

    @Test
    public void testHelpMe() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        File file1 = new File("src/test/testFile", "helpme");
        HelpMeJs helpMeJs1 = mapper.readValue(file1, HelpMeJs.class);

        assertThat(helpMeJs1, is(not(nullValue())));
    }

    @Test
    public void testSetActive() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        File file1 = new File("src/test/testFile", "setactive");
        SetActiveJs setActiveJs1 = mapper.readValue(file1, SetActiveJs.class);

        assertThat(setActiveJs1.getRouteId(), is("ZcakJrZnpuwg9fXoE"));
        assertThat(setActiveJs1.getMarkerId(), is("NMQ004"));
        assertThat(setActiveJs1.getClimberId(), is("climberId1"));
        assertThat(setActiveJs1.getClimberName(), is("Antonio Paul"));
    }

    @Test
    public void testClearActive() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        File file1 = new File("src/test/testFile", "clearactive");
        ClearActiveJs clearActiveJs1 = mapper.readValue(file1, ClearActiveJs.class);

        assertThat(clearActiveJs1, is(not(nullValue())));
    }
}