package com.nusclimb.live.crimp.common.json;

import junit.framework.Assert;

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
public class GetScoreResponseBodyTest {
    private GetScoreResponseBody mGetScoreResponseBody;

    @Before
    public void setup(){
        mGetScoreResponseBody = new GetScoreResponseBody();
        mGetScoreResponseBody.setCategoryId("categoryId");
        mGetScoreResponseBody.setRouteId("routeId");
        mGetScoreResponseBody.setClimberId("climberId");
        mGetScoreResponseBody.setClimberName("climberName");
        mGetScoreResponseBody.setScoreString("scoreString");
    }

    @Test
    public void testSerializeAndDeserialize() throws IOException {
        System.out.println("Testing GetScoreResponseBody serialization and deserialization");
        ObjectMapper mapper = new ObjectMapper();
        String serializeForm=null;
        serializeForm = mapper.writeValueAsString(mGetScoreResponseBody);
        GetScoreResponseBody deserializeForm = null;
        deserializeForm = mapper.readValue(serializeForm, GetScoreResponseBody.class);

        Assert.assertEquals(mGetScoreResponseBody.getCategoryId(), deserializeForm.getCategoryId());
        Assert.assertEquals(mGetScoreResponseBody.getRouteId(), deserializeForm.getRouteId());
        Assert.assertEquals(mGetScoreResponseBody.getClimberId(), deserializeForm.getClimberId());
        Assert.assertEquals(mGetScoreResponseBody.getClimberName(), deserializeForm.getClimberName());
        Assert.assertEquals(mGetScoreResponseBody.getScoreString(), deserializeForm.getScoreString());
        Assert.assertNotSame(mGetScoreResponseBody, deserializeForm);
    }

    @After
    public void tearDown(){
        mGetScoreResponseBody = null;
    }
}
