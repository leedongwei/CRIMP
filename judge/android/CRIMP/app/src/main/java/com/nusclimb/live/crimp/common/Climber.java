package com.nusclimb.live.crimp.common;

/**
 * This class is the main way for us to store Climber information. Climber information
 * requested from server are stored in this class.
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class Climber {
    private String climberId;
    private String climberName;
    private String totalScore;

    /**
     * Construct a new instance of Climber that has all its field set to null.
     */
    public Climber(){}

    /**
     * Construct a clone of the given Climber object.
     *
     * @param climber Climber object to clone.
     */
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
