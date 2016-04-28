package com.nusclimb.live.crimp.network.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class HeaderBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private String fbUserId;
    private String fbAccessToken;
    private Long sequentialToken;

    public String getFbUserId() {
        return fbUserId;
    }

    public void setFbUserId(String fbUserId) {
        this.fbUserId = fbUserId;
    }

    public String getFbAccessToken() {
        return fbAccessToken;
    }

    public void setFbAccessToken(String fbAccessToken) {
        this.fbAccessToken = fbAccessToken;
    }

    public Long getSequentialToken() {
        return sequentialToken;
    }

    public void setSequentialToken(Long sequentialToken) {
        this.sequentialToken = sequentialToken;
    }
}
