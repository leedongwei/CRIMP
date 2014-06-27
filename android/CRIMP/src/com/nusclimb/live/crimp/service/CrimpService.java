package com.nusclimb.live.crimp.service;

import com.octo.android.robospice.JacksonSpringAndroidSpiceService;

/**
 * Subclass of JacksonSpringAndroidSpiceService. Provides unique request id.
 * 
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 *
 */
public class CrimpService extends JacksonSpringAndroidSpiceService{
	private static int requestId = 0;
	
	public static int nextRequestId(){
		return requestId++;
	}
}
