package com.nusclimb.live.crimp.common.json;

import com.nusclimb.live.crimp.network.model.PostScoreJackson;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class PostScoreResponseBodyTest {
    private PostScoreJackson mPostScoreJackson;

    @Before
    public void setUp() {
        mPostScoreJackson = new PostScoreJackson();
    }

    @Test
    public void testToString(){
        System.out.println("Testing PostScoreJackson.toString()");
        Assert.assertEquals("{}", mPostScoreJackson.toString());
    }

    @After
    public void tearDown(){
        mPostScoreJackson = null;
    }
}
