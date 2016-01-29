package com.nusclimb.live.crimp.common.json;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class PostScoreResponseBodyTest {
    private PostScoreResponseBody mPostScoreResponseBody;

    @Before
    public void setUp() {
        mPostScoreResponseBody = new PostScoreResponseBody();
    }

    @Test
    public void testToString(){
        System.out.println("Testing PostScoreResponseBody.toString()");
        Assert.assertEquals("{}", mPostScoreResponseBody.toString());
    }

    @After
    public void tearDown(){
        mPostScoreResponseBody = null;
    }
}
