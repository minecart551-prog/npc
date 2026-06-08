package noppes.npcs.packets.server;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import noppes.npcs.CustomItems;
import noppes.npcs.packets.PacketServerBasic;

public class SPacketDimensionTeleport extends PacketServerBasic {
    private ResourceLocation id;

    public SPacketDimensionTeleport(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public boolean toolAllowed(ItemStack item){
        return item.getItem() == CustomItems.teleporter;
    }

    public static void encode(SPacketDimensionTeleport msg, FriendlyByteBuf buf) {
        buf.writeResourceLocation(msg.id);
    }

    public static SPacketDimensionTeleport decode(FriendlyByteBuf buf) {
        return new SPacketDimensionTeleport(buf.readResourceLocation());
    }

    @Override
    protected void handle() {
        ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, id);
        ServerLevel level = player.getServer().getLevel(dimension);
        BlockPos coords = level.getSharedSpawnPos();
        if(coords == null){
            coords = level.getSharedSpawnPos();
            if(!level.isEmptyBlock(coords)){
                coords = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, coords);
            }
            else{
                while(level.isEmptyBlock(coords) && coords.getY() > 0){
                    coords = coords.below();
                }
                if(coords.getY() == 0)
                    coords = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, coords);
            }
        }
        teleportPlayer(player, coords.getX(), coords.getY(), coords.getZ(), dimension);
    }

    public static void teleportPlayer(ServerPlayer player, double x, double y, double z, ResourceKey<Level> dimension){
        if(player.level().dimension() != dimension){
            MinecraftServer server = player.getServer();
            ServerLevel wor = server.getLevel(dimension);
            if(wor == null){
                player.sendSystemMessage(Component.literal("Broken transporter. Dimension does not exist"));
                return;
            }
            player.moveTo(x, y, z, player.getYRot(), player.getXRot());
            player.changeDimension(wor);
            //player.connection.teleport(x, y, z, player.yRot, player.xRot);

//            if(!wor.players.contains(player))
//                wor.addFreshEntity(player);
        }
        else{
            player.connection.teleport(x, y, z, player.getYRot(), player.getXRot());
        }

        //player.level.tickEntity(player, false);
    }

}