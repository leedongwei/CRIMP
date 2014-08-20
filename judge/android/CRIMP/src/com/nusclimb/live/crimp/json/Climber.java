package com.nusclimb.live.crimp.json;

/**
 * Jackson POJO
 * 
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 *
 */
public class Climber {
	private String c_id;
	private String c_name;
	
	public String getC_id(){
		return c_id;
	}
	
	public String getC_name(){
		return c_name;
	}
	
	public void setC_id(String c_id){
		this.c_id = c_id;
	}
	
	public void setC_name(String c_name){
		this.c_name = c_name;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("{ c_id="+c_id);
		sb.append(",c_name="+c_name);
		sb.append(" }");
		
		return sb.toString();
	}
}
