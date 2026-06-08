package noppes.npcs.packets.server;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraftforge.server.permission.nodes.PermissionNode;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.packets.PacketServerBasic;




public class SPacketNbtBookBlockSave extends PacketServerBasic {
    private BlockPos pos;
    private CompoundTag data;

    public SPacketNbtBookBlockSave(BlockPos pos, CompoundTag data) {
        this.pos = pos;
        this.data = data;
    }

    public boolean toolAllowed(ItemStack item){
        return item.getItem() == CustomItems.nbt_book;
    }

    @Override
    public PermissionNode<Boolean> getPermission(){
        return CustomNpcsPermissions.TOOL_NBTBOOK;
    }

    public static void encode(SPacketNbtBookBlockSave msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeNbt(msg.data);
    }

    public static SPacketNbtBookBlockSave decode(FriendlyByteBuf buf) {
        return new SPacketNbtBookBlockSave(buf.readBlockPos(), buf.readNbt());
    }

    @Override
    protected void handle() {
        BlockEntity tile = player.level().getBlockEntity(pos);
        if(tile != null) {
            tile.load(data);
            tile.setChanged();
        }
    }


}