package com.nusclimb.live.crimp;

import java.util.ArrayList;
import java.util.List;

public class Helper {
	public static List<Integer> primitiveToList(int[] primitive){
		List<Integer> myList = new ArrayList<Integer>();
		
		for(int i : primitive){
			myList.add(i);
		}
		
		return myList;
	}
	
	public static List<String> primitiveToList(String[] primitive){
		List<String> myList = new ArrayList<String>();
		
		for(String i : primitive){
			myList.add(i);
		}
		
		return myList;
	}

}
