package noppes.npcs.controllers.data;

import java.util.HashMap;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import noppes.npcs.EventHooks;
import noppes.npcs.constants.EnumQuestCompletion;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.packets.Packets;
import noppes.npcs.packets.client.PacketAchievement;
import noppes.npcs.packets.client.PacketChat;
import noppes.npcs.quests.QuestInterface;

public class PlayerQuestData {
	public HashMap<Integer,QuestData> activeQuests = new HashMap<Integer,QuestData>();
	public HashMap<Integer,Long> finishedQuests = new HashMap<Integer,Long>();
	
	public void loadNBTData(CompoundTag mainCompound) {
		if(mainCompound == null)
			return;
		CompoundTag compound = mainCompound.getCompound("QuestData");
		
        ListTag list = compound.getList("CompletedQuests", 10);
        if(list != null){
        	HashMap<Integer,Long> finishedQuests = new HashMap<Integer,Long>();
            for(int i = 0; i < list.size(); i++)
            {
                CompoundTag nbttagcompound = list.getCompound(i);
                finishedQuests.put(nbttagcompound.getInt("Quest"),nbttagcompound.getLong("Date"));
            }
            this.finishedQuests = finishedQuests;
        }
		
        ListTag list2 = compound.getList("ActiveQuests", 10);
        if(list2 != null){
        	HashMap<Integer,QuestData> activeQuests = new HashMap<Integer,QuestData>();
            for(int i = 0; i < list2.size(); i++)
            {
                CompoundTag nbttagcompound = list2.getCompound(i);
                int id = nbttagcompound.getInt("Quest");
                Quest quest = QuestController.instance.quests.get(id);
                if(quest == null)
                	continue;
                QuestData data = new QuestData(quest);
                data.readAdditionalSaveData(nbttagcompound);
                activeQuests.put(id,data);
            }
            this.activeQuests = activeQuests;
        }
        
	}

	public void saveNBTData(CompoundTag maincompound) {
		CompoundTag compound = new CompoundTag();
		ListTag list = new ListTag();
		for(int quest : finishedQuests.keySet()){
			CompoundTag nbttagcompound = new CompoundTag();
			nbttagcompound.putInt("Quest", quest);
			nbttagcompound.putLong("Date", finishedQuests.get(quest));
			list.add(nbttagcompound);
		}
		
		compound.put("CompletedQuests", list);
		
		ListTag list2 = new ListTag();
		for(int quest : activeQuests.keySet()){
			CompoundTag nbttagcompound = new CompoundTag();
			nbttagcompound.putInt("Quest", quest);
			activeQuests.get(quest).addAdditionalSaveData(nbttagcompound);
			list2.add(nbttagcompound);
		}
		
		compound.put("ActiveQuests", list2);
		
		maincompound.put("QuestData", compound);
	}

	public QuestData getQuestCompletion(Player player,EntityNPCInterface npc) {
		for(QuestData data : activeQuests.values()){
			Quest quest = data.quest;
			if(quest != null && quest.completion == EnumQuestCompletion.Npc && quest.completerNpc.equals(npc.getName().getString()) && quest.questInterface.isCompleted(player)){
				return data;
			}
		}
		return null;
	}

	public boolean checkQuestCompletion(Player player, int type) {
		boolean bo = false;
		for(QuestData data : this.activeQuests.values()){
			if(data.quest.type != type && type >= 0)
				continue;
			
			QuestInterface inter =  data.quest.questInterface;
			
			if(inter.isCompleted(player)){
				if(!data.isCompleted){
					if(!data.quest.complete(player,data)){
						Packets.send((ServerPlayer)player, new PacketAchievement(Component.translatable("quest.completed"), Component.translatable(data.quest.title), 2));
						Packets.send((ServerPlayer)player, new PacketChat(Component.translatable("quest.completed").append(": ").append(Component.translatable(data.quest.title))));
					}
					data.isCompleted = true;
					bo = true;
					
					EventHooks.onQuestFinished(PlayerData.get(player).scriptData, data.quest);
				}
			}
			else
				data.isCompleted = false;
		}
		return bo;
		
	}
	
}
