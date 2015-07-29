package com.nusclimb.live.crimp.hello;

/**
 * Created by user on 02-Jul-15.
 */
public class CategorySpinnerItem implements SpinnerItem{
    private String categoryName;
    private String categoryId;
    private int routeCount;
    private boolean isHint;

    public CategorySpinnerItem(String text, boolean isHint){
        this(text, null, 0, isHint);
    }

    public CategorySpinnerItem(String categoryName, String categoryId, int routeCount, boolean isHint){
        this.categoryName = categoryName;
        this.categoryId = categoryId;
        this.routeCount = routeCount;
        this.isHint = isHint;
    }

    public String getItemString(){
        return categoryName;
    }

    public String toString(){
        return categoryName;
    }

    public boolean isHint(){
        return isHint;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public int getRouteCount() {
        return routeCount;
    }
}
