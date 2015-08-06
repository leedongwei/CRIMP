package com.nusclimb.live.crimp.common.json;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;

/**
 * Response body for GET '/api/judge/categories'
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class CategoriesResponseBody{
    @JsonProperty("categories")
    private ArrayList<Category> categories;

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("\tcategories: [\n");

        if(categories.size()<=1){
            for(Category c:categories){
                sb.append("\t"+c.toString());
            }
        }
        else{
            sb.append("\t"+ categories.get(0).toString());

            for(int i=1; i<categories.size(); i++){
                sb.append(",\n");
                sb.append("\t"+categories.get(i).toString());
            }
        }
        sb.append("\t]\n");
        sb.append("}");

        return sb.toString();
    }

    public ArrayList<Category> getCategories() {
        return categories;
    }

    public void setCategories(ArrayList<Category> categories) {
        this.categories = categories;
    }

    private class Category{
        @JsonProperty("category_name")
        private String categoryName;
        @JsonProperty("category_id")
        private String categoryId;
        @JsonProperty("routes")
        private ArrayList<Route> routes;
        @JsonProperty("scores_finalized")
        private boolean scoresFinalized;
        @JsonProperty("time_start")
        private String timeStart;
        @JsonProperty("time_end")
        private String timeEnd;

        public String toString(){
            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            sb.append("\tcategory_name: '"+categoryName+"',\n");
            sb.append("\tcategory_id: '"+categoryId+"',\n");
            sb.append("\tscores_finalized: "+scoresFinalized+",\n");
            sb.append("\ttime_start: "+timeStart+",\n");
            sb.append("\ttime_end: "+timeEnd+",\n");
            sb.append("\troutes: [\n");
            if(routes.size()<=1){
                for(Route r:routes) {
                    sb.append(r.toString());
                }
            }
            else{
                sb.append(routes.get(0).toString());
                for(int i=1; i<routes.size(); i++){
                    sb.append(",\n");
                    sb.append(routes.get(i).toString());
                }
            }

            sb.append("\t]\n");
            sb.append("}");

            return sb.toString();
        }

        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }

        public String getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(String categoryId) {
            this.categoryId = categoryId;
        }

        public ArrayList<Route> getRoutes() {
            return routes;
        }

        public void setRoutes(ArrayList<Route> routes) {
            this.routes = routes;
        }

        public boolean isScoresFinalized() {
            return scoresFinalized;
        }

        public void setScoresFinalized(boolean scoresFinalized) {
            this.scoresFinalized = scoresFinalized;
        }

        public String getTimeStart() {
            return timeStart;
        }

        public void setTimeStart(String timeStart) {
            this.timeStart = timeStart;
        }

        public String getTimeEnd() {
            return timeEnd;
        }

        public void setTimeEnd(String timeEnd) {
            this.timeEnd = timeEnd;
        }

        private class Route{
            @JsonProperty("route_id")
            private String routeId;
            @JsonProperty("route_name")
            private String routeName;

            @Override
            public String toString(){
                StringBuilder sb = new StringBuilder();
                sb.append("{\n");
                sb.append("\troute_id: "+routeId+",\n");
                sb.append("\troute_name: "+routeName+"\n");
                sb.append("}");

                return sb.toString();
            }

            public String getRouteId() {
                return routeId;
            }

            public void setRouteId(String routeId) {
                this.routeId = routeId;
            }

            public String getRouteName() {
                return routeName;
            }

            public void setRouteName(String routeName) {
                this.routeName = routeName;
            }
        }
    }
}
