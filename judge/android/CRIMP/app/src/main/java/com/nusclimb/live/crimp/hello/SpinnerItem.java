package com.nusclimb.live.crimp.hello;

/**
 * http://stackoverflow.com/questions/13877681/how-can-i-add-a-hint-to-the-spinner-widget
 */
public class SpinnerItem {
    private final String text;
    private final boolean isHint;

    public SpinnerItem(String strItem, boolean isHint) {
        this.isHint = isHint;
        this.text = strItem;
    }

    public String getItemString() {
        return text;
    }

    @Override
    public String toString(){
        return text;
    }

    public boolean isHint() {
        return isHint;
    }
}