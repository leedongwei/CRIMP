package rocks.crimp.crimp.common.event;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class CameraAcquired {
    public final int widthPx;
    public final int heightPx;

    public CameraAcquired(int width, int height){
        this.widthPx = width;
        this.heightPx = height;
    }
}
