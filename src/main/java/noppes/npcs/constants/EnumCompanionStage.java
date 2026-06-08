package noppes.npcs.constants;

import noppes.npcs.api.constants.AnimationType;

public enum EnumCompanionStage {
	BABY(0, AnimationType.CRAWL, "companion.baby"), 
	CHILD(72000, AnimationType.NONE, "companion.child"),
	TEEN(180000, AnimationType.NONE, "companion.teenager"),
	ADULT(324000, AnimationType.NONE, "companion.adult"),
	FULLGROWN(450000, AnimationType.NONE, "companion.fullinflaten");
	
	public int matureAge;
	public int animation;
	public String name;
	EnumCompanionStage(int age, int animation, String name){
		this.matureAge = age;
		this.animation = animation;
		this.name = name;
	}
}
