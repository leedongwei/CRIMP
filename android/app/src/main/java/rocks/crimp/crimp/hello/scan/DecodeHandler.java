package rocks.crimp.crimp.hello.scan;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

import rocks.crimp.crimp.CrimpApplication;
import rocks.crimp.crimp.common.event.DecodeFail;
import rocks.crimp.crimp.common.event.DecodeSucceed;
import timber.log.Timber;

/**
 * Handler to {@code DecodeThread}. Process {@code Message} associated with
 * {@code DecodeThread}. This will process either a decode request or a stop
 * request.
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 *
 */
public class DecodeHandler extends Handler {
    public static final int DECODE = 2;
    public static final int QUIT = 3;
    private static final String PREFIX = "<BA2016>";
    //private static final String REGEX = PREFIX+"[a-zA-Z]{3}\\d{3};.++";
    private static final String REGEX = PREFIX+".{3}\\d{3};.++";

    private boolean running = true;

    private final MultiFormatReader multiFormatReader;  // ZXing stuff. For decoding QR code.

    public DecodeHandler() {
        // Instantiating the decoder.
        multiFormatReader = new MultiFormatReader();

        // Preparing hint of possible code format and inform decoder.
        Map<DecodeHintType,Object> hints = new EnumMap<>(DecodeHintType.class);
        Collection<BarcodeFormat> decodeFormats = EnumSet.of(BarcodeFormat.QR_CODE);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
        multiFormatReader.setHints(hints);
    }

    @Override
    public void handleMessage(Message message) {
        //Timber.d("Received message what:%d, arg1:%d, arg2:%d. Running:%b", message.what, message.arg1, message.arg2, running);
        if(!running){
            return;
        }

        switch (message.what) {
            case DECODE:
                decode((byte[]) message.obj, message.arg1, message.arg2);
                break;
            case QUIT:
                running = false;
                Looper.myLooper().quit();
                break;
            default:

                break;
        }
    }

    /**
     * Decode the data within the viewfinder rectangle, and time how long it took. For efficiency,
     * reuse the same reader objects from one decode to the next. The width and height of the
     * preview frame is based on how the camera is mounted and is not determined by
     * {@link Camera#setDisplayOrientation(int)}.
     *
     * @param data   The YUV preview frame.
     * @param width  The width of the preview frame.
     * @param height The height of the preview frame.
     */
    private void decode(byte[] data, int width, int height) {
        // Obtained a Result from decoding data.
        long start = System.currentTimeMillis();
        Result rawResult = null;
        PlanarYUVLuminanceSource source = buildLuminanceSource(data, width, height);
        if (source != null) {
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            try {
                rawResult = multiFormatReader.decodeWithState(bitmap);
            } catch (ReaderException re) {
                // continue
            } finally {
                multiFormatReader.reset();
            }
        }

        // Send a message to fragment, informing of decode outcome.
        if (rawResult != null) {
            // Need to verify the result.
            String result = verifyResult(rawResult.getText());
            long end = System.currentTimeMillis();
            if(result == null){
                Timber.d("Found non BA2015 result: %s", rawResult.getText());
                CrimpApplication.getBusInstance().post(new DecodeFail());
            }
            else{
                int yuvWidth;
                int yuvHeight;
                // Our app is in portrait mode so the yuvWidth should be smaller than yuvHeight.
                if(height < width){
                    yuvWidth = height;
                    yuvHeight = width;
                }
                else{
                    yuvWidth = width;
                    yuvHeight = height;
                }
                YuvImage yuv = new YuvImage(data, ImageFormat.NV21, yuvWidth, yuvHeight, null);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                yuv.compressToJpeg(new Rect(0, 0, yuvWidth, yuvHeight), 100, out);
                byte[] bytes = out.toByteArray();
                final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                Timber.d("Found barcode in %dms: %s", end-start, result);
                CrimpApplication.getBusInstance().post(new DecodeSucceed(result, bitmap));
            }
        }
        else {
            CrimpApplication.getBusInstance().post(new DecodeFail());
        }
    }

    /**
     * A factory method to build the appropriate LuminanceSource object based on the format
     * of the preview buffers, as described by Camera.Parameters. This is where we determine which
     * part of the preview frame to scan for QR code.
     * The {@code width} and {@code height} of the image is not affected by
     * {@link Camera#setDisplayOrientation(int)} and depend solely on how the camera is mounted.
     *
     * @param data A preview frame.
     * @param width The width of the image.
     * @param height The height of the image.
     * @return A PlanarYUVLuminanceSource instance.
     */
    private PlanarYUVLuminanceSource buildLuminanceSource(byte[] data, int width, int height) {
        /**
         * rect is the bounding rectangle that we are planning to look for our QR code. We will
         * look for QR code in a square with length equals to the floor of preview frame's width
         * and height.
         */
        Rect rect;
        if(width < height){
            rect = new Rect(0, 0, width, width);
        }
        else{
            rect = new Rect(0, 0, height, height);
        }

        // Go ahead and assume it's YUV rather than die.
        return new PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top,
                rect.right, rect.bottom, false);
    }

    /**
     * Method to verify that the string decoded from QR code is legitimate.
     *
     * @param rawResult Raw string decoded from QR code.
     * @return rawResult with PREFIX removed if it is valid. Null otherwise.
     */
    private String verifyResult(String rawResult){
        boolean valid = rawResult.matches(REGEX);
        if(valid){
            rawResult = rawResult.substring(PREFIX.length());
            return rawResult;
        }
        else{
            return null;
        }
    }
}
