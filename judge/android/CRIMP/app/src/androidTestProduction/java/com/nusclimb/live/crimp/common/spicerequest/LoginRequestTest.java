package com.nusclimb.live.crimp.common.spicerequest;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.MediumTest;

import com.nusclimb.live.crimp.network.model.LoginJackson;
import com.octo.android.robospice.JacksonSpringAndroidSpiceService;

import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
@RunWith(AndroidJUnit4.class)
@MediumTest
public class LoginRequestTest {
    private LoginJackson serverResponseObject;
    private String serverResponseString;

    @Before
    public void setup() throws IOException {
        // Prepare server responses
        InputStream inputStream = InstrumentationRegistry.getContext().getResources()
                .getAssets().open("api/judge/POSTlogin");
        ObjectMapper mapper = new ObjectMapper();
        serverResponseObject = mapper.readValue(inputStream, LoginJackson.class);
        serverResponseString = mapper.writeValueAsString(serverResponseObject);
        inputStream.close();
    }

    @Test
    public void testLoadDataFromNetwork() throws Exception {
        // Prepare server
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setBody(serverResponseString)
                .setHeader("Content-Type", "application/json; charset=utf-8"));
        server.start();
        HttpUrl baseUrl = server.url("/api/judge/login");

        JacksonSpringAndroidSpiceService mService = new JacksonSpringAndroidSpiceService();
        LoginRequest mRequest = new LoginRequest("accessToken", baseUrl.toString());
        mRequest.setRestTemplate(mService.createRestTemplate());
        LoginJackson actualResponse = mRequest.loadDataFromNetwork();

        // Check what we received if deserialized properly into LoginJackson.
        assertThat(actualResponse.getxUserId(), is(equalTo(serverResponseObject.getxUserId())));
        assertThat(actualResponse.getxAuthToken(), is(equalTo(serverResponseObject.getxAuthToken())));

        // Check what we send to server is correct.
        RecordedRequest request = server.takeRequest();
        ObjectMapper mapper = new ObjectMapper();
        RequestBody requestBody = mapper.readValue(request.getBody().readUtf8(), RequestBody.class);
        assertThat(request.getMethod(), is(equalTo("POST")));
        assertThat(requestBody.accessToken, is(equalTo("accessToken")));
        assertThat(requestBody.isProductionApp, is(equalTo(true)));
        assertThat(requestBody.map.size(), is(equalTo(0)));

        server.shutdown();
    }

    @Test
    public void testWrongResponse() throws Exception {
        // TODO: i am deliberating putting this syntax error here. I tried to do something but
        // was interrupted. this method need to do something.
        qwert;
        // Prepare server
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setBody(serverResponseString)
                .setHeader("Content-Type", "application/json; charset=utf-8"));
        server.start();
        HttpUrl baseUrl = server.url("/api/judge/login");

        JacksonSpringAndroidSpiceService mService = new JacksonSpringAndroidSpiceService();
        LoginRequest mRequest = new LoginRequest("accessToken", baseUrl.toString());
        mRequest.setRestTemplate(mService.createRestTemplate());
        LoginJackson actualResponse = mRequest.loadDataFromNetwork();

        // Check what we received if deserialized properly into LoginJackson.
        assertThat(actualResponse.getxUserId(), is(equalTo(serverResponseObject.getxUserId())));
        assertThat(actualResponse.getxAuthToken(), is(equalTo(serverResponseObject.getxAuthToken())));

        // Check what we send to server is correct.
        RecordedRequest request = server.takeRequest();
        ObjectMapper mapper = new ObjectMapper();
        RequestBody requestBody = mapper.readValue(request.getBody().readUtf8(), RequestBody.class);
        assertThat(request.getMethod(), is(equalTo("POST")));
        assertThat(requestBody.accessToken, is(equalTo("accessToken")));
        assertThat(requestBody.isProductionApp, is(equalTo(true)));
        assertThat(requestBody.map.size(), is(equalTo(0)));

        server.shutdown();
    }

    private static class RequestBody{
        public HashMap<String, Object> map = new HashMap<>();
        @JsonProperty("accessToken")
        public String accessToken;
        @JsonProperty("isProductionApp")
        public boolean isProductionApp;

        @JsonAnySetter
        public void handleUnknown(String key, Object value) {
            map.put(key, value);
        }
    }
}
