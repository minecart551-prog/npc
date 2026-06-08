package noppes.npcs.controllers.data;

import java.util.HashMap;

import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import noppes.npcs.EventHooks;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.event.PlayerEvent;
import noppes.npcs.api.wrapper.PlayerWrapper;
import noppes.npcs.controllers.FactionController;

public class PlayerFactionData {
	public HashMap<Integer,Integer> factionData = new HashMap<Integer,Integer>();
	
	public void loadNBTData(CompoundTag compound) {
		HashMap<Integer,Integer> factionData = new HashMap<Integer,Integer>();
		if(compound == null)
			return;
        ListTag list = compound.getList("FactionData", 10);
        if(list == null){
        	return;
        }

        for(int i = 0; i < list.size(); i++)
        {
            CompoundTag nbttagcompound = list.getCompound(i);
            factionData.put(nbttagcompound.getInt("Faction"),nbttagcompound.getInt("Points"));
        }
        this.factionData = factionData;
	}

	public void saveNBTData(CompoundTag compound) {
		ListTag list = new ListTag();
		for(int faction : factionData.keySet()){
			CompoundTag nbttagcompound = new CompoundTag();
			nbttagcompound.putInt("Faction", faction);
			nbttagcompound.putInt("Points", factionData.get(faction));
			list.add(nbttagcompound);
		}
		
		compound.put("FactionData", list);
	}

	public int getFactionPoints(Player player, int factionId) {
		Faction faction = FactionController.instance.getFaction(factionId);
		if(faction == null)
			return 0;
		if(!factionData.containsKey(factionId)){
			if(player.level().isClientSide) {
				factionData.put(factionId, faction.defaultPoints);
				return faction.defaultPoints;
			}
			PlayerScriptData handler = PlayerData.get(player).scriptData;
			PlayerWrapper wrapper = (PlayerWrapper) NpcAPI.Instance().getIEntity(player);
			
			PlayerEvent.FactionUpdateEvent event = new PlayerEvent.FactionUpdateEvent(wrapper, faction, faction.defaultPoints, true);
			EventHooks.OnPlayerFactionChange(handler, event);
			factionData.put(factionId, event.points);
			PlayerData data = PlayerData.get(player);
			data.updateClient = true;
		}
		return factionData.get(factionId);
	}

	public void increasePoints(Player player, int factionId, int points) {
		Faction faction = FactionController.instance.getFaction(factionId);
		if(faction == null || player == null || player.level().isClientSide)
			return;
		
		PlayerScriptData handler = PlayerData.get(player).scriptData;
		PlayerWrapper wrapper = (PlayerWrapper) NpcAPI.Instance().getIEntity(player);
		if(!factionData.containsKey(factionId)){
			PlayerEvent.FactionUpdateEvent event = new PlayerEvent.FactionUpdateEvent(wrapper, faction, faction.defaultPoints, true);
			EventHooks.OnPlayerFactionChange(handler, event);
			factionData.put(factionId, event.points);
		}
		PlayerEvent.FactionUpdateEvent event = new PlayerEvent.FactionUpdateEvent(wrapper, faction, points, false);
		EventHooks.OnPlayerFactionChange(handler, event);
		factionData.put(factionId, factionData.get(factionId) + points);
	}
	
	public CompoundTag getPlayerGuiData(){
		CompoundTag compound = new CompoundTag();
		saveNBTData(compound);
		
		ListTag list = new ListTag();
		for(int id : factionData.keySet()){
			Faction faction = FactionController.instance.getFaction(id);
			if(faction == null || faction.hideFaction)
				continue;
			CompoundTag com = new CompoundTag();
			faction.writeNBT(com);
			list.add(com);
		}
		compound.put("FactionList", list);
		
		return compound;
	}

}
