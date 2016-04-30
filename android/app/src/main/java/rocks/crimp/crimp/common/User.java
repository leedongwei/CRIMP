package rocks.crimp.crimp.common;

import java.io.Serializable;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String accessToken;
    private String userId;
    private String name;
    private Long sequentialToken;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getSequentialToken() {
        return sequentialToken;
    }

    public void setSequentialToken(Long sequentialToken) {
        this.sequentialToken = sequentialToken;
    }
}
