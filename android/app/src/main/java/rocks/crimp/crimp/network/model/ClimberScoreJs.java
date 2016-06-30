package rocks.crimp.crimp.network.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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
public class ClimberScoreJs implements Serializable{
    @JsonIgnore
    private static final long serialVersionUID = 1L;

    @JsonProperty("climber_id")
    private String climberId;
    @JsonProperty("climber_name")
    private String climberName;
    @JsonProperty("scores")
    private ArrayList<ScoreJs> scores;

    public String getClimberId() {
        return climberId;
    }

    public void setClimberId(String climberId) {
        this.climberId = climberId;
    }

    public String getClimberName() {
        return climberName;
    }

    public void setClimberName(String climberName) {
        this.climberName = climberName;
    }

    public ArrayList<ScoreJs> getScores() {
        return scores;
    }

    public void setScores(ArrayList<ScoreJs> scores) {
        this.scores = scores;
    }

    @JsonIgnore
    @Nullable
    public ScoreJs getScoreByMarkerId(@Nullable String markerId){
        if(markerId == null){
            return null;
        }

        for(ScoreJs scoreJs:scores){
            if(markerId.equals(scoreJs.getMarkerId())){
                return scoreJs;
            }
        }

        return null;
    }
}
