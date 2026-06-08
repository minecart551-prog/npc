package noppes.npcs.controllers;

import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import noppes.npcs.CustomContainer;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.event.CustomGuiEvent;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.wrapper.PlayerWrapper;
import noppes.npcs.api.wrapper.WrapperNpcAPI;
import noppes.npcs.api.wrapper.gui.CustomGuiWrapper;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.containers.ContainerCustomGui;
import noppes.npcs.packets.Packets;
import noppes.npcs.packets.client.PacketGuiData;

public class CustomGuiController {

    static boolean checkGui(CustomGuiEvent event) {
        Player player = event.player.getMCEntity();
        if(!(player.containerMenu instanceof ContainerCustomGui))
            return false;
        return ((ContainerCustomGui)player.containerMenu).customGui.getID() == event.gui.getID();
    }

    public static void onButton(CustomGuiEvent.ButtonEvent event) {
        Player player = event.player.getMCEntity();
        if(checkGui(event) && getOpenGui(player).getScriptHandler()!=null) {
            ((CustomGuiWrapper)event.gui).getScriptHandler().run(EnumScriptType.CUSTOM_GUI_BUTTON, event);
        }
        //WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onQuickCraft(CustomGuiEvent.SlotEvent event) {
        Player player = event.player.getMCEntity();
        if(checkGui(event) && getOpenGui(player).getScriptHandler()!=null) {
            ((CustomGuiWrapper)event.gui).getScriptHandler().run(EnumScriptType.CUSTOM_GUI_SLOT, event);
        }
        //WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onScrollClick(CustomGuiEvent.ScrollEvent event) {
        Player player = event.player.getMCEntity();
        if(checkGui(event) && getOpenGui(player).getScriptHandler()!=null) {
            ((CustomGuiWrapper)event.gui).getScriptHandler().run(EnumScriptType.CUSTOM_GUI_SCROLL, event);
        }
        //WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onSlotClick(CustomGuiEvent.SlotClickEvent event) {
        Player player = event.player.getMCEntity();
        if(checkGui(event) && getOpenGui(player).getScriptHandler()!=null) {
            ((CustomGuiWrapper)event.gui).getScriptHandler().run(EnumScriptType.CUSTOM_GUI_SLOT_CLICKED, event);
        }
        return true;
        //return WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onClose(CustomGuiEvent.CloseEvent event) {
        Player player = event.player.getMCEntity();
        if(checkGui(event) && getOpenGui(player).getScriptHandler()!=null) {
            ((CustomGuiWrapper) event.gui).getScriptHandler().run(EnumScriptType.CUSTOM_GUI_CLOSED, event);
        }
        //WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static CustomGuiWrapper getOpenGui(Player player) {
        if(player.containerMenu instanceof ContainerCustomGui)
            return ((ContainerCustomGui)player.containerMenu).customGui;
        else
            return null;
    }

}
