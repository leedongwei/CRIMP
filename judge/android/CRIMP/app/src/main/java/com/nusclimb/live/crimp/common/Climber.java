package com.nusclimb.live.crimp.common;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class Climber {
    private String climberId;
    private String climberName;
    private String totalScore;

    public Climber(){}

    public Climber(Climber climber){
        climberId = climber.getClimberId();
        climberName = climber.getClimberName();
        totalScore = climber.getTotalScore();
    }

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

    public String getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(String totalScore) {
        this.totalScore = totalScore;
    }
}
