package com.nusclimb.live.crimp.common.json;

import com.nusclimb.live.crimp.network.model.CategoriesJackson;

import junit.framework.Assert;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class RouteTest {
    private CategoriesJackson.Category.Route mRoute;

    @Before
    public void setup(){
        mRoute = new CategoriesJackson.Category.Route();
        mRoute.setRouteId("routeId");
        mRoute.setRouteName("routeName");
        mRoute.setScore("score");
    }

    @Test
    public void testRoute(){
        System.out.println("Testing Route constructor");
        CategoriesJackson.Category.Route cloneRoute =
                new CategoriesJackson.Category.Route(mRoute);
        Assert.assertEquals(mRoute.getRouteId(), cloneRoute.getRouteId());
        Assert.assertEquals(mRoute.getRouteName(), cloneRoute.getRouteName());
        Assert.assertEquals(mRoute.getScore(), cloneRoute.getScore());
        Assert.assertNotSame(mRoute, cloneRoute);
    }

    @Test
    public void testSerializeAndDeserialize() throws IOException {
        System.out.println("Testing Route serialization and deserialization");
        ObjectMapper mapper = new ObjectMapper();
        String serializeForm=null;
        serializeForm = mapper.writeValueAsString(mRoute);
        CategoriesJackson.Category.Route deserializeForm = null;
        deserializeForm = mapper.readValue(serializeForm,
                CategoriesJackson.Category.Route.class);

        Assert.assertEquals(mRoute.getRouteId(), deserializeForm.getRouteId());
        Assert.assertEquals(mRoute.getRouteName(), deserializeForm.getRouteName());
        Assert.assertEquals(mRoute.getScore(), deserializeForm.getScore());
        Assert.assertNotSame(mRoute, deserializeForm);
    }

    @After
    public void tearDown(){
        mRoute = null;
    }
}
