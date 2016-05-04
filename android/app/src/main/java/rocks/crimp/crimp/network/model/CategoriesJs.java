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
public class CategoriesJs implements Serializable{
    @JsonIgnore
    private static final long serialVersionUID = 1L;

    @JsonProperty("categories")
    private ArrayList<CategoryJs> categories;

    public ArrayList<CategoryJs> getCategories() {
        return categories;
    }

    public void setCategories(ArrayList<CategoryJs> categories) {
        this.categories = categories;
    }

    /**
     * Returns the first CategoryJs found that has the specified category id.
     *
     * @param id category id to look for
     * @return first CategoryJs found that has the specified category id. Null if there are no
     * such CategoryJs or CategoryJs list is null.
     */
    @JsonIgnore
    public CategoryJs getCategoryById(long id){
        if (categories == null){
            return null;
        }

        for(CategoryJs categoryJs:categories){
            if(categoryJs.getCategoryId() == id){
                return categoryJs;
            }
        }

        return null;
    }

    @JsonIgnore
    public CategoryJs getCategoryByName(String name){
        if (categories == null){
            return null;
        }

        for(CategoryJs categoryJs:categories){
            if(categoryJs.getCategoryName().equals(name)){
                return categoryJs;
            }
        }

        return null;
    }

    @JsonIgnore
    public CategoryJs getCategoryByAcronym(String acronym){
        if (categories == null){
            return null;
        }

        for(CategoryJs categoryJs:categories){
            if(categoryJs.getAcronym().equals(acronym)){
                return categoryJs;
            }
        }

        return null;
    }
}
