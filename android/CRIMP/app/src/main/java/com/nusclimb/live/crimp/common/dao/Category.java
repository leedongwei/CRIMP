package com.nusclimb.live.crimp.common.dao;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Serializable;
import java.util.List;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class Category implements Serializable{
    private static final long serialVersionUID = 1L;

    private final Long categoryId;
    private final String categoryName;
    private final String acronym;
    private final List<Route> routeList;

    public Category(@NonNull Long categoryId, @NonNull String categoryName,
                    @NonNull String acronym, @NonNull List<Route> routeList) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.acronym = acronym;
        this.routeList = routeList;
    }

    @NonNull
    public Long getCategoryId() {
        return categoryId;
    }

    @NonNull
    public String getCategoryName() {
        return categoryName;
    }

    @NonNull
    public String getAcronym() {
        return acronym;
    }

    @NonNull
    public List<Route> getRouteList() {
        return routeList;
    }

    @Nullable
    public Route getRouteById(long routeId){
        for(Route r:routeList){
            if(r.getRouteId() == routeId){
                return r;
            }
        }

        return null;
    }
}
