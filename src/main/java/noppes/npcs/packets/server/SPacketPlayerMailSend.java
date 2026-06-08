package noppes.npcs.packets.server;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import noppes.npcs.EventHooks;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.event.RoleEvent;
import noppes.npcs.containers.ContainerMail;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.data.PlayerMail;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.packets.PacketServerBasic;



public class SPacketPlayerMailSend extends PacketServerBasic {
    private final CompoundTag data;
    private final String username;
    public SPacketPlayerMailSend(String username, CompoundTag data) {
        this.username = username;
        this.data = data;
    }

    @Override
    public boolean toolAllowed(ItemStack item){
        return true;
    }

    public static void encode(SPacketPlayerMailSend msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.username);
        buf.writeNbt(msg.data);
    }

    public static SPacketPlayerMailSend decode(FriendlyByteBuf buf) {
        return new SPacketPlayerMailSend(buf.readUtf(32767), buf.readNbt());
    }

    @Override
    protected void handle() {
        String username = PlayerDataController.instance.hasPlayer(this.username);
        if(username.isEmpty()){
            NoppesUtilServer.sendGuiError(player, 0);
            return;
        }

        PlayerMail mail = new PlayerMail();
        //String s = player.func_145748_c_().getString();
        String s = player.getDisplayName().getString();
        if(!s.equals(player.getName().getString()))
            s += "(" + player.getName().getString() + ")";
        mail.readNBT(data);
        mail.sender = s;

        if(mail.subject.isEmpty()){
            NoppesUtilServer.sendGuiError(player, 1);
            return;
        }
        mail.items = ((ContainerMail)player.containerMenu).mail.items;

        CompoundTag comp = new CompoundTag();
        comp.putString("username", username);
        NoppesUtilServer.sendGuiClose(player, 1,comp);
        player.closeContainer();

        EntityNPCInterface npc = NoppesUtilServer.getEditingNpc(player);

        if(npc != null && EventHooks.onNPCRole(npc, new RoleEvent.MailmanEvent(player, npc.wrappedNPC, mail))){
            return;
        }

        PlayerDataController.instance.addPlayerMessage(player.getServer(), username, mail);
    }
}