package com.nusclimb.live.crimp.json;

/**
 * Jackson POJO
 * 
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 *
 */
public class Score {
	private String c_name;
	private String c_score;
	
	public String getC_name(){
		return c_name;
	}
	
	public String getC_score(){
		return c_score;
	}
	
	public void setC_name(String c_name){
		this.c_name = c_name;
	}
	
	public void setC_score(String c_score){
		this.c_score = c_score;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		
		sb.append("{ c_name="+c_name);
		sb.append(",c_score="+c_score);
		sb.append(" }");
		
		return sb.toString();
	}
}
