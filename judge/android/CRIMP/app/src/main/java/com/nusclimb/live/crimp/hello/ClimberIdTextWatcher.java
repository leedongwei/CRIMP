package com.nusclimb.live.crimp.hello;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

/**
 * This class monitors for any changes in the EditText view for climber id.
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class ClimberIdTextWatcher implements TextWatcher {
    private final String TAG = ClimberIdTextWatcher.class.getSimpleName();
    private final boolean DEBUG = false;
    private ToFragmentInteraction mToFragmentInteraction;

    public ClimberIdTextWatcher(ToFragmentInteraction toFragmentInteraction){
        mToFragmentInteraction = toFragmentInteraction;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {
        if (DEBUG) Log.d(TAG, "afterTextChanged: "+s.toString());
        mToFragmentInteraction.onClimberIdEditTextChange(s.toString());
    }

    /**
     * Interface that the Fragment/Activity hosting climber id EditText view must implement.
     */
    public interface ToFragmentInteraction {
        /**
         * Inform the hosting Fragment/Activity that a change to climber id EditText has
         * occur.
         *
         * @param climberId the climberId after change.
         */
        void onClimberIdEditTextChange(String climberId);
    }
}
