package com.nusclimb.live.crimp.common.json;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;

/**
 * Created by user on 17-Jul-15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClimbersResponse {
    @JsonProperty("category_id")
    private String categoryId;
    @JsonProperty("climbers")
    private ArrayList<Climber> climbers;

    public String toString(){
        StringBuilder sb = new StringBuilder();

        sb.append("{\n");
        sb.append("\tcategory_id: '"+categoryId+"',\n");
        sb.append("\tclimbers: [\n");

        if(climbers.size() <=1){
            for(Climber c: climbers){
                sb.append(c.toString()+"\n");
            }
        }
        else{
            sb.append(climbers.get(0).toString());
            for(int i=1; i<climbers.size(); i++){
                sb.append(",\n"+climbers.get(i).toString());
            }
        }

        sb.append("\t]\n");
        sb.append("}");

        return sb.toString();
    }

    /*=========================================================================
     * Getter/Setter
     *=======================================================================*/
    public String getCategoryId() {
        return categoryId;
    }
    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public ArrayList<Climber> getClimbers(){
        return climbers;
    }

    public void setClimbers(ArrayList<Climber> climbers){
        this.climbers = climbers;
    }
}
