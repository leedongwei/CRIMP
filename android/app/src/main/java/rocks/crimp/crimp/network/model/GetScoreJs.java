package rocks.crimp.crimp.network.model;

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
public class GetScoreJs implements Serializable{
    @JsonIgnore
    private static final long serialVersionUID = 1L;

    @JsonProperty("climber_scores")
    private ArrayList<ClimberScoreJs> climberScores;

    public ArrayList<ClimberScoreJs> getClimberScores() {
        return climberScores;
    }

    public void setClimberScores(ArrayList<ClimberScoreJs> climberScores) {
        this.climberScores = climberScores;
    }

    @JsonIgnore
    @Nullable
    public ClimberScoreJs getClimberScoreByMarkerId(@Nullable String markerId){
        if(markerId == null){
            return null;
        }

        for(ClimberScoreJs climberScore:climberScores){
            ScoreJs score = climberScore.getScoreByMarkerId(markerId);
            if(score != null){
                ClimberScoreJs result = new ClimberScoreJs();
                result.setClimberId(climberScore.getClimberId());
                result.setClimberName(climberScore.getClimberName());
                ArrayList<ScoreJs> scoreList = new ArrayList<>();
                scoreList.add(score);
                result.setScores(scoreList);

                return result;
            }
        }

        return null;
    }
}
