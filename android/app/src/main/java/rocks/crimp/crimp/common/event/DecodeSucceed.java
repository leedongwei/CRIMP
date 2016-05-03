package rocks.crimp.crimp.common.event;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class DecodeSucceed {
    public final String result;
    public final Bitmap image;

    public DecodeSucceed(@NonNull String result, @NonNull Bitmap image){
        this.result = result;
        this.image = image;
    }
}
