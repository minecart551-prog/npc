package noppes.npcs.controllers.data;

import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import noppes.npcs.controllers.FactionController;

public class FactionOptions {

	public boolean decreaseFactionPoints = false;
	public boolean decreaseFaction2Points = false;

	public int factionId = -1;
	public int faction2Id = -1;
	
	public int factionPoints = 100;
	public int faction2Points = 100;
	
    public void load(CompoundTag compound)
    {
    	factionId = compound.getInt("OptionFactions1");
    	faction2Id = compound.getInt("OptionFactions2");

    	decreaseFactionPoints = compound.getBoolean("DecreaseFaction1Points");
    	decreaseFaction2Points = compound.getBoolean("DecreaseFaction2Points");

    	factionPoints = compound.getInt("OptionFaction1Points");
    	faction2Points = compound.getInt("OptionFaction2Points");
    }
	public CompoundTag save(CompoundTag par1CompoundTag)
    {
		par1CompoundTag.putInt("OptionFactions1", factionId);
		par1CompoundTag.putInt("OptionFactions2", faction2Id);
		
		par1CompoundTag.putInt("OptionFaction1Points", factionPoints);
		par1CompoundTag.putInt("OptionFaction2Points", faction2Points);

		par1CompoundTag.putBoolean("DecreaseFaction1Points", decreaseFactionPoints);
		par1CompoundTag.putBoolean("DecreaseFaction2Points", decreaseFaction2Points);
		return par1CompoundTag;
    }
	public boolean hasFaction(int id) {
		return factionId == id || faction2Id == id;
	}
	public void addPoints(Player player) {
		if(factionId < 0 && faction2Id < 0)
			return;

		PlayerData playerdata = PlayerData.get(player);
		PlayerFactionData data = playerdata.factionData;	
		if(factionId >= 0 && factionPoints > 0)
			addPoints(player, data, factionId, decreaseFactionPoints, factionPoints);
		if(faction2Id >= 0 && faction2Points> 0)
			addPoints(player, data, faction2Id, decreaseFaction2Points, faction2Points);
		playerdata.updateClient = true;
	}
	
	private void addPoints(Player player, PlayerFactionData data, int factionId, boolean decrease, int points) {
		Faction faction = FactionController.instance.getFaction(factionId);
		if(faction == null)
			return;
		
		if(!faction.hideFaction){
			String message = decrease?"faction.decreasepoints":"faction.increasepoints";
			player.sendSystemMessage(Component.translatable(message, faction.name, points));
		}
		
		data.increasePoints(player, factionId, decrease?-points:points);
		
	}
}
