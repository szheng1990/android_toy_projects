package com.example.crystal.ball.sz;

import java.util.Random;

public class CrystalBall {
	
	public String getAnAnswer(){
		String answer = "";
		
		Random randomGenerator = new Random();
		int randomNumber = randomGenerator.nextInt(3);
		if (randomNumber == 0){
			answer = "yes";
		}else if (randomNumber == 1){
			answer = "no";
		}else {
			answer = "maybe";
		}
		
		return answer;
	}

}
