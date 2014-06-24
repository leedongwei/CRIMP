package com.nusclimb.live.crimp.json;

/**
 * Jackson POJO
 * 
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 *
 */
public class SessionUpload {
	private String j_name;
	private String auth_code;
	private String r_id;
	private String c_id;
	private String c_score;
	
	private String currentScore;
	
	public String getJ_name(){
		return j_name;
	}
	
	public String getAuth_code(){
		return auth_code;
	}
	
	public String getR_id(){
		return r_id;
	}
	
	public String getC_id(){
		return c_id;
	}
	
	public String getC_score(){
		return c_score;
	}
	
	public void setJ_name(String j_name){
		this.j_name = j_name;
	}
	
	public void setAuth_code(String auth_code){
		this.auth_code = auth_code;
	}
	
	public void setR_id(String r_id){
		this.r_id = r_id;
	}
	
	public void setC_id(String c_id){
		this.c_id = c_id;
	}
	
	public void setC_score(String c_score){
		this.c_score = c_score;
	}
	
	public void updateScoreWithOld(String oldScore){
		this.c_score = oldScore + this.currentScore;
	}
	
	public void setAll_current(String j_name, String auth_code, 
			String r_id, String c_id, String currentScore){
		this.j_name = j_name;
		this.auth_code = auth_code;
		this.r_id = r_id;
		this.c_id = c_id;
		
		this.currentScore = currentScore;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("{ j_name="+j_name);
		sb.append(",auth_code="+auth_code);
		sb.append(",r_id="+r_id);
		sb.append(",c_id="+c_id);
		sb.append(",c_score="+c_score);
		sb.append(",currentScore="+currentScore);
		sb.append(" }");
		return sb.toString();
	}
}
