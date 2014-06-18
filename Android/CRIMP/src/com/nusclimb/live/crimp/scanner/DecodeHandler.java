package com.nusclimb.live.crimp.scanner;

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
import com.nusclimb.live.crimp.activity.QRScanActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/**
 * Handler to DecodeThread.
 * 
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 *
 */
public class DecodeHandler extends Handler{
	private static final String TAG = DecodeHandler.class.getSimpleName();
	
	private final String PREFIX;	// Magic string to check if QR Code is valid

	private final QRScanActivity activity;
	private final MultiFormatReader multiFormatReader;
	private boolean running;

	DecodeHandler(QRScanActivity activity) {
		PREFIX = activity.getString(R.string.qr_prefix);
		running = true;
		multiFormatReader = new MultiFormatReader();
		Map<DecodeHintType,Object> hints = new EnumMap<>(DecodeHintType.class);
		Collection<BarcodeFormat> decodeFormats = EnumSet.of(BarcodeFormat.QR_CODE);
		hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
	    multiFormatReader.setHints(hints);
	    this.activity = activity;
	    
	    Log.d(TAG, "DecodeHandler constructed.");
	}

	@Override
	public void handleMessage(Message message) {
		if (!running) {
			Log.d(TAG, "DecodeHandler received msg but not running.");
			return;
	    }
	    switch (message.what) {
	    	case R.id.decode:
	    		decode((byte[]) message.obj, message.arg1, message.arg2);
	    		break;
	    	case R.id.quit:
	    		Log.d(TAG, "DecodeHandler receive msg 'quit'.");
	    		running = false;
	    		Looper.myLooper().quit();
	    		break;
	    	default:
	    		Log.w(TAG, "DecodeHandler receive unknown msg '" + message.what + "'.");
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
		long start = System.currentTimeMillis();
	    Result rawResult = null;
	    PlanarYUVLuminanceSource source = activity.getCameraManager().buildLuminanceSource(data, width, height);
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

	    Handler handler = activity.getHandler();
	    if (rawResult != null) {	    	
	    	// Need to verify the result.
	    	String result = verifyResult(rawResult.getText());
	    	long end = System.currentTimeMillis();
	    	if(result == null){
	    		Log.d(TAG, "Found non BA2014 result: " + rawResult.getText());
	    		if (handler != null) {
		    		Message message = Message.obtain(handler, R.id.decode_failed);
		    		message.sendToTarget();
		    	}
	    	}
	    	else{
		    	Log.i(TAG, "Found barcode in " + (end - start) + " ms: " + result);
		    	if (handler != null) {
		    		Message message = Message.obtain(handler, R.id.decode_succeeded, result);
			        Bundle bundle = new Bundle();
			        bundleThumbnail(source, bundle);        
			        message.setData(bundle);
			        message.sendToTarget();
		    	}
	    	}
	    } 
	    else {
	    	if (handler != null) {
	    		Message message = Message.obtain(handler, R.id.decode_failed);
	    		message.sendToTarget();
	    	}
	    }
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
	 * not a BA2014 string.
	 */
	private String verifyResult(String rawResult){
		if(!rawResult.startsWith(PREFIX)){
			Log.d(TAG, "Verifying '" + rawResult + "' to be wrong prefix.");
			return null;
		}
		
		int tokenCount = 0;
		for(int i = 0; i < rawResult.length(); i++) {
		    if(rawResult.charAt(i) == ';') 
		    	tokenCount++;
		}
		if(tokenCount == 1){
			String result = rawResult.split(PREFIX)[1];
			Log.i(TAG, "Verified result: " + result);
			return result;
		}

		return null;
	}
}
