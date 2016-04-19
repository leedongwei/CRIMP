package com.nusclimb.live.crimp.common.json;

import com.nusclimb.live.crimp.network.model.ClimberJackson;

import junit.framework.Assert;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class ClimberResponseBodyTest {
    private ClimberJackson mClimberJackson;

    @Before
    public void setup(){
        mClimberJackson = new ClimberJackson();
        mClimberJackson.setClimberId("climberId");
        mClimberJackson.setClimberName("climberName");
        mClimberJackson.setTotalScore("totalScore");
    }

    @Test
    public void testSerializeAndDeserialize() throws IOException {
        System.out.println("Testing ClimberJackson serialization and deserialization");
        ObjectMapper mapper = new ObjectMapper();
        String serializeForm=null;
        serializeForm = mapper.writeValueAsString(mClimberJackson);
        ClimberJackson deserializeForm = null;
        deserializeForm = mapper.readValue(serializeForm, ClimberJackson.class);

        Assert.assertEquals(mClimberJackson.getClimberId(), deserializeForm.getClimberId());
        Assert.assertEquals(mClimberJackson.getClimberName(), deserializeForm.getClimberName());
        Assert.assertEquals(mClimberJackson.getTotalScore(), deserializeForm.getTotalScore());
        Assert.assertNotSame(mClimberJackson, deserializeForm);
    }

    @After
    public void tearDown(){
        mClimberJackson = null;
    }
}
