package rocks.crimp.crimp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import rocks.crimp.crimp.network.model.CategoriesJs;
import rocks.crimp.crimp.network.model.CategoryJs;
import rocks.crimp.crimp.network.model.RouteJs;
import rocks.crimp.crimp.persistence.LocalModel;
import rocks.crimp.crimp.persistence.LocalModelImpl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class LocalModelTest {
    private static final int TEST_SIZE = 10;
    private static final String CATEGORIES_FILE = "categories_file";
    private LocalModel mLocalModel;

    @Before
    public void onBefore() {
        mLocalModel = LocalModelImpl.getInstance();
        mLocalModel.setupModel(new File(LocalModelImpl.DISK_CACHE_FILE_DIR));
    }

    @After
    public void onAfter(){
        mLocalModel.deleteModel();
    }

    @Test
    public void testIsDataExist()  {
        // prepare test data
        ArrayList<PairString> testDataList = new ArrayList<>();
        for(int i=0; i<TEST_SIZE; i++){
            testDataList.add(new PairString());
        }
        testDataList.add(new PairString("a", "aa"));
        testDataList.add(new PairString("b", "bb"));
        testDataList.add(new PairString("c", "cc"));
        testDataList.add(new PairString("d", "dd"));
        testDataList.add(new PairString("e", "ee"));

        for(PairString testData : testDataList){
            mLocalModel.putData(testData.key.toString(), testData.value);
        }

        Collections.shuffle(testDataList);

        for(PairString testData : testDataList){
            String nonExistKey = UUID.randomUUID().toString();
            String failMessage1 = "Failed to find ["+testData.key+":"+testData.value+"]";
            String failMessage2 = "Should not have found "+nonExistKey;
            assertThat(failMessage1, mLocalModel.isDataExist(testData.key.toString()), is(true));
            assertThat(failMessage2, mLocalModel.isDataExist(nonExistKey), is(false));
        }
    }

    @Test
    public void testFetch()  {
        // prepare test data
        ArrayList<PairString> testDataList = new ArrayList<>();
        for(int i=0; i<TEST_SIZE; i++){
            testDataList.add(new PairString());
        }
        testDataList.add(new PairString("a", "aa"));
        testDataList.add(new PairString("b", "bb"));
        testDataList.add(new PairString("c", "cc"));
        testDataList.add(new PairString("d", "dd"));
        testDataList.add(new PairString("e", "ee"));

        for(PairString testData : testDataList){
            mLocalModel.putData(testData.key.toString(), testData.value);
        }

        Collections.shuffle(testDataList);

        for(PairString testData : testDataList){
            String actualValue = mLocalModel.fetch(testData.key.toString(), String.class);
            String failMessage = "Key:"+testData.key;
            assertThat(failMessage, actualValue, is(testData.value));
        }
    }

    @Test
    public void testSaveAndLoadCategories() throws IOException {
        // create a new file with an ObjectOutputStream
        FileOutputStream fos = new FileOutputStream(CATEGORIES_FILE);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        ObjectOutputStream oos = new ObjectOutputStream(bos);

        CategoriesJs categoriesJsBefore = injectCategories();
        mLocalModel.saveCategoriesAndCloseStream(oos, categoriesJsBefore);

        FileInputStream fis = new FileInputStream(CATEGORIES_FILE);
        BufferedInputStream bis = new BufferedInputStream(fis);
        ObjectInputStream ois = new ObjectInputStream(bis);
        CategoriesJs categoriesJsAfter;
        categoriesJsAfter = mLocalModel.loadCategoriesAndCloseStream(ois);

        assertThat(categoriesJsAfter, is(notNullValue()));
        List<CategoryJs> categoryListBefore = categoriesJsBefore.getCategories();
        List<CategoryJs> categoryListAfter = categoriesJsAfter.getCategories();
        assertThat(categoryListAfter, is(notNullValue()));

        for(int i=0; i<categoryListBefore.size(); i++){
            CategoryJs categoryBefore = categoryListBefore.get(i);
            CategoryJs categoryAfter = categoryListAfter.get(i);
            assertThat(categoryAfter.getAcronym(), is(categoryBefore.getAcronym()));
            assertThat(categoryAfter.getCategoryId(), is(categoryBefore.getCategoryId()));
            assertThat(categoryAfter.getCategoryName(), is(categoryBefore.getCategoryName()));

            List<RouteJs> routeListBefore = categoryBefore.getRoutes();
            List<RouteJs> routeListAfter = categoryAfter.getRoutes();
            for(int j=0; j<routeListBefore.size(); j++){
                RouteJs routeBefore = routeListBefore.get(i);
                RouteJs routeAfter = routeListAfter.get(i);
                assertThat(routeAfter.getRouteId(), is(routeBefore.getRouteId()));
                assertThat(routeAfter.getRouteName(), is(routeBefore.getRouteName()));
                assertThat(routeAfter.getScoreType(), is(routeBefore.getScoreType()));
                assertThat(routeAfter.getTimeEnd(), is(routeBefore.getTimeEnd()));
                assertThat(routeAfter.getTimeStart(), is(routeBefore.getTimeStart()));
            }
        }
    }

    public static CategoriesJs injectCategories(){
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

    private static class PairString {
        public final String key;
        public final String value;

        public PairString(){
            this.key = UUID.randomUUID().toString();
            this.value = UUID.randomUUID().toString();
        }

        public PairString(String key, String value){
            this.key = key;
            this.value = value;
        }
    }

}
