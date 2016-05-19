package rocks.crimp.crimp.hello.score.scoremodule;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public abstract class ScoreModule {
    public abstract void notifyScore(String score);

    public interface ScoreModuleInterface{
        void append(String s);
        void backspace();
    }
}
