package noppes.npcs.blocks.tiles;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import noppes.npcs.CustomBlocks;
import noppes.npcs.api.constants.QuestType;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerQuestData;
import noppes.npcs.controllers.data.QuestData;
import noppes.npcs.quests.QuestLocation;

import java.util.ArrayList;
import java.util.List;

public class TileWaypoint extends TileNpcEntity{
	
	public String name = "";

	private int ticks = 10;
	private List<Player> recentlyChecked = new ArrayList<Player>();
	private List<Player> toCheck;
	public int range = 10;

	public TileWaypoint(BlockPos pos, BlockState state){
		super(CustomBlocks.tile_waypoint, pos, state);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, TileWaypoint tile) {
		if(level.isClientSide || tile.name.isEmpty())
			return;
		tile.ticks--;
		if(tile.ticks > 0)
			return;
		tile.ticks = 10;

		tile.toCheck = tile.getPlayerList(tile.range, tile.range, tile.range);
		tile.toCheck.removeAll(tile.recentlyChecked);

		List<Player> listMax = tile.getPlayerList(tile.range + 10, tile.range + 10, tile.range + 10);
		tile.recentlyChecked.retainAll(listMax);
		tile.recentlyChecked.addAll(tile.toCheck);
		
		if(tile.toCheck.isEmpty())
			return;
		for(Player player : tile.toCheck){
			PlayerData pdata = PlayerData.get(player);
			PlayerQuestData playerdata = pdata.questData;
			for(QuestData data : playerdata.activeQuests.values()){
				if(data.quest.type != QuestType.LOCATION)
					continue;
				QuestLocation quest = (QuestLocation) data.quest.questInterface;
				if(quest.setFound(data, tile.name)){
					player.sendSystemMessage(Component.translatable(tile.name).append(" ").append(Component.translatable("quest.found")));

					playerdata.checkQuestCompletion(player, QuestType.LOCATION);
					pdata.updateClient = true;
				}
			}
		}
	}

	private List<Player> getPlayerList(int x, int y, int z){
		return level.getEntitiesOfClass(Player.class, new AABB(worldPosition, worldPosition.offset(1, 1, 1)).inflate(x, y, z));
	}

	@Override
	public void load(CompoundTag compound) {
		super.load(compound);
		name = compound.getString("LocationName");
		range = compound.getInt("LocationRange");
		if(range < 2)
			range = 2;
	}

	@Override
    public void saveAdditional(CompoundTag compound){
		if(!name.isEmpty())
			compound.putString("LocationName", name);
		compound.putInt("LocationRange", range);
    	super.saveAdditional(compound);
	}
}
