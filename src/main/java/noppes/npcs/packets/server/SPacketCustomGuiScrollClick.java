package noppes.npcs.packets.server;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import noppes.npcs.EventHooks;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.gui.ICustomGuiComponent;
import noppes.npcs.api.gui.IScroll;
import noppes.npcs.api.wrapper.PlayerWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiScrollWrapper;
import noppes.npcs.containers.ContainerCustomGui;
import noppes.npcs.packets.PacketServerBasic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


public class SPacketCustomGuiScrollClick extends PacketServerBasic {
    private final UUID id;
    private final int slotId;
    private final boolean doubleClicked;

    public SPacketCustomGuiScrollClick(UUID id, int slotId, boolean doubleClicked) {
        this.id = id;
        this.slotId = slotId;
        this.doubleClicked = doubleClicked;
    }

    @Override
    public boolean toolAllowed(ItemStack item){
        return true;
    }

    public static void encode(SPacketCustomGuiScrollClick msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.id);
        buf.writeInt(msg.slotId);
        buf.writeBoolean(msg.doubleClicked);
    }

    public static SPacketCustomGuiScrollClick decode(FriendlyByteBuf buf) {
        return new SPacketCustomGuiScrollClick(buf.readUUID(), buf.readInt(), buf.readBoolean());
    }

    @Override
    protected void handle() {
        if(player.containerMenu instanceof ContainerCustomGui container) {
            ICustomGuiComponent comp = container.activeGui.getComponentUuid(id);
            if(comp instanceof CustomGuiScrollWrapper scroll){

                if(scroll.isMultiSelect()){
                    List<Integer> list = Arrays.stream(scroll.getSelection()).boxed().collect(Collectors.toList());
                    if(list.contains(slotId)){
                        list.remove(Integer.valueOf(slotId));
                    }
                    else{
                        list.add(slotId);
                    }
                    scroll.setSelection(list.stream().mapToInt(Integer::intValue).toArray());
                }
                else{
                    scroll.setSelection(slotId);
                }

                if(doubleClicked){
                    scroll.onDoubleClick(container.activeGui);
                }
                else{
                    scroll.onClick(container.activeGui);
                }
                PlayerWrapper pw = (PlayerWrapper)NpcAPI.Instance().getIEntity(player);
                EventHooks.onCustomGuiScrollClick(pw, container.activeGui, scroll, slotId, scroll.getSelectionList(), doubleClicked);
            }
        }
    }
}