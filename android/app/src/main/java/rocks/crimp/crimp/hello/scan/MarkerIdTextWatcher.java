package rocks.crimp.crimp.hello.scan;

import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;

import rocks.crimp.crimp.CrimpApplication;
import rocks.crimp.crimp.common.Action;
import timber.log.Timber;

/**
 * This class monitors for any changes in the EditText view for climber id.
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
class MarkerIdTextWatcher implements TextWatcher {
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
        if(markerId!=null && markerId.matches(ScanFragment.MARKER_ID_PATTERN)){
            makeButton.act();
            enableNextButton.act();
        }
        else{
            CrimpApplication.getAppState().edit()
                    .putString(CrimpApplication.MARKER_ID, s.toString())
                    .apply();

            if(s.toString().matches(ScanFragment.MARKER_ID_DIGIT_PATTERN)){
                makeButton.act();
                enableNextButton.act();
            }
            else{
                disableNextButton.act();
            }
        }
    }
}
