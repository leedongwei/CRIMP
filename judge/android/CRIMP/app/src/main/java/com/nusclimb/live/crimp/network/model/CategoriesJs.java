package com.nusclimb.live.crimp.network.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
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
}
