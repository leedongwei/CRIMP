package com.nusclimb.live.crimp.common.json;

import com.nusclimb.live.crimp.network.model.GetScoreJackson;

import junit.framework.Assert;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class GetScoreResponseBodyTest {
    private GetScoreJackson mGetScoreJackson;

    @Before
    public void setup(){
        mGetScoreJackson = new GetScoreJackson();
        mGetScoreJackson.setCategoryId("categoryId");
        mGetScoreJackson.setRouteId("routeId");
        mGetScoreJackson.setClimberId("climberId");
        mGetScoreJackson.setClimberName("climberName");
        mGetScoreJackson.setScoreString("scoreString");
    }

    @Test
    public void testSerializeAndDeserialize() throws IOException {
        System.out.println("Testing GetScoreJackson serialization and deserialization");
        ObjectMapper mapper = new ObjectMapper();
        String serializeForm=null;
        serializeForm = mapper.writeValueAsString(mGetScoreJackson);
        GetScoreJackson deserializeForm = null;
        deserializeForm = mapper.readValue(serializeForm, GetScoreJackson.class);

        Assert.assertEquals(mGetScoreJackson.getCategoryId(), deserializeForm.getCategoryId());
        Assert.assertEquals(mGetScoreJackson.getRouteId(), deserializeForm.getRouteId());
        Assert.assertEquals(mGetScoreJackson.getClimberId(), deserializeForm.getClimberId());
        Assert.assertEquals(mGetScoreJackson.getClimberName(), deserializeForm.getClimberName());
        Assert.assertEquals(mGetScoreJackson.getScoreString(), deserializeForm.getScoreString());
        Assert.assertNotSame(mGetScoreJackson, deserializeForm);
    }

    @After
    public void tearDown(){
        mGetScoreJackson = null;
    }
}
