package rocks.crimp.crimp.hello.scan;

import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;

import rocks.crimp.crimp.CrimpApplication;
import rocks.crimp.crimp.common.Action;
import rocks.crimp.crimp.hello.HelloActivity;
import timber.log.Timber;

/**
 * This class monitors for any changes in the EditText view for climber id.
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
class MarkerIdTextWatcher implements TextWatcher {
    private static final String REGEXP = "[0-9]{3}";
    private Action makeButton;
    private Action enableNextButton;
    private Action disableNextButton;

    public MarkerIdTextWatcher(@NonNull Action makeButton, @NonNull Action enableNextButton,
                               @NonNull Action disableNextButton) {
        this.makeButton = makeButton;
        this.enableNextButton = enableNextButton;
        this.disableNextButton = disableNextButton;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {
        Timber.d("afterTextChanged: %s", s.toString());

        String markerId = CrimpApplication.getAppState().getString(CrimpApplication.MARKER_ID, null);
        String markerIdDigits;
        if(markerId == null){
            markerIdDigits = "";
        }
        else{
            if(!markerId.matches(ScanFragment.MARKER_ID_PATTERN)){
                throw new RuntimeException("Malformed markerId: "+markerId);
            }
            markerIdDigits = markerId.substring(ScanFragment.MARKER_ID_DIGIT_START,
                    ScanFragment.MARKER_ID_DIGIT_END);
        }

        // Checking whether to turn into button
        boolean isValidMarkerIdDigit = s.toString().matches(REGEXP);
        if(isValidMarkerIdDigit){
            // turn into button
            makeButton.act();
        }
        else{
            // do nothing
        }

        // Checking whether to enable next button
        boolean isNewClimber = !s.toString().equals(markerIdDigits);
        if(isValidMarkerIdDigit && isNewClimber){
            // Enable next button
            enableNextButton.act();
        }
        else{
            // Disable next button
            disableNextButton.act();
        }
    }
}
