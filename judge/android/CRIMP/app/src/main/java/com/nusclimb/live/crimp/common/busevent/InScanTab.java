package com.nusclimb.live.crimp.common.busevent;

/**
 * Created by weizhi on 15/7/2015.
 */
public class InScanTab {
    private final String TAG = InScanTab.class.getSimpleName();

    private final String routeId;

    public InScanTab(String routeId){
        this.routeId = routeId;
    }

    public String getRouteId(){
        return routeId;
    }
}
