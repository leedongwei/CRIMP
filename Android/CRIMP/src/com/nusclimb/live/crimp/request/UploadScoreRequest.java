package com.nusclimb.live.crimp.request;

/**
 * Spice request to download old score for current session.
 * 
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 *
 */
public class UploadScoreRequest extends ScoreRequest {
	private int id;
	
	/**
	 * Construct a request for score. This request is specific to a single 
	 * score upload.
	 * 
	 * @param id Message Id.
	 * @param climberId Id of climber that we are requesting scores of.
	 * @param r_id Route id. Contains both round and route number.
	 */
	public UploadScoreRequest(int id, String climberId, String r_id) {
		super(climberId, r_id);
		this.id = id;
	}
	
	@Override
	public String createCacheKey(){
		return "["+id+"]"+super.createCacheKey();
	}
	
}
