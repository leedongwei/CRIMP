package com.nusclimb.live.crimp.qr;

import com.nusclimb.live.crimp.qr.QRScanActivity;

import android.os.Looper;
import android.util.Log;

/**
 * Worker thread for handling decode operation.
 * 
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 *
 */
public class DecodeThread extends Thread{
	private static final String TAG = DecodeThread.class.getSimpleName();
	
	public static final String BARCODE_BITMAP = "barcode_bitmap";
	public static final String BARCODE_SCALED_FACTOR = "barcode_scaled_factor";
	
	private DecodeHandler handler;
	private QRScanActivity activity;
	
	public DecodeThread(QRScanActivity activity){
		this.activity = activity;
		
		Log.d(TAG, "DecodeThread constructed.");
	}
	
	@Override
	public void run(){
		Log.d(TAG, "DecodeThread begin running");
		Looper.prepare();
	    handler = new DecodeHandler(activity);
	    Looper.loop();
	}
	
	public DecodeHandler getHandler(){
		return handler;
	}
}
