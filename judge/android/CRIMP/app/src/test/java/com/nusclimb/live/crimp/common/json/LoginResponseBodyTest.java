package com.nusclimb.live.crimp.common.json;

import com.nusclimb.live.crimp.network.model.LoginJackson;

import junit.framework.Assert;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class LoginResponseBodyTest {
    private LoginJackson mLoginJackson;

    @Before
    public void setup(){
        mLoginJackson = new LoginJackson();
        mLoginJackson.setxUserId("userId");
        mLoginJackson.setxAuthToken("authToken");
    }

    @Test
    public void testSerializeAndDeserialize() throws IOException {
        System.out.println("Testing LoginJackson serialization and deserialization");
        ObjectMapper mapper = new ObjectMapper();
        String serializeForm=null;
        serializeForm = mapper.writeValueAsString(mLoginJackson);

        LoginJackson deserializeForm = null;
        deserializeForm = mapper.readValue(serializeForm, LoginJackson.class);

        Assert.assertEquals(mLoginJackson.getxUserId(), deserializeForm.getxUserId());
        Assert.assertEquals(mLoginJackson.getxAuthToken(), deserializeForm.getxAuthToken());
        Assert.assertNotSame(mLoginJackson, deserializeForm);
    }

    @After
    public void tearDown(){
        mLoginJackson = null;
    }
}
