package com.nusclimb.live.crimp.qr;

import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.hello.ScanFragment;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/**
 * Handler to {@code DecodeThread}. Process {@code Message} associated with
 * {@code DecodeThread}. This will process either a decode request or a stop
 * request.
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 *
 */
public class DecodeHandler extends Handler{
    private static final String TAG = DecodeHandler.class.getSimpleName();
    private final boolean DEBUG = false;

    private final String PREFIX;	// Magic string to check if QR Code is valid

    private Handler mainThreadHandler;
    private Point previewResolution;	// Size of the previewView
    private final MultiFormatReader multiFormatReader;  // ZXing stuff. For decoding QR code.
    private boolean running;

    DecodeHandler(Handler mainThreadHandler, String qrPrefix, Point previewResolution) {
        PREFIX = qrPrefix;
        this.mainThreadHandler = mainThreadHandler;
        this.previewResolution = previewResolution;
        running = true;

        // Instantiating the decoder.
        multiFormatReader = new MultiFormatReader();

        // Preparing hint of possible code format and inform decoder.
        Map<DecodeHintType,Object> hints = new EnumMap<>(DecodeHintType.class);
        Collection<BarcodeFormat> decodeFormats = EnumSet.of(BarcodeFormat.QR_CODE);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
        multiFormatReader.setHints(hints);

        Log.d(TAG, "DecodeHandler constructed.");
    }

    @Override
    public void handleMessage(Message message) {
        switch (message.what) {
            case R.id.decode:
                if(DEBUG) Log.d(TAG, "decode msg received. running:"+running);
                if(running)
                    decode((byte[]) message.obj, message.arg1, message.arg2);
                break;
            case R.id.decode_pause:
                if(DEBUG) Log.d(TAG, "decode_pause msg received. running:"+running);
                running = false;
                break;
            case R.id.decode_resume:
                if(DEBUG) Log.d(TAG, "decode_resume msg received. running:"+running);
                running = true;
                break;
            case R.id.quit:
                Log.d(TAG+".handleMessage()", "DecodeHandler receive msg 'quit'.");
                running = false;
                Looper.myLooper().quit();
                break;
            default:
                Log.w(TAG+".handleMessage()", "DecodeHandler receive unknown msg '" + message.what + "'.");
                break;
        }
    }

    /**
     * Decode the data within the viewfinder rectangle, and time how long it took. For efficiency,
     * reuse the same reader objects from one decode to the next.
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
                Log.d(TAG+".decode()", "Found non BA2015 result: " + rawResult.getText());
                if (mainThreadHandler != null) {
                    Message message = Message.obtain(mainThreadHandler, R.id.decode_failed);
                    message.sendToTarget();
                }
            }
            else{
                Log.i(TAG+".decode()", "Found barcode in " + (end - start) + " ms: " + result);
                if (mainThreadHandler != null) {
                    Message message = Message.obtain(mainThreadHandler, R.id.decode_succeeded, result);
                    Bundle bundle = new Bundle();
                    bundleThumbnail(source, bundle);
                    message.setData(bundle);
                    message.sendToTarget();
                }
            }
        }
        else {
            if (mainThreadHandler != null) {
                Message message = Message.obtain(mainThreadHandler, R.id.decode_failed);
                message.sendToTarget();
            }
        }
    }

    /**
     * A factory method to build the appropriate LuminanceSource object based on the format
     * of the preview buffers, as described by Camera.Parameters.
     *
     * @param data A preview frame.
     * @param width The width of the image.
     * @param height The height of the image.
     * @return A PlanarYUVLuminanceSource instance.
     */
    private PlanarYUVLuminanceSource buildLuminanceSource(byte[] data, int width, int height) {
        int left, right, top, bottom;
        top = 0;
        bottom = height;
        right = width;
        if(width >= previewResolution.y){
            left = width - previewResolution.y;
        }
        else{
            left = 0;
        }
        Rect rect = new Rect(left, top, right, bottom);

        // Go ahead and assume it's YUV rather than die.
        return new PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top,
                rect.width(), rect.height(), false);
    }

    /**
     * Method to store scanned image as a bundle.
     *
     * @param source The scanned image.
     * @param bundle The scanned image converted to bundle.
     */
    private static void bundleThumbnail(PlanarYUVLuminanceSource source, Bundle bundle) {
        int[] pixels = source.renderThumbnail();
        int width = source.getThumbnailWidth();
        int height = source.getThumbnailHeight();
        Bitmap bitmap = Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.ARGB_8888);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
        bundle.putByteArray(DecodeThread.BARCODE_BITMAP, out.toByteArray());
        bundle.putFloat(DecodeThread.BARCODE_SCALED_FACTOR, (float) width / source.getWidth());
    }

    /**
     * Method to verify that the string decoded from QR code is legitimate.
     *
     * @param rawResult Raw string decoded from QR code.
     * @return String containing climber info in the format [id:name]. Null if string is
     * not a BA2015 string.
     */
    private String verifyResult(String rawResult){
        if(!rawResult.startsWith(PREFIX)){
            Log.d(TAG+".verifyResult()", "Verifying '" + rawResult + "' to be wrong prefix.");
            return null;
        }

        int tokenCount = 0;
        for(int i = 0; i < rawResult.length(); i++) {
            if(rawResult.charAt(i) == ';')
                tokenCount++;
        }
        if(tokenCount == 1){
            String result = rawResult.split(PREFIX)[1];
            Log.i(TAG+".verifyResult()", "Verified result: " + result);
            return result;
        }

        return null;
    }
}
