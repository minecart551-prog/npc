package noppes.npcs.controllers;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NBTTags;
import noppes.npcs.constants.SyncType;
import noppes.npcs.controllers.data.*;
import noppes.npcs.items.ItemScripted;
import noppes.npcs.packets.Packets;
import noppes.npcs.packets.client.PacketSync;

public class SyncController {
	
	public static void syncPlayer(ServerPlayer player){
		ListTag list = new ListTag();
		
		for(Faction faction : FactionController.instance.factions.values()){
			list.add(faction.writeNBT(new CompoundTag()));
		}
		CompoundTag compound = new CompoundTag();
		compound.put("Data", list);
		Packets.send(player, new PacketSync(SyncType.FACTION, compound, true));
		
		for(QuestCategory category : QuestController.instance.categories.values()){
			Packets.send(player, new PacketSync(SyncType.QUEST_CATEGORY, category.writeNBT(new CompoundTag()), false));
		}
		Packets.send(player, new PacketSync(SyncType.QUEST_CATEGORY, new CompoundTag(), true));
				
		for(DialogCategory category : DialogController.instance.categories.values()){
			Packets.send(player, new PacketSync(SyncType.DIALOG_CATEGORY, category.writeNBT(new CompoundTag()), false));
		}
		Packets.send(player, new PacketSync(SyncType.DIALOG_CATEGORY, new CompoundTag(), true));
		
		list = new ListTag();			
		for(RecipeCarpentry category : RecipeController.instance.globalRecipes.values()){
			list.add(category.writeNBT());
			if(list.size() > 10){
				compound = new CompoundTag();
				compound.put("Data", list);
				Packets.send(player, new PacketSync(SyncType.RECIPE_NORMAL, compound, false));
				list = new ListTag();
			}
		}
		compound = new CompoundTag();
		compound.put("Data", list);
		Packets.send(player, new PacketSync(SyncType.RECIPE_NORMAL, compound, true));
		
		list = new ListTag();			
		for(RecipeCarpentry category : RecipeController.instance.anvilRecipes.values()){
			list.add(category.writeNBT());
			if(list.size() > 10){
				compound = new CompoundTag();
				compound.put("Data", list);
				Packets.send(player, new PacketSync(SyncType.RECIPE_CARPENTRY, compound, false));
				list = new ListTag();
			}
		}
		compound = new CompoundTag();
		compound.put("Data", list);
		Packets.send(player, new PacketSync(SyncType.RECIPE_CARPENTRY, compound, true));

		PlayerData data = PlayerData.get(player);
		Packets.send(player, new PacketSync(SyncType.PLAYER_DATA, data.getNBT(), true));
	}
	
	public static void syncAllDialogs() {
		for(DialogCategory category : DialogController.instance.categories.values()){
			Packets.sendAll(new PacketSync(SyncType.DIALOG_CATEGORY, category.writeNBT(new CompoundTag()), false));
		}
		Packets.sendAll(new PacketSync(SyncType.DIALOG_CATEGORY, new CompoundTag(), true));
	}
	
	public static void syncAllQuests() {
		for(QuestCategory category : QuestController.instance.categories.values()){
			Packets.sendAll(new PacketSync(SyncType.QUEST_CATEGORY, category.writeNBT(new CompoundTag()), false));
		}
		Packets.sendAll(new PacketSync(SyncType.QUEST_CATEGORY, new CompoundTag(), true));
	}
}
