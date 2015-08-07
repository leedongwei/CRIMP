package com.nusclimb.live.crimp.hello;

import java.util.List;

/**
 * Category information
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class CategoryItem implements HintableSpinnerItem{
    private String categoryId;          // ID of the category for this route.
    private String fullCategoryName;    // Full name of this category or hint text.
    private boolean isHint;             // Whether this is a hint.
    private List<RouteItem> routes;     // List of routes for this category.

    public CategoryItem(String hintText, boolean isHint){
        this.fullCategoryName = hintText;
        this.isHint = isHint;
    }

    public CategoryItem(String categoryId, String fullCategoryName, boolean isHint, List<RouteItem> routes){
        this.categoryId = categoryId;
        this.fullCategoryName = fullCategoryName;
        this.isHint = isHint;
        this.routes = routes;
    }

    public List<RouteItem> getRoutes(){
        return routes;
    }

    @Override
    public String toString(){
        return getText();
    }

    @Override
    public String getId() {
        return categoryId;
    }

    @Override
    public String getText() {
        return fullCategoryName;
    }

    @Override
    public boolean isHint() {
        return isHint;
    }
}
