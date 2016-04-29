package com.nusclimb.live.crimp;

import com.nusclimb.live.crimp.persistence.LocalModel;
import com.nusclimb.live.crimp.persistence.LocalModelImpl;

import org.hamcrest.MatcherAssert;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class LocalModelTest {
    private static final int TEST_SIZE = 10;
    private LocalModel mLocalModel;
    private ArrayList<PairString> testDataList;

    @Before
    public void onBefore() {
        testDataList = new ArrayList<>();
        for(int i=0; i<TEST_SIZE; i++){
            testDataList.add(new PairString());
        }

        testDataList.add(new PairString("a", "aa"));
        testDataList.add(new PairString("b", "bb"));
        testDataList.add(new PairString("c", "cc"));
        testDataList.add(new PairString("d", "dd"));
        testDataList.add(new PairString("e", "ee"));

        mLocalModel = LocalModelImpl.getInstance(new File(LocalModelImpl.DISK_CACHE_FILE_DIR));
    }

    @After
    public void onAfter(){
        testDataList = null;
        mLocalModel.deleteModel();
        mLocalModel = null;
    }

    @AfterClass
    public static void onAfterClass(){
        File path = new File(LocalModelImpl.DISK_CACHE_FILE_DIR);
        if (path.isDirectory()) {
            String[] children = path.list();
            for (int i = 0; i < children.length; i++) {
                File childFile = new File(path, children[i]);
                childFile.delete();
            }
        }
        path.delete();
    }

    @Test
    public void testIsDataExist()  {
        for(PairString testData : testDataList){
            mLocalModel.putData(testData.key.toString(), testData.value);
        }

        Collections.shuffle(testDataList);

        for(PairString testData : testDataList){
            MatcherAssert.assertThat(mLocalModel.isDataExist(testData.key.toString()), is(true));
            MatcherAssert.assertThat(mLocalModel.isDataExist(UUID.randomUUID().toString()), is(false));
        }
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
