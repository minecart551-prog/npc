package noppes.npcs.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import noppes.npcs.controllers.data.PlayerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class PlayerListMixin {

	/**
	 * PlayerList.remove(ServerPlayer) is the single "this player has left" path - it is
	 * called from ServerGamePacketListenerImpl.onDisconnect() and from server shutdown, and
	 * it writes the player to disk first (the save() call at the top of the method).
	 *
	 * Injecting at the tail therefore runs after vanilla has persisted the player, which is
	 * what makes it safe to throw the in-memory PlayerData away: the next login reloads from
	 * disk exactly like it does today, but the map no longer grows by one entry (holding the
	 * player, its script engines and whatever npc it was editing or riding) per login.
	 *
	 * Note that ServerPlayConnectionEvents.Disconnect fires earlier, at the head of
	 * ServerGamePacketListenerImpl.onDisconnect(), i.e. *before* this save - which is why the
	 * cleanup cannot live there.
	 */
	@Inject(method = "remove(Lnet/minecraft/server/level/ServerPlayer;)V", at = @At("RETURN"))
	private void cnpc_releasePlayerData(ServerPlayer player, CallbackInfo ci) {
		PlayerData.logout(player);
	}
}
