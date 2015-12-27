package com.nusclimb.live.crimp.hello;

/**
 * http://stackoverflow.com/questions/13877681/how-can-i-add-a-hint-to-the-spinner-widget
 */
public interface SpinnerItem {
    public String getItemString();
    public String toString();
    public boolean isHint();
}