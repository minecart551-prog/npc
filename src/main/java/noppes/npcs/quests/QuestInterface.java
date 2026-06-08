package noppes.npcs.quests;

import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import noppes.npcs.api.handler.data.IQuestObjective;

public abstract class QuestInterface{
	public int questId;
	public abstract void addAdditionalSaveData(CompoundTag compound);
	public abstract void readAdditionalSaveData(CompoundTag compound);
	public abstract boolean isCompleted(Player player);
	public abstract void handleComplete(Player player);
	public abstract IQuestObjective[] getObjectives(Player player);
}
