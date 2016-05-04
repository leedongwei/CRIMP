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
    private Action validAction;
    private Action invalidAction;

    public MarkerIdTextWatcher(@NonNull Action validAction, @NonNull Action invalidAction) {
        this.validAction = validAction;
        this.invalidAction = invalidAction;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {
        Timber.d("afterTextChanged: %s", s.toString());
        String committedId = CrimpApplication.getAppState()
                .getString(CrimpApplication.MARKER_ID, "");
        if(s.toString().matches(REGEXP) && !s.toString().equals(committedId)){
            validAction.act();
        }
        else{
            invalidAction.act();
        }
    }
}
