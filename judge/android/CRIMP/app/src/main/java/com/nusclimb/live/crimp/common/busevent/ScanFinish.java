package com.nusclimb.live.crimp.common.busevent;

/**
 * Created by Zhi on 7/12/2015.
 */
public class ScanFinish {
    private final String TAG = ScanFinish.class.getSimpleName();
    private final String climberId;
    private final String climberName;

    public ScanFinish(String climberId, String climberName){
        this.climberId = climberId;
        this.climberName = climberName;
    }

    public String getClimberId(){
        return climberId;
    }

    public String getClimberName(){
        return climberName;
    }
}
