package noppes.npcs.packets.server;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.server.permission.nodes.PermissionNode;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.client.EntityUtil;
import noppes.npcs.controllers.ServerCloneController;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.packets.PacketServerBasic;



public class SPacketToolMounter extends PacketServerBasic {

    private int type; //0:client clone, 1:server clone, 2:mob, 3:player mount
    private String name = "";
    private int tab = -1;
    private CompoundTag compound = new CompoundTag();


    private SPacketToolMounter(int type, String name, int tab, CompoundTag compound) {
        this.type = type;
        this.name = name;
        this.tab = tab;
        this.compound = compound;
    }

    public SPacketToolMounter(int type, String name, int tab) {
        this.type = type;
        this.name = name;
        this.tab = tab;
    }

    public SPacketToolMounter(int type, CompoundTag compound) {
        this.type = type;
        this.compound = compound;
    }

    public SPacketToolMounter() {
        this.type = 3;
    }

    public boolean toolAllowed(ItemStack item){
        return item.getItem() == CustomItems.mount;
    }

    @Override
    public PermissionNode<Boolean> getPermission(){
        return CustomNpcsPermissions.TOOL_MOUNTER;
    }

    public static void encode(SPacketToolMounter msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.type);
        buf.writeUtf(msg.name);
        buf.writeInt(msg.tab);
        buf.writeNbt(msg.compound);
    }

    public static SPacketToolMounter decode(FriendlyByteBuf buf) {
        return new SPacketToolMounter(buf.readInt(), buf.readUtf(32767), buf.readInt(), buf.readNbt());
    }

    @Override
    protected void handle() {
        PlayerData data = PlayerData.get(player);
        if(data.mounted == null)//shouldnt happen
            return;
        if(type == 0){
            Entity entity = EntityType.create(compound, player.level()).get();
            entity.setPos(data.mounted.getX(), data.mounted.getY(), data.mounted.getZ());
            player.level().addFreshEntity(entity);
            entity.startRiding(data.mounted, true);
        }
        else if(type == 1){
            Entity entity = EntityType.create(ServerCloneController.Instance.getCloneData(player.createCommandSourceStack(), name, tab), player.level()).get();
            entity.setPos(data.mounted.getX(), data.mounted.getY(), data.mounted.getZ());
            player.level().addFreshEntity(entity);
            entity.startRiding(data.mounted, true);
        }
        else if(type == 2){
            ResourceLocation loc = EntityUtil.getAllEntities(player.level(), false).get(name);
            EntityType type = BuiltInRegistries.ENTITY_TYPE.get(loc);
            Entity entity = type.create(player.level());
            if(entity == null)
                return;
            entity.setPos(data.mounted.getX(), data.mounted.getY(), data.mounted.getZ());
            player.level().addFreshEntity(entity);
            entity.startRiding(data.mounted, true);

        }
        else{
            player.startRiding(data.mounted, true);
        }
    }

}