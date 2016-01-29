package com.nusclimb.live.crimp.common.json;

import junit.framework.Assert;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import org.codehaus.jackson.map.ObjectWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class CategoryTest {
    CategoriesResponseBody.Category.Route mRoute1;
    CategoriesResponseBody.Category.Route mRoute2;

    private CategoriesResponseBody.Category mCategory;

    @Before
    public void setup(){
        mRoute1 = new CategoriesResponseBody.Category.Route();
        mRoute1.setRouteId("routeId1");
        mRoute1.setRouteName("routeName1");
        mRoute1.setScore("score1");

        mRoute2 = new CategoriesResponseBody.Category.Route();
        mRoute2.setRouteId("routeId2");
        mRoute2.setRouteName("routeName2");
        mRoute2.setScore("score2");

        mCategory = new CategoriesResponseBody.Category();
        mCategory.setCategoryName("categoryName");
        mCategory.setCategoryId("categoryId");
        ArrayList<CategoriesResponseBody.Category.Route> routes = new ArrayList<>();
        routes.add(mRoute1);
        routes.add(mRoute2);
        mCategory.setRoutes(routes);
        mCategory.setScoresFinalized(true);
        mCategory.setTimeStart("timeStart");
        mCategory.setTimeEnd("timeEnd");
    }

    @Test
    public void testCategory(){
        System.out.println("Testing Category constructor");
        CategoriesResponseBody.Category cloneCategory =
                new CategoriesResponseBody.Category(mCategory);
        Assert.assertNotSame(mCategory, cloneCategory);
        Assert.assertEquals(mCategory.getCategoryName(), cloneCategory.getCategoryName());
        Assert.assertEquals(mCategory.getCategoryId(), cloneCategory.getCategoryId());
        Assert.assertEquals(mCategory.isScoresFinalized(), cloneCategory.isScoresFinalized());
        Assert.assertEquals(mCategory.getTimeStart(), cloneCategory.getTimeStart());
        Assert.assertEquals(mCategory.getTimeEnd(), cloneCategory.getTimeEnd());

        ArrayList<CategoriesResponseBody.Category.Route> cloneRoutes = cloneCategory.getRoutes();
        Assert.assertNotSame(mCategory.getRoutes(), cloneRoutes);
        Assert.assertNotNull(cloneRoutes);
        Assert.assertEquals(2, cloneRoutes.size());

        Assert.assertNotSame(mRoute1, cloneRoutes.get(0));
        Assert.assertEquals(mRoute1.getRouteId(), cloneRoutes.get(0).getRouteId());
        Assert.assertEquals(mRoute1.getRouteName(), cloneRoutes.get(0).getRouteName());
        Assert.assertEquals(mRoute1.getScore(), cloneRoutes.get(0).getScore());

        Assert.assertNotSame(mRoute2, cloneRoutes.get(1));
        Assert.assertEquals(mRoute2.getRouteId(), cloneRoutes.get(1).getRouteId());
        Assert.assertEquals(mRoute2.getRouteName(), cloneRoutes.get(1).getRouteName());
        Assert.assertEquals(mRoute2.getScore(), cloneRoutes.get(1).getScore());
    }

    @Test
    public void testfindRouteById(){
        System.out.println("Testing Category.findRouteById()");
        CategoriesResponseBody.Category.Route foundRoute1 = mCategory.findRouteById("routeId1");
        Assert.assertSame(mRoute1, foundRoute1);

        CategoriesResponseBody.Category.Route foundRoute2 = mCategory.findRouteById("routeId2");
        Assert.assertSame(mRoute2, foundRoute2);
    }

    @Test
    public void testSerializeAndDeserialize() throws IOException {
        System.out.println("Testing Category serialization and deserialization");
        ObjectMapper mapper = new ObjectMapper();
        String serializeForm=null;
        serializeForm = mapper.writeValueAsString(mCategory);
        CategoriesResponseBody.Category deserializeForm = null;
        deserializeForm = mapper.readValue(serializeForm, CategoriesResponseBody.Category.class);

        Assert.assertNotSame(mCategory, deserializeForm);
        Assert.assertEquals(mCategory.getCategoryName(), deserializeForm.getCategoryName());
        Assert.assertEquals(mCategory.getCategoryId(), deserializeForm.getCategoryId());
        Assert.assertEquals(mCategory.isScoresFinalized(), deserializeForm.isScoresFinalized());
        Assert.assertEquals(mCategory.getTimeStart(), deserializeForm.getTimeStart());
        Assert.assertEquals(mCategory.getTimeEnd(), deserializeForm.getTimeEnd());

        ArrayList<CategoriesResponseBody.Category.Route> cloneRoutes = deserializeForm.getRoutes();
        Assert.assertNotNull(cloneRoutes);
        Assert.assertEquals(2, cloneRoutes.size());

        Assert.assertNotSame(mRoute1, cloneRoutes.get(0));
        Assert.assertEquals(mRoute1.getRouteId(), cloneRoutes.get(0).getRouteId());
        Assert.assertEquals(mRoute1.getRouteName(), cloneRoutes.get(0).getRouteName());
        Assert.assertEquals(mRoute1.getScore(), cloneRoutes.get(0).getScore());

        Assert.assertNotSame(mRoute2, cloneRoutes.get(1));
        Assert.assertEquals(mRoute2.getRouteId(), cloneRoutes.get(1).getRouteId());
        Assert.assertEquals(mRoute2.getRouteName(), cloneRoutes.get(1).getRouteName());
        Assert.assertEquals(mRoute2.getScore(), cloneRoutes.get(1).getScore());
    }

    @After
    public void tearDown(){
        mRoute1 = null;
        mRoute2 = null;
        mCategory = null;
    }
}
