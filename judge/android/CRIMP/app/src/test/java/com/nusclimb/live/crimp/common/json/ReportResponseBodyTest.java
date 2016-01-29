package com.nusclimb.live.crimp.common.json;

import junit.framework.Assert;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import org.codehaus.jackson.map.ObjectWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class ReportResponseBodyTest {
    private ReportResponseBody mReportResponseBody;

    @Before
    public void setup(){
        mReportResponseBody = new ReportResponseBody();
        mReportResponseBody.setAdminId("adminId");
        mReportResponseBody.setAdminName("adminName");
        mReportResponseBody.setCategoryId("categoryId");
        mReportResponseBody.setRouteId("routeId");
        mReportResponseBody.setState(10);
    }

    @Test
    public void testSerializeAndDeserialize() throws IOException {
        System.out.println("Testing ReportResponseBody serialization and deserialization");
        ObjectMapper mapper = new ObjectMapper();
        String serializeForm=null;
        serializeForm = mapper.writeValueAsString(mReportResponseBody);
        ReportResponseBody deserializeForm = null;
        deserializeForm = mapper.readValue(serializeForm, ReportResponseBody.class);

        Assert.assertEquals(mReportResponseBody.getAdminId(), deserializeForm.getAdminId());
        Assert.assertEquals(mReportResponseBody.getAdminName(), deserializeForm.getAdminName());
        Assert.assertEquals(mReportResponseBody.getCategoryId(), deserializeForm.getCategoryId());
        Assert.assertEquals(mReportResponseBody.getRouteId(), deserializeForm.getRouteId());
        Assert.assertEquals(mReportResponseBody.getState(), deserializeForm.getState());
        Assert.assertNotSame(mReportResponseBody, deserializeForm);
    }

    @After
    public void tearDown(){
        mReportResponseBody = null;
    }
}
