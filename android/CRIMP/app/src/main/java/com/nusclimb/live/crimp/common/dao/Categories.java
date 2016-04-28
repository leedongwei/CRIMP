package com.nusclimb.live.crimp.common.dao;

import com.nusclimb.live.crimp.network.model.CategoriesJs;
import com.nusclimb.live.crimp.network.model.CategoryJs;
import com.nusclimb.live.crimp.network.model.RouteJs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class Categories implements Serializable {
    private static final long serialVersionUID = 1L;

    private final ArrayList<Category> categoryList;

    public Categories(List<Category> categoryList){
        this.categoryList = new ArrayList<>(categoryList);
    }

    public Categories(CategoriesJs categoriesJs){
        this.categoryList = new ArrayList<>();

        ArrayList<CategoryJs> categoryJsList = categoriesJs.getCategories();
        for(CategoryJs categoryJs:categoryJsList){
            ArrayList<RouteJs> routeJsList = categoryJs.getRoutes();

            ArrayList<Route> routeList = new ArrayList<>();
            for(RouteJs routeJs:routeJsList){
                Route route = new Route(routeJs.getRouteId(), routeJs.getRouteName(),
                        routeJs.getScoreType(), routeJs.getTimeStart(), routeJs.getTimeEnd());
                routeList.add(route);
            }

            Category category = new Category(categoryJs.getCategoryId(), categoryJs.getCategoryName(), categoryJs.getAcronym(), routeList);
        }
    }
}
