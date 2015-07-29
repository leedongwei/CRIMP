package com.nusclimb.live.crimp.hello;

/**
 * Created by user on 02-Jul-15.
 */
public class RouteSpinnerItem implements SpinnerItem {
    private final String ROUTE = "Route ";
    private int routeNumber;
    private String text;
    private boolean isHint;

    public RouteSpinnerItem(String text){
        this.text = text;
        this.isHint = true;
    }

    public RouteSpinnerItem(int routeNumber){
        this.routeNumber = routeNumber;
        this.isHint = false;
    }

    public int getRouteNumber(){
        return routeNumber;
    }

    @Override
    public String toString(){
        if(isHint)
            return text;
        else
            return ROUTE+routeNumber;
    }

    @Override
    public String getItemString() {
        if(isHint)
            return text;
        else
            return ROUTE+routeNumber;
    }

    @Override
    public boolean isHint() {
        return isHint;
    }
}
