package com.nusclimb.live.crimp.request;

import android.util.Log;

import com.nusclimb.live.crimp.Helper;
import com.nusclimb.live.crimp.json.Score;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

/**
 * Spice request for score of a climber for a route.
 * 
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 *
 */
public class ScoreRequest extends SpringAndroidSpiceRequest<Score> {
	private static final String TAG = ScoreRequest.class.getSimpleName();
	private String baseUrl = "http://crimp-stage.herokuapp.com/judge/get/";
	
	private String climberId;
	private String r_id;
	
	/**
	 * Construct a request for score.
	 * 
	 * @param climberId Id of climber that we are requesting scores of.
	 * @param r_id Route id. Contains both round and route number.
	 */
	public ScoreRequest(String climberId, String r_id) {
		super(Score.class);
		this.climberId = climberId;
		this.r_id = r_id;
	}
	
	/**
	 * Construct a request for score.
	 * 
	 * @param climberId Id of climber that we are requesting scores of.
	 * @param round Full name of the round for the score.
	 * @param route Full name of the route for the score.
	 */
	public ScoreRequest(String climberId, String round, String route) {
		super(Score.class);
		this.climberId = climberId;
		
		// Convert to server alias.
		String serverAliasRound = Helper.toServerRound(round);
		String serverAliasRoute = Helper.parseRoute(route);
		this.r_id = serverAliasRound + serverAliasRoute;
	}

	@Override
	public Score loadDataFromNetwork() throws Exception {
		// Craft URL.
		String address = baseUrl + climberId + "/" + r_id + 
				"?q=" + Helper.nextAlphaNumeric(6);
		
		// Actual network calls.
		Score content = getRestTemplate().getForObject(address, Score.class);
		
		Log.v(TAG, "Address=" + address + "\ncontent=" + content.toString());
		
		return content;
    }
	
	public String getClimberId(){
		return climberId;
	}
	
	public String getR_id(){
		return r_id;
	}
	
	public String createCacheKey() {
		return climberId + r_id;
	}
	
	public String getBaseUrl(){
		return baseUrl;
	}
	
	public void setBaseUrl(String url){
		baseUrl = url;
	}
}
