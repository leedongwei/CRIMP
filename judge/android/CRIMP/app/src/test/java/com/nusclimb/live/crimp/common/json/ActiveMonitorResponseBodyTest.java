package com.nusclimb.live.crimp.common.json;

import com.nusclimb.live.crimp.common.json.ActiveMonitorResponseBody;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ActiveMonitorResponseBodyTest {
    private ActiveMonitorResponseBody mActiveMonitorResponseBody;

    @Before
    public void setUp() {
        mActiveMonitorResponseBody = new ActiveMonitorResponseBody();
    }

    @Test
    public void testToString(){
        System.out.println("Testing ActiveMonitorResponseBody.toString()");
        Assert.assertEquals("{}", mActiveMonitorResponseBody.toString());
    }

    @After
    public void tearDown(){
        mActiveMonitorResponseBody = null;
    }
}