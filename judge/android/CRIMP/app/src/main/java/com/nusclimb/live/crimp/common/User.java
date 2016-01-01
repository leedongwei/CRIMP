package com.nusclimb.live.crimp.common;

/**
 * This class is the main way for us to store user information. User information such as
 * user id, authentication token gethered during usage are stored in this class.
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class User {
    private String facebookAccessToken;
    private String userName;
    private String userId;
    private String authToken;
    private String categoryId;
    private String routeId;

    /**
     * Construct a empty instance of User where all its field are set to null.
     */
    public User(){}

    /**
     * Construct a new instance of User that is a copy of the given User object.
     *
     * @param user User object to copy from
     */
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
