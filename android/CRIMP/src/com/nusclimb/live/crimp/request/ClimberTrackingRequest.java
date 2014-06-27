package com.nusclimb.live.crimp.request;

import com.nusclimb.live.crimp.Helper;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

/**
 * Spice request for score of a climber for a route.
 * 
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 *
 */
public class ClimberTrackingRequest extends SpringAndroidSpiceRequest<String> {
	private static final String TAG = ClimberTrackingRequest.class.getSimpleName();
	private String baseUrl = "http://crimp-sockstage.herokuapp.com/judge/";
	
	public enum State{
		PUSH("push"),
		POP("pop");
		
		private String string;
		
		private State(String string){
			this.string = string;
		}
		
		@Override
		public String toString(){
			return this.string;
		}
	}
	
	private String c_id;
	private String r_id;
	private State mState;
	
	public ClimberTrackingRequest(String c_id, String r_id, State state) {
		super(String.class);
		this.c_id = c_id;
		this.r_id = r_id;
		this.mState = state;
	}
	
	public ClimberTrackingRequest(String climberId, String round, String route, State state) {
		super(String.class);
		this.c_id = climberId;
		
		// Convert to server alias.
		String serverAliasRound = Helper.toServerRound(round);
		String serverAliasRoute = Helper.parseRoute(route);
		this.r_id = serverAliasRound + serverAliasRoute;
		
		this.mState = state;
	}

	@Override
	public String loadDataFromNetwork() throws Exception {
		// Craft URL.
		String address = baseUrl + mState.toString() + "/" + c_id + "/" + r_id + 
				"?q=" + Helper.nextAlphaNumeric(6);
		
		// Actual network calls.
		String reply = getRestTemplate().getForObject(address, String.class);
		
		return reply;
    }
	
	public String createCacheKey() {
		return "TrackingClimber:"+mState.toString();
	}
}
