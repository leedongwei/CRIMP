package com.nusclimb.live.crimp.common.json;

import com.nusclimb.live.crimp.network.model.CategoriesJackson;

import junit.framework.Assert;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class CategoriesResponseBodyTest {
    private CategoriesJackson mCategoriesJackson;
    private CategoriesJackson.Category mCategoryA;
    private CategoriesJackson.Category mCategoryB;
    private CategoriesJackson.Category.Route mRouteA1;
    private CategoriesJackson.Category.Route mRouteA2;
    private CategoriesJackson.Category.Route mRouteA3;
    private CategoriesJackson.Category.Route mRouteB1;
    private CategoriesJackson.Category.Route mRouteB2;
    private CategoriesJackson.Category.Route mRouteB3;

    @Before
    public void setup(){
        mRouteA1 = new CategoriesJackson.Category.Route();
        mRouteA1.setRouteId("routeIdA1");
        mRouteA1.setRouteName("routeNameA1");
        mRouteA1.setScore("scoreA1");

        mRouteA2 = new CategoriesJackson.Category.Route();
        mRouteA2.setRouteId("routeIdA2");
        mRouteA2.setRouteName("routeNameA2");
        mRouteA2.setScore("scoreA2");

        mRouteA3 = new CategoriesJackson.Category.Route();
        mRouteA3.setRouteId("routeIdA3");
        mRouteA3.setRouteName("routeNameA3");
        mRouteA3.setScore("scoreA3");

        mRouteB1 = new CategoriesJackson.Category.Route();
        mRouteB1.setRouteId("routeIdB1");
        mRouteB1.setRouteName("routeNameB1");
        mRouteB1.setScore("scoreB1");

        mRouteB2 = new CategoriesJackson.Category.Route();
        mRouteB2.setRouteId("routeIdB2");
        mRouteB2.setRouteName("routeNameB2");
        mRouteB2.setScore("scoreB2");

        mRouteB3 = new CategoriesJackson.Category.Route();
        mRouteB3.setRouteId("routeIdB3");
        mRouteB3.setRouteName("routeNameB3");
        mRouteB3.setScore("scoreB3");

        mCategoryA = new CategoriesJackson.Category();
        mCategoryA.setCategoryName("categoryNameA");
        mCategoryA.setCategoryId("categoryIdA");
        ArrayList<CategoriesJackson.Category.Route> routesA = new ArrayList<>();
        routesA.add(mRouteA1);
        routesA.add(mRouteA2);
        routesA.add(mRouteA3);
        mCategoryA.setRoutes(routesA);
        mCategoryA.setScoresFinalized(true);
        mCategoryA.setTimeStart("timeStartA");
        mCategoryA.setTimeEnd("timeEndA");

        mCategoryB = new CategoriesJackson.Category();
        mCategoryB.setCategoryName("categoryNameB");
        mCategoryB.setCategoryId("categoryIdB");
        ArrayList<CategoriesJackson.Category.Route> routesB = new ArrayList<>();
        routesB.add(mRouteB1);
        routesB.add(mRouteB2);
        routesB.add(mRouteB3);
        mCategoryB.setRoutes(routesB);
        mCategoryB.setScoresFinalized(false);
        mCategoryB.setTimeStart("timeStartB");
        mCategoryB.setTimeEnd("timeEndB");

        ArrayList<CategoriesJackson.Category> categories = new ArrayList<>();
        categories.add(mCategoryA);
        categories.add(mCategoryB);
        mCategoriesJackson = new CategoriesJackson();
        mCategoriesJackson.setCategories(categories);
    }

    @Test
    public void testCategoriesResponseBody(){
        System.out.println("Testing CategoriesJackson constructor");
        CategoriesJackson cloneCategoriesJackson =
                new CategoriesJackson(mCategoriesJackson);
        Assert.assertNotSame(mCategoriesJackson, cloneCategoriesJackson);

        ArrayList<CategoriesJackson.Category> cloneCategories =
                cloneCategoriesJackson.getCategories();
        Assert.assertNotSame(mCategoriesJackson.getCategories(), cloneCategories);
        Assert.assertNotNull(cloneCategories);
        Assert.assertEquals(2, cloneCategories.size());

        // Check CategoryA
        Assert.assertNotSame(mCategoryA, cloneCategories.get(0));
        Assert.assertEquals(mCategoryA.getCategoryName(), cloneCategories.get(0).getCategoryName());
        Assert.assertEquals(mCategoryA.getCategoryId(), cloneCategories.get(0).getCategoryId());
        Assert.assertEquals(mCategoryA.isScoresFinalized(),
                cloneCategories.get(0).isScoresFinalized());
        Assert.assertEquals(mCategoryA.getTimeStart(), cloneCategories.get(0).getTimeStart());
        Assert.assertEquals(mCategoryA.getTimeEnd(), cloneCategories.get(0).getTimeEnd());

        // Check CategoryA's routes
        ArrayList<CategoriesJackson.Category.Route> cloneRoutesA =
                cloneCategories.get(0).getRoutes();
        Assert.assertNotSame(mCategoriesJackson.getCategories().get(0).getRoutes(),
                cloneRoutesA);
        Assert.assertNotNull(cloneRoutesA);
        Assert.assertEquals(3, cloneRoutesA.size());

        Assert.assertNotSame(mRouteA1, cloneRoutesA.get(0));
        Assert.assertEquals(mRouteA1.getRouteId(), cloneRoutesA.get(0).getRouteId());
        Assert.assertEquals(mRouteA1.getRouteName(), cloneRoutesA.get(0).getRouteName());
        Assert.assertEquals(mRouteA1.getScore(), cloneRoutesA.get(0).getScore());

        Assert.assertNotSame(mRouteA2, cloneRoutesA.get(1));
        Assert.assertEquals(mRouteA2.getRouteId(), cloneRoutesA.get(1).getRouteId());
        Assert.assertEquals(mRouteA2.getRouteName(), cloneRoutesA.get(1).getRouteName());
        Assert.assertEquals(mRouteA2.getScore(), cloneRoutesA.get(1).getScore());

        Assert.assertNotSame(mRouteA3, cloneRoutesA.get(2));
        Assert.assertEquals(mRouteA3.getRouteId(), cloneRoutesA.get(2).getRouteId());
        Assert.assertEquals(mRouteA3.getRouteName(), cloneRoutesA.get(2).getRouteName());
        Assert.assertEquals(mRouteA3.getScore(), cloneRoutesA.get(2).getScore());

        // Check CategoryB
        Assert.assertNotSame(mCategoryB, cloneCategories.get(1));
        Assert.assertEquals(mCategoryB.getCategoryName(), cloneCategories.get(1).getCategoryName());
        Assert.assertEquals(mCategoryB.getCategoryId(), cloneCategories.get(1).getCategoryId());
        Assert.assertEquals(mCategoryB.isScoresFinalized(),
                cloneCategories.get(1).isScoresFinalized());
        Assert.assertEquals(mCategoryB.getTimeStart(), cloneCategories.get(1).getTimeStart());
        Assert.assertEquals(mCategoryB.getTimeEnd(), cloneCategories.get(1).getTimeEnd());

        // Check CategoryB's routes
        ArrayList<CategoriesJackson.Category.Route> cloneRoutesB =
                cloneCategories.get(1).getRoutes();
        Assert.assertNotSame(mCategoriesJackson.getCategories().get(1).getRoutes(),
                cloneRoutesB);
        Assert.assertNotNull(cloneRoutesB);
        Assert.assertEquals(3, cloneRoutesB.size());

        Assert.assertNotSame(mRouteB1, cloneRoutesB.get(0));
        Assert.assertEquals(mRouteB1.getRouteId(), cloneRoutesB.get(0).getRouteId());
        Assert.assertEquals(mRouteB1.getRouteName(), cloneRoutesB.get(0).getRouteName());
        Assert.assertEquals(mRouteB1.getScore(), cloneRoutesB.get(0).getScore());

        Assert.assertNotSame(mRouteB2, cloneRoutesB.get(1));
        Assert.assertEquals(mRouteB2.getRouteId(), cloneRoutesB.get(1).getRouteId());
        Assert.assertEquals(mRouteB2.getRouteName(), cloneRoutesB.get(1).getRouteName());
        Assert.assertEquals(mRouteB2.getScore(), cloneRoutesB.get(1).getScore());

        Assert.assertNotSame(mRouteB3, cloneRoutesB.get(2));
        Assert.assertEquals(mRouteB3.getRouteId(), cloneRoutesB.get(2).getRouteId());
        Assert.assertEquals(mRouteB3.getRouteName(), cloneRoutesB.get(2).getRouteName());
        Assert.assertEquals(mRouteB3.getScore(), cloneRoutesB.get(2).getScore());
    }

    @Test
    public void testFindCategoryById(){
        System.out.println("Testing CategoriesJackson.findCategoryById()");
        CategoriesJackson.Category foundCategoryA =
                mCategoriesJackson.findCategoryById("categoryIdA");
        CategoriesJackson.Category foundCategoryB =
                mCategoriesJackson.findCategoryById("categoryIdB");
        Assert.assertSame(mCategoryA, foundCategoryA);
        Assert.assertSame(mCategoryB, foundCategoryB);
    }

    @Test
    public void testSerializeAndDeserialize() throws IOException {
        System.out.println("Testing CategoriesJackson serialization and deserialization");
        ObjectMapper mapper = new ObjectMapper();
        String serializeForm=null;
        serializeForm = mapper.writeValueAsString(mCategoriesJackson);
        CategoriesJackson deserializeForm = null;
        deserializeForm = mapper.readValue(serializeForm, CategoriesJackson.class);

        Assert.assertNotSame(mCategoriesJackson, deserializeForm);
        ArrayList<CategoriesJackson.Category> cloneCategories =
                deserializeForm.getCategories();
        Assert.assertNotSame(mCategoriesJackson.getCategories(), cloneCategories);
        Assert.assertNotNull(cloneCategories);
        Assert.assertEquals(2, cloneCategories.size());

        // Check CategoryA
        Assert.assertNotSame(mCategoryA, cloneCategories.get(0));
        Assert.assertEquals(mCategoryA.getCategoryName(), cloneCategories.get(0).getCategoryName());
        Assert.assertEquals(mCategoryA.getCategoryId(), cloneCategories.get(0).getCategoryId());
        Assert.assertEquals(mCategoryA.isScoresFinalized(),
                cloneCategories.get(0).isScoresFinalized());
        Assert.assertEquals(mCategoryA.getTimeStart(), cloneCategories.get(0).getTimeStart());
        Assert.assertEquals(mCategoryA.getTimeEnd(), cloneCategories.get(0).getTimeEnd());

        // Check CategoryA's routes
        ArrayList<CategoriesJackson.Category.Route> cloneRoutesA =
                cloneCategories.get(0).getRoutes();
        Assert.assertNotSame(mCategoriesJackson.getCategories().get(0).getRoutes(),
                cloneRoutesA);
        Assert.assertNotNull(cloneRoutesA);
        Assert.assertEquals(3, cloneRoutesA.size());

        Assert.assertNotSame(mRouteA1, cloneRoutesA.get(0));
        Assert.assertEquals(mRouteA1.getRouteId(), cloneRoutesA.get(0).getRouteId());
        Assert.assertEquals(mRouteA1.getRouteName(), cloneRoutesA.get(0).getRouteName());
        Assert.assertEquals(mRouteA1.getScore(), cloneRoutesA.get(0).getScore());

        Assert.assertNotSame(mRouteA2, cloneRoutesA.get(1));
        Assert.assertEquals(mRouteA2.getRouteId(), cloneRoutesA.get(1).getRouteId());
        Assert.assertEquals(mRouteA2.getRouteName(), cloneRoutesA.get(1).getRouteName());
        Assert.assertEquals(mRouteA2.getScore(), cloneRoutesA.get(1).getScore());

        Assert.assertNotSame(mRouteA3, cloneRoutesA.get(2));
        Assert.assertEquals(mRouteA3.getRouteId(), cloneRoutesA.get(2).getRouteId());
        Assert.assertEquals(mRouteA3.getRouteName(), cloneRoutesA.get(2).getRouteName());
        Assert.assertEquals(mRouteA3.getScore(), cloneRoutesA.get(2).getScore());

        // Check CategoryB
        Assert.assertNotSame(mCategoryB, cloneCategories.get(1));
        Assert.assertEquals(mCategoryB.getCategoryName(), cloneCategories.get(1).getCategoryName());
        Assert.assertEquals(mCategoryB.getCategoryId(), cloneCategories.get(1).getCategoryId());
        Assert.assertEquals(mCategoryB.isScoresFinalized(),
                cloneCategories.get(1).isScoresFinalized());
        Assert.assertEquals(mCategoryB.getTimeStart(), cloneCategories.get(1).getTimeStart());
        Assert.assertEquals(mCategoryB.getTimeEnd(), cloneCategories.get(1).getTimeEnd());

        // Check CategoryB's routes
        ArrayList<CategoriesJackson.Category.Route> cloneRoutesB =
                cloneCategories.get(1).getRoutes();
        Assert.assertNotSame(mCategoriesJackson.getCategories().get(1).getRoutes(),
                cloneRoutesB);
        Assert.assertNotNull(cloneRoutesB);
        Assert.assertEquals(3, cloneRoutesB.size());

        Assert.assertNotSame(mRouteB1, cloneRoutesB.get(0));
        Assert.assertEquals(mRouteB1.getRouteId(), cloneRoutesB.get(0).getRouteId());
        Assert.assertEquals(mRouteB1.getRouteName(), cloneRoutesB.get(0).getRouteName());
        Assert.assertEquals(mRouteB1.getScore(), cloneRoutesB.get(0).getScore());

        Assert.assertNotSame(mRouteB2, cloneRoutesB.get(1));
        Assert.assertEquals(mRouteB2.getRouteId(), cloneRoutesB.get(1).getRouteId());
        Assert.assertEquals(mRouteB2.getRouteName(), cloneRoutesB.get(1).getRouteName());
        Assert.assertEquals(mRouteB2.getScore(), cloneRoutesB.get(1).getScore());

        Assert.assertNotSame(mRouteB3, cloneRoutesB.get(2));
        Assert.assertEquals(mRouteB3.getRouteId(), cloneRoutesB.get(2).getRouteId());
        Assert.assertEquals(mRouteB3.getRouteName(), cloneRoutesB.get(2).getRouteName());
        Assert.assertEquals(mRouteB3.getScore(), cloneRoutesB.get(2).getScore());
    }

    @After
    public void tearDown(){
        mCategoryA = null;
        mCategoryB = null;
        mRouteA1 = null;
        mRouteA2 = null;
        mRouteA3 = null;
        mRouteB1 = null;
        mRouteB2 = null;
        mRouteB3 = null;
    }
}
