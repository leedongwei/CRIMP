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
	private static final String BASE_URL = "http://crimp-stage.herokuapp.com/judge/get/";
	
	private String climberId;
	private String round;
	private String route;
	
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
		this.round = round;
		this.route = route;
	}

	@Override
	public Score loadDataFromNetwork() throws Exception {
		// Convert to server alias.
		String serverAliasRound = Helper.toServerRound(round);
		String serverAliasRoute = Helper.parseRoute(route);
		
		if(serverAliasRound == null){
			// Wrong round name. Quit early.
			Log.w(TAG, round + " is not a valid round full name.");
			throw new Exception("Invalid round name");
		}
		
		// Craft URL.
		String address = BASE_URL + climberId + "/" + serverAliasRound + serverAliasRoute;
		
		// Actual network calls.
		Score content = getRestTemplate().getForObject(address, Score.class);
		
		Log.v(TAG, content.toString());
		
		return content;
    }
	
	public String createCacheKey() {
		return climberId + round + route;
	}
}
