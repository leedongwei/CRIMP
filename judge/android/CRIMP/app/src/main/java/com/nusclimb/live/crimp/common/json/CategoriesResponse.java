package com.nusclimb.live.crimp.common.json;

import java.util.ArrayList;

/**
 * Created by user on 30-Jun-15.
 */
public class CategoriesResponse extends ArrayList<Category>{
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("categories: [\n");

        if(this.size()<=1){
            for(Category c:this){
                sb.append("\t"+c.toString());
            }

            sb.append("]");
        }
        else{
            sb.append("\t"+ this.get(0).toString());

            for(int i=1; i<this.size(); i++){
                sb.append(",\n\t"+this.get(i).toString());
            }

            sb.append("]");
        }

        return sb.toString();
    }
}
