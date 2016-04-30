package rocks.crimp.crimp.network.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CategoryJs implements Serializable{
    @JsonIgnore
    private static final long serialVersionUID = 1L;

    @JsonProperty("category_id")
    private long categoryId;
    @JsonProperty("category_name")
    private String categoryName;
    @JsonProperty("acronym")
    private String acronym;
    @JsonProperty("routes")
    private ArrayList<RouteJs> routes;

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getAcronym() {
        return acronym;
    }

    public void setAcronym(String acronym) {
        this.acronym = acronym;
    }

    public ArrayList<RouteJs> getRoutes() {
        return routes;
    }

    public void setRoutes(ArrayList<RouteJs> routes) {
        this.routes = routes;
    }

    @JsonIgnore
    public RouteJs getRouteById(long id){
        if (routes == null){
            return null;
        }

        for(RouteJs routeJs:routes){
            if(routeJs.getRouteId() == id){
                return routeJs;
            }
        }

        return null;
    }
}
