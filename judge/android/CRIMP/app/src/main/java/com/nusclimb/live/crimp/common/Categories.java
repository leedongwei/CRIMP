package com.nusclimb.live.crimp.common;

import com.nusclimb.live.crimp.common.json.CategoriesResponseBody;
import com.nusclimb.live.crimp.hello.HintableSpinnerItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper class to hold all categories information. This class stores categories information in
 * the form of Jackson POJO and produce a List of {@link HintableSpinnerItem} to populate a
 * {@link com.nusclimb.live.crimp.hello.HintableArrayAdapter HintableArrayAdapter}.
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 * @see <a href="http://wiki.fasterxml.com/JacksonDataBinding">JacksonDataBinding</a>
 */
public class Categories {
    List<HintableSpinnerItem> spinnerList;  // For populating HintableArrayAdapter.
    CategoriesResponseBody categoriesInfo;  // Contain all info regarding categories and routes.

    /**
     * Create an object that stores all information about categories and routes.
     *
     * @param mResponseBody Jackson POJO containing categories and routes information.
     */
    public Categories(CategoriesResponseBody mResponseBody){
        categoriesInfo = mResponseBody;
    }

    /**
     * Create a list of {@link HintableSpinnerItem} for populating a
     * {@link com.nusclimb.live.crimp.hello.HintableArrayAdapter} and return a copy of the list.
     *
     * @param categoryHint Hint message to appear on spinner for categories.
     * @param routeHint Hint message to appear on spinner for routes.
     * @return List of HintableSpinnerItem.
     */
    public List<HintableSpinnerItem> getCategoriesSpinnerListCopy(String categoryHint,
                                                                  String routeHint){
        if(spinnerList == null){
            spinnerList = new ArrayList<HintableSpinnerItem>();
            spinnerList.add(new CategoryItem(categoryHint));
            for(CategoriesResponseBody.Category c: categoriesInfo.getCategories()){
                List<HintableSpinnerItem> routes = new ArrayList<HintableSpinnerItem>();
                routes.add(new RouteItem(routeHint));
                for(CategoriesResponseBody.Category.Route r: c.getRoutes()){
                    routes.add(new RouteItem(c.getCategoryId(), r.getRouteId(),r.getRouteName(),
                            r.getScore()));
                }

                spinnerList.add(new CategoryItem(c.getCategoryId(), c.getCategoryName(), routes));
            }
        }

        // Return a copy of spinnerList instead of spinnerList itself. This is to prevent
        // modifying spinnerList directly from outside this class and end up with a spinnerList
        // that contain information different from categoriesInfo.
        return new ArrayList<HintableSpinnerItem>(spinnerList);
    }

    /**
     * Return a copy of a Jackson POJO containing all categories information.
     *
     * @return All categories information.
     */
    public CategoriesResponseBody getJSON(){
        return new CategoriesResponseBody(categoriesInfo);
    }

    /**
     * Return a copy of a CategoryItem at the specified position in the list.
     *
     * @param position Position of the CategoryItem to return.
     * @return Copy of a CategoryItem at the specified position in the list.
     */
    public CategoryItem getCategorySpinnerItem(int position){
        return new CategoryItem((CategoryItem)spinnerList.get(position+1));
    }



    /**
     * Category item for spinner.
     */
    public class CategoryItem implements HintableSpinnerItem{
        private String categoryId;                  // ID of the category for this route.
        private String fullCategoryName;            // Full name of this category or hint text.
        private boolean isHint;                     // Whether this is a hint.
        private List<HintableSpinnerItem> routes;   // List of routes for this category.

        /**
         * Construct a CategoryItem object with the same values as the given item.
         *
         * @param item The constructed CategoryItem has the same values as item.
         */
        public CategoryItem(CategoryItem item){
            this.categoryId = item.getId();
            this.fullCategoryName = item.getText();
            this.isHint = item.isHint();
            this.routes = item.getRoutes();
        }

        /**
         * Construct a CategoryItem that is a hint for spinner.
         *
         * @param hintText Hint message.
         */
        public CategoryItem(String hintText){
            this.fullCategoryName = hintText;
            this.isHint = true;
        }

        /**
         * Construct a CategoryItem object based on the given parameters.
         *
         * @param categoryId category ID.
         * @param fullCategoryName Full category name.
         * @param routes List of routes for this category.
         */
        public CategoryItem(String categoryId, String fullCategoryName,
                            List<HintableSpinnerItem> routes){
            this.categoryId = categoryId;
            this.fullCategoryName = fullCategoryName;
            this.isHint = false;
            this.routes = routes;
        }

        /**
         * Return a copy of the list of routes in this category.
         *
         * @return A copy of the list of routes in this category.
         */
        public List<HintableSpinnerItem> getRoutes(){
            return new ArrayList<HintableSpinnerItem>(routes);
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



    /**
     * Route item for spinner.
     */
    public class RouteItem implements HintableSpinnerItem {
        private String categoryId;      // ID of the category for this route.
        private String routeId;         // ID of this route.
        private String fullRouteName;   // Full name of this route or hint text.
        private boolean isHint;         // Whether this is a hint.
        private String score;           // Score for this route (if applicable).

        /**
         * Construct a RouteItem that is a hint for spinner.
         *
         * @param hintText Hint message.
         */
        public RouteItem(String hintText){
            this.fullRouteName = hintText;
            this.isHint = true;
        }

        /**
         * Construct a RouteItem with the given parameters.
         *
         * @param categoryId Category ID for this route.
         * @param routeId Route ID.
         * @param fullRouteName Full route name.
         */
        public RouteItem(String categoryId, String routeId, String fullRouteName){
            this.categoryId = categoryId;
            this.routeId = routeId;
            this.fullRouteName = fullRouteName;
            this.isHint = false;
        }

        /**
         * Construct a RouteItem with the give parameters.
         *
         * @param categoryId Category ID for this route.
         * @param routeId Route ID.
         * @param fullRouteName Full route name.
         * @param score Score for this route.
         */
        public RouteItem(String categoryId, String routeId, String fullRouteName, String score){
            this.categoryId = categoryId;
            this.routeId = routeId;
            this.fullRouteName = fullRouteName;
            this.isHint = false;
            this.score = score;
        }

        public String getCategoryId(){
            return categoryId;
        }

        public String getScore() {
            return score;
        }

        @Override
        public String toString(){
            return getText();
        }

        @Override
        public String getId() {
            return routeId;
        }

        @Override
        public String getText() {
            return fullRouteName;
        }

        @Override
        public boolean isHint() {
            return isHint;
        }
    }
}
