package noppes.npcs.controllers.data;

import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import noppes.npcs.api.event.QuestEvent;



public class QuestData {
	public Quest quest;
	public boolean isCompleted;
	public CompoundTag extraData = new CompoundTag();
	public QuestData(Quest quest){
		this.quest = quest;
	}
	public void addAdditionalSaveData(CompoundTag nbttagcompound){
		nbttagcompound.putBoolean("QuestCompleted", isCompleted);
		nbttagcompound.put("ExtraData", extraData);
	}
	public void readAdditionalSaveData(CompoundTag nbttagcompound){
		isCompleted = nbttagcompound.getBoolean("QuestCompleted");
		extraData = nbttagcompound.getCompound("ExtraData");
	}
}
