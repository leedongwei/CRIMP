package com.nusclimb.live.crimp.hello;

/**
 * Interface for spinner item with additional flag to indicate if it is a hint.
 */
public interface HintableSpinnerItem {
    String getId();
    String getText();
    boolean isHint();
}