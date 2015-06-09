package com.nusclimb.live.crimp.common.spicerequest;

import android.util.Log;

import com.nusclimb.live.crimp.common.json.SessionUpload;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

/**
 * Spice request to upload score for current session.
 * 
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 *
 */
public class UploadScoreSubmit extends SpringAndroidSpiceRequest<Object>{
	private static final String TAG = UploadScoreSubmit.class.getSimpleName();
	private static final String BASE_URL_POST = "http://crimp-stage.herokuapp.com/judge/set";
	
	private SessionUpload uploadContent;
	private int id;
	
	public UploadScoreSubmit(SessionUpload uploadContent, int requestId){
		super(Object.class);
		id = requestId;
		this.uploadContent = uploadContent;
	}
	
	@Override
	public Object loadDataFromNetwork() throws Exception {
		Log.i(TAG, "Doing post: "+uploadContent.toString());
		
		return getRestTemplate().postForObject(BASE_URL_POST, uploadContent, Object.class);
    }
	
	public void updateScoreWithOld(String oldScore){
		uploadContent.updateScoreWithOld(oldScore);
	}
	
	public String createCacheKey() {
		return "["+id+"]" + uploadContent.toString();
	}
	
}