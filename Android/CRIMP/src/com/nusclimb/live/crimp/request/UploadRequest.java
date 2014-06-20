package com.nusclimb.live.crimp.request;

import android.util.Log;

import com.nusclimb.live.crimp.Helper;
import com.nusclimb.live.crimp.json.Score;
import com.nusclimb.live.crimp.json.SessionUpload;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

/**
 * Spice request to upload score for current session.
 * 
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 *
 */
public class UploadRequest extends SpringAndroidSpiceRequest<Object>{
	private static final String TAG = UploadRequest.class.getSimpleName();
	private static final String BASE_URL_GET = "http://crimp-stage.herokuapp.com/judge/get/";
	private static final String BASE_URL_POST = "http://crimp-stage.herokuapp.com/judge/set";
	
	private SessionUpload uploadContent;
	private int id;
	
	public UploadRequest(SessionUpload uploadContent, int requestId){
		super(Object.class);
		id = requestId;
		this.uploadContent = uploadContent;
	}
	
	@Override
	public Object loadDataFromNetwork() throws Exception {
		if(uploadContent.getC_score() == null){
			Log.w(TAG, "c_score is null.");
			
			// Get old score request
			// Craft URL.
			String address = BASE_URL_GET + uploadContent.getC_id() + "/" + 
			uploadContent.getR_id() + "?q=" + Helper.nextAlphaNumeric(6);
			
			// Actual network calls.
			Score content = getRestTemplate().getForObject(address, Score.class);
			String oldScore = content.getC_score();
			
			if(oldScore == null){
				Log.w(TAG, "Requested old score is null.");
				throw new Exception("Get old score failed.");
			}
			else{
				Log.i(TAG, "Address=" + address + "\ncontent="+content.toString());
			}
			
			uploadContent.updateScoreWithOld(oldScore);
		}
		
		//TODO wipe score.
		//uploadContent.setC_score("");
		
		Log.i(TAG, "Doing post: "+uploadContent.toString());
		
		return getRestTemplate().postForObject(BASE_URL_POST, uploadContent, Object.class);
    }
	
	public String createCacheKey() {
		return id + uploadContent.toString();
	}
	
}
