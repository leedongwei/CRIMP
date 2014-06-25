package com.nusclimb.live.crimp.request;

import android.util.Log;

import com.nusclimb.live.crimp.json.SessionUpload;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

/**
 * Spice request to upload score for current session.
 * 
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 *
 */
public class UploadScoreSubmit extends SpringAndroidSpiceRequest<Object>{
	private static final String TAG = UploadScoreSubmit.class.getSimpleName();
	private String baseUrl = "http://crimp-stage.herokuapp.com/judge/set";
	
	private SessionUpload uploadContent;
	private int id;
	
	/**
	 * Construct a request to submit score. 
	 * 
	 * @param uploadContent POJO for session info.
	 * @param requestId Message id.
	 */
	public UploadScoreSubmit(SessionUpload uploadContent, int requestId){
		super(Object.class);
		id = requestId;
		this.uploadContent = uploadContent;
	}
	
	@Override
	public Object loadDataFromNetwork() throws Exception {
		Log.i(TAG, "Doing post: "+uploadContent.toString());
		
		return getRestTemplate().postForObject(baseUrl, uploadContent, Object.class);
    }
	
	public void updateScoreWithOld(String oldScore){
		uploadContent.updateScoreWithOld(oldScore);
	}
	
	public String getBaseUrl(){
		return baseUrl;
	}
	
	public void setBaseUrl(String url){
		baseUrl = url;
	}
	
	public SessionUpload getUploadContent(){
		return uploadContent;
	}
	
	public String createCacheKey() {
		return "["+id+"]" + uploadContent.toString();
	}
	
}