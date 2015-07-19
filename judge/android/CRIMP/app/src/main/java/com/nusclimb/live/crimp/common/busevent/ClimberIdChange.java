package com.nusclimb.live.crimp.common.busevent;

/**
 * Created by weizhi on 18/7/2015.
 */
public class ClimberIdChange {
    private final String TAG = ClimberIdChange.class.getSimpleName();
    private final int idLength;

    public ClimberIdChange(int idLength){
        this.idLength = idLength;
    }

    public int getIdLength(){
        return idLength;
    }
}
