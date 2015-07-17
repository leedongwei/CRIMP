package com.nusclimb.live.crimp.common.busevent;

/**
 * Created by Zhi on 7/12/2015.
 */
public class RouteFinish {
    private final String TAG = RouteFinish.class.getSimpleName();

    private final String routeId;

    public RouteFinish(String routeId){
        this.routeId = routeId;
    }

    public String getRouteId(){
        return routeId;
    }
}
