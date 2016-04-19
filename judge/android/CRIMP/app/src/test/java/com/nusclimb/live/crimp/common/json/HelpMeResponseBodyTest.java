package com.nusclimb.live.crimp.common.json;

import com.nusclimb.live.crimp.network.model.HelpMeJackson;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class HelpMeResponseBodyTest {
    private HelpMeJackson mHelpMeJackson;

    @Before
    public void setUp() {
        mHelpMeJackson = new HelpMeJackson();
    }

    @Test
    public void testToString(){
        System.out.println("Testing HelpMeJackson.toString()");
        Assert.assertEquals("{}", mHelpMeJackson.toString());
    }

    @After
    public void tearDown(){
        mHelpMeJackson = null;
    }
}
