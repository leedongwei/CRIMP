package com.nusclimb.live.crimp.service;

import com.octo.android.robospice.JacksonSpringAndroidSpiceService;

public class CrimpService extends JacksonSpringAndroidSpiceService{
	private static int requestId = 0;
	
	public static int nextRequestId(){
		return requestId++;
	}
}
