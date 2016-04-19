package com.nusclimb.live.crimp.common.json;

import com.nusclimb.live.crimp.network.model.ActiveMonitorJackson;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ActiveMonitorResponseBodyTest {
    private ActiveMonitorJackson mActiveMonitorJackson;

    @Before
    public void setUp() {
        mActiveMonitorJackson = new ActiveMonitorJackson();
    }

    @Test
    public void testToString(){
        System.out.println("Testing ActiveMonitorJackson.toString()");
        Assert.assertEquals("{}", mActiveMonitorJackson.toString());
    }

    @After
    public void tearDown(){
        mActiveMonitorJackson = null;
    }
}