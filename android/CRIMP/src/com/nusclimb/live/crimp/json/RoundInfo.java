package com.nusclimb.live.crimp.json;

/**
 * Jackson POJO
 * 
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 *
 */
public class RoundInfo {
	private Climbers climbers;
	
	public Climbers getClimbers(){
		return climbers;
	}
	
	public void setClimber(Climbers climbers){
		this.climbers = climbers;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("{ climbers="+climbers.toString());
		sb.append(" }");
		return sb.toString();
	}
}
