package com.nusclimb.live.crimp.common.json;

import java.util.ArrayList;

/**
 * Jackson POJO
 * 
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 *
 */
@SuppressWarnings("serial")
public class Climbers extends ArrayList<Climber> {
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("[ ");
		for(Climber i : this){
			sb.append(i.toString());
			sb.append(",\n");
		}
		
		if(!this.isEmpty()){
			sb.deleteCharAt(sb.length()-2);
		}
		
		sb.append(" ]");
		
		return sb.toString();
	}
}
