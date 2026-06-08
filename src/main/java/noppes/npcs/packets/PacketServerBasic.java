package noppes.npcs.packets;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.server.permission.nodes.PermissionNode;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.entity.EntityNPCInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

public abstract class PacketServerBasic {
    private static final Logger LOGGER = LogManager.getLogger();
    public ServerPlayer player;
    public EntityNPCInterface npc;

    public boolean requiresNpc(){
        return false;
    }

    public PermissionNode<Boolean> getPermission(){
        return null;
    }

    public boolean toolAllowed(ItemStack item){
        return item.getItem() == CustomItems.wand;
    }

    public static void handle(final PacketServerBasic msg, MinecraftServer server, ServerPlayer player) {
        server.execute(() -> {
            msg.player = player;
            msg.npc = NoppesUtilServer.getEditingNpc(msg.player);

            if (msg.requiresNpc() && msg.npc == null) {
                return;
            }
            if (msg.getPermission() != null && !CustomNpcsPermissions.hasPermission(msg.player, msg.getPermission())) {
                return;
            }
            if (!msg.toolAllowed(msg.player.getInventory().getSelected())) {
                msg.warn("tried to use custom npcs without a tool in hand, possibly a hacker");
                return;
            }
            msg.handle();
        });
    }

    private void warn(String warning){
        LOGGER.warn(player.getName().getString() + ": " + warning + " - " + this);
    }

    protected abstract void handle();

}
