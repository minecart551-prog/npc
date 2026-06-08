package noppes.npcs.packets;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.server.permission.nodes.PermissionNode;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.shared.common.util.LogWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletableFuture;

public abstract class IPacketServer implements Packet<ServerGamePacketListener> {
    private static final Logger LOGGER = LogManager.getLogger();
    public ServerPlayer player;
    public EntityNPCInterface npc;

    @Override
    public void handle(ServerGamePacketListener handler) {
        enqueueWork(() -> {
            try {
                player = ((ServerGamePacketListenerImpl) handler).player;
                npc = NoppesUtilServer.getEditingNpc(player);

                if (requiresNpc() && npc == null) {
                    return;
                }
                if (getPermission() != null && !CustomNpcsPermissions.hasPermission(player, getPermission())) {
                    return;
                }
                if (!toolAllowed(player.getInventory().getSelected())) {
                    warn("tried to use custom npcs without a tool in hand, possibly a hacker");
                    return;
                }
                handle();
            } catch (Throwable e) {
                LogWriter.except(e);
                throw e;
            }
        });
    }

    public boolean requiresNpc(){
        return false;
    }

    public PermissionNode<Boolean> getPermission(){
        return null;
    }

    public boolean toolAllowed(ItemStack item){
        return item.getItem() == CustomItems.wand;
    }

    public abstract void handle();

    private void warn(String warning){
        LOGGER.warn(player.getName().getString() + ": " + warning + " - " + this);
    }

    public CompletableFuture<Void> enqueueWork(Runnable runnable) {
        //BlockableEventLoop<?> executor = MinecraftServer LogicalSidedProvider.WORKQUEUE.get(LogicalSide.SERVER);
        // Must check ourselves as Minecraft will sometimes delay tasks even when they are received on the client thread
        // Same logic as ThreadTaskExecutor#runImmediately without the join
//        if (!executor.isSameThread()) {
//            return executor.submitAsync(runnable); // Use the internal method so thread check isn't done twice
//        } else {
            runnable.run();
            return CompletableFuture.completedFuture(null);
//        }
    }
}
