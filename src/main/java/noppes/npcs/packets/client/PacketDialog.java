package noppes.npcs.packets.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.gui.player.GuiDialogInteract;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.shared.common.PacketBasic;


public class PacketDialog extends PacketBasic {
	private final int entityId;
    private final int dialogId;

    public PacketDialog(int entityId, int dialogId) {
    	this.entityId = entityId;
    	this.dialogId = dialogId;
    }

    public static void encode(PacketDialog msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
        buf.writeInt(msg.dialogId);
    }

    public static PacketDialog decode(FriendlyByteBuf buf) {
        return new PacketDialog(buf.readInt(), buf.readInt());
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected void handle() {
        Entity entity = Minecraft.getInstance().level.getEntity(entityId);

        if(entity == null || !(entity instanceof EntityNPCInterface))
            return;
        Dialog dialog = DialogController.instance.dialogs.get(dialogId);
        openDialog(dialog, (EntityNPCInterface) entity,player);
	}

    public static void openDialog(Dialog dialog, EntityNPCInterface npc, Player player){
        Screen gui = Minecraft.getInstance().screen;
        if(gui == null || !(gui instanceof GuiDialogInteract))
            CustomNpcs.proxy.openGui(player, new GuiDialogInteract(npc, dialog));
        else{
            GuiDialogInteract dia = (GuiDialogInteract) gui;
            dia.appendDialog(dialog);
        }
    }
}