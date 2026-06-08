package noppes.npcs;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerSkinData;
import noppes.npcs.packets.Packets;
import noppes.npcs.packets.client.PacketSyncSkin;

public class SkinEventHandler implements ServerTickEvents.EndTick, ServerPlayConnectionEvents.Join {
    public void onEndTick(MinecraftServer server){
        if(PlayerSkinData.needsAnyResync()){
            for(ServerPlayer player: server.getPlayerList().getPlayers()){
                PlayerData playerData = PlayerData.get(player);
                if(playerData.skinData.isActive() && playerData.skinData.hasChanged()){
                    Packets.sendAll(new PacketSyncSkin(playerData.playername, playerData.skinData));
                    playerData.skinData.markSynced();
                }
            }
            PlayerSkinData.resyncPerformed();
        }
    }
    @Override
    public void onPlayReady(ServerGamePacketListenerImpl handler, PacketSender sender, MinecraftServer server) {
        ServerPlayer player = handler.player;
        PlayerData playerData = PlayerData.get(player);

        if(playerData.skinData.isActive()) {
            Packets.sendAll(new PacketSyncSkin(playerData.playername, playerData.skinData));
        }

        for(ServerPlayer otherPlayer: server.getPlayerList().getPlayers()){
            PlayerData otherPlayerData = PlayerData.get(otherPlayer);
            if(otherPlayerData.skinData.isActive()){
                Packets.send(player, new PacketSyncSkin(otherPlayerData.playername, otherPlayerData.skinData));
            }
        }
    }
}
