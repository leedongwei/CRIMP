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
public class ClimberResponseBodyTest {
    private ClimberResponseBody mClimberResponseBody;

    @Before
    public void setup(){
        mClimberResponseBody = new ClimberResponseBody();
        mClimberResponseBody.setClimberId("climberId");
        mClimberResponseBody.setClimberName("climberName");
        mClimberResponseBody.setTotalScore("totalScore");
    }

    @Test
    public void testSerializeAndDeserialize() throws IOException {
        System.out.println("Testing ClimberResponseBody serialization and deserialization");
        ObjectWriter ow = new ObjectMapper().writer();
        String serializeForm=null;
        serializeForm = ow.writeValueAsString(mClimberResponseBody);

        ObjectReader objReader = new ObjectMapper().reader()
                .withType(ClimberResponseBody.class);
        ClimberResponseBody deserializeForm = null;
        deserializeForm = objReader.readValue(serializeForm);

        Assert.assertEquals(mClimberResponseBody.getClimberId(), deserializeForm.getClimberId());
        Assert.assertEquals(mClimberResponseBody.getClimberName(), deserializeForm.getClimberName());
        Assert.assertEquals(mClimberResponseBody.getTotalScore(), deserializeForm.getTotalScore());
        Assert.assertNotSame(mClimberResponseBody, deserializeForm);
    }

    @After
    public void tearDown(){
        mClimberResponseBody = null;
    }
}
