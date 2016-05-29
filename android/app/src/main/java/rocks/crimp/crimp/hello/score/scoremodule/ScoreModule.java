package rocks.crimp.crimp.hello.score.scoremodule;

import android.support.annotation.Nullable;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public interface ScoreModule {
    void notifyScore(@Nullable String score);

    interface ScoreModuleInterface{
        void append(String s);
        void backspace();
    }
}
