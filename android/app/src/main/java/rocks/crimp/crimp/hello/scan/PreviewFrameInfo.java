package rocks.crimp.crimp.hello.scan;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class PreviewFrameInfo {
    public final int previewFrameWidth;
    public final int previewFrameHeight;
    public final int angleToRotatePreviewFrameClockwise;
    public final byte[] data;

    public PreviewFrameInfo(int previewFrameWidth, int previewFrameHeight,
                            int angleToRotatePreviewFrameClockwise, byte[] data){
        this.previewFrameWidth = previewFrameWidth;
        this.previewFrameHeight = previewFrameHeight;
        this.angleToRotatePreviewFrameClockwise = angleToRotatePreviewFrameClockwise;
        this.data = data;
    }
}
