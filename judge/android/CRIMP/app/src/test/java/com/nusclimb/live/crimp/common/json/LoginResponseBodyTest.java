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
public class LoginResponseBodyTest {
    private LoginResponseBody mLoginResponseBody;

    @Before
    public void setup(){
        mLoginResponseBody = new LoginResponseBody();
        mLoginResponseBody.setxUserId("userId");
        mLoginResponseBody.setxAuthToken("authToken");
    }

    @Test
    public void testSerializeAndDeserialize() throws IOException {
        System.out.println("Testing LoginResponseBody serialization and deserialization");
        ObjectWriter ow = new ObjectMapper().writer();
        String serializeForm=null;
        serializeForm = ow.writeValueAsString(mLoginResponseBody);

        ObjectReader objReader = new ObjectMapper().reader()
                .withType(LoginResponseBody.class);
        LoginResponseBody deserializeForm = null;
        deserializeForm = objReader.readValue(serializeForm);

        Assert.assertEquals(mLoginResponseBody.getxUserId(), deserializeForm.getxUserId());
        Assert.assertEquals(mLoginResponseBody.getxAuthToken(), deserializeForm.getxAuthToken());
        Assert.assertNotSame(mLoginResponseBody, deserializeForm);
    }

    @After
    public void tearDown(){
        mLoginResponseBody = null;
    }
}
