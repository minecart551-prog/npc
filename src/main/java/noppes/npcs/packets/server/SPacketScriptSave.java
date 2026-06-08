package noppes.npcs.packets.server;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.server.permission.nodes.PermissionNode;
import noppes.npcs.CustomBlocks;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.wrapper.ItemScriptedWrapper;
import noppes.npcs.blocks.tiles.TileScripted;
import noppes.npcs.blocks.tiles.TileScriptedDoor;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.packets.IPacketServer;
import noppes.npcs.packets.PacketServerBasic;

public class SPacketScriptSave extends PacketServerBasic {
    private int type; //0:npc, 1:block, 2:item, 3:forge, 4:player, 5:door
    private CompoundTag data;

    public SPacketScriptSave(int type, CompoundTag data) {
        this.type = type;
        this.data = data;
    }

    public SPacketScriptSave(FriendlyByteBuf buf) {
        type = buf.readInt();
        data = buf.readNbt();
    }

    public static SPacketScriptSave decode(FriendlyByteBuf buf) {
        return new SPacketScriptSave(buf);
    }

    @Override
    public boolean toolAllowed(ItemStack item){
        return item.getItem() == CustomItems.scripter || item.getItem() == CustomBlocks.scripted_door_item || item.getItem() == CustomItems.wand ||
                item.getItem() == CustomItems.scripted_item || item.getItem() == CustomBlocks.scripted_item;
    }

    @Override
    public boolean requiresNpc(){
        return type == 0;
    }

    @Override
    public PermissionNode<Boolean> getPermission(){
        return CustomNpcsPermissions.TOOL_SCRIPTER;
    }

    @Override
    protected void handle() {
        if(type == 0){
            npc.script.load(data);
            npc.updateAI = true;
            npc.script.lastInited = -1;
        }
        if(type == 1){
            PlayerData pd = PlayerData.get(player);
            BlockEntity tile = player.level().getBlockEntity(pd.scriptBlockPos);
            if(!(tile instanceof TileScripted))
                return;
            TileScripted script = (TileScripted) tile;
            script.setNBT(data);
            script.lastInited = -1;
            player.level().blockEntityChanged(pd.scriptBlockPos);
        }
        if(type == 2){
            if(!player.isCreative())
                return;
            ItemScriptedWrapper wrapper = (ItemScriptedWrapper) NpcAPI.Instance().getIItemStack(player.getMainHandItem());
            wrapper.setMCNbt(data);
            wrapper.lastInited = -1;
            wrapper.saveScriptData();
            wrapper.updateClient = true;
            player.containerMenu.sendAllDataToRemote();
        }
        if(type == 3){
            ScriptController.Instance.setForgeScripts(data);
        }
        if(type == 4){
            ScriptController.Instance.setPlayerScripts(data);
        }
        if(type == 5){
            PlayerData pd = PlayerData.get(player);
            BlockEntity tile = player.level().getBlockEntity(pd.scriptBlockPos);
            if(!(tile instanceof TileScriptedDoor))
                return;
            TileScriptedDoor script = (TileScriptedDoor) tile;
            script.setNBT(data);
            script.lastInited = -1;
        }
    }

    public static void encode(SPacketScriptSave msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.type);
        buf.writeNbt(msg.data);
    }
}