package noppes.npcs.packets.server;

import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import noppes.npcs.CustomBlocks;
import noppes.npcs.CustomItems;
import noppes.npcs.blocks.tiles.TileBuilder;
import noppes.npcs.controllers.SchematicController;
import noppes.npcs.packets.PacketServerBasic;
import noppes.npcs.schematics.SchematicWrapper;



public class SPacketSchematicsTileBuild extends PacketServerBasic {

    private BlockPos pos;

    public SPacketSchematicsTileBuild(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public boolean toolAllowed(ItemStack item){
        return item.getItem() == CustomItems.wand || item.getItem() == CustomBlocks.builder_item || item.getItem() == CustomBlocks.copy_item;
    }

    public static void encode(SPacketSchematicsTileBuild msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
    }

    public static SPacketSchematicsTileBuild decode(FriendlyByteBuf buf) {
        return new SPacketSchematicsTileBuild(buf.readBlockPos());
    }

    @Override
    protected void handle() {
        TileBuilder tile = (TileBuilder) player.level().getBlockEntity(pos);
        SchematicWrapper schem = tile.getSchematic();
        schem.init(pos.offset(1, tile.yOffest, 1), player.level(), tile.rotation * 90);
        SchematicController.Instance.build(tile.getSchematic(), player.createCommandSourceStack());
        player.level().removeBlock(pos, false);
    }

}