package com.nusclimb.live.crimp.common;

import com.nusclimb.live.crimp.common.json.CategoriesResponseBody;
import com.nusclimb.live.crimp.hello.HintableSpinnerItem;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is the main way hold all categories information. This class contains method
 * that allow us to store categories information requested from server, produce a List of
 * {@link HintableSpinnerItem} to populate a
 * {@link com.nusclimb.live.crimp.hello.HintableArrayAdapter HintableArrayAdapter}, and retrieve
 * information about category and routes.
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class Categories {
    CategoriesResponseBody categoriesInfo;  // Contain all info regarding categories and routes.

    public Categories(){
        categoriesInfo = new CategoriesResponseBody();
    }

    /**
     * Construct a Categories object from several list containing information on all categories.
     * This method allow us to rebuild a Categories object from serializable information (such as
     * from a bundle).
     *
     * @param cNameList category name list
     * @param cIdList category id list
     * @param cCountList category route count list
     * @param rNameList route name list
     * @param rIdList route id list
     * @param rScoreList route score list
     * @param cFinalizeArray category score finalize array
     * @param cStartList category start time list
     * @param cEndList category end time list
     */
    public Categories(List<String> cNameList, List<String> cIdList, List<Integer> cCountList,
                      List<String> rNameList, List<String> rIdList, List<String> rScoreList,
                      byte[] cFinalizeArray, List<String> cStartList, List<String> cEndList){

        categoriesInfo = new CategoriesResponseBody();
        ArrayList<CategoriesResponseBody.Category> arr = new ArrayList<>();
        int k = 0;
        for(int i=0; i<cNameList.size(); i++){
            CategoriesResponseBody.Category mCategory = new CategoriesResponseBody.Category();
            mCategory.setCategoryName(cNameList.get(i));
            mCategory.setCategoryId(cIdList.get(i));
            if(cFinalizeArray[i] == 1)
                mCategory.setScoresFinalized(true);
            else
                mCategory.setScoresFinalized(false);
            mCategory.setTimeStart(cStartList.get(i));
            mCategory.setTimeEnd(cEndList.get(i));

            ArrayList<CategoriesResponseBody.Category.Route> mRouteList = new ArrayList<>();
            for(int j=0; j<cCountList.get(i); j++,k++){
                CategoriesResponseBody.Category.Route mRoute = new CategoriesResponseBody.Category.Route();
                mRoute.setRouteName(rNameList.get(k));
                mRoute.setRouteId(rIdList.get(k));
                mRoute.setScore(rScoreList.get(k));

                mRouteList.add(mRoute);
            }

            mCategory.setRoutes(mRouteList);
            arr.add(mCategory);
        }
        categoriesInfo.setCategories(arr);
    }

    /**
     * Create an object that stores all information about categories and routes.
     *
     * @param mResponseBody Jackson POJO containing categories and routes information.
     */
    public Categories(CategoriesResponseBody mResponseBody){
        categoriesInfo = new CategoriesResponseBody(mResponseBody);
    }

    /**
     * Construct a clone of the given Categories object.
     *
     * @param categories Categories object to clone.
     */
    public Categories(Categories categories){
        categoriesInfo = new CategoriesResponseBody(categories.getJSON());
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
        List<HintableSpinnerItem> spinnerList = new ArrayList<>();
        spinnerList.add(new CategoryItem(categoryHint, routeHint));

        for(CategoriesResponseBody.Category c: categoriesInfo.getCategories()){
            List<HintableSpinnerItem> routes = new ArrayList<>();
            routes.add(new RouteItem(routeHint));
            for(CategoriesResponseBody.Category.Route r: c.getRoutes()){
                routes.add(new RouteItem(c.getCategoryId(), r.getRouteId(),r.getRouteName(),
                        r.getScore()));
            }

            spinnerList.add(new CategoryItem(c.getCategoryId(), c.getCategoryName(), routes));
        }

        return spinnerList;
    }

    @Override
    public String toString(){
        return categoriesInfo.toString();
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
     * Return a list of category names in the order they appear in categoriesInfo.
     *
     * @return ArrayList of category name.
     */
    public ArrayList<String> getCategoryNameList(){
        ArrayList<String> categoryNameList = new ArrayList<>();
        if(categoriesInfo.getCategories()!=null){
            for(CategoriesResponseBody.Category c : categoriesInfo.getCategories()){
                categoryNameList.add(c.getCategoryName());
            }
        }
        return categoryNameList;
    }

    /**
     * Return a list of category id in the order they appear in categoriesInfo.
     *
     * @return ArrayList of category id.
     */
    public ArrayList<String> getCategoryIdList(){
        ArrayList<String> categoryIdList = new ArrayList<>();
        if(categoriesInfo.getCategories()!=null) {
            for (CategoriesResponseBody.Category c : categoriesInfo.getCategories()) {
                categoryIdList.add(c.getCategoryId());
            }
        }
        return categoryIdList;
    }

    /**
     * Return a list of route count in each category, ordered by the order each category appear
     * in categoriesInfo.
     *
     * @return ArrayList of route count.
     */
    public ArrayList<Integer> getCategoryRouteCountList(){
        ArrayList<Integer> categoryRouteCountList = new ArrayList<>();
        if(categoriesInfo.getCategories()!=null) {
            for (CategoriesResponseBody.Category c : categoriesInfo.getCategories()) {
                categoryRouteCountList.add(c.getRoutes().size());
            }
        }
        return categoryRouteCountList;
    }

    /**
     * Return a list of route name, ordered by the order they appear in categoriesInfo.
     *
     * @return ArrayList of route name.
     */
    public ArrayList<String> getRouteNameList(){
        ArrayList<String> routeNameList = new ArrayList<>();
        if(categoriesInfo.getCategories()!=null) {
            for (CategoriesResponseBody.Category c : categoriesInfo.getCategories()) {
                for (CategoriesResponseBody.Category.Route r : c.getRoutes()) {
                    routeNameList.add(r.getRouteName());
                }
            }
        }
        return routeNameList;
    }

    /**
     * Return a list of route id, ordered by the order they appear in categoriesInfo.
     *
     * @return ArrayList of route id.
     */
    public ArrayList<String> getRouteIdList(){
        ArrayList<String> routeIdList = new ArrayList<>();
        if(categoriesInfo.getCategories()!=null) {
            for (CategoriesResponseBody.Category c : categoriesInfo.getCategories()) {
                for (CategoriesResponseBody.Category.Route r : c.getRoutes()) {
                    routeIdList.add(r.getRouteId());
                }
            }
        }
        return routeIdList;
    }

    /**
     * Return a list of route score, ordered by the order they appear in categoriesInfo.
     *
     * @return ArrayList of route score.
     */
    public ArrayList<String> getRouteScoreList(){
        ArrayList<String> routeScoreList = new ArrayList<>();
        if(categoriesInfo.getCategories()!=null) {
            for (CategoriesResponseBody.Category c : categoriesInfo.getCategories()) {
                for (CategoriesResponseBody.Category.Route r : c.getRoutes()) {
                    routeScoreList.add(r.getScore());
                }
            }
        }
        return routeScoreList;
    }

    /**
     * Return a list of finalized flag (in bytes. true:1; false:0) for categories in the order their
     * category appear in categoriesInfo.
     *
     * @return Array of 1's and 0's corresponding to the finalized flag for each category.
     */
    public byte[] getCategoryFinalizeArray(){
        if(categoriesInfo.getCategories()!=null) {
            int size = categoriesInfo.getCategories().size();
            byte[] categoryFinalizedArray = new byte[size];
            for (int i = 0; i < size; i++) {
                if (categoriesInfo.getCategories().get(i).isScoresFinalized())
                    categoryFinalizedArray[i] = 1;
                else
                    categoryFinalizedArray[i] = 0;
            }
            return categoryFinalizedArray;
        }
        else{
            return null;
        }
    }

    /**
     * Return a list of category time start in the order they appear in categoriesInfo.
     *
     * @return ArrayList of category time start.
     */
    public ArrayList<String> getCategoryStartList(){
        ArrayList<String> categoryStartList = new ArrayList<>();
        if(categoriesInfo.getCategories()!=null) {
            for (CategoriesResponseBody.Category c : categoriesInfo.getCategories()) {
                categoryStartList.add(c.getTimeStart());
            }
        }
        return categoryStartList;
    }

    /**
     * Return a list of category time end in the order they appear in categoriesInfo.
     *
     * @return ArrayList of category time end.
     */
    public ArrayList<String> getCategoryEndList(){
        ArrayList<String> categoryEndList = new ArrayList<>();
        if(categoriesInfo.getCategories()!=null) {
            for (CategoriesResponseBody.Category c : categoriesInfo.getCategories()) {
                categoryEndList.add(c.getTimeEnd());
            }
        }
        return categoryEndList;
    }

    public CategoriesResponseBody.Category findCategoryById(String categoryId){
        if(categoriesInfo != null){
            return categoriesInfo.findCategoryById(categoryId);
        }
        return null;
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
         * @param categoryHintText Hint for category spinner
         * @param routeHintText Hint to display for route spinner when category spinner
         *                      is showing hint.
         */
        public CategoryItem(String categoryHintText, String routeHintText){
            this.fullCategoryName = categoryHintText;
            this.isHint = true;
            List<HintableSpinnerItem> hintRoutes = new ArrayList<>();
            hintRoutes.add(new RouteItem(routeHintText));
            this.routes = hintRoutes;
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
            return new ArrayList<>(routes);
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
