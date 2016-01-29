package com.nusclimb.live.crimp.common.json;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class HelpMeResponseBodyTest {
    private HelpMeResponseBody mHelpMeResponseBody;

    @Before
    public void setUp() {
        mHelpMeResponseBody = new HelpMeResponseBody();
    }

    @Test
    public void testToString(){
        System.out.println("Testing HelpMeResponseBody.toString()");
        Assert.assertEquals("{}", mHelpMeResponseBody.toString());
    }

    @After
    public void tearDown(){
        mHelpMeResponseBody = null;
    }
}
