package com.nusclimb.live.crimp.request;

import android.util.Log;

import com.nusclimb.live.crimp.Helper;
import com.nusclimb.live.crimp.json.Climber;
import com.nusclimb.live.crimp.json.RoundInfo;
import com.nusclimb.live.crimp.json.RoundInfoMap;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

/**
 * Spice request for a list of all climbers taking part in a round.
 * 
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 *
 */
public class ClimberInfoRequest extends SpringAndroidSpiceRequest<RoundInfoMap> {
	private static final String TAG = ClimberInfoRequest.class.getSimpleName();
	private static final String BASE_URL = "http://crimp-stage.herokuapp.com/judge/get/";
	
	private String round;
	
	/**
	 * Construct a request for climber info.
	 * 
	 * @param round  Full name of the round to request.
	 */
	public ClimberInfoRequest(String round) {
		super(RoundInfoMap.class);
		this.round = round;
	}

	@Override
	public RoundInfoMap loadDataFromNetwork() throws Exception {
		// round is in full name. Need to convert to server alias.
		String serverAliasRound = Helper.toServerRound(round);
		
		if(serverAliasRound == null){
			// Wrong round name. Quit early.
			Log.w(TAG, round + " is not a valid round full name.");
			throw new Exception("Invalid round name");
		}
		
		// Craft URL.
		String address = BASE_URL + serverAliasRound;
		
		// Actual network calls.
		RoundInfo content = getRestTemplate().getForObject(address, RoundInfo.class);
		
		Log.v(TAG, content.toString());
		
		// Convert to map.
		RoundInfoMap map = new RoundInfoMap();
		for(Climber i : content.getClimbers()){
			map.put(i.getC_id(), i.getC_name());
		}
		
		return map;
    }
	
	public String createCacheKey() {
		return round;
	}

}
