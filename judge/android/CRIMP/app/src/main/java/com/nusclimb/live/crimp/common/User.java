package com.nusclimb.live.crimp.common;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class User {
    private String facebookAccessToken;
    private String userName;
    private String userId;
    private String authToken;
    private String categoryId;
    private String routeId;

    public User(){
        clearAll();
    }

    public User(User user){
        setFacebookAccessToken(user.getFacebookAccessToken());
        setUserName(user.getUserName());
        setUserId(user.getUserId());
        setAuthToken(user.getAuthToken());
        setCategoryId(user.getCategoryId());
        setRouteId(user.getRouteId());
    }

    public void clearAll(){
        facebookAccessToken = null;
        userName = null;
        userId = null;
        authToken = null;
        categoryId = null;
        routeId = null;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("facebookAccessToken="+facebookAccessToken+" ");
        sb.append("userName="+userName+" ");
        sb.append("userId="+userId+" ");
        sb.append("authToken="+authToken+" ");
        sb.append("categoryId="+categoryId+" ");
        sb.append("routeId="+routeId+" ");
        sb.append("}");

        return sb.toString();
    }

    public String getFacebookAccessToken() {
        return facebookAccessToken;
    }

    public void setFacebookAccessToken(String facebookAccessToken) {
        this.facebookAccessToken = facebookAccessToken;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

}
