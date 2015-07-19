package com.nusclimb.live.crimp.common.busevent;

/**
 * Created by Zhi on 7/12/2015.
 */
public class RouteFinish {
    private final String TAG = RouteFinish.class.getSimpleName();

    private final String routeId;
    //private final String categoryFullName;
    //private final String routeFullName;

    public RouteFinish(String routeId){
        this.routeId = routeId;
        //this.categoryFullName = categoryFullName;
        //this.routeFullName = routeFullName;
    }

    public String getRouteId(){
        return routeId;
    }

    /*
    public String getCategoryFullName(){
        return categoryFullName;
    }

    public String getRouteFullName(){
        return routeFullName;
    }
    */
}
