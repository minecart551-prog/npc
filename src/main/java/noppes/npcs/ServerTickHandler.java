package noppes.npcs;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import noppes.npcs.api.constants.QuestType;
import noppes.npcs.constants.SyncType;
import noppes.npcs.controllers.MassBlockController;
import noppes.npcs.controllers.SchematicController;
import noppes.npcs.controllers.SyncController;
import noppes.npcs.controllers.VisibilityController;
import noppes.npcs.controllers.data.Availability;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerQuestData;
import noppes.npcs.controllers.NaturalSpawnCache;
import noppes.npcs.entity.data.DataScenes;
import noppes.npcs.entity.data.DataScenes.SceneContainer;
import noppes.npcs.entity.data.DataScenes.SceneState;
import noppes.npcs.packets.Packets;
import noppes.npcs.packets.client.PacketSync;
import noppes.npcs.shared.client.util.AnalyticsTracking;

import java.util.ArrayList;

public class ServerTickHandler implements ServerTickEvents.StartTick, ServerPlayConnectionEvents.Join {
	public int ticks = 0;

	@Override
	public void onStartTick(MinecraftServer server) {
		if(ticks++ >= 20){
			SchematicController.Instance.updateBuilding();
			MassBlockController.Update();

			// Every ~1 second (20 ticks), check if any cached NPCs should be restored
			for(ServerLevel level : server.getAllLevels()){
				NaturalSpawnCache.instance.checkNearbyPlayers(level);
			}

			ticks = 0;
			for(SceneState state : DataScenes.StartedScenes.values()){
				if(!state.paused)
					state.ticks++;
			}
			for(SceneContainer entry : DataScenes.ScenesToRun){
				entry.update();
			}
			DataScenes.ScenesToRun = new ArrayList<>();
			
		}
		for(ServerLevel level : server.getAllLevels()){
			NPCSpawning.findChunksForSpawning(level);
		}
		for(Player player : server.getPlayerList().getPlayers()){
			PlayerData data = PlayerData.get(player);


			if(player.getCommandSenderWorld().getDayTime()%24000==1 || player.getCommandSenderWorld().getDayTime()%240000==12001){
				VisibilityController.instance.onUpdate((ServerPlayer) player);
			}

			if(data.updateClient) {
				Packets.send((ServerPlayer)player, new PacketSync(SyncType.PLAYER_DATA, data.getSyncNBT(), true));
				VisibilityController.instance.onUpdate((ServerPlayer) player);
				data.updateClient = false;
			}


			if(data.prevHeldItem != player.getMainHandItem()) {
				if(data.prevHeldItem.getItem() == CustomItems.wand || player.getMainHandItem().getItem() == CustomItems.wand) {
					VisibilityController.instance.onUpdate((ServerPlayer) player);
				}
			}
			data.prevHeldItem = player.getMainHandItem();
		}
	}

	@Override
	public void onPlayReady(ServerGamePacketListenerImpl handler, PacketSender sender, MinecraftServer server) {
		ServerPlayer player = (ServerPlayer) handler.player;
		for(ServerLevel level : server.getAllLevels()) {
			ServerScoreboard board = level.getScoreboard();
			for(String objective : Availability.scores) {
				Objective so = board.getObjective(objective);
				if(so != null) {
					if(board.getObjectiveDisplaySlotCount(so) == 0) {
						player.connection.send(new ClientboundSetObjectivePacket(so, 0));
					}

					Score sco = board.getOrCreatePlayerScore(player.getScoreboardName(), so);
					player.connection.send(new ClientboundSetScorePacket(ServerScoreboard.Method.CHANGE, so.getName(), sco.getOwner(), sco.getScore()));
				}
			}
		}
		player.inventoryMenu.addSlotListener( new ContainerListener() {

			@Override
			public void slotChanged(AbstractContainerMenu container, int slotInd, ItemStack stack) {
				if(player.level().isClientSide)
					return;
				PlayerQuestData playerdata = PlayerData.get(player).questData;
				playerdata.checkQuestCompletion(player, QuestType.ITEM);
			}

			@Override
			public void dataChanged(AbstractContainerMenu container, int varToUpdate, int newValue) {

			}
		});

		PlayerData data = PlayerData.get(handler.player);
		String serverName = "local";
		if(server.isDedicatedServer()){
			serverName = "server";
		}
		else if(server.isPublished()) {
			serverName =  "lan";
		}
		//AnalyticsTracking.sendData(data.iAmStealingYourDatas, "join", serverName);

		SyncController.syncPlayer(handler.player);
	}
}
