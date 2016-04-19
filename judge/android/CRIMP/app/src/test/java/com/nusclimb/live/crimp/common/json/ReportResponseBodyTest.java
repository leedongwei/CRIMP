package com.nusclimb.live.crimp.common.json;

import com.nusclimb.live.crimp.network.model.ReportJackson;

import junit.framework.Assert;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class ReportResponseBodyTest {
    private ReportJackson mReportJackson;

    @Before
    public void setup(){
        mReportJackson = new ReportJackson();
        mReportJackson.setAdminId("adminId");
        mReportJackson.setAdminName("adminName");
        mReportJackson.setCategoryId("categoryId");
        mReportJackson.setRouteId("routeId");
        mReportJackson.setState(10);
    }

    @Test
    public void testSerializeAndDeserialize() throws IOException {
        System.out.println("Testing ReportJackson serialization and deserialization");
        ObjectMapper mapper = new ObjectMapper();
        String serializeForm=null;
        serializeForm = mapper.writeValueAsString(mReportJackson);
        ReportJackson deserializeForm = null;
        deserializeForm = mapper.readValue(serializeForm, ReportJackson.class);

        Assert.assertEquals(mReportJackson.getAdminId(), deserializeForm.getAdminId());
        Assert.assertEquals(mReportJackson.getAdminName(), deserializeForm.getAdminName());
        Assert.assertEquals(mReportJackson.getCategoryId(), deserializeForm.getCategoryId());
        Assert.assertEquals(mReportJackson.getRouteId(), deserializeForm.getRouteId());
        Assert.assertEquals(mReportJackson.getState(), deserializeForm.getState());
        Assert.assertNotSame(mReportJackson, deserializeForm);
    }

    @After
    public void tearDown(){
        mReportJackson = null;
    }
}
