package com.nusclimb.live.crimp.hello;

/**
 * Route information
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class RouteItem implements HintableSpinnerItem {
    private String categoryId;      // ID of the category for this route.
    private String routeId;         // ID of this route.
    private String fullRouteName;   // Full name of this route or hint text.
    private boolean isHint;         // Whether this is a hint.
    private String score;           // Score for this route (if applicable).

    public RouteItem(String hintText, boolean isHint){
        this.fullRouteName = hintText;
        this.isHint = isHint;
    }

    public RouteItem(String categoryId, String routeId, String fullRouteName, boolean isHint){
        this.categoryId = categoryId;
        this.routeId = routeId;
        this.fullRouteName = fullRouteName;
        this.isHint = isHint;
    }

    public RouteItem(String categoryId, String routeId, String fullRouteName, boolean isHint, String score){
        this.categoryId = categoryId;
        this.routeId = routeId;
        this.fullRouteName = fullRouteName;
        this.isHint = isHint;
        this.score = score;
    }

    public String getCategoryId(){
        return categoryId;
    }

    public String getScore() {
        return score;
    }

    @Override
    public String toString(){
        return getText();
    }

    @Override
    public String getId() {
        return routeId;
    }

    @Override
    public String getText() {
        return fullRouteName;
    }

    @Override
    public boolean isHint() {
        return isHint;
    }
}
